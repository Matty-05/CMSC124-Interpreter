import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    when {
        args.size == 2 && args[0] == "--tokenize" -> runFile(args[1])
        args.size == 2 && args[0] == "--parse" -> runParseFile(args[1])
        args.isEmpty() -> runPrompt()
        args.size == 1 && !args[0].startsWith("--") -> runProgram(args[0])
        else -> {
            System.err.println("Usage: run [--tokenize <path>] [--parse <path>]")
            exitProcess(64)
        }
    }
}

fun runFile(path: String) {
    val source = File(path).readText()
    val tokens = scan(source)
    // A rejected file puts nothing on stdout. The whole file is scanned first
    // so every error is reported, then the token stream is discarded.
    if (ErrorReporter.hadError) exitProcess(65)
    for (token in tokens) {
        println(token)
    }
    exitProcess(0)
}

fun runParseFile(path: String) {
    val source = File(path).readText()
    val lines = source.lines().filter { it.isNotBlank() }

    var hadAnyError = false

    for (line in lines) {
        ErrorReporter.hadError = false
        val scanner = Scanner(line)
        val tokens = scanner.scanTokens()
        val parser = Parser(tokens)
        try {
            val expr = parser.parse()
            if (!ErrorReporter.hadError) println(AstPrinter.print(expr))
        } catch (e: Parser.ParseError) {
            // Error message and hadError flag were already set inside Parser's error() function.
        }
        if (ErrorReporter.hadError) hadAnyError = true
    }

    if (hadAnyError) exitProcess(65)
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
        if (line.isBlank()) continue
        if (line.trim().lowercase() in listOf("exit", "quit")) break
        ErrorReporter.hadError = false
        val scanner = Scanner(line)
        val tokens = scanner.scanTokens()
        val parser = Parser(tokens)
        try {
            val expr = parser.parse()
            if (!ErrorReporter.hadError) println(AstPrinter.print(expr))
        } catch (e: Parser.ParseError) {
            // Error message and hadError flag were already set inside Parser's error() function.
        }
    }
}

fun scan(source: String): List<Token> = Scanner(source).scanTokens()
