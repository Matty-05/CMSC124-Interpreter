// Holds the value bound to each unit name. A block's environment points to the
// one around it, so lookup and assignment walk outward until they find the name.
class Environment(private val enclosing: Environment? = null) {
    private companion object {
        const val HISTORY_LIMIT = 100
    }

    private data class Binding(val value: Any?, val isUnit: Boolean)
    // The wrapper distinguishes an absent unit from a unit containing void.
    data class UnitValue(val value: Any?)
    data class Change(
        val name: String, val before: UnitValue?, val after: UnitValue?,
        val line: Int, val expression: Expr?, val description: String?
    )
    data class UnitScan(val value: Any?, val changes: List<Change>)
    data class AccessPointScan(val changes: List<Change>, val truncated: Boolean)
    private data class Entry(val order: Long, val change: Change)
    private class HistoryClock(var next: Long = 0)
    private data class SavedScope(val environment: Environment, val units: Map<String, Binding>, val cursor: Long)

    private val values = mutableMapOf<String, Binding>()
    private val historyClock: HistoryClock = enclosing?.historyClock ?: HistoryClock()
    private var history: MutableMap<String, ArrayDeque<Entry>>? = null
    private var lastDiscardedOrder = -1L
    private var accessPoints: MutableMap<String, List<SavedScope>>? = null

    // Protocols, parameters, and natives are bindings but are not unit declarations.
    fun define(name: String, value: Any?, declaration: Token? = null) {
        write(name, Binding(value, isUnit = false), declaration?.line ?: 0,
            description = "protocol declaration")
    }

    fun defineUnit(name: Token, value: Any?, initializer: Expr?) {
        write(name.lexeme, Binding(value, isUnit = true), name.line, initializer)
    }

    fun get(name: Token): Any? {
        values[name.lexeme]?.let { return it.value }
        if (enclosing != null) return enclosing.get(name)
        throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }

    // x = value; changes the nearest existing x instead of creating a new one
    fun assign(name: Token, value: Any?, expression: Expr) {
        val binding = values[name.lexeme]
        if (binding != null) {
            write(name.lexeme, binding.copy(value = value), name.line, expression)
            return
        }
        if (enclosing != null) return enclosing.assign(name, value, expression)
        throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }

    fun scanUnit(name: Token, all: Boolean): UnitScan {
        val binding = values[name.lexeme]
        if (binding == null && enclosing != null) return enclosing.scanUnit(name, all)
        if (binding?.isUnit != true) throw RuntimeError(name, "Undefined unit '${name.lexeme}'.")

        val entries = history?.get(name.lexeme).orEmpty()
        val selected = if (all) entries else entries.takeLast(5)
        return UnitScan(binding.value, selected.map { it.change })
    }

    fun saveAccessPoint(name: String) {
        val points = accessPoints ?: mutableMapOf<String, List<SavedScope>>().also { accessPoints = it }
        points[name] = generateSequence(this) { it.enclosing }.map { scope ->
            SavedScope(scope, scope.values.filterValues { it.isUnit }, historyClock.next)
        }.toList()
    }

    fun changesSinceAccessPoint(name: Token): AccessPointScan {
        val savedScopes = findAccessPoint(name)
        val changes = savedScopes.asSequence().flatMap { saved ->
            saved.environment.history?.values.orEmpty().asSequence().flatten()
                .filter { it.order >= saved.cursor }
        }.sortedBy { it.order }.map { it.change }.toList()
        return AccessPointScan(changes, savedScopes.any { it.environment.lastDiscardedOrder >= it.cursor })
    }

    fun restoreAccessPoint(name: Token): List<Change> = buildList {
        for (saved in findAccessPoint(name)) {
            val scope = saved.environment
            val names = scope.values.filterValues { it.isUnit }.keys + saved.units.keys
            for (unit in names) {
                scope.write(unit, saved.units[unit], name.line, description = "restore ${name.lexeme}")?.let(::add)
            }
        }
    }

    private fun findAccessPoint(name: Token): List<SavedScope> {
        val label = name.literal as String
        return accessPoints?.get(label) ?: enclosing?.findAccessPoint(name)
            ?: throw RuntimeError(name, "Access point \"$label\" not found.")
    }

    private fun write(name: String, binding: Binding?, line: Int, expression: Expr? = null,
                      description: String? = null): Change? {
        val previous = if (binding == null) values.remove(name) else values.put(name, binding)
        val before = previous?.takeIf { it.isUnit }?.let { UnitValue(it.value) }
        val after = binding?.takeIf { it.isUnit }?.let { UnitValue(it.value) }
        if (before == after) return null
        val change = Change(name, before, after, line, expression, description)
        val log = history ?: mutableMapOf<String, ArrayDeque<Entry>>().also { history = it }
        val entries = log.getOrPut(name) { ArrayDeque() }
        if (entries.size == HISTORY_LIMIT) {
            lastDiscardedOrder = maxOf(lastDiscardedOrder, entries.removeFirst().order)
        }
        entries.addLast(Entry(historyClock.next++, change))
        return change
    }
}
