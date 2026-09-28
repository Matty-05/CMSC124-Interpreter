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
    // The whole file is scanned first, so every error is already on stderr.
    // The tokens print either way, so a rejected file still shows what the
    // scanner made of it; the exit code is what marks it as rejected.
    for (token in tokens) {
        println(token)
    }
    if (ErrorReporter.hadError) exitProcess(65)
    exitProcess(0)
}

fun runParseFile(path: String) {
    val source = File(path).readText()
    ErrorReporter.hadError = false
    val scanner = Scanner(source)
    val tokens = scanner.scanTokens()
    val parser = Parser(tokens)
    try {
        val expr = parser.parse()
        if (!ErrorReporter.hadError) println(printExpr(expr))
    } catch (e: Parser.ParseError) {
        // error already reported to stderr inside Parser.error()
    }
    if (ErrorReporter.hadError) exitProcess(65)
    exitProcess(0)
}


fun printExpr(expr: Expr): String {
    return when (expr) {
        is Expr.Literal -> expr.value?.toString() ?: "void"
        is Expr.Binary -> "(${expr.operator.lexeme} ${printExpr(expr.left)} ${printExpr(expr.right)})"
    }
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
        val tokens = scan(line)
        // Unlike a file, the REPL shows the tokens even when the line has an
        // error. The diagnostics print first, during the scan, so the user sees
        // what went wrong and what the scanner still made of the rest. The
        // error is not fatal, so the prompt comes back either way.
        for (token in tokens) {
            println(token)
        }
    }
}

fun scan(source: String): List<Token> = Scanner(source).scanTokens()
