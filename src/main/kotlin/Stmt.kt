sealed class Stmt {
    // exprStmt -> expression
    class Expression(val expression: Expr) : Stmt()

    // reportStmt -> "report" expression
    class Report(val expression: Expr) : Stmt()
}

// program -> statement* EOF
class Program(val statements: List<Stmt>)
