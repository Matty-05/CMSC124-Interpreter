object Evaluator {
    // Current scope; the global environment persists between REPL lines.
    val globals = Environment().apply {
        // clock() → seconds since the Unix epoch, as a number
        define("clock", object : NierCallable {
            override fun arity(): Int = 0
            override fun call(evaluator: Evaluator, arguments: List<Any?>): Any? =
                System.currentTimeMillis() / 1000.0
            override fun toString(): String = "<native clock>"
        })
    }
    private var environment = globals
    // True while the outermost call is running, so only that call handles a stack overflow.
    private var callActive = false

    fun execute(stmt: Stmt) {
        when (stmt) {
            is Stmt.Protocol -> environment.define(stmt.name.lexeme, NierProtocol(stmt, environment), stmt.name)
            is Stmt.Transmit -> throw Return(stmt.value?.let(::evaluate))
            is Stmt.If -> {
                if (isTruthy(evaluate(stmt.condition))) execute(stmt.thenBranch)
                else stmt.elseBranch?.let(::execute)
            }
            // The condition is checked again before every pass, so a false start runs the body zero times.
            is Stmt.While -> while (isTruthy(evaluate(stmt.condition))) execute(stmt.body)
            is Stmt.Block -> executeBlock(stmt.statements, Environment(environment))
            is Stmt.Expression -> evaluate(stmt.expression)
            is Stmt.Report -> println(stringify(evaluate(stmt.expression)))
            is Stmt.Hack -> Hack.run(stmt.expression)
            is Stmt.Scan -> {
                if (stmt.target.type == TokenType.STRING) {
                    val scan = environment.changesSinceAccessPoint(stmt.target)
                    if (scan.truncated) println("[Pod 042] Some changes since ${stmt.target.lexeme} are no longer retained.")
                    scan.changes.forEach(::printChange)
                } else {
                    val scan = environment.scanUnit(stmt.target, stmt.all)
                    println("[Pod 042] Scan: ${stmt.target.lexeme} = ${Hack.text(scan.value)}")
                    scan.changes.forEach { change ->
                        println("line ${change.line}: ${display(change.after)} (${describe(change)})")
                    }
                }
            }
            is Stmt.AccessPoint -> environment.saveAccessPoint(stmt.name.literal as String)
            is Stmt.Restore -> {
                val changes = environment.restoreAccessPoint(stmt.name)
                println("[Pod 042] Restoring ${stmt.name.lexeme}:")
                changes.forEach(::printChange)
            }
            // A unit with no value holds void.
            is Stmt.Unit -> environment.defineUnit(stmt.name, stmt.initializer?.let(::evaluate), stmt.initializer)
        }
    }

    private fun display(value: Environment.UnitValue?): String =
        if (value == null) "<undefined>" else Hack.text(value.value)

    private fun describe(change: Environment.Change): String =
        change.description ?: change.expression?.let(Hack::source) ?: "void"

    private fun printChange(change: Environment.Change) {
        println("[Pod 042] ${change.name}: ${display(change.before)} -> ${display(change.after)}")
    }

    internal fun assign(expr: Expr.Assign, value: Any?): Any? {
        environment.assign(expr.name, value, expr.value)
        return value
    }

    fun executeBlock(statements: List<Stmt>, scope: Environment) {
        val previous = environment
        try {
            environment = scope
            statements.forEach(::execute)
        } finally {
            environment = previous
        }
    }

    fun evaluate(expr: Expr): Any? = when (expr) {
        is Expr.Literal -> expr.value
        is Expr.Grouping -> evaluate(expr.expression)
        is Expr.Variable -> environment.get(expr.name)
        is Expr.Assign -> assign(expr, evaluate(expr.value))
        is Expr.Call -> {
            val callee = evaluate(expr.callee)
            val arguments = expr.arguments.map(::evaluate)
            // Checked here once, so protocols and natives share the same two errors.
            if (callee !is NierCallable) throw RuntimeError(expr.paren, "Can only call protocols.")
            if (arguments.size != callee.arity()) {
                throw RuntimeError(expr.paren, "Expected ${callee.arity()} arguments but got ${arguments.size}.")
            }
            if (callActive) callee.call(this, arguments)
            else try {
                callActive = true
                callee.call(this, arguments)
            } catch (_: StackOverflowError) {
                // By now the stack has unwound to the outermost call, so the error points at
                // the line that started the recursion, not where the protocol was declared.
                throw RuntimeError(expr.paren, "Stack overflow: protocol calls nested too deeply.")
            } finally {
                callActive = false
            }
        }
        is Expr.Unary -> {
            val right = evaluate(expr.right)
            when (expr.operator.type) {
                TokenType.MINUS -> -checkNumberOperand(expr.operator, right)
                TokenType.NOT -> !isTruthy(right)
                else -> throw RuntimeError(expr.operator, "Unsupported unary operator '${expr.operator.lexeme}'.")
            }
        }
        is Expr.Binary -> {
            val left = evaluate(expr.left)
            when {
                expr.operator.type == TokenType.AND && !isTruthy(left) -> false
                expr.operator.type == TokenType.OR && isTruthy(left) -> true
                else -> {
                    val right = evaluate(expr.right)
                    when (expr.operator.type) {
                        TokenType.AND -> isTruthy(right)
                        TokenType.OR -> isTruthy(right)
                        TokenType.EQUAL_EQUAL -> left == right
                        TokenType.BANG_EQUAL -> left != right
                        TokenType.PLUS -> when {
                            left is Double && right is Double -> left + right
                            left is String && right is String -> left + right
                            else -> throw RuntimeError(expr.operator, "Operands must be two numbers or two strings.")
                        }
                        TokenType.MINUS, TokenType.STAR, TokenType.SLASH,
                        TokenType.GREATER, TokenType.GREATER_EQUAL,
                        TokenType.LESS, TokenType.LESS_EQUAL -> {
                            val (leftNumber, rightNumber) = checkNumberOperands(expr.operator, left, right)
                            when (expr.operator.type) {
                                TokenType.MINUS -> leftNumber - rightNumber
                                TokenType.STAR -> leftNumber * rightNumber
                                TokenType.SLASH -> {
                                    if (rightNumber == 0.0) throw RuntimeError(expr.operator, "Division by zero.")
                                    leftNumber / rightNumber
                                }
                                TokenType.GREATER -> leftNumber > rightNumber
                                TokenType.GREATER_EQUAL -> leftNumber >= rightNumber
                                TokenType.LESS -> leftNumber < rightNumber
                                else -> leftNumber <= rightNumber
                            }
                        }
                        else -> throw RuntimeError(expr.operator, "Unsupported binary operator '${expr.operator.lexeme}'.")
                    }
                }
            }
        }
    }

    private fun checkNumberOperand(operator: Token, value: Any?): Double {
        if (value is Double) return value
        throw RuntimeError(operator, "Operand must be a number.")
    }

    private fun checkNumberOperands(operator: Token, left: Any?, right: Any?): Pair<Double, Double> {
        if (left is Double && right is Double) return Pair(left, right)
        throw RuntimeError(operator, "Operands must be numbers.")
    }

    fun isTruthy(value: Any?): Boolean = value != null && value != false

    fun stringify(value: Any?): String = when (value) {
        null -> "void"
        true -> "active"
        false -> "inactive"
        is Double -> value.toString().removeSuffix(".0")
        else -> value.toString()
    }
}
