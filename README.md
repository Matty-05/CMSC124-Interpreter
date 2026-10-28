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
- A fresh clone needs a JDK 21 installation and nothing else; the Gradle wrapper fetches Gradle and the Kotlin compiler on first build. Run `chmod +x build.sh run gradlew` before building, then `./build.sh`, which runs `./gradlew installDist` and produces the launcher at `build/install/CMSC124-Interpreter/bin/CMSC124-Interpreter` that `./run` invokes.

## Running it

| Command | What it does |
|---|---|
| `./run <file>` | Runs a program: executes its statements in order. Only `report` statements print. |
| `./run --tokenize <file>` | Prints the token stream. |
| `./run --parse <file>` | Parses one statement per line and prints each tree. |
| `./run --eval <file>` | Evaluates each line and prints its value, as in Lab 3. |
| `./run` | Starts the REPL, which runs statements and prints the value of a bare expression. |

Exit codes: 0 when the file scans (and, with `--parse`, parses) cleanly, 65 when the scanner or parser rejects it, 70 when a successfully parsed program fails during evaluation, 64 for bad arguments or, with `--tokenize`, `--parse`, or `--eval`, a file that can't be read.

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
| `scan` | Prints a variable's name and value together |

Every word here is a word a user cannot use as a variable name, so the list is
kept deliberately small. Keywords are reserved in all contexts.

### Operators

| Operator | Category | Operands | Associativity | Precedence |
|---|---|---|---|---|
| `=` | assignment | binary | not parsed yet | not parsed yet |
| `or` | logical | binary | left | 1 |
| `and` | logical | binary | left | 2 |
| `==` | equality | binary | left | 3 |
| `!=` | equality | binary | left | 3 |
| `<` | comparison | binary | left | 4 |
| `<=` | comparison | binary | left | 4 |
| `>` | comparison | binary | left | 4 |
| `>=` | comparison | binary | left | 4 |
| `+` | arithmetic | binary | left | 5 |
| `-` | arithmetic | binary | left | 5 |
| `*` | arithmetic | binary | left | 6 |
| `/` | arithmetic | binary | left | 6 |
| `not` | logical | unary | right | 7 |
| `-` | arithmetic negation | unary | right | 7 |

Category and operand count are settled as of Lab 1. Precedence runs from 1
(loosest) to 7 (tightest) and is encoded in the grammar below: each level's rule
calls the next tighter one. Assignment is not parsed yet, since it belongs to
statements, which arrive in Lab 4. Unary operators are listed as
right-associative because they nest to the right: `- -1` is `-(-1)`.

`-` appears twice, as binary subtraction and as unary negation. The scanner
emits the same `MINUS` token for both. Distinguishing them is the parser's
responsibility.

### Literals

| Kind | Syntax | Produces |
|---|---|---|
| Number | `42`, `3.14`, `1_000_000` | A numeric value. Integers and decimals are both supported. A leading dot (`.5`) and a trailing dot (`3.`) are not valid numbers. Underscores may appear between digits and are stripped from the literal. A number may not end with one. |
| String | `"hello"` | A text value. Double quotes only. Escape sequences are supported. A string may not span lines. |
| Boolean | `active`, `inactive` | A truth value. |
| Nil | `void` | The absence of a value. |

Supported escape sequences: `\n` (newline), `\t` (tab), `\"` (double quote), and
`\\` (backslash). The lexeme keeps the backslash as written; the literal holds
the character it denotes.

A newline inside a string literal is a lexical error rather than part of the
string.

Underscores in numbers are separators for readability. They may appear anywhere
between digits, including in the fractional part, so `1_000_000` and `3_000.5`
are both valid. The lexeme keeps the underscores and the literal drops them.
The rule is exact: every separator needs a digit on both sides. A number that
ends with one, such as `1_000_`, is a lexical error, and so is one where a
separator divides nothing else — a run of them (`1__000`) or one sitting
against the decimal point (`1_.5`). A leading underscore makes the token an
identifier instead, since underscore is a valid identifier start character, so
`_1000` is a name rather than a number.

