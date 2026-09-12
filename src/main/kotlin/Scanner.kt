class Scanner(private val source: String) {
    private val tokens = mutableListOf<Token>()
    private var start = 0
    private var current = 0
    private var line = 1

    companion object {
        val keywords = mapOf(
            "unit" to TokenType.UNIT,
            "report" to TokenType.REPORT,
            "protocol" to TokenType.PROTOCOL,
            "transmit" to TokenType.TRANSMIT,
            "active" to TokenType.ACTIVE,
            "inactive" to TokenType.INACTIVE,
            "void" to TokenType.VOID,
            "if" to TokenType.IF,
            "else" to TokenType.ELSE,
            "while" to TokenType.WHILE,
            "for" to TokenType.FOR,
            "and" to TokenType.AND,
            "or" to TokenType.OR,
            "not" to TokenType.NOT
        )
    }

    fun scanTokens(): List<Token> {
        while (!isAtEnd()) {
            start = current
            scanToken()
        }
        tokens.add(Token(TokenType.EOF, "", null, line))
        return tokens
    }

    private fun isAtEnd(): Boolean = current >= source.length

    private fun advance(): Char {
        val c = source[current]
        current++
        return c
    }

    private fun peek(): Char {
        if (isAtEnd()) return '\u0000'
        return source[current]
    }

    private fun peekNext(): Char {
        if (current + 1 >= source.length) return '\u0000'
        return source[current + 1]
    }

    private fun match(expected: Char): Boolean {
        if (isAtEnd() || source[current] != expected) return false
        current++
        return true
    }

    private fun addToken(type: TokenType, literal: Any? = null) {
        val text = source.substring(start, current)
        tokens.add(Token(type, text, literal, line))
    }

    private fun scanToken() {
        val c = advance()
        when (c) {
            '(' -> addToken(TokenType.LEFT_PAREN)
            ')' -> addToken(TokenType.RIGHT_PAREN)
            '{' -> addToken(TokenType.LEFT_BRACE)
            '}' -> addToken(TokenType.RIGHT_BRACE)
            '+' -> addToken(TokenType.PLUS)
            '-' -> addToken(TokenType.MINUS)
            '*' -> addToken(TokenType.STAR)

            '=' -> addToken(if (match('=')) TokenType.EQUAL_EQUAL else TokenType.EQUAL)
            '!' -> {
                if (match('=')) addToken(TokenType.BANG_EQUAL)
                else ErrorReporter.error(line, "Unexpected character '!'. Use 'not' for negation.")
            }
            '<' -> addToken(if (match('=')) TokenType.LESS_EQUAL else TokenType.LESS)
            '>' -> addToken(if (match('=')) TokenType.GREATER_EQUAL else TokenType.GREATER)
            ',' -> addToken(TokenType.COMMA)
            '.' -> addToken(TokenType.DOT)

            '/' -> addToken(TokenType.SLASH)
            '#' -> {
                while (peek() != '\n' && !isAtEnd()) advance()
            }

            ' ', '\r', '\t' -> {  }
            '\n' -> line++

            '"' -> string()  
 
            else -> {
                when {
                    c.isDigit() -> number()
                    isAlpha(c) -> identifier()
                    else -> ErrorReporter.error(line, "Unexpected character '$c'.")
                }
            }
        }
    }

    private fun isAlpha(c: Char): Boolean = c.isLetter() || c == '_'
    private fun isAlphaNumeric(c: Char): Boolean = isAlpha(c) || c.isDigit()

    private fun identifier() {
        while (isAlphaNumeric(peek())) advance()
        val text = source.substring(start, current)
        val type = keywords[text] ?: TokenType.IDENTIFIER
        addToken(type)
    }

    private fun number() {
        while (peek().isDigit()) advance()

        if (peek() == '.' && peekNext().isDigit()) {
            advance() // consume the "."
            while (peek().isDigit()) advance()
        }

        val value = source.substring(start, current).toDouble()
        addToken(TokenType.NUMBER, value)
    }

    private fun string() {
        val sb = StringBuilder()
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++
            if (peek() == '\\') {
                advance()

                if (isAtEnd()) {
                    ErrorReporter.error(line, "Unterminated string.")
                    return
                }

                val escapeCharacter = advance()
                when (escapeCharacter) {
                    'n' -> sb.append('\n')
                    't' -> sb.append('\t')
                    '"' -> sb.append('"') 
                    '\\' -> sb.append('\\')
                    else -> ErrorReporter.error(line, "Invalid escape sequence '\\$escapeCharacter'.")
                }

            } else {
                sb.append(advance())   
            }
        }

        if (isAtEnd()) {
            ErrorReporter.error(line, "Unterminated string.")
            return
        }

        advance() 
        addToken(TokenType.STRING, sb.toString())
    }
}