object AstPrinter {
    fun print(stmt: Stmt): String = when (stmt) {
        is Stmt.Protocol -> "(protocol ${stmt.name.lexeme} (${stmt.parameters.joinToString(" ") { it.lexeme }}) ${print(stmt.body)})"
        is Stmt.Transmit -> "(transmit${stmt.value?.let { " ${print(it)}" }.orEmpty()})"
        is Stmt.If -> "(if ${print(stmt.condition)} ${print(stmt.thenBranch)}${stmt.elseBranch?.let { " ${print(it)}" }.orEmpty()})"
        is Stmt.While -> "(while ${print(stmt.condition)} ${print(stmt.body)})"
        is Stmt.Block -> "(block${stmt.statements.joinToString("") { " ${print(it)}" }})"
        is Stmt.Expression -> print(stmt.expression)
        is Stmt.Report -> "(report ${print(stmt.expression)})"
        is Stmt.Hack -> "(hack ${print(stmt.expression)})"
        is Stmt.Scan -> "(scan ${if (stmt.all) "all " else ""}${stmt.target.lexeme})"
        is Stmt.AccessPoint -> "(access_point ${stmt.name.lexeme})"
        is Stmt.Restore -> "(restore ${stmt.name.lexeme})"
        is Stmt.Unit -> stmt.initializer?.let { "(unit ${stmt.name.lexeme} ${print(it)})" } ?: "(unit ${stmt.name.lexeme})"
    }

    // Walks any Expr node and returns its prefix-parenthesized string form,
    // e.g. (+ 1.0 2.0). One branch per node type defined in Expr.kt.
    // `literal` formats literal values; --parse keeps the default, while hack passes
    // one that quotes and escapes strings so they stay readable and on one line.
    fun print(expr: Expr, literal: (Any?) -> String = ::stringify): String {
        return when (expr) {
            is Expr.Literal -> literal(expr.value)
            is Expr.Binary -> parenthesize(expr.operator.lexeme, literal, expr.left, expr.right)
            is Expr.Unary -> parenthesize(expr.operator.lexeme, literal, expr.right)
            is Expr.Grouping -> parenthesize("group", literal, expr.expression)
            is Expr.Variable -> expr.name.lexeme
            is Expr.Call -> parenthesize("call", literal, expr.callee, *expr.arguments.toTypedArray())
            is Expr.Assign -> "(= ${expr.name.lexeme} ${print(expr.value, literal)})"
        }
    }

    // Builds "(name child1 child2 ...)" by recursively printing each child.
    private fun parenthesize(name: String, literal: (Any?) -> String, vararg exprs: Expr): String {
        val sb = StringBuilder()
        sb.append("(").append(name)
        for (expr in exprs) {
            sb.append(" ")
            sb.append(print(expr, literal))
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
