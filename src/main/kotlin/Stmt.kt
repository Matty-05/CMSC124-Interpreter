sealed class Stmt {
    class If(val condition: Expr, val thenBranch: Stmt, val elseBranch: Stmt?, val line: Int) : Stmt()
    class Block(val statements: List<Stmt>, val line: Int) : Stmt()
    // exprStmt -> expression
    class Expression(val expression: Expr) : Stmt()

    // reportStmt -> "report" expression
    class Report(val expression: Expr) : Stmt()

    // unitDecl -> "unit" IDENTIFIER ( "=" expression )?
    class Unit(val name: Token, val initializer: Expr?) : Stmt()
}

// program -> declaration* EOF
class Program(val statements: List<Stmt>)
