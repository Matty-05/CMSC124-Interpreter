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
        val tokens = Scanner(source).scanTokens()

        val realTokens = tokens.dropLast(1) // drop the scanner's own trailing EOF

        if (realTokens.isEmpty()) {
            throw Parser.ParseError("[line 1] Error: Expect expression.")
        }

        // Store actual AST roots. Rendering happens only after parsing succeeds.
        val expressions = mutableListOf<Expr>()
        val errors = mutableListOf<String>()
        for ((line, lineTokens) in realTokens.groupBy { it.line }) {
            val eof = Token(TokenType.EOF, "", null, line)
            try {
                val expression = Parser(lineTokens + eof).parse()
                expressions += expression
            } catch (e: Parser.ParseError) {
                errors += e.message.orEmpty()
            }
        }

        if (errors.isNotEmpty()) throw Parser.ParseError(errors.joinToString("\n"))
        for (expression in expressions) {
            println(AstPrinter.print(expression))
        }
    } catch (e: Scanner.ScanError) {
        System.err.println(e.message)
        exitProcess(65)
    } catch (e: Parser.ParseError) {
        System.err.println(e.message)
        exitProcess(65)
    }
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

        try {
            val tokens = Scanner(line).scanTokens()
            val parser = Parser(tokens)
            val expr = parser.parse()
            println(AstPrinter.print(expr))
        } catch (e: Scanner.ScanError) {
            System.err.println(e.message)
        } catch (e: Parser.ParseError) {
            System.err.println(e.message)
        }
    }
}


fun scan(source: String): List<Token> = Scanner(source).scanTokens()
