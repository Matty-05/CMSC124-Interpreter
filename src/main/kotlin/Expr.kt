sealed class Expr {

    // program → expression* EOF
    class Program(val expressions: List<Expr>) : Expr()

    // primary → NUMBER | STRING | "active" | "inactive" | "void"
    class Literal(val value: Any?) : Expr()

    // or, and, equality, comparison, term, factor — every left-associative binary rule
    class Binary(val left: Expr, val operator: Token, val right: Expr) : Expr()

    // unary → ( "not" | "-" ) unary | primary
    class Unary(val operator: Token, val right: Expr) : Expr()

    // primary → "(" expression ")"
    class Grouping(val expression: Expr) : Expr()
}