# Flux Compiler

A small interpreter for the toy language "Flux" implemented in Java.

## Overview

Flux is a minimal scripting language with integers, strings, variables, arithmetic, control flow, and functions. This repository contains a simple lexer, parser, AST, and interpreter so you can experiment with language features and extend the compiler.

## Features

- Integer and string literals
 - Integer, double and string literals
- Variables (`let`), assignment
- Arithmetic and comparison operators (`+ - * / > < ==`)
- Control flow: `if`, `else`, `while`, blocks
- `print(...)` for output
- First-class functions: `fun` declarations, calls, and `return`
- String indexing and slicing: `s[2]` -> single-character string, `s[1:4]` -> substring
 - Comments: single-line `:? this is a comment` and multi-line `:{ ... :}` are supported and ignored by the lexer
 - Typed function parameters: you may declare parameter types with `name : Type`, e.g. `fun f(a : Int, b : Str, c : double) { ... }`
 - Builtin `input(prompt)` — prints `prompt` and returns a line of user input as a string. Supported in the interpreter and in generated Java programs.

## Build

Compile sources to the `bin` directory using `javac`:

```bash
javac -d bin -sourcepath src src/App.java src/flx/*.java
```

## Run

Run a `.flx` program with the `flx.Compiler` entry point:

```bash
java -cp bin flx.Compiler test.flx
```

Or run the REPL with `flx.Main`:

```bash
java -cp bin flx.Main
```

Code generation

The compiler can emit a Java program from a Flux source and run it. The generated Java runtime includes small helper functions (arithmetic, indexing, and `input`) so many Flux programs— including those using `input(prompt)`—work when compiled and executed via the code generator.

## Example (`test.flx`)

```
let x = 56;
while (x > 0) {
	print(x);
	x = x - 2;
}

fun add(a, b) {
	return a + b;
}

let y = add(2, 3);
print(y);
```

## Notes

- Functions are declared with `fun name(params) { ... }` and stored as closures capturing the surrounding environment.
- `return` unwinds the function body and returns a value to the caller.
- The interpreter has lexical scoping via nested `Environment` objects.

## Extending

The source lives in `src/flx/`. Key files:

- [src/flx/Lexer.java](src/flx/Lexer.java) — tokenization
- [src/flx/Parser.java](src/flx/Parser.java) — parsing to AST
- [src/flx/Expr.java](src/flx/Expr.java) and [src/flx/Stmt.java](src/flx/Stmt.java) — AST nodes
- [src/flx/Interpreter.java](src/flx/Interpreter.java) — evaluation

Contributions and experiments are welcome — add tests, additional types, or VM/bytecode backends as exercises.
