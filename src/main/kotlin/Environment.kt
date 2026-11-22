// Holds the value bound to each unit name. A block's environment points to the
// one around it, so lookup and assignment walk outward until they find the name.
class Environment(private val enclosing: Environment? = null) {
    private data class Binding(val value: Any?, val isUnit: Boolean)
    // The wrapper distinguishes an absent unit from a unit containing void.
    data class UnitValue(val value: Any?)
    data class Change(val name: String, val before: UnitValue?, val after: UnitValue?)
    private data class Entry(val order: Long, val change: Change)
    private class HistoryClock(var next: Long = 0)
    private data class SavedScope(val environment: Environment, val units: Map<String, Binding>, val cursor: Int)

    private val values = mutableMapOf<String, Binding>()
    private val historyClock: HistoryClock = enclosing?.historyClock ?: HistoryClock()
    private val history = mutableListOf<Entry>()
    private val accessPoints = mutableMapOf<String, List<SavedScope>>()

    // Protocols, parameters, and natives are bindings but are not unit declarations.
    fun define(name: String, value: Any?) {
        write(name, Binding(value, isUnit = false))
    }

    fun defineUnit(name: String, value: Any?) {
        write(name, Binding(value, isUnit = true))
    }

    fun get(name: Token): Any? {
        values[name.lexeme]?.let { return it.value }
        if (enclosing != null) return enclosing.get(name)
        throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }

    // x = value; changes the nearest existing x instead of creating a new one
    fun assign(name: Token, value: Any?) {
        val binding = values[name.lexeme]
        if (binding != null) {
            write(name.lexeme, binding.copy(value = value))
            return
        }
        if (enclosing != null) return enclosing.assign(name, value)
        throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }

    fun unitHistory(name: Token, all: Boolean): List<Change> {
        val binding = values[name.lexeme]
        if (binding == null && enclosing != null) return enclosing.unitHistory(name, all)
        if (binding?.isUnit != true) throw RuntimeError(name, "Undefined unit '${name.lexeme}'.")

        val entries = history.asReversed().asSequence().map { it.change }
            .filter { it.name == name.lexeme }
        return (if (all) entries else entries.take(5)).toList().asReversed()
    }

    fun saveAccessPoint(name: String) {
        accessPoints[name] = generateSequence(this) { it.enclosing }.map { scope ->
            SavedScope(scope, scope.values.filterValues { it.isUnit }, scope.history.size)
        }.toList()
    }

    fun changesSinceAccessPoint(name: Token): List<Change> = findAccessPoint(name)
        .flatMap { saved -> saved.environment.history.drop(saved.cursor) }
        .sortedBy { it.order }.map { it.change }

    fun restoreAccessPoint(name: Token): List<Change> = buildList {
        for (saved in findAccessPoint(name)) {
            val scope = saved.environment
            val names = scope.values.filterValues { it.isUnit }.keys + saved.units.keys
            for (unit in names) scope.write(unit, saved.units[unit])?.let(::add)
        }
    }

    private fun findAccessPoint(name: Token): List<SavedScope> {
        val label = name.literal as String
        return accessPoints[label] ?: enclosing?.findAccessPoint(name)
            ?: throw RuntimeError(name, "Access point \"$label\" not found.")
    }

    private fun write(name: String, binding: Binding?): Change? {
        val previous = if (binding == null) values.remove(name) else values.put(name, binding)
        val before = previous?.takeIf { it.isUnit }?.let { UnitValue(it.value) }
        val after = binding?.takeIf { it.isUnit }?.let { UnitValue(it.value) }
        if (before == after) return null
        return Change(name, before, after).also { history += Entry(historyClock.next++, it) }
    }
}
