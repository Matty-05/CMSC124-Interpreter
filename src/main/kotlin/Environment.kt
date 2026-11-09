// Holds the value bound to each unit name. A block's environment points to the
// one around it, so lookup and assignment walk outward until they find the name.
class Environment(private val enclosing: Environment? = null) {
    private val values = mutableMapOf<String, Any?>()

    // unit x = value; always binds in this scope, so an inner unit shadows an outer one
    fun define(name: String, value: Any?) {
        values[name] = value
    }

    fun get(name: Token): Any? {
        if (name.lexeme in values) return values[name.lexeme]
        if (enclosing != null) return enclosing.get(name)
        throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }

    // x = value; changes the nearest existing x instead of creating a new one
    fun assign(name: Token, value: Any?) {
        if (name.lexeme in values) {
            values[name.lexeme] = value
            return
        }
        if (enclosing != null) return enclosing.assign(name, value)
        throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }
}
