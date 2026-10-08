object AstPrinter {
    // Walks any Expr node and returns its prefix-parenthesized string form,
    // e.g. (+ 1.0 2.0). One branch per node type defined in Expr.kt.
    fun print(expr: Expr): String {
        return when (expr) {
            is Expr.Literal -> stringify(expr.value)
            is Expr.Binary -> parenthesize(expr.operator.lexeme, expr.left, expr.right)
            is Expr.Unary -> parenthesize(expr.operator.lexeme, expr.right)
            is Expr.Grouping -> parenthesize("group", expr.expression)
        }
    }

    // Builds "(name child1 child2 ...)" by recursively printing each child.
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

    // Converts a literal's raw value into its printed form.
    // Doubles always show a decimal point (4 -> "4.0"); null prints as "void".
    private fun stringify(value: Any?): String {
        if (value == null) return "void"
        return value.toString()
    }
}