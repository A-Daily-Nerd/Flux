# Flux Grammar Reference

This document gives a concise overview of the Flux language grammar and examples to help authors write programs and read the parser implementation.

**Tokens**
- Identifiers: letter followed by letters/digits/underscores, e.g. `myVar`, `add_1`
- Integers: digits, e.g. `123`
- Doubles: digits with `.` (fractional part), e.g. `3.14`
- Strings: double-quoted, supports escaped `\"`, e.g. `"hello"`
- Keywords: `fun`, `return`, `if`, `else`, `while`, `true`, `false`, `null`
- Operators/punctuation: `+ - * / == > < = , : ( ) [ ] { }` and cast syntax like `(Int)`
- Comments: single-line `:? ...` and block `:{ ... :}`

**Literals**
- Integer literal: `42`
- Double literal: `2.5`
- String literal: `"text"`
- Boolean: `true`, `false`
- Null: `null`

**Expressions (high level)**
- Primary: literal | identifier | `(` expression `)`
- Call: `expr(args...)` (postfix)
- Index: `expr[expr]` returns single-character string for strings
- Slice: `expr[start:end]` (either `start` or `end` may be omitted)
- Unary: `-expr` (numeric negation)
- Binary: `expr + expr`, `expr - expr`, `expr * expr`, `expr / expr`
- Comparison: `==`, `>`, `<` (comparable for numbers or strings)
- Cast: `(Type) expr` where `Type` is `Int`, `Double`, `Str`, or `Bool`

Operator precedence (high → low, left-associative unless noted):
- Index/slice/call (postfix)
- Unary `-`
- `*`, `/`
- `+`, `-`
- Comparisons `>`, `<`, `==`

**Statements**
- Variable declaration: `var name: Type = expr` or `var name = expr` (type optional)
- Assignment: `name = expr`
- Expression statement: `expr;` (in top-level code semicolon optional)
- Print: `print expr` (printer statement)
- Return (inside functions): `return expr`
- If: `if cond thenBranch else elseBranch` (curly blocks supported)
- While: `while cond body`
- Block: `{ stmt* }`

**Functions**
- Definition: `fun name(param1:Type, param2) { ... }` — parameter types are optional.
- Return: use `return expr` or fall off function to return `null`.
- Calls: `name(arg1, arg2)`; `input(prompt)` is a builtin that returns a `Str`.

**Types & Casting**
- Primitive types: `Int`, `Double`, `Str`, `Bool`.
- Casts: `(Int) expr`, `(Double) expr`, `(Str) expr`, `(Bool) expr` — safe at runtime: strings are parsed when possible.

**Indexing and Slicing**
- String index: `s[3]` returns the character at index `3` as a string (negative indices count from end).
- Slice: `s[1:4]` returns substring from `1` (inclusive) to `4` (exclusive); `s[:3]`, `s[2:]`, `s[-3:]` supported.

**Comments**
- Single-line: start with `:?` and continue to end-of-line.
- Block: start with `:{` and end with `:}`; blocks can span lines.

**Examples**
- Function and call:
```
fun add(a: Int, b: Int) {
  return a + b
}

print add(1, 2)  # prints 3
```

- Input and cast to Int:
```
var a = input("Enter a number: ")
a = (Int) a
print a
```

- String indexing & slicing:
```
var s = "hello"
print s[1]    # "e"
print s[1:4]  # "ell"
```

**Notes for implementers**
- The parser is a recursive-descent parser. Postfix operations (calls, indexing, slices) are handled in a loop after parsing primary expressions.
- Type checking infers and enforces primitive types; casts are lowered to runtime helpers in the Java codegen.
- Code generation emits helper methods named `__flx_*` (e.g., `__flx_cast_int`) for runtime conversions.

If you want, I can also generate a compact BNF or a printable cheatsheet PDF from this markdown. Which format would you prefer next?
