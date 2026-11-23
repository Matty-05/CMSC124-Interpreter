// hack <expression> shows how an expression got its result: the source, its grouping
// tree, each operation innermost first with real values, and the result.
//
// The tree is walked here, but every operation is handed back to the Evaluator with its
// operands already reduced to literals. So hack follows exactly the same rules and
// raises exactly the same errors as normal evaluation.
object Hack {
    fun run(expr: Expr) {
        println("[Pod 042] Hack: ${source(expr)}")
        // The --parse tree, except strings are quoted and escaped like everywhere else in hack.
        println("Grouping: ${AstPrinter.print(expr) { value ->
            if (value is String) text(value) else AstPrinter.print(Expr.Literal(value))
        }}")
        val trace = Trace()
        try {
            val result = trace.walk(expr).value
            printSteps(trace.steps)
            println("Result: ${Evaluator.stringify(result)}")
        } catch (e: RuntimeError) {
            // Show the steps that worked, so the failing one is the next operation.
            printSteps(trace.steps)
            throw e
        }
    }

    private fun printSteps(steps: List<String>) {
        if (steps.isEmpty()) {
            println("Steps: none")
            return
        }
        println("Steps:")
        // No indent: the harness strips expected lines, so "  1." could never match.
        steps.forEachIndexed { index, step -> println("${index + 1}. $step") }
    }

    // An operand as written (names) and with names replaced by their values.
    private class Shown(val written: String, val value: String)

    private class Walked(val value: Any?, val shown: Shown)

    // One trace per hack statement, so a hack inside a called protocol keeps its own steps.
    private class Trace {
        val steps = mutableListOf<String>()

        fun walk(expr: Expr): Walked = when (expr) {
            is Expr.Literal -> plain(expr.value)
            is Expr.Grouping -> walk(expr.expression)
            is Expr.Variable -> {
                val value = Evaluator.evaluate(expr)
                Walked(value, Shown(expr.name.lexeme, text(value)))
            }
            is Expr.Unary -> {
                val right = walk(expr.right)
                val op = expr.operator.lexeme + if (expr.operator.type == TokenType.NOT) " " else ""
                step(op + right.shown.written, op + right.shown.value,
                    Evaluator.evaluate(Expr.Unary(expr.operator, Expr.Literal(right.value))))
            }
            is Expr.Binary -> binary(expr)
            is Expr.Call -> {
                val callee = walk(expr.callee)
                val arguments = expr.arguments.map(::walk)
                // The callee keeps its name on both sides; <protocol f>(40) reads badly.
                val name = callee.shown.written
                step("$name(${arguments.joinToString(", ") { it.shown.written }})",
                    "$name(${arguments.joinToString(", ") { it.shown.value }})",
                    Evaluator.evaluate(Expr.Call(Expr.Literal(callee.value), expr.paren,
                        arguments.map { Expr.Literal(it.value) })))
            }
            is Expr.Assign -> {
                val value = walk(expr.value)
                val name = expr.name.lexeme
                val old = try { Evaluator.evaluate(Expr.Variable(expr.name)) } catch (_: RuntimeError) { null }
                // Assigning to an undeclared name throws the normal assignment error here.
                val result = Evaluator.assign(expr, value.value)
                steps += "${pair("$name = ${value.shown.written}", "$name = ${value.shown.value}")}" +
                    " ($name was ${text(old)}, now ${text(result)})"
                plain(result)
            }
        }

        private fun binary(expr: Expr.Binary): Walked {
            val left = walk(expr.left)
            val op = expr.operator
            val decided = (op.type == TokenType.AND && !Evaluator.isTruthy(left.value)) ||
                (op.type == TokenType.OR && Evaluator.isTruthy(left.value))
            if (decided) {
                // The Evaluator short-circuits too, so the right side never runs.
                val result = Evaluator.evaluate(Expr.Binary(Expr.Literal(left.value), op, expr.right))
                val right = source(expr.right)
                steps += "${pair("${left.shown.written} ${op.lexeme} $right", "${left.shown.value} ${op.lexeme} $right")}" +
                    " = ${text(result)} (right side skipped)"
                return plain(result)
            }
            val right = walk(expr.right)
            return step("${left.shown.written} ${op.lexeme} ${right.shown.written}",
                "${left.shown.value} ${op.lexeme} ${right.shown.value}",
                Evaluator.evaluate(Expr.Binary(Expr.Literal(left.value), op, Expr.Literal(right.value))))
        }

        // Records "written -> values = result" and passes the result up as a plain value.
        private fun step(written: String, value: String, result: Any?): Walked {
            val shown = pair(written, value)
            // A step like -5 = -5 tells nothing, so it is left out.
            if (shown != text(result)) steps += "$shown = ${text(result)}"
            return plain(result)
        }

        // Only show the values side when it differs from what was written.
        private fun pair(written: String, value: String) =
            if (written == value) written else "$written -> $value"

        private fun plain(value: Any?) = Walked(value, Shown(text(value), text(value)))
    }

    // The expression written back out in infix form for the Hack: line.
    internal fun source(expr: Expr): String = when (expr) {
        is Expr.Literal -> text(expr.value)
        is Expr.Grouping -> "(${source(expr.expression)})"
        is Expr.Variable -> expr.name.lexeme
        is Expr.Unary -> expr.operator.lexeme +
            (if (expr.operator.type == TokenType.NOT) " " else "") + source(expr.right)
        is Expr.Binary -> "${source(expr.left)} ${expr.operator.lexeme} ${source(expr.right)}"
        is Expr.Call -> "${source(expr.callee)}(${expr.arguments.joinToString(", ") { source(it) }})"
        is Expr.Assign -> "${expr.name.lexeme} = ${source(expr.value)}"
    }

    // Values as report prints them, except strings are written back as Nier string
    // literals: quoted, so "1" and 1 differ, and escaped, so a quote or newline inside
    // the string cannot break the line.
    internal fun text(value: Any?): String =
        if (value is String) "\"${escape(value)}\"" else Evaluator.stringify(value)

    // Undoes the scanner's four escapes. The backslash goes first so the backslashes
    // added for the others are not doubled.
    private fun escape(value: String): String = value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\t", "\\t")
}
