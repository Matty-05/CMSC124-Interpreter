sealed class Expr {
    // primary → NUMBER | STRING | "active" | "inactive" | "void"
    class Literal(val value: Any?) : Expr()

    // term, factor, and, or — any left-associative binary rule
    class Binary(val left: Expr, val operator: Token, val right: Expr) : Expr()

    // unary → ( "not" | "-" ) unary | primary
    class Unary(val operator: Token, val right: Expr) : Expr()

     // primary → "(" expression ")"
    class Grouping(val expression: Expr) : Expr()
}