### Identifiers

- Start characters: an ASCII letter (`a`–`z`, `A`–`Z`) or an underscore (`_`)
- Continue characters: an ASCII letter, a digit (`0`–`9`), or an underscore
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
- Harness note: `tests/lab3/manifest.json` sets `comment_prefix` to `#`, since
  its inline `# expect:` annotations live in comments. The lab1 and lab2 folders
  use sidecar files, so they don't need it.

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

## Grammar

```
program    → statement* EOF ;      (one statement per line)
statement  → reportStmt
           | exprStmt ;
reportStmt → "report" expression ;
exprStmt   → expression ;
expression → or ;
or         → and ( "or" and )* ;
and        → equality ( "and" equality )* ;
equality   → comparison ( ( "!=" | "==" ) comparison )* ;
comparison → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
term       → factor ( ( "+" | "-" ) factor )* ;
factor     → unary ( ( "*" | "/" ) unary )* ;
unary      → ( "not" | "-" ) unary
           | primary ;
primary    → NUMBER | STRING | "active" | "inactive" | "void"
           | "(" expression ")" ;
```

`program` is the parser's entry point, `parse()`. It parses the whole file and
returns a single program node holding the file's statements in order. Each
statement must end where its line ends: since the scanner does not emit a token
for newlines, a token on a later line marks the end of the statement, the way a
`;` would in other languages.

A statement is either a `report` statement or an expression statement.
`report <expression>` evaluates the expression and prints its value. An
expression statement is an expression on its own line: it is evaluated, and in
a file its value is thrown away. Statements may only appear at the start of a
line, so `report` cannot be used inside an expression (`1 + report 2` is a
syntax error).

Every mode reads a file with this same grammar and differs only in what it
prints:

| Mode | Prints |
|---|---|
| `./run <file>` | only what `report` statements print |
| `./run --eval <file>` | the value of each expression statement, and whatever `report` prints |
| `./run --parse <file>` | the tree of each statement |
| REPL | what `report` prints, and the value of a bare expression |

A Lab 2 or Lab 3 file holds only expressions, so `--parse` and `--eval` print
exactly what they did before.

Each rule matches one function in `Parser.kt` with the same name. A rule only
calls the rule for the next tighter level, which is what makes the grammar
unambiguous: `1 + 2 * 3` can only come out as `1 + (2 * 3)`. The `( ... )*`
loops in `term` and `factor` build the tree left to right, so `1 - 2 - 3` is
`(1 - 2) - 3`. `unary` calls itself, so prefix operators stack (`not not
active`, `- -1`).

`not` sits at the same level as unary `-`, the tightest operator level. It applies only to
the operand directly after it, so `not 1 + 2` is `(not 1) + 2`.

### Splitting rule

A file holds one statement per line. The whole file is scanned, and the parser
reads all of its tokens, ending each statement where its line ends.

- Blank lines and comment-only lines hold no tokens, so they are skipped.
- An expression cannot continue onto the next line, even inside parentheses.
  `(1 +` on one line and `2)` on the next is two broken lines, not one
  expression.
- Two expressions on one line (`1 2`), or a `report` after an expression on
  the same line, is an error: the parser expects the line to end after the
  first statement.
- A file with no statements at all, including one with only comments, is
  rejected.
- After a syntax error the parser skips the rest of that line and starts again
  on the next one (`synchronize()`), so each bad line gets its own diagnostic.

## Parse output format

```
(+ 1.0 (* 2.0 3.0))
```

One line of output per statement, in file order. Every operator node prints in
prefix form, wrapped in parentheses.

- Expression statements print as: the expression's tree, as in Lab 2
- `report` statements print as: `(report expression)`, e.g.
  `report 1 + 2` prints `(report (+ 1.0 2.0))`

- Binary operators print as: `(op left right)`, e.g. `(- (- 1.0 2.0) 3.0)`
- Unary operators print as: `(op operand)`, e.g. `(not true)`, `(- 1.0)`
- Groupings print as: `(group expression)`, e.g. `(group (+ 2.0 3.0))`
- Numbers print as: a decimal, always with a fractional part: `4` prints as
  `4.0`. Digit separators are gone, since the literal is printed rather than the
  lexeme.
