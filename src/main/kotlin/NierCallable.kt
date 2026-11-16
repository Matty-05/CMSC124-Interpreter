// Anything that can be called with (...): native functions now, protocols next.
interface NierCallable {
    // How many arguments a call must pass.
    fun arity(): Int

    fun call(evaluator: Evaluator, arguments: List<Any?>): Any?
}
