package flx;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Codegen {

    public static void compileAndRun(List<Stmt> program) throws IOException, InterruptedException {
        String src = generate(program);
        Path out = Path.of("bin/FluxProgram.java");
        Files.createDirectories(out.getParent());
        Files.writeString(out, src);

        ProcessBuilder pb = new ProcessBuilder("javac", "-d", "bin", out.toString());
        pb.inheritIO();
        int rc = pb.start().waitFor();
        if (rc != 0) throw new RuntimeException("javac failed");

        ProcessBuilder run = new ProcessBuilder("java", "-cp", "bin", "FluxProgram");
        run.inheritIO();
        int rc2 = run.start().waitFor();
        if (rc2 != 0) throw new RuntimeException("program exited with " + rc2);
    }

    private static String generate(List<Stmt> program) {
        StringBuilder sb = new StringBuilder();
        sb.append("public class FluxProgram {\n");
        // helper methods
        sb.append("  private static Object add(Object a, Object b) {\n");
        sb.append("    if (a instanceof String || b instanceof String) return String.valueOf(a) + String.valueOf(b);\n");
        sb.append("    if (a instanceof Double || b instanceof Double) return ((Number)a).doubleValue() + ((Number)b).doubleValue();\n");
        sb.append("    return ((Number)a).intValue() + ((Number)b).intValue();\n");
        sb.append("  }\n");

        sb.append("  private static Object sub(Object a, Object b) {\n");
        sb.append("    if (a instanceof Double || b instanceof Double) return ((Number)a).doubleValue() - ((Number)b).doubleValue();\n");
        sb.append("    return ((Number)a).intValue() - ((Number)b).intValue();\n");
        sb.append("  }\n");

        sb.append("  private static Object mul(Object a, Object b) {\n");
        sb.append("    if (a instanceof Double || b instanceof Double) return ((Number)a).doubleValue() * ((Number)b).doubleValue();\n");
        sb.append("    return ((Number)a).intValue() * ((Number)b).intValue();\n");
        sb.append("  }\n");

        sb.append("  private static Object div(Object a, Object b) {\n");
        sb.append("    if (a instanceof Double || b instanceof Double) return ((Number)a).doubleValue() / ((Number)b).doubleValue();\n");
        sb.append("    return ((Number)a).intValue() / ((Number)b).intValue();\n");
        sb.append("  }\n");

        sb.append("  private static boolean gt(Object a, Object b) {\n");
        sb.append("    if (a instanceof Number && b instanceof Number) return ((Number)a).doubleValue() > ((Number)b).doubleValue();\n");
        sb.append("    return String.valueOf(a).compareTo(String.valueOf(b)) > 0;\n");
        sb.append("  }\n");

        sb.append("  private static boolean lt(Object a, Object b) {\n");
        sb.append("    if (a instanceof Number && b instanceof Number) return ((Number)a).doubleValue() < ((Number)b).doubleValue();\n");
        sb.append("    return String.valueOf(a).compareTo(String.valueOf(b)) < 0;\n");
        sb.append("  }\n");

        sb.append("  private static boolean eq(Object a, Object b) {\n");
        sb.append("    if (a == null) return b == null;\n");
        sb.append("    return a.equals(b);\n");
        sb.append("  }\n");
        sb.append("  private static String input(Object prompt) {\n");
        sb.append("    if (prompt != null) System.out.print(String.valueOf(prompt));\n");
        sb.append("    try { java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(System.in)); return br.readLine(); } catch (Exception e) { return null; }\n");
        sb.append("  }\n");

        sb.append("  private static String idx(Object s, Object i) { String str = (String)s; int idx = ((Number)i).intValue(); if (idx < 0) idx = str.length() + idx; return String.valueOf(str.charAt(idx)); }\n");
        sb.append("  private static String slc(Object s, Object a, Object b) { String str = (String)s; int len = str.length(); int si = a==null?0:((Number)a).intValue(); int ei = b==null?len:((Number)b).intValue(); if (si<0) si = len+si; if (ei<0) ei=len+ei; if (si<0) si=0; if (ei>len) ei=len; if (ei<si) return \"\"; return str.substring(si, ei); }\n");

        // functions
        for (Stmt s : program) {
            if (s instanceof Stmt.Function f) {
                sb.append(generateFunction(f));
            }
        }

        // main method
        sb.append("  public static void main(String[] args) {\n");
        // top-level statements
        for (Stmt s : program) {
            if (!(s instanceof Stmt.Function)) sb.append(generateStmt(s, 2));
        }
        sb.append("  }\n");

        sb.append("}\n");
        return sb.toString();
    }

    private static String generateFunction(Stmt.Function f) {
        StringBuilder sb = new StringBuilder();
        sb.append("  private static Object ").append(f.name()).append("(");
        for (int i = 0; i < f.params().size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append("Object ").append(f.params().get(i));
        }
        sb.append(") {\n");
        for (Stmt st : f.body()) sb.append(generateStmt(st, 4));
        if (!functionHasReturn(f.body())) sb.append("    return null;\n");
        sb.append("  }\n");
        return sb.toString();
    }

    private static boolean functionHasReturn(List<Stmt> stmts) {
        for (Stmt s : stmts) {
            if (s instanceof Stmt.Return) return true;
            if (s instanceof Stmt.Block b) {
                if (functionHasReturn(b.stmts())) return true;
            }
            if (s instanceof Stmt.If i) {
                if (functionHasReturn(List.of(i.thenBranch()))) return true;
                if (i.elseBranch() != null && functionHasReturn(List.of(i.elseBranch()))) return true;
            }
        }
        return false;
    }

    private static String generateStmt(Stmt s, int indent) {
        String ind = "";
        for (int i = 0; i < indent; i++) ind += " ";
        StringBuilder sb = new StringBuilder();
        if (s instanceof Stmt.VarDecl v) {
            sb.append(ind).append("Object ").append(v.name()).append(" = ").append(generateExpr(v.init())).append(";\n");
        } else if (s instanceof Stmt.Assign a) {
            sb.append(ind).append(a.name()).append(" = ").append(generateExpr(a.value())).append(";\n");
        } else if (s instanceof Stmt.Print p) {
            sb.append(ind).append("System.out.println(").append(generateExpr(p.expr())).append(");\n");
        } else if (s instanceof Stmt.Block b) {
            sb.append(ind).append("{\n");
            for (Stmt st : b.stmts()) sb.append(generateStmt(st, indent+2));
            sb.append(ind).append("}\n");
        } else if (s instanceof Stmt.If i) {
            sb.append(ind).append("if ((Boolean) (").append(generateExpr(i.cond())).append(") )\n");
            sb.append(generateStmt(i.thenBranch(), indent));
            if (i.elseBranch() != null) {
                sb.append(ind).append("else\n");
                sb.append(generateStmt(i.elseBranch(), indent));
            }
        } else if (s instanceof Stmt.While w) {
            sb.append(ind).append("while ((Boolean) (").append(generateExpr(w.cond())).append(") )\n");
            sb.append(generateStmt(w.body(), indent));
        } else if (s instanceof Stmt.ExprStmt e) {
            sb.append(ind).append(generateExpr(e.expr())).append(";\n");
        } else if (s instanceof Stmt.Return r) {
            sb.append(ind).append("return ").append(generateExpr(r.value())).append(";\n");
        }
        return sb.toString();
    }

    private static String generateExpr(Expr e) {
        if (e instanceof Expr.Literal l) {
            Object v = l.value();
            if (v instanceof String) return "\"" + ((String)v).replace("\"", "\\\"") + "\"";
            if (v instanceof Integer) return "Integer.valueOf(" + v + ")";
            if (v instanceof Double) return "Double.valueOf(" + v + ")";
            if (v instanceof Boolean) return ((Boolean)v) ? "Boolean.TRUE" : "Boolean.FALSE";
            return "null";
        }
        if (e instanceof Expr.Variable v) return v.name();
        if (e instanceof Expr.Binary b) {
            switch (b.op().type()) {
                case PLUS: return "add(" + generateExpr(b.left()) + ", " + generateExpr(b.right()) + ")";
                case MINUS: return "sub(" + generateExpr(b.left()) + ", " + generateExpr(b.right()) + ")";
                case STAR: return "mul(" + generateExpr(b.left()) + ", " + generateExpr(b.right()) + ")";
                case SLASH: return "div(" + generateExpr(b.left()) + ", " + generateExpr(b.right()) + ")";
                case GREATER: return "gt(" + generateExpr(b.left()) + ", " + generateExpr(b.right()) + ")";
                case LESS: return "lt(" + generateExpr(b.left()) + ", " + generateExpr(b.right()) + ")";
                case EQUAL_EQUAL: return "eq(" + generateExpr(b.left()) + ", " + generateExpr(b.right()) + ")";
                default: return "null";
            }
        }
        if (e instanceof Expr.Unary u) {
            if (u.op().type() == TokenType.MINUS) return "sub(Integer.valueOf(0), " + generateExpr(u.right()) + ")";
        }
        if (e instanceof Expr.Call c) {
            StringBuilder sb = new StringBuilder();
            sb.append(c.callee() instanceof Expr.Variable v ? v.name() : generateExpr(c.callee())).append("(");
            for (int i = 0; i < c.args().size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(generateExpr(c.args().get(i)));
            }
            sb.append(")");
            return sb.toString();
        }
        if (e instanceof Expr.Index idx) return "idx(" + generateExpr(idx.target()) + ", " + generateExpr(idx.index()) + ")";
        if (e instanceof Expr.Slice sl) return "slc(" + generateExpr(sl.target()) + ", " + (sl.start()==null?"null":generateExpr(sl.start())) + ", " + (sl.end()==null?"null":generateExpr(sl.end())) + ")";
        if (e instanceof Expr.Cast c) {
            String t = c.typeName().toLowerCase();
            switch (t) {
                case "int": return "Integer.valueOf(((Number)" + generateExpr(c.expr()) + ").intValue())";
                case "double": return "Double.valueOf(((Number)" + generateExpr(c.expr()) + ").doubleValue())";
                case "str": return "String.valueOf(" + generateExpr(c.expr()) + ")";
                case "bool": return "Boolean.valueOf(" + generateExpr(c.expr()) + ")";
                default: return generateExpr(c.expr());
            }
        }
        return "null";
    }
}
