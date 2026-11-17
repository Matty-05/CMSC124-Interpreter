class NierProtocol(
    private val declaration: Stmt.Protocol,
    private val closure: Environment
) : NierCallable {
    private companion object {
        var callActive = false
    }

    override fun arity(): Int = declaration.parameters.size

    override fun call(evaluator: Evaluator, arguments: List<Any?>): Any? {
        if (callActive) return invoke(evaluator, arguments)
        callActive = true
        try {
            return invoke(evaluator, arguments)
        } catch (_: StackOverflowError) {
            // Unwind to the outermost call before allocating the diagnostic.
            throw RuntimeError(declaration.name, "Stack overflow: protocol calls nested too deeply.")
        } finally {
            callActive = false
        }
    }

    private fun invoke(evaluator: Evaluator, arguments: List<Any?>): Any? {
        val scope = Environment(closure)
        declaration.parameters.forEachIndexed { index, parameter ->
            scope.define(parameter.lexeme, arguments[index])
        }
        return try {
            evaluator.executeBlock(declaration.body.statements, scope)
            null
        } catch (result: Return) {
            result.value
        }
    }

    override fun toString(): String = "<protocol ${declaration.name.lexeme}>"
}
