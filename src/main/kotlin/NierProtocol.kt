class NierProtocol(
    private val declaration: Stmt.Protocol,
    private val closure: Environment
) : NierCallable {
    override fun arity(): Int = declaration.parameters.size

    // A stack overflow from deep recursion is reported by the evaluator at the call site.
    override fun call(evaluator: Evaluator, arguments: List<Any?>): Any? {
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
