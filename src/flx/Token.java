package flx;

public record Token(TokenType type, String lexeme, int line) {
    @Override
    public String toString() {
        return type + " " + lexeme;
    }
}
