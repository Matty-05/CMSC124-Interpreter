enum class TokenType {
    // single-character tokens
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    PLUS, MINUS, STAR, SLASH,
    COMMA, DOT,

    // one or two character tokens
    EQUAL, EQUAL_EQUAL,
    BANG, BANG_EQUAL,
    LESS, LESS_EQUAL,
    GREATER, GREATER_EQUAL,

    // literals
    IDENTIFIER, STRING, NUMBER,

    // keywords
    UNIT, REPORT, PROTOCOL, TRANSMIT,
    ACTIVE, INACTIVE, VOID,
    IF, ELSE, WHILE, FOR,
    AND, OR, NOT,

    EOF
}