- Strings print as: their contents without quotes: `"hello"` prints as `hello`
- Booleans print as: `true` and `false`
- `void` prints as: `void`

## Semantics

### Values and types

Nier is dynamically typed. A value's type is only known when the program runs,
so the evaluator stores every value in Kotlin's `Any?`, the type every Kotlin
value fits into, and checks what it holds when an operator needs to know.

| Nier value | Example | Stored as |
|---|---|---|
| Number | `42`, `3.5` | `Double`. Integers and decimals are one type. |
| String | `"hi"` | `String` |
| Boolean | `active`, `inactive` | `Boolean` (`true`, `false`) |
| Nothing | `void` | `null` |

The scanner already builds number literals as `Double` and the parser stores
`active`, `inactive`, and `void` as `true`, `false`, and `null`, so values reach
the evaluator in this form without conversion.

### Value printing

- Numbers: without a trailing `.0`, so `2 + 3` prints `5` and `5 / 2` prints
  `2.5`. Very large or very small numbers keep Kotlin's scientific notation,
  such as `1.0E7`.
- Booleans: `active` and `inactive`, the way they are written in Nier.
- Nil: `void`.
- Strings: without quotes, so `"hi" + " there"` prints `hi there`.

This is the format of `report`, `--eval`, and the REPL. `--parse` keeps its own
format from Lab 2, where numbers always show a decimal point (`4.0`) and
booleans print as `true` and `false`.

### Output

A program's only output is what its `report` statements print, one value per
line, in the order they run. A bare expression on its own line is evaluated but
prints nothing when a file is run, so a program shows only what it chooses to
report. `--eval` and the REPL still print each bare expression's value, since
showing values is their purpose. Diagnostics always go to stderr.

### Truthiness

`inactive` and `void` are false. Every other value is true, including `0` and
the empty string `""`. One function, `isTruthy`, applies this rule everywhere a
value is used as a condition: `not`, `and`, and `or` now, and `if` and `while`
in later labs.

### Operator semantics

- Arithmetic: `-`, `*`, and `/` take two numbers, and unary `-` takes one.
  Anything else is a runtime error.
- `+` on strings: two numbers add and two strings join (`"a" + "b"` is `ab`).
  Any other pair, such as `"a" + 1`, is a runtime error rather than a silent
  conversion, since a mixed `+` is far more likely a mistake than intent.
- Mixed types: never converted. An operator that needs numbers rejects anything
  else with a runtime error.
- Comparison: `<`, `<=`, `>`, and `>=` take two numbers and produce `active` or
  `inactive`.
- Equality across types: `==` and `!=` accept any two values. Values of
  different types are never equal, so `1 == "1"` is `inactive` and not an
  error. `void == void` is `active`.
- Logical: `not` works on any value by its truthiness. `and` and `or` produce
  `active` or `inactive` and short-circuit: `and` skips its right side when the
  left is false, and `or` skips it when the left is true.
- Division by zero: a runtime error. Nier is for testing protocols safely, so a
  division that has no real answer stops the program instead of quietly
  producing `Infinity`.
- Evaluation order: the left operand is always evaluated before the right.

Runtime errors print on stderr as `[line N] Runtime error: <message>` and exit
with code 70. The messages are:

| Cause | Message |
|---|---|
| `-`, `*`, `/`, or a comparison on a non-number | `Operands must be numbers.` |
| Unary `-` on a non-number | `Operand must be a number.` |
| `+` on anything but two numbers or two strings | `Operands must be two numbers or two strings.` |
| Dividing by zero | `Division by zero.` |

### Scope and bindings

- Redeclaration in the same scope: [allowed or an error]
- Uninitialized variable holds: [value]
- Shadowing: [behavior]
- Undefined name: [static error with exit 65, or runtime error with exit 70]

### Control flow and functions

