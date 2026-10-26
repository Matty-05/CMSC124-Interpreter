import java.io.File
import java.nio.charset.MalformedInputException
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
        val program = Parser(Scanner(source).scanTokens()).parse()
        println(AstPrinter.print(program))
    } catch (e: Scanner.ScanError) {
        System.err.println(e.message)
        exitProcess(65)
    } catch (e: Parser.ParseError) {
        System.err.println(e.message)
        exitProcess(65)
    } catch (_: StackOverflowError) {
        System.err.println("[line 1] Error: Expression nested too deeply.")
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
        val line = try {
            readlnOrNull()
        } catch (_: MalformedInputException) {
            System.err.println("Error: Invalid UTF-8 input.")
            continue
        } ?: break
        if (line.isBlank()) continue
        if (line.trim().lowercase() in listOf("exit", "quit")) break

        try {
            val tokens = Scanner(line).scanTokens()
            val program = Parser(tokens).parse()
            println(AstPrinter.print(program))
        } catch (e: Scanner.ScanError) {
            System.err.println(e.message)
        } catch (e: Parser.ParseError) {
            System.err.println(e.message)
        } catch (_: StackOverflowError) {
            System.err.println("[line 1] Error: Expression nested too deeply.")
        }
    }
}


fun scan(source: String): List<Token> = Scanner(source).scanTokens()
