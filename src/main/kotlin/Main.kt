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
    val tokens = Scanner(source).scanTokens()
    if (ErrorReporter.hadError) exitProcess(65)

    val realTokens = tokens.dropLast(1) // drop the scanner's own trailing EOF

    if (realTokens.isEmpty()) {
        ErrorReporter.error(1, "Expect expression.")
        exitProcess(65)
    }

    val output = mutableListOf<String>()
    for ((line, lineTokens) in realTokens.groupBy { it.line }) {
        val eof = Token(TokenType.EOF, "", null, line)
        try {
            output += AstPrinter.print(Parser(lineTokens + eof).parse())
        } catch (e: Parser.ParseError) {
            // Already reported inside Parser.error().
        }
    }
    
    if (ErrorReporter.hadError) exitProcess(65)
    output.forEach(::println)
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
        // Unlike a file, the REPL shows the tokens even when the line has an
        // error. The diagnostics print first, during the scan, so the user sees
        // what went wrong and what the scanner still made of the rest. The
        // error is not fatal, so the prompt comes back either way.
        for (token in tokens) {
            println(token)
        }
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
