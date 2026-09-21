class Parser(private val tokens: List<Token>) {
    private var current = 0

    private fun isAtEnd(): Boolean = peek().type == TokenType.EOF

    private fun peek(): Token = tokens[current]

    private fun previous(): Token = tokens[current - 1]

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun check(type: TokenType): Boolean {
        if (isAtEnd()) return false
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
        ErrorReporter.error(token, message)
        return ParseError()
    }

    class ParseError : RuntimeException()

    private fun primary(): Expr {
        if (match(TokenType.NUMBER, TokenType.STRING)) {
            return Expr.Literal(previous().literal)
        }
        if (match(TokenType.ACTIVE)) return Expr.Literal(true)
        if (match(TokenType.INACTIVE)) return Expr.Literal(false)
        if (match(TokenType.VOID)) return Expr.Literal(null)

        throw error(peek(), "Expect expression.")
    }

    private fun term(): Expr {
        var expr = primary()
        while (match(TokenType.MINUS, TokenType.PLUS)) {
            val operator = previous()
            val right = primary()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    fun parse(): Expr {
        val expr = term()
        if (!isAtEnd()) throw error(peek(), "Expect end of expression.")
        return expr
    }
}