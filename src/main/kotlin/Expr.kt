sealed class Expr {
    class Literal(val value: Any?) : Expr()
    class Binary(val left: Expr, val operator: Token, val right: Expr) : Expr()
}