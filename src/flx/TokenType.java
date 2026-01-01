package flx;

public enum TokenType {
    // literals
    IDENTIFIER, INT, STRING,
    DOUBLE,

    // keywords
    LET, IF, ELSE, WHILE, PRINT,
    TRUE, FALSE,
    FUN, RETURN,

    // operators
    PLUS, MINUS, STAR, SLASH,
    EQUAL, EQUAL_EQUAL,
    GREATER, LESS,

    // punctuation
    LEFT_PAREN, RIGHT_PAREN,
    LEFT_BRACE, RIGHT_BRACE,
    LEFT_BRACKET, RIGHT_BRACKET,
    COLON,
    SEMICOLON, COMMA,

    EOF
}
