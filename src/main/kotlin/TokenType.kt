enum class TokenType {
    // single-character tokens
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    PLUS, MINUS, STAR, SLASH,

    // one or two character tokens
    EQUAL, EQUAL_EQUAL,
    BANG, BANG_EQUAL,
    LESS, LESS_EQUAL,
    GREATER, GREATER_EQUAL,

    // literals
    IDENTIFIER, STRING, NUMBER,

    // keywords
    VAR, PRINT, IF, ELSE, WHILE, TRUE, FALSE, NIL,

    EOF
}