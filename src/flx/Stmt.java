package flx;

import java.util.List;

public sealed interface Stmt {
    record VarDecl(String name, Expr init) implements Stmt {}
    record Print(Expr expr) implements Stmt {}
    record Block(List<Stmt> stmts) implements Stmt {}
    record If(Expr cond, Stmt thenBranch, Stmt elseBranch) implements Stmt {}
    record While(Expr cond, Stmt body) implements Stmt {}
    record Assign(String name, Expr value) implements Stmt {}
    record ExprStmt(Expr expr) implements Stmt {}
    record Function(String name, List<String> params, List<String> types, List<Stmt> body) implements Stmt {}
    record Return(Expr value) implements Stmt {}
}
