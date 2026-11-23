class Parser(private val tokens: List<Token>) {
    private var current = 0
    private var exprLine = 0   // the line of the expression being parsed
    private val errors = mutableListOf<String>()
    private var protocolDepth = 0

    private companion object {
        const val MAX_ARGUMENTS = 255
        const val MAX_PARAMETERS = 255
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

    // call → primary ( "(" arguments? ")" )*
    // The loop lets a call's result be called again, as in f()().
    private fun call(): Expr {
        var expr = primary()
        while (match(TokenType.LEFT_PAREN)) expr = finishCall(expr)
        return expr
    }

    // arguments → expression ( "," expression )*
    private fun finishCall(callee: Expr): Expr {
        val arguments = mutableListOf<Expr>()
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                if (arguments.size == MAX_ARGUMENTS) {
                    throw error(peek(), "Can't have more than $MAX_ARGUMENTS arguments.")
                }
                arguments += expression()
            } while (match(TokenType.COMMA))
        }
        val paren = consume(TokenType.RIGHT_PAREN, "Expect ')' after arguments.")
        return Expr.Call(callee, paren, arguments)
    }

    // unary → ( "not" | "-" ) unary | call
    private fun unary(): Expr {
        if (match(TokenType.NOT, TokenType.MINUS)) {
            val operator = previous()
            val right = unary()
            return Expr.Unary(operator, right)
        }
        return call()
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

    // declaration → protocolDecl | unitDecl | statement
    private fun declaration(): Stmt {
        if (match(TokenType.PROTOCOL)) return protocolDecl()
        if (match(TokenType.UNIT)) return unitDecl()
        return statement()
    }

    // protocolDecl → "protocol" IDENTIFIER "(" parameters? ")" block
    private fun protocolDecl(): Stmt.Protocol {
        val name = consume(TokenType.IDENTIFIER, "Expect protocol name.")
        consume(TokenType.LEFT_PAREN, "Expect '(' after protocol name.")
        val parameters = mutableListOf<Token>()
        val parameterNames = mutableSetOf<String>()
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                if (parameters.size == MAX_PARAMETERS) {
                    throw error(peek(), "Can't have more than $MAX_PARAMETERS parameters.")
                }
                val parameter = consume(TokenType.IDENTIFIER, "Expect parameter name.")
                if (!parameterNames.add(parameter.lexeme)) {
                    throw error(parameter, "Duplicate parameter '${parameter.lexeme}'.")
                }
                parameters += parameter
            } while (match(TokenType.COMMA))
        }
        consume(TokenType.RIGHT_PAREN, "Expect ')' after parameters.")
        exprLine = peek().line
        consume(TokenType.LEFT_BRACE, "Expect '{' before protocol body.")
        protocolDepth++
        try {
            return Stmt.Protocol(name, parameters, block())
        } finally {
            protocolDepth--
        }
    }

    // unitDecl → "unit" IDENTIFIER ( "=" expression )?
    private fun unitDecl(): Stmt {
        val name = consume(TokenType.IDENTIFIER, "Expect unit name.")
        val initializer = if (match(TokenType.EQUAL)) expression() else null
        return Stmt.Unit(name, initializer)
    }

    // statement → transmitStmt | ifStmt | whileStmt | forStmt | block | reportStmt | hackStmt | exprStmt
    private fun statement(): Stmt {
        if (match(TokenType.TRANSMIT)) return transmitStmt()
        if (match(TokenType.IF)) return ifStmt()
        if (match(TokenType.WHILE)) return whileStmt()
        if (match(TokenType.FOR)) return forStmt()
        if (match(TokenType.LEFT_BRACE)) return block()
        if (match(TokenType.REPORT)) return reportStmt()
        if (match(TokenType.HACK)) return hackStmt()
        if (match(TokenType.SCAN)) return scanStmt()
        if (match(TokenType.ACCESS_POINT)) {
            return Stmt.AccessPoint(consume(TokenType.STRING, "Expect access point name as a string."))
        }
        if (match(TokenType.RESTORE)) {
            return Stmt.Restore(consume(TokenType.STRING, "Expect access point name as a string."))
        }
        return Stmt.Expression(expression())
    }

    // The opening brace is already consumed for either kind of block body.
    private fun block(): Stmt.Block {
        val line = previous().line
        return Stmt.Block(statements(inBlock = true), line)
    }

    // reportStmt → "report" expression
    private fun reportStmt(): Stmt = Stmt.Report(expression())

    // hackStmt → "hack" expression
    private fun hackStmt(): Stmt = Stmt.Hack(previous(), expression())

    private fun scanStmt(): Stmt.Scan {
        if (match(TokenType.STRING)) return Stmt.Scan(previous())
        val name = consume(TokenType.IDENTIFIER, "Expect unit name or access point string after 'scan'.")
        // 'all' is contextual, so 'scan all' can still refer to a unit named all.
        if (name.lexeme == "all" && match(TokenType.IDENTIFIER)) return Stmt.Scan(previous(), all = true)
        return Stmt.Scan(name)
    }

    // transmitStmt → "transmit" expression?
    private fun transmitStmt(): Stmt.Transmit {
        val keyword = previous()
        if (protocolDepth == 0) throw error(keyword, "Can't transmit from outside a protocol.")
        val value = if (atLineEnd() || check(TokenType.RIGHT_BRACE)) null else expression()
        return Stmt.Transmit(keyword, value)
    }

    // ifStmt → "if" "(" expression ")" statement ( "else" statement )?
    // Parsing the inner statement first binds else to the nearest unmatched if.
    private fun ifStmt(): Stmt {
        val keyword = previous()
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
    }

    // whileStmt → "while" "(" expression ")" statement
    private fun whileStmt(): Stmt {
        val keyword = previous()
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'while'.")
        val condition = expression()
        consume(TokenType.RIGHT_PAREN, "Expect ')' after condition.")
        exprLine = peek().line
        return Stmt.While(condition, statement(), keyword.line)
    }

    // forStmt → "for" "(" ( unitDecl | expression )? "," expression? "," expression? ")" statement
    // Nier has no ';', so commas separate the clauses. There is no for node: the loop is
    // rewritten as { initializer  while (condition) { body  increment } }, so the
    // evaluator runs it with the block and while code it already has.
    private fun forStmt(): Stmt {
        val keyword = previous()
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'for'.")
        val initializer = when {
            check(TokenType.COMMA) -> null
            match(TokenType.UNIT) -> unitDecl()
            else -> Stmt.Expression(expression())
        }
        consume(TokenType.COMMA, "Expect ',' after loop initializer.")
        val condition = if (check(TokenType.COMMA)) null else expression()
        consume(TokenType.COMMA, "Expect ',' after loop condition.")
        val increment = if (check(TokenType.RIGHT_PAREN)) null else expression()
        consume(TokenType.RIGHT_PAREN, "Expect ')' after for clauses.")
        exprLine = peek().line

        var body = statement()
        if (increment != null) {
            body = Stmt.Block(listOf(body, Stmt.Expression(increment)), keyword.line)
        }
        // A missing condition loops forever.
        body = Stmt.While(condition ?: Expr.Literal(true), body, keyword.line)
        // The outer block holds the loop variable, so it is gone after the loop.
        if (initializer != null) body = Stmt.Block(listOf(initializer, body), keyword.line)
        return body
    }

    // After a stack overflow, skip the whole statement from its first token, matching
    // braces and parentheses in a loop so skipping does not use the stack again.
    private fun skipNested(start: Int) {
        current = start
        var open = 0
        while (!isAtEnd()) {
            when (advance().type) {
                TokenType.LEFT_BRACE, TokenType.LEFT_PAREN -> open++
                TokenType.RIGHT_BRACE, TokenType.RIGHT_PAREN -> open--
                else -> Unit
            }
            exprLine = previous().line
            if (open <= 0 && atLineEnd()) break
        }
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
            val start = current
            try {
                val stmt = declaration()
                if (!atLineEnd() && !(inBlock && peek().type == TokenType.RIGHT_BRACE)) {
                    throw error(peek(), "Expect end of statement.")
                }
                statements += stmt
            } catch (e: StackOverflowError) {
                // The JVM ran out of stack, so the limit depends on the machine. Only the
                // top-level loop handles it: by then the stack has unwound and recovery is safe.
                if (inBlock) throw e
                errors += "[line ${tokens[start].line}] Error: ${nestedKind(tokens[start])} nested too deeply."
                skipNested(start)
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

    // Named after the statement's first token, so the message is the same on every run
    // no matter where the stack happened to run out.
    private fun nestedKind(first: Token): String = when (first.type) {
        TokenType.LEFT_BRACE, TokenType.PROTOCOL -> "Block"
        TokenType.IF -> "Conditional"
        TokenType.WHILE, TokenType.FOR -> "Loop"
        else -> "Expression"
    }

    // Skip the bad line, preserving a closing brace for its block.
    private fun synchronize(inBlock: Boolean) {
        while (!atLineEnd() && !(inBlock && peek().type == TokenType.RIGHT_BRACE)) advance()
    }
}
