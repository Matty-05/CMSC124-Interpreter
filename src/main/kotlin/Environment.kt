// Holds the value bound to each unit name. One global environment for now.
class Environment {
    private val values = mutableMapOf<String, Any?>()

    // unit x = value; declaring a name again rebinds it
    fun define(name: String, value: Any?) {
        values[name] = value
    }

    fun get(name: Token): Any? {
        if (name.lexeme in values) return values[name.lexeme]
        throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }

    fun assign(name: Token, value: Any?) {
        if (name.lexeme !in values) {
            throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
        }
        values[name.lexeme] = value
    }
}