- Logical operators return: `active` or `inactive`, never the operand itself
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
[line 1] Error: Unexpected character '!'. Use 'not' for negation.
[line 3] Error: Invalid escape sequence '\q'.
[line 5] Error: A number can't end with a separator.
[line 7] Error: A digit separator has to sit between two digits.
```

Parser diagnostics name the token where parsing failed, or `end` when the line
ran out:

```
[line 1] Error at end: Expect expression.
[line 1] Error at '2': Expect end of statement.
[line 1] Error at end: Expect ')' after expression.
[line 1] Error: Expect statement.
```

`Expect end of statement.` means something was left on the line after a
complete statement, as in `1 2` or `1 report 2`. The last form is for a file
with no statements at all.

Error handling uses exceptions instead of a shared global error flag.
The scanner collects diagnostics on its own instance and throws `Scanner.ScanError`
with the collected tokens and messages when scanning finishes. The parser throws
`Parser.ParseError` with a diagnostic message. The command-line functions catch
these exceptions and print their messages to stderr. The parser collects the
error from each bad line and keeps going, then throws them together, so every
bad line is still reported.

Diagnostics are written to stderr. The scanner keeps going after an error
rather than stopping at the first one, so a file with several problems reports
all of them in one run. With `--tokenize`, the tokens print to stdout either way: a clean file
exits 0, and a file with an error still prints the tokens the scanner managed
to produce and then exits 65. The exit code is what marks a file as rejected.
Showing the tokens lets you see what the scanner made of the rest of the file.
A character or string that caused an error produces no token, except an
invalid escape, where the string is still emitted without the bad escape.

`--parse` is stricter. A lexical error rejects the file before any parsing
happens. Otherwise the parser reads the whole file, and the parsed trees are
printed only if no line failed, so a rejected file prints nothing to stdout.

The REPL works one line at a time and prints each line's value. A line with a
lexical error prints only that error, without trying to parse it, a line with a
syntax error prints only the parser's error, and a line that fails while
evaluating prints its runtime error. Either way the prompt comes back, since a
bad line must not end the session.

| Failure | Exit code |
|---|---|
| Clean scan | 0 |
| Unterminated string literal | 65 |
| Newline inside a string literal | 65 |
| Invalid escape sequence | 65 |
| Digit separator not flanked by digits | 65 |
| Character that cannot begin any lexeme | 65 |
| Syntax error on any line (`--parse`) | 65 |
| File with no expressions (`--parse`) | 65 |
| Invalid command-line arguments | 64 |
| Source file missing or unreadable (`--tokenize`, `--parse`, `--eval`) | 64 |
| Runtime error (`--eval`) | 70 |

A file that scans and parses but then fails while evaluating exits 70. Output
printed before the failing line stays on stdout, since evaluation runs top to
bottom and stops at the first runtime error.

## Testing conventions

| Folder | Activity | Mode | Flag |
|---|---|---|---|
| tests/lab1 | Scanner | sidecar | `--tokenize` |
| tests/lab2 | Parser | sidecar | `--parse` |
| tests/lab3 | Evaluator | inline | `--eval` |
| tests/lab4 | Context | inline | none |
| tests/lab5 | Functions | inline | none |

Lab 1 tests hold one case per file, grouped by token category, so each file
name says what it checks (`tests/lab1/numbers/leading-dot.mata`,
`tests/lab1/errors/separator-trailing.mata`). The harness finds them
recursively, and the one `manifest.json` in `tests/lab1` covers every subfolder.

```
numbers/       integers, zero, decimals, both dot rules (3. and .5 are not
               numbers), 42abc splitting, 42+1 with no spaces, separators,
               and 1._5 scanning as 1 . _5 since _5 is a name
strings/       a basic string, the empty string, each escape on its own, and
               a # inside a string staying part of the string
identifiers/   a plain name, _1000 and _ as names, an underscore and a digit
               inside a name, a keyword prefix (unitary), case sensitivity
               (count vs Count), and Unit staying a name
