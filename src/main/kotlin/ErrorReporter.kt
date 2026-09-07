object ErrorReporter {
    var hadError = false

    fun error(line: Int, message: String) {
        hadError = true
        System.err.println("[line $line] Error: $message")
    }
}