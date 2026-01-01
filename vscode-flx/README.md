# Flux (flx) VS Code Syntax Extension (local)

This folder contains a minimal VS Code extension that provides syntax highlighting for `.flx` files using a TextMate grammar.

Files
- `package.json` — extension manifest
- `syntaxes/flx.tmLanguage.json` — TextMate grammar
- `language-configuration.json` — comments, brackets

Install & test locally
1. Open the workspace root in VS Code: `File → Open Folder...` and select the repository folder.
2. Open the `vscode-flx` folder and press F5 to launch an Extension Development Host.
3. Open any `.flx` file (e.g., `test.flx`); the `flx` language should be active and highlight tokens.

Alternative: Package as .vsix
- Install `vsce` (`npm i -g vsce`).
- From `vscode-flx` run `vsce package` to create a `.vsix` file and then install it in VS Code.

Language highlights & notes

- File extension: `.flx` — the extension activates the `flx` language for these files.
- Comments: single-line `:? ...` and block `:{ ... :}` are recognized by the language configuration and highlighted/ignored by the lexer.
- Functions: `fun name(params) { ... }` with optional typed parameters `name : Type`.
- Builtins: `print(...)`, `input(prompt)` (prints prompt and returns a string), `len(...)`, and other small helpers supported by the runtime.
- Strings: indexing `s[i]` and slicing `s[a:b]` are valid expressions in Flux and highlighted by the grammar.

Extending the extension

If you want richer language features (linting, completions, hover, or signature help) we can extend this extension using the Language Server Protocol (LSP) or implement a small JS/TS-based language host to provide simple completions.

Contributions welcome — open an issue or send a pull request with grammar improvements or extra features.
# Flux (flx) VS Code Syntax Extension (local)

This folder contains a minimal VS Code extension that provides syntax highlighting for `.flx` files using a TextMate grammar.

Files
- package.json — extension manifest
- syntaxes/flx.tmLanguage.json — TextMate grammar
- language-configuration.json — comments, brackets

Install & test locally
1. Open the containing folder in VS Code: `File → Open Folder...` and select the workspace root.
2. In the Explorer, open the `vscode-flx` folder and press F5 to launch an Extension Development Host.
3. Open any `.flx` file (e.g., `test.flx`); the new `flx` language should be active and highlight tokens.

Alternative: Package as .vsix
- Install `vsce` (`npm i -g vsce`).
- From `vscode-flx` run `vsce package` to create a `.vsix` file and then install it in VS Code.
