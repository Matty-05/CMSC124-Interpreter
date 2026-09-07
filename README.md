# Nier

## Creators

- Ralph Ryan T. Escabarte (RalphREE)
- John Matthew N. Fallarme (Matty-05)

## Overview

Nier is a programming language designed for YoRHa androids and other operators who wish to test their combat protocols without the risk of losing a unit in the field should their routines go wrong. The language acts like a virtual Bunker terminal, producing an output that shows what would happen should the protocol they wrote actually be transmitted to a live android. It uses terms and concepts an operator would be familiar with in their day-to-day work. Values are stored in units (variables), results come back through report (print), control returns through transmit (return), and reusable behavior is packaged into protocols (functions).

## Host language and build

- Host language: Kotlin 2.0.20, targeting the JVM (Java 21)
- Version metadata: `build.gradle.kts` (pins the Kotlin plugin version); Gradle 8.10.2 is pinned in `gradle/wrapper/gradle-wrapper.properties`
- Build: `./build.sh`
- A fresh clone needs a JDK 21 installation and nothing else; the Gradle wrapper fetches Gradle and the Kotlin compiler on first build. Run `chmod +x build.sh run gradlew` before building, then `./build.sh`, which runs `./gradlew installDist` and produces the launcher at `build/install/cmsc124/bin/cmsc124` that `./run` invokes.

## Running it

| Command | What it does |
|---|---|
| `./run <file>` | Executes a program. Available from Lab 4. |
| `./run --tokenize <file>` | Prints the token stream. |
| `./run --parse <file>` | Prints the parsed tree. |
| `./run --eval <file>` | Evaluates each expression and prints its value. |
| `./run` | Starts the REPL. |

Exit codes: 0 when the file scans cleanly, 65 when the scanner rejects it with a lexical error, 70 when a successfully parsed program fails during evaluation.

## File extension

`.mata` — matches the `ext` field in every `tests/lab*/manifest.json`.

## Lexical structure

### Keywords

| Keyword | Purpose |
|---|---|
| `unit` | Declares a variable |
| `report` | Prints a value to standard output |
| `protocol` | Declares a function |
| `transmit` | Returns a value from a function |
| `active` | Boolean true |
| `inactive` | Boolean false |
| `void` | The absence of a value |
| `if` | Conditional branch |
| `else` | Alternative branch of a conditional |
| `while` | Loop while a condition holds |
| `for` | Counted loop |
| `and` | Logical conjunction |
| `or` | Logical disjunction |
| `not` | Logical negation |

Every word here is a word a user cannot use as a variable name, so the list is
kept deliberately small. Keywords are reserved in all contexts.

### Operators

| Operator | Category | Operands | Associativity | Precedence |
|---|---|---|---|---|
| `=` | assignment | binary | — | — |
| `or` | logical | binary | — | — |
| `and` | logical | binary | — | — |
| `==` | equality | binary | — | — |
| `!=` | equality | binary | — | — |
| `<` | comparison | binary | — | — |
| `<=` | comparison | binary | — | — |
| `>` | comparison | binary | — | — |
| `>=` | comparison | binary | — | — |
| `+` | arithmetic | binary | — | — |
| `-` | arithmetic | binary | — | — |
| `*` | arithmetic | binary | — | — |
| `/` | arithmetic | binary | — | — |
| `not` | logical | unary | — | — |
| `-` | arithmetic negation | unary | — | — |

Category and operand count are settled as of Lab 1. Associativity (left, right,
or none) and precedence (1 = loosest) are filled in for Lab 2, when the grammar
has to encode them in how its rules delegate to each other.

`-` appears twice, as binary subtraction and as unary negation. The scanner
emits the same `MINUS` token for both. Distinguishing them is the parser's
responsibility.

### Literals

| Kind | Syntax | Produces |
|---|---|---|
| Number | `42`, `3.14` | A numeric value. Integers and decimals are both supported. A leading dot (`.5`) and a trailing dot (`3.`) are not valid numbers. |
| String | `"hello"` | A text value. Double quotes only. Escape sequences are supported. A string may not span lines. |
| Boolean | `active`, `inactive` | A truth value. |
| Nil | `void` | The absence of a value. |

Supported escape sequences: `\n` (newline), `\t` (tab), `\"` (double quote), and
`\\` (backslash). The lexeme keeps the backslash as written; the literal holds
the character it denotes.

A newline inside a string literal is a lexical error rather than part of the
string.

### Identifiers

- Start characters: an ASCII letter (`a`–`z`, `A`–`Z`)
- Continue characters: an ASCII letter or a digit (`0`–`9`)
- Case-sensitive: yes. `count` and `Count` are different names.
- An identifier may not be one of the reserved keywords listed above. Words that
  merely begin with a keyword are ordinary identifiers, so `unitary` is a valid
  identifier and not `unit` followed by `ary`.

### Comments

- Line comments: `#`, which discards the rest of the line
- Block comments: not supported
- Nesting: not applicable
- A comment may appear at the end of a line of code, as in `unit x = 4  # note`
- Comments are discarded by the scanner and never emitted as tokens
- The harness note: `comment_prefix` in `tests/lab*/manifest.json` is set to `#`.

Block comments were cut to keep the scanner's comment handling to a single
case. Nested block comments in particular require tracking a depth counter and
are a common source of line-counting bugs.

## Whitespace and termination

- Whitespace significant: no. Spaces, tabs, and carriage returns are discarded
  by the scanner. Newlines are discarded as tokens but counted, so that every
  token carries an honest line number.
- Statement terminator: a newline. There are no semicolons.
- Block delimiters: braces, `{` and `}`.
- Grouping delimiters: parentheses, `(` and `)`.

Nier is bracketed rather than indentation-sensitive, so the scanner never needs
to emit synthetic indent or dedent tokens and never has to track a stack of
indentation levels.

## Token output format

```
Token(type=NUMBER, lexeme=4, literal=4.0, line=1)
```

One token per line. The fields are:

- `type` — the token type, drawn from the list in the lexical structure section
- `lexeme` — the exact source text the token was scanned from
- `literal` — the value the lexeme denotes, or `null` for tokens that carry no
  value, such as keywords and operators
- `line` — the 1-based line number the lexeme began on

Frozen as of Lab 1. Any change is recorded in the changelog, since every
committed `.expected` file is compared against this format byte for byte.

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
