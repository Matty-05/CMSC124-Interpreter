import java.io.File
import java.nio.charset.MalformedInputException
import java.io.IOException
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

// A path that can't be read is a bad argument, so it exits 64 like a usage error.
fun readSource(path: String): String = try {
    File(path).readText()
} catch (e: IOException) {
    System.err.println("Error: Could not read file '$path'.")
    exitProcess(64)
}

fun runFile(path: String) {
    val source = readSource(path)
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
    val source = readSource(path)
    try {
        val statements = Parser(Scanner(source).scanTokens()).parse()
        val trees = statements.map { stmt ->
            try {
                AstPrinter.print(stmt)
            } catch (_: StackOverflowError) {
                System.err.println("[line ${statementLine(stmt)}] Error: Expression nested too deeply.")
                exitProcess(65)
            }
        }
        trees.forEach(::println)
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
    val statements = try {
        Parser(Scanner(readSource(path)).scanTokens()).parse()
    } catch (e: Scanner.ScanError) {
        System.err.println(e.message)
        exitProcess(65)
    } catch (e: Parser.ParseError) {
        System.err.println(e.message)
        exitProcess(65)
    }

    for (stmt in statements) {
        try {
            when (stmt) {
                is Stmt.Expression -> println(Evaluator.stringify(Evaluator.evaluate(stmt.expression)))
                is Stmt.Report -> Evaluator.execute(stmt)
            }
        } catch (e: RuntimeError) {
            System.err.println("[line ${e.token.line}] Runtime error: ${e.message}")
            exitProcess(70)
        } catch (_: StackOverflowError) {
            System.err.println("[line ${statementLine(stmt)}] Runtime error: Expression nested too deeply.")
            exitProcess(70)
        }
    }
}

fun runProgram(path: String) {
    val statements = try {
        Parser(Scanner(readSource(path)).scanTokens()).parse()
    } catch (e: Scanner.ScanError) {
        System.err.println(e.message)
        exitProcess(65)
    } catch (e: Parser.ParseError) {
        System.err.println(e.message)
        exitProcess(65)
    }

    for (stmt in statements) {
        try {
            Evaluator.execute(stmt)
        } catch (e: RuntimeError) {
            System.err.println("[line ${e.token.line}] Runtime error: ${e.message}")
            exitProcess(70)
        } catch (_: StackOverflowError) {
            System.err.println("[line ${statementLine(stmt)}] Runtime error: Expression nested too deeply.")
            exitProcess(70)
        }
    }
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
            Parser(tokens).parse().forEach { stmt ->
                when (stmt) {
                    is Stmt.Expression -> println(Evaluator.stringify(Evaluator.evaluate(stmt.expression)))
                    is Stmt.Report -> Evaluator.execute(stmt)
                }
            }
        } catch (e: Scanner.ScanError) {
            System.err.println(e.message)
        } catch (e: Parser.ParseError) {
            System.err.println(e.message)
        } catch (e: RuntimeError) {
            System.err.println("[line ${e.token.line}] Runtime error: ${e.message}")
        } catch (_: StackOverflowError) {
            System.err.println("[line 1] Runtime error: Expression nested too deeply.")
        }
    }
}

private fun statementLine(stmt: Stmt): Int = when (stmt) {
    is Stmt.Expression -> expressionLine(stmt.expression)
    is Stmt.Report -> expressionLine(stmt.expression)
}


fun scan(source: String): List<Token> = Scanner(source).scanTokens()

private fun expressionLine(expr: Expr): Int {
    var current = expr
    while (true) {
        when (current) {
            is Expr.Binary -> return current.operator.line
            is Expr.Unary -> return current.operator.line
            is Expr.Grouping -> current = current.expression
            is Expr.Program -> current = current.expressions.firstOrNull() ?: return 1
            is Expr.Literal -> return 1
        }
    }
}
