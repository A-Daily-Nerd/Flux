package flx;

import java.util.List;

public class Interpreter {

    private Environment env = new Environment();

    // function representation
    private record FlxFunction(List<String> params, List<String> types, List<Stmt> body, Environment closure) {}

    private static class ReturnException extends RuntimeException {
        final Object value;
        ReturnException(Object value) { this.value = value; }
    }

    public void execute(Stmt stmt) {
        switch (stmt) {
            case Stmt.VarDecl v -> env.define(v.name(), eval(v.init()));
            case Stmt.Assign a -> env.assign(a.name(), eval(a.value()));
            case Stmt.Print p -> System.out.println(eval(p.expr()));
            case Stmt.Block b -> {
                Environment blockEnv = new Environment(env);
                runBlock(b.stmts(), blockEnv);
            }
            case Stmt.ExprStmt e -> eval(e.expr());
            case Stmt.If i -> {
                if ((boolean) eval(i.cond())) execute(i.thenBranch());
                else if (i.elseBranch() != null) execute(i.elseBranch());
            }
            case Stmt.While w -> {
                while ((boolean) eval(w.cond())) execute(w.body());
            }
            case Stmt.Function f -> env.define(f.name(), new FlxFunction(f.params(), f.types(), f.body(), env));
            case Stmt.Return r -> throw new ReturnException(eval(r.value()));
        }
    }

    private void runBlock(List<Stmt> stmts, Environment blockEnv) {
        Environment previous = this.env;
        try {
            this.env = blockEnv;
            for (Stmt s : stmts) execute(s);
        } finally {
            this.env = previous;
        }
    }

    private Object eval(Expr e) {
        return switch (e) {
            case Expr.Literal l -> l.value();
            case Expr.Variable v -> env.get(v.name());
            case Expr.Binary b -> apply(b);
            case Expr.Unary u -> applyUnary(u);
            case Expr.Call c -> applyCall(c);
            case Expr.Index i -> applyIndex(i);
            case Expr.Slice s -> applySlice(s);
            case Expr.Cast c -> applyCast(c);
        };
    }

    private Object applyCast(Expr.Cast c) {
        Object val = eval(c.expr());
        String t = c.typeName().toLowerCase();
        switch (t) {
            case "int", "integer": {
                if (val instanceof Integer) return val;
                if (val instanceof Double) return ((Double) val).intValue();
                if (val instanceof String) {
                    String s = ((String) val).trim();
                    if (s.isEmpty()) throw new RuntimeException("Cannot cast empty string to int");
                    try {
                        return Integer.parseInt(s);
                    } catch (NumberFormatException ex) {
                        try {
                            return (int) Double.parseDouble(s);
                        } catch (NumberFormatException ex2) {
                            throw new RuntimeException("Cannot cast to int: '" + s + "'");
                        }
                    }
                }
                throw new RuntimeException("Cannot cast to int: " + val);
            }
            case "double", "float": {
                if (val instanceof Double) return val;
                if (val instanceof Integer) return ((Integer) val).doubleValue();
                if (val instanceof String) {
                    String s = ((String) val).trim();
                    if (s.isEmpty()) throw new RuntimeException("Cannot cast empty string to double");
                    try {
                        return Double.parseDouble(s);
                    } catch (NumberFormatException ex) {
                        throw new RuntimeException("Cannot cast to double: '" + s + "'");
                    }
                }
                throw new RuntimeException("Cannot cast to double: " + val);
            }
            case "str", "string":
                return String.valueOf(val);
            case "bool", "boolean": {
                if (val instanceof Boolean) return val;
                if (val instanceof String) {
                    String s = ((String) val).trim().toLowerCase();
                    if (s.equals("true") || s.equals("1")) return true;
                    if (s.equals("false") || s.equals("0")) return false;
                    throw new RuntimeException("Cannot cast to bool: '" + s + "'");
                }
                if (val instanceof Number) return ((Number) val).doubleValue() != 0.0;
                throw new RuntimeException("Cannot cast to bool: " + val);
            }
            default:
                throw new RuntimeException("Unknown cast type: " + t);
        }
    }

    private Object applyIndex(Expr.Index i) {
        Object target = eval(i.target());
        Object idxObj = eval(i.index());
        if (!(target instanceof String)) throw new RuntimeException("Indexing supported only on strings");
        if (!(idxObj instanceof Integer)) throw new RuntimeException("Index must be an integer");
        String s = (String) target;
        int idx = (Integer) idxObj;
        if (idx < 0) idx = s.length() + idx;
        if (idx < 0 || idx >= s.length()) throw new RuntimeException("String index out of bounds");
        return String.valueOf(s.charAt(idx));
    }

    private Object applySlice(Expr.Slice sl) {
        Object target = eval(sl.target());
        if (!(target instanceof String)) throw new RuntimeException("Slicing supported only on strings");
        String s = (String) target;
        int len = s.length();
        Integer start = null;
        Integer end = null;
        if (sl.start() != null) {
            Object st = eval(sl.start());
            if (!(st instanceof Integer)) throw new RuntimeException("Slice indices must be integers");
            start = (Integer) st;
        }
        if (sl.end() != null) {
            Object en = eval(sl.end());
            if (!(en instanceof Integer)) throw new RuntimeException("Slice indices must be integers");
            end = (Integer) en;
        }
        int si = start == null ? 0 : start;
        int ei = end == null ? len : end;
        if (si < 0) si = len + si;
        if (ei < 0) ei = len + ei;
        if (si < 0) si = 0;
        if (ei > len) ei = len;
        if (ei < si) return "";
        return s.substring(si, ei);
    }

