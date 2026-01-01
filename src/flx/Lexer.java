package flx;

import java.util.*;

public class Lexer {

    private final String src;
    private final List<Token> tokens = new ArrayList<>();
    private int pos = 0;
    private int line = 1;
    private int start = 0;

    private static final Map<String, TokenType> keywords = Map.of(
            "let", TokenType.LET,
            "if", TokenType.IF,
            "else", TokenType.ELSE,
            "while", TokenType.WHILE,
            "print", TokenType.PRINT,
            "fun", TokenType.FUN,
            "return", TokenType.RETURN,
            "true", TokenType.TRUE,
            "false", TokenType.FALSE
    );

    public Lexer(String src) {
        this.src = src;
    }

    public List<Token> tokenize() {
        while (!isAtEnd()) {
            start = pos;
            char c = advance();
            switch (c) {
                case ' ' , '\r', '\t' -> {}
                case '\n' -> line++;
                case '(' -> add(TokenType.LEFT_PAREN);
                case ')' -> add(TokenType.RIGHT_PAREN);
                case '{' -> add(TokenType.LEFT_BRACE);
                case '}' -> add(TokenType.RIGHT_BRACE);
                case '[' -> add(TokenType.LEFT_BRACKET);
                case ']' -> add(TokenType.RIGHT_BRACKET);
                case ':' -> {
                    if (match('?')) {
                        // single-line comment starting with :?
                        while (!isAtEnd() && peek() != '\n') advance();
                    } else if (match('{')) {
                        // multi-line comment :{ ... :}
                        while (!isAtEnd()) {
                            if (peek() == ':' && pos + 1 < src.length() && src.charAt(pos + 1) == '}') {
                                pos += 2; // consume :}
                                break;
                            }
                            if (peek() == '\n') line++;
                            advance();
                        }
                    } else {
                        add(TokenType.COLON);
                    }
                }
                case ';' -> add(TokenType.SEMICOLON);
                case ',' -> add(TokenType.COMMA);
                case '+' -> add(TokenType.PLUS);
                case '-' -> add(TokenType.MINUS);
                case '*' -> add(TokenType.STAR);
                case '/' -> add(TokenType.SLASH);
                case '=' -> add(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
                case '>' -> add(TokenType.GREATER);
                case '<' -> add(TokenType.LESS);
                case '"' -> string();
                default -> {
                    if (isDigit(c)) number();
                    else if (isAlpha(c)) identifier();
                    else throw error("Unexpected character: " + c);
                }
            }
        }
        tokens.add(new Token(TokenType.EOF, "", line));
        return tokens;
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();
        String text = src.substring(start, pos);
        add(keywords.getOrDefault(text, TokenType.IDENTIFIER), text);
    }

    private void number() {
        boolean isDouble = false;
        while (isDigit(peek())) advance();
        if (peek() == '.' && pos + 1 < src.length() && Character.isDigit(src.charAt(pos + 1))) {
            isDouble = true;
            advance(); // consume '.'
            while (isDigit(peek())) advance();
        }
        String lex = src.substring(start, pos);
        add(isDouble ? TokenType.DOUBLE : TokenType.INT, lex);
    }

    private void string() {
        int begin = pos;
        while (!isAtEnd() && peek() != '"') {
            if (peek() == '\n') line++;
            advance();
        }
        if (isAtEnd()) throw error("Unterminated string");
        advance();
        tokens.add(new Token(TokenType.STRING, src.substring(begin, pos - 1), line));
    }

    private char advance() { return src.charAt(pos++); }
    private boolean match(char c) { return !isAtEnd() && src.charAt(pos) == c && ++pos > 0; }
    private char peek() { return isAtEnd() ? '\0' : src.charAt(pos); }
    private boolean isAtEnd() { return pos >= src.length(); }
    private boolean isDigit(char c) { return c >= '0' && c <= '9'; }
    private boolean isAlpha(char c) { return Character.isLetter(c) || c == '_'; }
    private boolean isAlphaNumeric(char c) { return isAlpha(c) || isDigit(c); }
    private void add(TokenType t) { tokens.add(new Token(t, "", line)); }
    private void add(TokenType t, String lex) { tokens.add(new Token(t, lex, line)); }

    private RuntimeException error(String msg) {
        return new RuntimeException("[Line " + line + "] " + msg);
    }
}
