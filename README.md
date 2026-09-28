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
| `./run <file>` | Executes a program. Available from Lab 4; until then it prints the team banner that `tests/lab0` checks. |
| `./run --tokenize <file>` | Prints the token stream. |
| `./run --parse <file>` | Parses one expression per line and prints each tree. |
| `./run --eval <file>` | Evaluates each expression and prints its value. Available from Lab 3. |
| `./run` | Starts the REPL. |

Exit codes: 0 when the file scans (and, with `--parse`, parses) cleanly, 65 when the scanner or parser rejects it, 70 when a successfully parsed program fails during evaluation.

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
| `or` | logical | binary | not parsed yet | not parsed yet |
| `and` | logical | binary | not parsed yet | not parsed yet |
| `==` | equality | binary | not parsed yet | not parsed yet |
| `!=` | equality | binary | not parsed yet | not parsed yet |
| `<` | comparison | binary | not parsed yet | not parsed yet |
| `<=` | comparison | binary | not parsed yet | not parsed yet |
| `>` | comparison | binary | not parsed yet | not parsed yet |
| `>=` | comparison | binary | not parsed yet | not parsed yet |
| `+` | arithmetic | binary | left | 1 |
| `-` | arithmetic | binary | left | 1 |
| `*` | arithmetic | binary | left | 2 |
| `/` | arithmetic | binary | left | 2 |
| `not` | logical | unary | right | 3 |
| `-` | arithmetic negation | unary | right | 3 |

Category and operand count are settled as of Lab 1. Precedence runs from 1
(loosest) to 3 (tightest) and is encoded in the grammar below: each level's rule
calls the next tighter one. The operators marked "not parsed yet" are scanned
but have no grammar rule, so a line that uses one is rejected. Unary operators
are listed as right-associative because they nest to the right: `- -1` is
`-(-1)`.

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
- Harness note: `comment_prefix` in `tests/lab*/manifest.json` is set to `#`.

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
expression → term ;
term       → factor ( ( "+" | "-" ) factor )* ;
factor     → unary ( ( "*" | "/" ) unary )* ;
unary      → ( "not" | "-" ) unary
           | primary ;
primary    → NUMBER | STRING | "active" | "inactive" | "void"
           | "(" expression ")" ;
```

Each rule matches one function in `Parser.kt` with the same name. A rule only
calls the rule for the next tighter level, which is what makes the grammar
unambiguous: `1 + 2 * 3` can only come out as `1 + (2 * 3)`. The `( ... )*`
loops in `term` and `factor` build the tree left to right, so `1 - 2 - 3` is
`(1 - 2) - 3`. `unary` calls itself, so prefix operators stack (`not not
active`, `- -1`).

`not` sits at the same level as unary `-`, above `*` and `/`. It applies only to
the operand directly after it, so `not 1 + 2` is `(not 1) + 2`.

### Splitting rule

A file holds one expression per line. The whole file is scanned first, then the
tokens are grouped by the line they came from and each group is parsed as its
own expression.

- Blank lines and comment-only lines hold no tokens, so they are skipped.
- An expression cannot continue onto the next line, even inside parentheses.
  `(1 +` on one line and `2)` on the next is two broken lines, not one
  expression.
- Two expressions on one line (`1 2`) is an error: the parser expects the line
  to end after the first one.
- A file with no expressions at all, including one with only comments, is
  rejected.
- Every line is parsed even after an earlier line fails, so each bad line gets
  its own diagnostic.

## Parse output format

```
(+ 1.0 (* 2.0 3.0))
```

One line of output per expression, in file order. Every operator node prints in
prefix form, wrapped in parentheses.

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
[line 1] Error: Unexpected character '!'. Use 'not' for negation.
[line 3] Error: Invalid escape sequence '\q'.
[line 5] Error: A number can't end with a separator.
[line 7] Error: A digit separator has to sit between two digits.
```

Parser diagnostics name the token where parsing failed, or `end` when the line
ran out:

```
[line 1] Error at end: Expect expression.
[line 1] Error at '2': Expect end of expression.
[line 1] Error at end: Expect ')' after expression.
[line 1] Error: Expect expression.
```

The last form is for a file with no expressions at all.

