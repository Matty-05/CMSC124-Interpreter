object Evaluator {
    fun evaluate(expr: Expr): Any? = when (expr) {
        is Expr.Literal -> expr.value
        is Expr.Grouping -> evaluate(expr.expression)
        is Expr.Unary -> {
            val right = evaluate(expr.right)
            when (expr.operator.type) {
                TokenType.MINUS -> -number(expr.operator, right)
                else -> throw RuntimeError(expr.operator, "Unsupported unary operator '${expr.operator.lexeme}'.")
            }
        }
        is Expr.Binary -> {
            val left = evaluate(expr.left)
            val right = evaluate(expr.right)
            when (expr.operator.type) {
                TokenType.PLUS, TokenType.MINUS, TokenType.STAR, TokenType.SLASH -> {
                    if (left !is Double || right !is Double) {
                        throw RuntimeError(expr.operator, "Operands must be numbers.")
                    }
                    when (expr.operator.type) {
                        TokenType.PLUS -> left + right
                        TokenType.MINUS -> left - right
                        TokenType.STAR -> left * right
                        else -> {
                            if (right == 0.0) throw RuntimeError(expr.operator, "Division by zero.")
                            left / right
                        }
                    }
                }
                else -> throw RuntimeError(expr.operator, "Unsupported binary operator '${expr.operator.lexeme}'.")
            }
        }
    }

    private fun number(operator: Token, value: Any?): Double {
        if (value is Double) return value
        throw RuntimeError(operator, "Operand must be a number.")
    }

    fun stringify(value: Any?): String = when (value) {
        null -> "void"
        is Double -> value.toString().removeSuffix(".0")
        else -> value.toString()
    }
}
