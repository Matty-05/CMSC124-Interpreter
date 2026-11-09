class Parser(private val tokens: List<Token>) {
    private var current = 0
    private var exprLine = 0   // the line of the expression being parsed
    private val errors = mutableListOf<String>()
    private var blockDepth = 0
    private var ifDepth = 0

    private companion object {
        const val MAX_BLOCK_DEPTH = 128
        const val MAX_IF_DEPTH = 128
    }

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

    // statement → ifStmt | block | reportStmt | exprStmt
    private fun statement(): Stmt {
        if (match(TokenType.IF)) return ifStmt()
        if (match(TokenType.LEFT_BRACE)) {
            val line = previous().line
            if (blockDepth == MAX_BLOCK_DEPTH) {
                skipBlock()
                throw ParseError("[line $line] Error: Block nested too deeply.")
            }
            blockDepth++
            try {
                return Stmt.Block(statements(inBlock = true), line)
            } finally {
                blockDepth--
            }
        }
        if (match(TokenType.REPORT)) return reportStmt()
        return Stmt.Expression(expression())
    }

    // reportStmt → "report" expression
    private fun reportStmt(): Stmt = Stmt.Report(expression())

    // ifStmt → "if" "(" expression ")" statement ( "else" statement )?
    // Parsing the inner statement first binds else to the nearest unmatched if.
    private fun ifStmt(): Stmt {
        val keyword = previous()
        if (ifDepth == MAX_IF_DEPTH) {
            throw ParseError("[line ${keyword.line}] Error: Conditional nested too deeply.")
        }
        ifDepth++
        try {
            consume(TokenType.LEFT_PAREN, "Expect '(' after 'if'.")
            val condition = expression()
            consume(TokenType.RIGHT_PAREN, "Expect ')' after condition.")
            exprLine = peek().line
            val thenBranch = statement()
            val elseBranch = if (peek().type == TokenType.ELSE) {
                advance()
                exprLine = peek().line
                statement()
            } else null
            return Stmt.If(condition, thenBranch, elseBranch, keyword.line)
        } finally {
            ifDepth--
        }
    }

    // The opening brace was consumed. Skip its contents without growing the stack.
    private fun skipBlock() {
        var remaining = 1
        while (!isAtEnd() && remaining > 0) {
            when (advance().type) {
                TokenType.LEFT_BRACE -> remaining++
                TokenType.RIGHT_BRACE -> remaining--
                else -> Unit
            }
        }
        exprLine = previous().line
    }

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
