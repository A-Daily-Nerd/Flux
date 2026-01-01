package flx;

public sealed interface Expr {
    record Literal(Object value) implements Expr {}
    record Variable(String name) implements Expr {}
    record Binary(Expr left, Token op, Expr right) implements Expr {}
    record Unary(Token op, Expr right) implements Expr {}
    record Call(Expr callee, java.util.List<Expr> args) implements Expr {}
    record Index(Expr target, Expr index) implements Expr {}
    record Slice(Expr target, Expr start, Expr end) implements Expr {}
    record Cast(String typeName, Expr expr) implements Expr {}
}
