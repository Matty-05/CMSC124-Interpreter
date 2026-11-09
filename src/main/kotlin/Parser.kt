class Parser(private val tokens: List<Token>) {
    private var current = 0
    private var exprLine = 0   // the line of the expression being parsed
    private val errors = mutableListOf<String>()

    // True at the end of the file, or when the next token is on a later line.
    private fun atLineEnd(): Boolean = isAtEnd() || peek().line != exprLine

    private fun isAtEnd(): Boolean = peek().type == TokenType.EOF

    private fun peek(): Token = tokens[current]

    private fun previous(): Token = tokens[current - 1]

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun check(type: TokenType): Boolean {
        if (atLineEnd()) return false
        return peek().type == type
    }

    private fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }
        return false
    }

    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) return advance()
        throw error(peek(), message)
    }

    private fun error(token: Token, message: String): ParseError {
        // A token past the end of this expression's line counts as "end".
        if (token.type == TokenType.EOF || token.line != exprLine) {
            return ParseError("[line $exprLine] Error at end: $message")
        }
        return ParseError("[line ${token.line}] Error at '${token.lexeme}': $message")
    }

    class ParseError(message: String) : RuntimeException(message)

    // primary → NUMBER | STRING | "active" | "inactive" | "void" | IDENTIFIER | "(" expression ")"
    private fun primary(): Expr {
        if (match(TokenType.NUMBER, TokenType.STRING)) {
            return Expr.Literal(previous().literal)
        }
        if (match(TokenType.ACTIVE)) return Expr.Literal(true)
        if (match(TokenType.INACTIVE)) return Expr.Literal(false)
        if (match(TokenType.VOID)) return Expr.Literal(null)
        if (match(TokenType.IDENTIFIER)) return Expr.Variable(previous())

        if (match(TokenType.LEFT_PAREN)) {
            val expr = expression()
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.")
            return Expr.Grouping(expr)
        }

        throw error(peek(), "Expect expression.")
    }

    // unary → ( "not" | "-" ) unary | primary
    private fun unary(): Expr {
        if (match(TokenType.NOT, TokenType.MINUS)) {
            val operator = previous()
            val right = unary()
            return Expr.Unary(operator, right)
        }
        return primary()
    }

    // factor → unary ( ( "/" | "*" ) unary )*
    private fun factor(): Expr {
        var expr = unary()
        while (match(TokenType.SLASH, TokenType.STAR)) {
            val operator = previous()
            val right = unary()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }
    
    // term → factor ( ( "-" | "+" ) factor )*
    private fun term(): Expr {
        var expr = factor()
        while (match(TokenType.MINUS, TokenType.PLUS)) {
            val operator = previous()
            val right = factor()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    // comparison → term ( ( ">" | ">=" | "<" | "<=" ) term )*
    private fun comparison(): Expr {
        var expr = term()
        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            val operator = previous()
            val right = term()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    // equality → comparison ( ( "!=" | "==" ) comparison )*
    private fun equality(): Expr {
        var expr = comparison()
        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            val operator = previous()
            val right = comparison()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    // and → equality ( "and" equality )*
    private fun and(): Expr {
        var expr = equality()
        while (match(TokenType.AND)) {
            val operator = previous()
            val right = equality()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    // or → and ( "or" and )*
    private fun or(): Expr {
        var expr = and()
        while (match(TokenType.OR)) {
            val operator = previous()
            val right = and()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    // assignment → IDENTIFIER "=" assignment | or
    private fun assignment(): Expr {
        val expr = or()

        if (match(TokenType.EQUAL)) {
            val equals = previous()
            val value = assignment()

            if (expr is Expr.Variable) return Expr.Assign(expr.name, value)
            throw error(equals, "Invalid assignment target.")
        }

        return expr
    }

    // expression → assignment
    private fun expression(): Expr = assignment()

    // declaration → unitDecl | statement
    private fun declaration(): Stmt {
        if (match(TokenType.UNIT)) return unitDecl()
        return statement()
    }

    // unitDecl → "unit" IDENTIFIER ( "=" expression )?
    private fun unitDecl(): Stmt {
        val name = consume(TokenType.IDENTIFIER, "Expect unit name.")
        val initializer = if (match(TokenType.EQUAL)) expression() else null
        return Stmt.Unit(name, initializer)
    }

    // statement → block | reportStmt | exprStmt
    private fun statement(): Stmt {
        if (match(TokenType.LEFT_BRACE)) {
            val line = previous().line
            return Stmt.Block(statements(inBlock = true), line)
        }
        if (match(TokenType.REPORT)) return reportStmt()
        return Stmt.Expression(expression())
    }

    // reportStmt → "report" expression
    private fun reportStmt(): Stmt = Stmt.Report(expression())

    // program → declaration* EOF, one declaration per line
    fun parse(): Program {
        val statements = statements(inBlock = false)
        if (errors.isNotEmpty()) throw ParseError(errors.joinToString("\n"))
        if (statements.isEmpty()) throw ParseError("[line 1] Error: Expect statement.")
        return Program(statements)
    }

    // Braces span lines; individual declarations still end on their own line.
    private fun statements(inBlock: Boolean): List<Stmt> {
        val statements = mutableListOf<Stmt>()
        while (!isAtEnd() && !(inBlock && peek().type == TokenType.RIGHT_BRACE)) {
            exprLine = peek().line
            try {
                val stmt = declaration()
                if (!atLineEnd() && !(inBlock && peek().type == TokenType.RIGHT_BRACE)) {
                    throw error(peek(), "Expect end of statement.")
                }
                statements += stmt
            } catch (_: StackOverflowError) {
                errors += "[line $exprLine] Error: Expression nested too deeply."
                synchronize(inBlock)
            } catch (e: ParseError) {
                errors += e.message.orEmpty()
                synchronize(inBlock)
            }
        }
        if (inBlock) {
            if (isAtEnd()) throw error(peek(), "Expect '}' after block.")
            exprLine = advance().line
        }
        return statements
    }

    // Skip the bad line, preserving a closing brace for its block.
    private fun synchronize(inBlock: Boolean) {
        while (!atLineEnd() && !(inBlock && peek().type == TokenType.RIGHT_BRACE)) advance()
    }
}
