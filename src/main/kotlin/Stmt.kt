sealed class Stmt {
    class Protocol(val name: Token, val parameters: List<Token>, val body: Block) : Stmt()
    class Transmit(val keyword: Token, val value: Expr?) : Stmt()
    class If(val condition: Expr, val thenBranch: Stmt, val elseBranch: Stmt?, val line: Int) : Stmt()
    // whileStmt -> "while" "(" expression ")" statement
    class While(val condition: Expr, val body: Stmt, val line: Int) : Stmt()
    class Block(val statements: List<Stmt>, val line: Int) : Stmt()
    // exprStmt -> expression
    class Expression(val expression: Expr) : Stmt()

    // reportStmt -> "report" expression
    class Report(val expression: Expr) : Stmt()

    // hackStmt -> "hack" expression
    class Hack(val keyword: Token, val expression: Expr) : Stmt()

    // unitDecl -> "unit" IDENTIFIER ( "=" expression )?
    class Unit(val name: Token, val initializer: Expr?) : Stmt()
}

// program -> declaration* EOF
class Program(val statements: List<Stmt>)