keywords/      one file per keyword, all fifteen
operators/     one file per single- and two-character operator, maximal
               munch with no spaces (a<=b), and // as two SLASH tokens
comments/      full-line, trailing, end of file with no newline, and a / or
               a quote inside a comment
lines/         empty file, blank lines, a comment line, tabs, CRLF line
               endings
programs/      the sample program below and a control-flow program
errors/        every rejection: unterminated string, string across lines,
               invalid escape, trailing / doubled / before-dot separators,
               a bare !, an unexpected character, a backslash right before
               end of file, and two errors in one file (the scanner keeps
               going and reports both)
```

Every file in `errors/` pairs a `.expected` holding the tokens the scanner still
produced with a `.exit` holding 65. The exact diagnostic goes in a
`# expect error:` comment at the top of the `.mata` file. Sidecar mode does not
check stderr, so these lines document the message for a reader and are not
enforced. They use the harness's inline syntax, so they carry over if the
folder ever switches to inline mode. A string that crosses a line reports two
errors, because the closing quote on the next line opens a new unterminated
string.

Lab 2 tests follow the same layout, one case per file, run with `--parse`:

```
literals/      one file per literal: number, string, active, inactive, void
unary/         chained unary operators (- -1, not not active)
grouping/      a group that changes the tree ((2 + 3) * 4) and a redundant
               one (((((1)))))
precedence/    * before + (1 + 2 * 3), left associativity (1 - 2 - 3), and
               one expression that uses every level at once
comparison/    each comparison operator, comparison below + and *, and left
               associativity (1 < 2 < 3)
equality/      == and !=, equality below comparison (1 < 2 == 3 > 4), left
               associativity, and equality above and
logical/       and, or, and and binding tighter than or
lines/         two expressions on two lines, and an expression that tries to
               continue onto the next line (rejected)
errors/        an unclosed parenthesis, a missing right operand, a bad line
               after a good one, and two bad lines that are both reported
empty/         a file with only a comment, which is rejected
```

Every rejected lab2 file has an empty `.expected`, since `--parse` prints
nothing for a rejected file, and a `.exit` holding 65.

Lab 3 tests use inline mode, as the Lab 3 spec asks: each file holds one case,
with its expected result written in a `#` comment on the same line, so no
sidecar files are needed. The answers are worked out by hand before running,
since the values are now computed and can be checked against arithmetic.

```
3 + 4 * 2  # expect: 11
"a" - 1  # expect runtime error: Operands must be numbers.
(1 + 2  # expect error: Expect ')' after expression.
```

Each of those lines is its own file. `expect:` checks stdout. `expect runtime
error:` checks the message on stderr and exit 70, and `expect error:` does the
same for a static error with exit 65. Error annotations name the message only,
without the `[line N]` prefix, and each error file holds a single error, since
evaluation stops at the first one. One file, `errors/output-before-error`, also
checks that a value printed before the failing line stays on stdout.

```
literals/      one file per value type
arithmetic/    precedence, grouping, left associativity, division
strings/       + joining two strings
not/           not on every kind of value, which tests the truthiness rule
logical/       and, or, and short-circuiting
comparison/    each comparison, including where < and <= differ
equality/      equal and unequal values, mixed types, and void
precedence/    one expression that uses every level
errors/        one runtime or static error per file, including dividing by
               negative zero
```

Lab 4 tests are inline too, but run with plain `./run` and no flag, since
running a program is what they test. They check what a program prints, which
is only what its `report` statements print. A file that should print nothing
says so with `# expect nothing`, so a silent file is never mistaken for an
unannotated one.

```
report 1 + 2  # expect: 3
1 + 2  # expect nothing
```

```
report/                one file per kind of value, and several reports in
                       order
expression-statement/  a bare expression prints nothing, alone and before a
                       report
errors/                a runtime error, output printed before an error, and
                       a report with no expression
```

`tests/lab0/hello.mata` is now a real Nier program: four `report` lines that
print the team banner its `.expected` file holds.

Run locally with:

