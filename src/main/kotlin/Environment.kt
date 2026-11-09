// Each scope stores its own names and links to the surrounding scope.
class Environment(private val enclosing: Environment? = null) {
    private val values = mutableMapOf<String, Any?>()

    // unit x = value; declaring a name again rebinds it
    fun define(name: String, value: Any?) {
        values[name] = value
    }

    fun get(name: Token): Any? {
        if (name.lexeme in values) return values[name.lexeme]
        if (enclosing != null) return enclosing.get(name)
        throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }

    fun assign(name: Token, value: Any?) {
        if (name.lexeme !in values) {
            if (enclosing != null) return enclosing.assign(name, value)
            throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
        }
        values[name.lexeme] = value
    }
}
