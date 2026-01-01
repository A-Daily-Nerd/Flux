package flx;

import java.util.*;

public class TypeChecker {

    private final Map<String, FunctionSig> functions = new HashMap<>();
    private final Deque<Map<String, String>> scopes = new ArrayDeque<>();

    private static final class FunctionSig {
        final List<String> paramNames;
        final List<String> paramTypes;
        String returnType;
        FunctionSig(List<String> paramNames, List<String> paramTypes) {
            this.paramNames = paramNames;
            this.paramTypes = paramTypes;
            this.returnType = null;
        }
    }

    public void check(List<Stmt> program) {
        // register functions first
        for (Stmt s : program) {
            if (s instanceof Stmt.Function f) {
                functions.put(f.name(), new FunctionSig(f.params(), f.types()));
            }
        }

        // register builtins (name -> signature)
        // input(prompt) -> str
        functions.put("input", new FunctionSig(List.of("prompt"), List.of("str")));

        // infer return types for all functions before checking top-level
        for (Stmt s : program) {
            if (s instanceof Stmt.Function f) {
                FunctionSig sig = functions.get(f.name());
                beginScope();
                for (int i = 0; i < f.params().size(); i++) {
                    String pname = f.params().get(i);
                    String ptype = sig.paramTypes.get(i);
                    declare(pname, ptype == null ? "any" : normalize(ptype));
                }
                Set<String> returns = new HashSet<>();
                for (Stmt st : f.body()) collectReturns(st, returns);
                endScope();
                if (!returns.isEmpty()) {
                    sig.returnType = returns.iterator().next();
                }
            }
        }

        // check top-level statements
        beginScope();
        for (Stmt s : program) {
            checkStmt(s);
        }
        endScope();
    }

    private void checkStmt(Stmt s) {
        switch (s) {
            case Stmt.VarDecl v -> {
                String t = typeOf(v.init());
                declare(v.name(), t);
            }
            case Stmt.Assign a -> {
                String target = lookup(a.name());
                if (target == null) throw new RuntimeException("Undefined variable " + a.name());
                String valT = typeOf(a.value());
                if (!typeCompatible(valT, target)) throw new RuntimeException("Type mismatch assigning to " + a.name() + ": expected " + target + ", got " + valT);
            }
            case Stmt.Print p -> { typeOf(p.expr()); }
            case Stmt.Block b -> {
                beginScope();
                for (Stmt st : b.stmts()) checkStmt(st);
                endScope();
            }
            case Stmt.If i -> {
                String condT = typeOf(i.cond());
                if (!"bool".equals(condT)) throw new RuntimeException("Condition must be boolean");
                checkStmt(i.thenBranch());
                if (i.elseBranch() != null) checkStmt(i.elseBranch());
            }
            case Stmt.While w -> {
                String condT = typeOf(w.cond());
                if (!"bool".equals(condT)) throw new RuntimeException("Condition must be boolean");
                checkStmt(w.body());
            }
            case Stmt.Function f -> {
                // check body with params in scope
                FunctionSig sig = functions.get(f.name());
                beginScope();
                for (int i = 0; i < f.params().size(); i++) {
                    String pname = f.params().get(i);
                    String ptype = sig.paramTypes.get(i);
                    declare(pname, ptype == null ? "any" : normalize(ptype));
                }
                // collect return types
                Set<String> returns = new HashSet<>();
                for (Stmt st : f.body()) {
                    collectReturns(st, returns);
                    checkStmt(st);
                }
                endScope();
                if (!returns.isEmpty()) {
                    String r = returns.iterator().next();
                    sig.returnType = r;
                }
            }
            case Stmt.Return r -> { /* return handled in collectReturns */ }
            case Stmt.ExprStmt e -> { typeOf(e.expr()); }
            default -> {}
        }
    }

    private void collectReturns(Stmt s, Set<String> returns) {
        switch (s) {
            case Stmt.Return r -> {
                returns.add(typeOf(r.value()));
            }
            case Stmt.Block b -> b.stmts().forEach(st -> collectReturns(st, returns));
            case Stmt.If i -> { collectReturns(i.thenBranch(), returns); if (i.elseBranch() != null) collectReturns(i.elseBranch(), returns); }
            case Stmt.While w -> collectReturns(w.body(), returns);
            default -> {}
        }
    }

