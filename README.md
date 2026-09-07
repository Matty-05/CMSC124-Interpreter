# Nier

## Creators

- Ralph Ryan T. Escabarte (RalphREE)
- John Matthew N. Fallarme (Matty-05)

## Overview

To be implemented.

## Host language and build

- Host language: Kotlin 2.0.20, targeting the JVM (Java 21)
- Version metadata: `build.gradle.kts` (pins the Kotlin plugin version); Gradle 8.10.2 is pinned in `gradle/wrapper/gradle-wrapper.properties`
- Build: `./build.sh`
- A fresh clone needs a JDK 21 installation and nothing else; the Gradle wrapper fetches Gradle and the Kotlin compiler on first build. Run `chmod +x build.sh run gradlew` before building, then `./build.sh`, which runs `./gradlew installDist` and produces the launcher at `build/install/CMSC124-Interpreter/bin/CMSC124-Interpreter` that `./run` invokes.

## Running it

| Command | What it does |
|---|---|
| `./run <file>` | Executes a program. Available from Lab 4. |
| `./run --tokenize <file>` | Prints the token stream. |
| `./run --parse <file>` | Prints the parsed tree. |
| `./run --eval <file>` | Evaluates each expression and prints its value. |
| `./run` | Starts the REPL. |

Exit codes: 0 when the program runs to completion without error, 65 when the scanner or parser rejects the input with a lexical or syntax error, 70 when evaluation of a successfully parsed program fails with a runtime error.

## File extension

`.mata` — matches the `ext` field in every `tests/lab*/manifest.json`.

## Lexical structure

### Keywords

| Keyword | Purpose |
|---|---|
| [word] | [what it does] |

### Operators

| Operator | Category | Operands | Associativity | Precedence |
|---|---|---|---|---|
| [op] | [arithmetic, comparison, logical, assignment, other] | [unary or binary] | [left, right, none] | [1 = loosest] |

### Literals

| Kind | Syntax | Produces |
|---|---|---|
| [number] | [e.g. 42, 3.14] | [what runtime value] |
| [string] | [e.g. "hello", escapes supported] | [what runtime value] |
| [boolean] | [true, false] | [what runtime value] |
| [nil] | [spelling] | [what runtime value] |

### Identifiers

- Start characters: [which]
- Continue characters: [which]
- Case-sensitive: [yes or no]
- [Reserved patterns, length limits, or other restrictions.]

### Comments

- Line comments: [token]
- Block comments: [tokens, or "not supported"]
- Nesting: [supported or not]
- [Harness note: comment_prefix in tests/lab*/manifest.json is set to the
  token above.]

## Whitespace and termination

- Whitespace significant: [yes or no, and where]
- Statement terminator: [e.g. semicolon, newline, none]
- Block delimiters: [e.g. braces, indentation]
- Grouping delimiters: [e.g. parentheses]

## Token output format

```
[one line of real --tokenize output]
```

[What each field means. Frozen as of Lab 1; changes are recorded in the
changelog.]

## Grammar

```
[Your complete context-free grammar, current as of the latest activity.
Unambiguous, with precedence and associativity encoded in rule structure.]
```

## Parse output format

```
[one line of real --parse output, e.g. (+ 1.0 (* 2.0 3.0))]
```

- Groupings print as: [form]
- Numbers print as: [form]

## Semantics

### Values and types

[What runtime values exist, and how they are represented in the host
language.]

### Value printing

- Numbers: [e.g. 5 rather than 5.0]
- Nil: [spelling]
- Strings: [with or without quotes]

### Truthiness

[The complete rule. Which values are false in a condition; everything else is
true.]

### Operator semantics

- Arithmetic: [accepted operand types]
- `+` on strings: [concatenation, error, or coercion]
- Mixed types: [what happens]
- Comparison: [accepted operand types]
- Equality across types: [false, or an error]
- Division by zero: [value produced, or runtime error]

### Scope and bindings

- Redeclaration in the same scope: [allowed or an error]
- Uninitialized variable holds: [value]
- Shadowing: [behavior]
- Undefined name: [static error with exit 65, or runtime error with exit 70]

### Control flow and functions

- Logical operators return: [booleans, or the operand]
- Dangling else binds to: [which if]
- Closure capture of a loop variable: [per iteration, or shared]
- Function with no return statement produces: [value]
- Arity mismatch: [message and exit code]

## Native functions

| Name | Arguments | Returns | Notes |
|---|---|---|---|
| [name] | [count and types] | [type] | [caveats] |

## Errors and diagnostics

Message format:

```
[one real static error]
[one real runtime error]
```

| Failure | Exit code |
|---|---|
| [lexical error] | 65 |
| [syntax error] | 65 |
| [runtime error] | 70 |

## Testing conventions

| Folder | Activity | Mode | Flag |
|---|---|---|---|
| tests/lab1 | Scanner | sidecar | `--tokenize` |
| tests/lab2 | Parser | sidecar | `--parse` |
| tests/lab3 | Evaluator | inline | `--eval` |
| tests/lab4 | Context | inline | none |
| tests/lab5 | Functions | inline | none |

```
[specific tests]...
```

Run locally with:

```bash
curl -sSL https://raw.githubusercontent.com/WhiteLicorice/cmsc-124-harness/v1.1/run_tests.py -o run_tests.py
./build.sh
python3 run_tests.py tests/lab1
```

## Sample code

```
[a short program]
```

Output:

```
[its output]
```

## Design rationale

[Why the language is the way it is. Cover the choices that surprised you, the
features you cut, and the decisions you reversed. Specific reasons, not
approval of your own work.]

## Known limitations

- [What doesn't work, what is unimplemented, where behavior is worse than you
  would like.]

## Changelog

| Activity | What changed in the language |
|---|---|
| Lab 1 | [entry] |
