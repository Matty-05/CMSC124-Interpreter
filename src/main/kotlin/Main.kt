import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    when {
        args.size == 2 && args[0] == "--tokenize" -> runFile(args[1])
        args.size == 2 && args[0] == "--parse" -> runParseFile(args[1])
        args.size == 2 && args[0] == "--eval" -> runEvalFile(args[1])
        args.isEmpty() -> runPrompt()
        args.size == 1 && !args[0].startsWith("--") -> runProgram(args[0])
        else -> {
            System.err.println("Usage: run [<path> | --tokenize <path> | --parse <path> | --eval <path>]")
            exitProcess(64)
        }
    }
}

fun runFile(path: String) {
    val source = File(path).readText()
    try {
        scan(source).forEach(::println)
    } catch (e: Scanner.ScanError) {
        System.err.println(e.message)
        // Token mode still shows the tokens collected from rejected input.
        e.tokens.forEach(::println)
        exitProcess(65)
    }
    exitProcess(0)
}

fun runParseFile(path: String) {
    val source = File(path).readText()
    try {
        val program = Parser(Scanner(source).scanTokens()).parse()
        println(AstPrinter.print(program))
    } catch (e: Scanner.ScanError) {
        System.err.println(e.message)
        exitProcess(65)
    } catch (e: Parser.ParseError) {
        System.err.println(e.message)
        exitProcess(65)
    }
    exitProcess(0)
}

fun runEvalFile(path: String) {
    val program = try {
        Parser(Scanner(File(path).readText()).scanTokens()).parse()
    } catch (e: Scanner.ScanError) {
        System.err.println(e.message)
        exitProcess(65)
    } catch (e: Parser.ParseError) {
        System.err.println(e.message)
        exitProcess(65)
    }

    try {
        for (expr in program.expressions) println(Evaluator.stringify(Evaluator.evaluate(expr)))
    } catch (e: RuntimeError) {
        System.err.println("[line ${e.token.line}] Runtime error: ${e.message}")
        exitProcess(70)
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

        try {
            val tokens = Scanner(line).scanTokens()
            Parser(tokens).parse().expressions.forEach {
                println(Evaluator.stringify(Evaluator.evaluate(it)))
            }
        } catch (e: Scanner.ScanError) {
            System.err.println(e.message)
        } catch (e: Parser.ParseError) {
            System.err.println(e.message)
        } catch (e: RuntimeError) {
            System.err.println("[line ${e.token.line}] Runtime error: ${e.message}")
        }
    }
}


fun scan(source: String): List<Token> = Scanner(source).scanTokens()
