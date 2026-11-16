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

    fun execute(stmt: Stmt) {
        when (stmt) {
            is Stmt.Protocol -> environment.define(stmt.name.lexeme, NierProtocol(stmt, environment))
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
            // A unit with no value holds void.
            is Stmt.Unit -> environment.define(stmt.name.lexeme, stmt.initializer?.let { evaluate(it) })
        }
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
        is Expr.Assign -> {
            val value = evaluate(expr.value)
            environment.assign(expr.name, value)
            value
        }
        is Expr.Call -> {
            val callee = evaluate(expr.callee)
            val arguments = expr.arguments.map(::evaluate)
            // Checked here once, so protocols and natives share the same two errors.
            if (callee !is NierCallable) throw RuntimeError(expr.paren, "Can only call protocols.")
            if (arguments.size != callee.arity()) {
                throw RuntimeError(expr.paren, "Expected ${callee.arity()} arguments but got ${arguments.size}.")
            }
            callee.call(this, arguments)
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

    private fun isTruthy(value: Any?): Boolean = value != null && value != false

    fun stringify(value: Any?): String = when (value) {
        null -> "void"
        true -> "active"
        false -> "inactive"
        is Double -> value.toString().removeSuffix(".0")
        else -> value.toString()
    }
}
