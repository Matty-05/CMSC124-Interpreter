object ErrorReporter {
    var hadError = false

    fun error(line: Int, message: String) {
        hadError = true
        System.err.println("[line $line] Error: $message")
    }

    fun error(token: Token, message: String) {
        hadError = true
        val where = if (token.type == TokenType.EOF) "end" else "'${token.lexeme}'"
        System.err.println("[line ${token.line}] Error at $where: $message")
    }
}