Diagnostics are written to stderr. The scanner keeps going after an error
rather than stopping at the first one, so a file with several problems reports
all of them in one run. With `--tokenize`, the tokens print to stdout either way: a clean file
exits 0, and a file with an error still prints the tokens the scanner managed
to produce and then exits 65. The exit code is what marks a file as rejected.
Showing the tokens lets you see what the scanner made of the rest of the file.
A character or string that caused an error produces no token, except an
invalid escape, where the string is still emitted without the bad escape.

`--parse` is stricter. A lexical error rejects the file before any parsing
happens. Otherwise every line is parsed, and the parsed trees are printed only
if no line failed, so a rejected file prints nothing to stdout.

The REPL works one line at a time. It prints the diagnostics first, then the
line's tokens, then the parsed tree if the line had no errors. Either way the
prompt comes back, since a bad line must not end the session.

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

Exit 70 is reserved for runtime errors and is unused until Lab 3.

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
literals/      one file per literal: number, decimal, string, active,
               inactive, void
unary/         chained unary operators (- -1, not not active)
grouping/      a group that changes the tree ((2 + 3) * 4) and a redundant
               one (((((1)))))
precedence/    * before + (1 + 2 * 3) and left associativity (1 - 2 - 3)
lines/         two expressions on two lines, and an expression that tries to
               continue onto the next line (rejected)
errors/        an unclosed parenthesis, a missing right operand, a token that
               can't begin an expression, and a bad line after a good one
empty/         a file with only a comment, which is rejected
```

Every rejected lab2 file has an empty `.expected`, since `--parse` prints
nothing for a rejected file, and a `.exit` holding 65.

Run locally with:

```bash
curl -sSL https://raw.githubusercontent.com/WhiteLicorice/cmsc-124-harness/v1.1/run_tests.py -o run_tests.py
./build.sh
python3 run_tests.py tests/lab1
python3 run_tests.py tests/lab2
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
Nier already ends statements with a newline instead of a semicolon, so we made `--parse` follow the same rule. We split on the line numbers the scanner already records instead of on the raw text, so every diagnostic keeps its real line number. Not letting an expression continue past the end of a line means a missing operand is caught on the line where it happened, instead of silently pulling in the next line.

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
- Equality, comparison, `and`, `or`, `=`, and identifiers are scanned but not
  parsed yet. A line that uses one is rejected, e.g. `1 < 2` reports
  `Error at '<': Expect end of expression.`
- An expression cannot span lines, even inside parentheses.
- `not` binds tighter than `+` and `*`, so `not 1 + 2` parses as
  `(+ (not 1.0) 2.0)`.
- Strings print without quotes, so the string `"1.0"` and the number `1` both
  print as `1.0`.
- Numbers print with Kotlin's `Double.toString()`, which switches to scientific
  notation at ten million and above or below 0.001: `10_000_000` prints as
  `1.0E7` and `0.0001` as `1.0E-4`.
- A lexical error stops `--parse` before parsing, so a file with both a lexical
  and a syntax error reports only the lexical one.
- The error for an empty or comment-only file always says line 1, even when the
  comment is further down.
- The REPL still parses a line that had a lexical error, so it can show a
  second, parser error: `unit y = !x` also reports
  `Error at 'unit': Expect expression.`

## Changelog

| Activity | What changed in the language |
|---|---|
| Lab 1 | Nier defined: 15 keywords, brace-delimited blocks, newline statement termination, `#` line comments, double-quoted strings with escapes and no line spanning, integer and decimal numbers with no leading or trailing dot, letter-or-underscore identifiers. Logical negation is the `not` keyword. A bare `!` is a lexical error. Token output format frozen. Added `scan` for name-and-value state inspection, distinct from `report`'s value-only output. Added underscore digit separators in numeric literals, valid only between two digits. |
| Lab 2 | Expression grammar added: `+ -` (loosest), then `* /`, then prefix `not` and `-`, then literals and parenthesized groups. Binary operators are left-associative. `--parse` prints one prefix-form tree per line. One expression per line, and an expression cannot span lines. A file with no expressions is rejected. The REPL now also parses each line and prints its tree after the tokens. Equality, comparison, `and`, `or`, and identifiers are not parsed yet. |