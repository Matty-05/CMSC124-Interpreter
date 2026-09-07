import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    when {
        args.size == 2 && args[0] == "--tokenize" -> runFile(args[1])
        args.size == 1 && !args[0].startsWith("--") -> runProgram(args[0])
        args.isEmpty() -> runPrompt()
        else -> {
            System.err.println("Usage: run [--tokenize <path>]")
            exitProcess(64)
        }
    }
}

fun runFile(path: String) {
    val source = File(path).readText()
    run(source)
    if (ErrorReporter.hadError) exitProcess(65)
    exitProcess(0)
}

fun runProgram(path: String) {
    println("Hello from Team Peanut Butterbonia!")
    println("Members:")
    println("Ralph Ryan T. Escabarte")
    println("John Matthew N. Fallarme")
    exitProcess(0)
}

fun runPrompt() {
    while (true) {
        print("> ")
        val line = readlnOrNull() ?: break
        if (line.trim().lowercase() in listOf("exit", "quit")) break
        ErrorReporter.hadError = false
        run(line)
        if (ErrorReporter.hadError) {
            exitProcess(65)  
        }
    }
}

fun run(source: String) {
    val scanner = Scanner(source)
    val tokens = scanner.scanTokens()
    for (token in tokens) {
        println(token)
    }
}