```bash
curl -sSL https://raw.githubusercontent.com/WhiteLicorice/cmsc-124-harness/v1.1/run_tests.py -o run_tests.py
./build.sh
python3 run_tests.py tests/lab0
python3 run_tests.py tests/lab1
python3 run_tests.py tests/lab2
python3 run_tests.py tests/lab3
python3 run_tests.py tests/lab4
```

## Sample code

```
unit count = 1_000_000
scan count

unit greeting = "Unit ready"
report greeting
```

Output:

```
Token(type=UNIT, lexeme=unit, literal=null, line=1)
Token(type=IDENTIFIER, lexeme=count, literal=null, line=1)
Token(type=EQUAL, lexeme==, literal=null, line=1)
Token(type=NUMBER, lexeme=1_000_000, literal=1000000.0, line=1)
Token(type=SCAN, lexeme=scan, literal=null, line=2)
Token(type=IDENTIFIER, lexeme=count, literal=null, line=2)
Token(type=UNIT, lexeme=unit, literal=null, line=4)
Token(type=IDENTIFIER, lexeme=greeting, literal=null, line=4)
Token(type=EQUAL, lexeme==, literal=null, line=4)
Token(type=STRING, lexeme="Unit ready", literal=Unit ready, line=4)
Token(type=REPORT, lexeme=report, literal=null, line=5)
Token(type=IDENTIFIER, lexeme=greeting, literal=null, line=5)
Token(type=EOF, lexeme=, literal=null, line=5)
```

## Design rationale

- Braces over indentation
If whitespaces were significant, then our scanner would need to track an indentation stack and would emit synthetic INDENT/DEDENT tokens whenever the depth changes and that's an entirely separate piece of bookkeping our scanner doesn't need. With braces, whitespaces can now be freely discarded which keeps our scanning loop much simpler.

- '#' over '//'
// were originally used for comments, but since / is already our division operator, that meant our scanner needed a lookahead check inside the / case. Switching to # for comments removes the ambiguity entirely since # isn't used for anything else in Nier.

- not rather than !
Since our and and or are already spelled as words rather than symbols, we made the negation a word as well to maintain consistency. If we allow both not and ! to mean the same thing then it would just be two spellings for one operator, adding confusion without adding capabilities. We fully commit to word-based logical operators so that ! in Nier source code is treated as a lexical error.

- No multi-line string
We decided that string cannot span multiple lines since according to lab manual, this is a common source of off-by-one line-counting bugs. So, by rejecting newlines inside strings entirely, we remove that entire category of bug.

- No leading or trailing dot number
We decided not to allow leading or trailing dot so it can act as its own independent DOT token in other contexts. Our number() function only treats . as part of the number when a digit immediately follows it.

- 'scan' for state inspection
Nier's stated purpose is testing combat protocols safely, and safe testing means being able to see what a variable holds without guessing. 'report' only shows a value, not which variable produced it, which gets confusing once several units are being inspected at once. 'scan' prints the name and value together, so the output is self-labeling. Most languages bolt this on well after launch, like Rust's dbg! macro or Python's f"{x=}", so we built it in from Lab 1 since inspection is central to what Nier is for.

- Underscores as digit separators
Long numbers are hard to read at a glance, since 1000000 and 10000000 look almost the same. Java, Kotlin, Python, and Rust all added separators for this reason so we did the same. We require every separator to have a digit on both sides, so 1_000_, 1__000, and 1_.5 are all errors. A separator that divides nothing is almost certainly a typo, and accepting it silently would hide the mistake. Checking both sides rather than just the end also keeps the scanner honest against its own documentation, since a rule written as "between digits" should be the rule the code enforces. We left leading underscores alone since _1000 already scans as an identifier, and overriding that would mean special casing a rule we already have.

- One expression per line
Nier already ends statements with a newline instead of a semicolon, so we made `--parse` follow the same rule. The parser reads the whole file's tokens and uses the line numbers the scanner already records to tell where each expression ends, so every diagnostic keeps its real line number. Not letting an expression continue past the end of a line means a missing operand is caught on the line where it happened, instead of silently pulling in the next line.

