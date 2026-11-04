sealed class Stmt {
    // exprStmt -> expression
    class Expression(val expression: Expr) : Stmt()

    // reportStmt -> "report" expression
    class Report(val expression: Expr) : Stmt()

    // unitDecl -> "unit" IDENTIFIER ( "=" expression )?
    class Unit(val name: Token, val initializer: Expr?) : Stmt()
}

// program -> declaration* EOF
class Program(val statements: List<Stmt>)