    private Object applyCall(Expr.Call c) {
        // builtin `input(prompt)` support
        if (c.callee() instanceof Expr.Variable vn && vn.name().equals("input")) {
            List<Expr> argExprs = c.args();
            String prompt = "";
            if (argExprs.size() > 0) prompt = String.valueOf(eval(argExprs.get(0)));
            System.out.print(prompt);
            try {
                java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
                String line = br.readLine();
                return line;
            } catch (java.io.IOException ex) {
                throw new RuntimeException("Error reading input: " + ex.getMessage());
            }
        }

        Object callee = eval(c.callee());
        if (!(callee instanceof FlxFunction fn)) throw new RuntimeException("Not a function");

        List<Expr> argExprs = c.args();
        if (argExprs.size() != fn.params().size()) throw new RuntimeException("Argument count mismatch");

        Environment callEnv = new Environment(fn.closure());
        // type checking for parameters
        for (int i = 0; i < fn.params().size(); i++) {
            Object val = eval(argExprs.get(i));
            String expected = fn.types().get(i);
            if (expected != null) {
                if (!matchesType(val, expected)) {
                    throw new RuntimeException("Type mismatch for parameter '" + fn.params().get(i) + "': expected " + expected + ", got " + (val == null ? "null" : val.getClass().getSimpleName()));
                }
            }
            callEnv.define(fn.params().get(i), val);
        }

        try {
            runBlock(fn.body(), callEnv);
        } catch (ReturnException re) {
            return re.value;
        }
        return null;
    }

    private boolean matchesType(Object val, String expected) {
        if (expected == null) return true;
        String e = expected.toLowerCase();
        if (e.equals("int") || e.equals("integer")) return val instanceof Integer;
        if (e.equals("double") || e.equals("float")) return val instanceof Double || val instanceof Integer;
        if (e.equals("str") || e.equals("string")) return val instanceof String;
        if (e.equals("bool") || e.equals("boolean")) return val instanceof Boolean;
        // unknown types: accept (could be extended to user-defined types)
        return true;
    }

    private Object apply(Expr.Binary b) {
        Object l = eval(b.left());
        Object r = eval(b.right());
        // string concatenation
        if (b.op().type() == TokenType.PLUS && (l instanceof String || r instanceof String)) {
            return String.valueOf(l) + String.valueOf(r);
        }

        // numeric operations: support Integer and Double
        boolean lNum = l instanceof Integer || l instanceof Double;
        boolean rNum = r instanceof Integer || r instanceof Double;

        return switch (b.op().type()) {
            case PLUS -> {
                if (lNum && rNum) {
                    if (l instanceof Double || r instanceof Double) yield ((Number) l).doubleValue() + ((Number) r).doubleValue();
                    else yield (Integer) l + (Integer) r;
                }
                throw new RuntimeException("Invalid operands for +");
            }
            case MINUS -> {
                if (lNum && rNum) {
                    if (l instanceof Double || r instanceof Double) yield ((Number) l).doubleValue() - ((Number) r).doubleValue();
                    else yield (Integer) l - (Integer) r;
                }
                throw new RuntimeException("Invalid operands for -");
            }
            case STAR -> {
                if (lNum && rNum) {
                    if (l instanceof Double || r instanceof Double) yield ((Number) l).doubleValue() * ((Number) r).doubleValue();
                    else yield (Integer) l * (Integer) r;
                }
                throw new RuntimeException("Invalid operands for *");
            }
            case SLASH -> {
                if (lNum && rNum) {
                    if (l instanceof Double || r instanceof Double) yield ((Number) l).doubleValue() / ((Number) r).doubleValue();
                    else yield (Integer) l / (Integer) r;
                }
                throw new RuntimeException("Invalid operands for /");
            }
            case GREATER -> {
                if (lNum && rNum) yield ((Number) l).doubleValue() > ((Number) r).doubleValue();
                if (l instanceof String && r instanceof String) yield ((String) l).compareTo((String) r) > 0;
                throw new RuntimeException("Invalid operands for >");
            }
            case LESS -> {
                if (lNum && rNum) yield ((Number) l).doubleValue() < ((Number) r).doubleValue();
                if (l instanceof String && r instanceof String) yield ((String) l).compareTo((String) r) < 0;
                throw new RuntimeException("Invalid operands for <");
            }
            case EQUAL_EQUAL -> {
                if (lNum && rNum) yield ((Number) l).doubleValue() == ((Number) r).doubleValue();
                else yield l.equals(r);
            }
            default -> throw new RuntimeException("Invalid operator");
        };
    }

    private Object applyUnary(Expr.Unary u) {
        Object r = eval(u.right());

        return switch (u.op().type()) {
            case MINUS -> {
                if (r instanceof Double) yield -((Double) r);
                if (r instanceof Integer) yield -((Integer) r);
                throw new RuntimeException("Invalid operand for unary -");
            }
            default -> throw new RuntimeException("Invalid unary operator");
        };
    }

}