- `not` at the unary level
`not` and unary `-` are both prefix operators that take one operand, so they share one rule. This keeps the grammar to a single unary level. The cost is that `not` binds tighter than arithmetic, which we note under known limitations.

## Known limitations

- Block comments are not supported, so every commented line needs its own `#`.
- Diagnostics report a line number but no column, so a line with two problems
  points at the line twice.
- `scan` has its own token type and is recognized by the scanner, but it has no
  runtime behavior until Lab 3.
- Digit separators are stripped when the literal is built, so `1_000_000` and
  `1000000` differ only in their lexeme.
- Identifier start characters are documented as ASCII letters, but the scanner
  uses Kotlin's `isLetter()`, which accepts any Unicode letter. `café` is
  currently a valid identifier. The scanner is more permissive than this
  document says.
- Assignment (`=`) and identifiers are scanned but not parsed yet; they arrive
  with statements in Lab 4. A line that uses one is rejected, e.g. `x + 1`
  reports `Error at 'x': Expect expression.`
- An expression cannot span lines, even inside parentheses.
- `not` binds tighter than every binary operator, so `not 1 + 2` parses as
  `(+ (not 1.0) 2.0)` and `not 1 == 2` as `(== (not 1.0) 2.0)`.
- Strings print without quotes, so the string `"1.0"` and the number `1` both
  print as `1.0`.
- Numbers print with Kotlin's `Double.toString()`, which switches to scientific
  notation at ten million and above or below 0.001: `10_000_000` prints as
  `1.0E7` and `0.0001` as `1.0E-4`.
- A lexical error stops `--parse` before parsing, so a file with both a lexical
  and a syntax error reports only the lexical one.
- The error for an empty or comment-only file always says line 1, even when the
  comment is further down.
- Numbers are binary floating point, so `0.1 + 0.2` evaluates to
  `0.30000000000000004` rather than `0.3`.
- Negative zero exists and prints as `-0`, since `-0` is a different `Double`
  from `0`. It still counts as zero for division, so `1 / -0` is a division by
  zero error.
- `--eval` prints numbers without a trailing `.0` but keeps scientific notation,
  so `10_000_000 * 1` prints `1.0E7`.

## Changelog

| Activity | What changed in the language |
|---|---|
| Lab 1 | Nier defined: 15 keywords, brace-delimited blocks, newline statement termination, `#` line comments, double-quoted strings with escapes and no line spanning, integer and decimal numbers with no leading or trailing dot, letter-or-underscore identifiers. Logical negation is the `not` keyword. A bare `!` is a lexical error. Token output format frozen. Added `scan` for name-and-value state inspection, distinct from `report`'s value-only output. Added underscore digit separators in numeric literals, valid only between two digits. |
| Lab 2 | Expression grammar added, loosest to tightest: `or`, `and`, `== !=`, `< <= > >=`, `+ -`, `* /`, then prefix `not` and `-`, then literals and parenthesized groups. Every binary operator is left-associative. `--parse` prints one prefix-form tree per line. One expression per line, and an expression cannot span lines. A file with no expressions is rejected. The parser reads the whole file and recovers at the next line after an error, so every bad line is reported. The REPL prints each line's parsed tree. Assignment and identifiers are not parsed yet. |
| Lab 3 | Expressions now evaluate with `--eval`, and the REPL prints values. Values are numbers, strings, `active`/`inactive`, and `void`. `inactive` and `void` are false and everything else is true. `+` adds two numbers or joins two strings; arithmetic and comparison need numbers; equality works on any two values, and different types are never equal. `and` and `or` short-circuit. Division by zero is a runtime error. Runtime errors exit 70. Numbers print without a trailing `.0` and booleans print as `active` and `inactive`. |
| Lab 4 | Programs run with plain `./run <file>`. A file is a list of statements, one per line: `report <expression>` prints a value, and an expression on its own line is evaluated without printing. `--parse` prints `report` statements as `(report ...)`, and `--eval` and the REPL still print bare expression values. |
