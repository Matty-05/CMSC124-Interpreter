object AstPrinter {
    fun print(expr: Expr): String {
        return when (expr) {
            is Expr.Literal -> stringify(expr.value)
            is Expr.Binary -> parenthesize(expr.operator.lexeme, expr.left, expr.right)
            is Expr.Unary -> parenthesize(expr.operator.lexeme, expr.right)
            is Expr.Grouping -> parenthesize("group", expr.expression)
        }
    }

    private fun parenthesize(name: String, vararg exprs: Expr): String {
        val sb = StringBuilder()
        sb.append("(").append(name)
        for (expr in exprs) {
            sb.append(" ")
            sb.append(print(expr))
        }
        sb.append(")")
        return sb.toString()
    }

    private fun stringify(value: Any?): String {
        if (value == null) return "void"
        if (value is Double) {
            return value.toString()
        }
        return value.toString()
    }
}