    private String typeOf(Expr e) {
        if (e == null) return "any";
        if (e instanceof Expr.Literal l) {
            Object v = l.value();
            if (v instanceof Integer) return "int";
            if (v instanceof Double) return "double";
            if (v instanceof String) return "str";
            if (v instanceof Boolean) return "bool";
            return "any";
        }
        if (e instanceof Expr.Variable v) {
            String t = lookup(v.name());
            if (t == null) throw new RuntimeException("Undefined variable " + v.name());
            return t;
        }
        if (e instanceof Expr.Unary u) {
            String rt = typeOf(u.right());
            if (u.op().type() == TokenType.MINUS) {
                if ("int".equals(rt) || "double".equals(rt)) return rt;
                throw new RuntimeException("Unary - requires numeric operand");
            }
            return "any";
        }
        if (e instanceof Expr.Binary b) {
            String lt = typeOf(b.left());
            String rt = typeOf(b.right());
            TokenType op = b.op().type();
            if (op == TokenType.PLUS) {
                if ("str".equals(lt) || "str".equals(rt)) return "str";
                if (isNumeric(lt) && isNumeric(rt)) return promote(lt, rt);
                throw new RuntimeException("Invalid operands for +");
            }
            if (op == TokenType.MINUS || op == TokenType.STAR || op == TokenType.SLASH) {
                if (isNumeric(lt) && isNumeric(rt)) return promote(lt, rt);
                throw new RuntimeException("Invalid numeric operands");
            }
            if (op == TokenType.GREATER || op == TokenType.LESS || op == TokenType.EQUAL_EQUAL) return "bool";
            throw new RuntimeException("Unknown binary operator");
        }
        if (e instanceof Expr.Call c) {
            if (!(c.callee() instanceof Expr.Variable callee)) throw new RuntimeException("Call of non-function");
            FunctionSig sig = functions.get(callee.name());
            if (sig == null) throw new RuntimeException("Undefined function " + callee.name());
            if (c.args().size() != sig.paramTypes.size()) throw new RuntimeException("Argument count mismatch in call to " + callee.name());
            for (int i = 0; i < c.args().size(); i++) {
                String at = typeOf(c.args().get(i));
                String pt = sig.paramTypes.get(i);
                if (pt != null && !typeCompatible(at, normalize(pt))) throw new RuntimeException("Type mismatch in call to " + callee.name() + " for param " + sig.paramNames.get(i));
            }
            return sig.returnType == null ? "any" : sig.returnType;
        }
        if (e instanceof Expr.Index idx) {
            String tt = typeOf(idx.target());
            if (!"str".equals(tt)) throw new RuntimeException("Indexing supported only on strings");
            return "str";
        }
        if (e instanceof Expr.Slice sl) {
            String tt = typeOf(sl.target());
            if (!"str".equals(tt)) throw new RuntimeException("Slicing supported only on strings");
            return "str";
        }
        if (e instanceof Expr.Cast cst) return normalize(cst.typeName());
        throw new RuntimeException("Unhandled expr type in type checker");
    }

    private boolean isNumeric(String t) { return "int".equals(t) || "double".equals(t) || "any".equals(t); }
    private String promote(String a, String b) { if ("double".equals(a) || "double".equals(b)) return "double"; if ("any".equals(a) || "any".equals(b)) return "any"; return "int"; }

    private boolean typeCompatible(String given, String expected) {
        if (expected == null || "any".equals(expected)) return true;
        if (expected.equals(given)) return true;
        if (expected.equals("double") && "int".equals(given)) return true; // int promotes to double
        return false;
    }

    private String normalize(String t) {
        if (t == null) return null;
        String low = t.toLowerCase();
        if (low.equals("int") || low.equals("integer")) return "int";
        if (low.equals("double") || low.equals("float")) return "double";
        if (low.equals("str") || low.equals("string")) return "str";
        if (low.equals("bool") || low.equals("boolean")) return "bool";
        return low;
    }

    private void beginScope() { scopes.push(new HashMap<>()); }
    private void endScope() { scopes.pop(); }
    private void declare(String name, String type) { scopes.peek().put(name, type == null ? "any" : normalize(type)); }
    private String lookup(String name) {
        for (Map<String, String> s : scopes) {
            if (s.containsKey(name)) return s.get(name);
        }
        return null;
    }
}
