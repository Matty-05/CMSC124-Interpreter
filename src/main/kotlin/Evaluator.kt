object Evaluator {
    fun evaluate(expr: Expr): Any? = when (expr) {
        // Main prints each expression's value; evaluating a whole program yields the last one.
        is Expr.Program -> expr.expressions.map(::evaluate).last()
        is Expr.Literal -> expr.value
        is Expr.Grouping -> evaluate(expr.expression)
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
