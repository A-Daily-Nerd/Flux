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

    // Expose the generated Java source for inspection without compiling.
    public static String generateSource(List<Stmt> program) {
        return generate(program);
    }

    private static String generate(List<Stmt> program) {
        StringBuilder sb = new StringBuilder();
        sb.append("public class FluxProgram {\n");
        // helper methods
        sb.append("  private static Object __flx_add(Object a, Object b) {\n");
        sb.append("    if (a instanceof String || b instanceof String) return String.valueOf(a) + String.valueOf(b);\n");
        sb.append("    if (a instanceof Double || b instanceof Double) return ((Number)a).doubleValue() + ((Number)b).doubleValue();\n");
        sb.append("    return ((Number)a).intValue() + ((Number)b).intValue();\n");
        sb.append("  }\n");

        sb.append("  private static Object __flx_sub(Object a, Object b) {\n");
        sb.append("    if (a instanceof Double || b instanceof Double) return ((Number)a).doubleValue() - ((Number)b).doubleValue();\n");
        sb.append("    return ((Number)a).intValue() - ((Number)b).intValue();\n");
        sb.append("  }\n");

        sb.append("  private static Object __flx_mul(Object a, Object b) {\n");
        sb.append("    if (a instanceof Double || b instanceof Double) return ((Number)a).doubleValue() * ((Number)b).doubleValue();\n");
        sb.append("    return ((Number)a).intValue() * ((Number)b).intValue();\n");
        sb.append("  }\n");

        sb.append("  private static Object __flx_div(Object a, Object b) {\n");
        sb.append("    if (a instanceof Double || b instanceof Double) return ((Number)a).doubleValue() / ((Number)b).doubleValue();\n");
        sb.append("    return ((Number)a).intValue() / ((Number)b).intValue();\n");
        sb.append("  }\n");

        sb.append("  private static boolean __flx_gt(Object a, Object b) {\n");
        sb.append("    if (a instanceof Number && b instanceof Number) return ((Number)a).doubleValue() > ((Number)b).doubleValue();\n");
        sb.append("    return String.valueOf(a).compareTo(String.valueOf(b)) > 0;\n");
        sb.append("  }\n");

        sb.append("  private static boolean __flx_lt(Object a, Object b) {\n");
        sb.append("    if (a instanceof Number && b instanceof Number) return ((Number)a).doubleValue() < ((Number)b).doubleValue();\n");
        sb.append("    return String.valueOf(a).compareTo(String.valueOf(b)) < 0;\n");
        sb.append("  }\n");

        sb.append("  private static boolean __flx_eq(Object a, Object b) {\n");
        sb.append("    if (a == null) return b == null;\n");
        sb.append("    return a.equals(b);\n");
        sb.append("  }\n");
        sb.append("  private static String __flx_input(Object prompt) {\n");
        sb.append("    if (prompt != null) System.out.print(String.valueOf(prompt));\n");
        sb.append("    try { java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(System.in)); return br.readLine(); } catch (Exception e) { return null; }\n");
        sb.append("  }\n");

        sb.append("  private static String __flx_idx(Object s, Object i) { String str = (String)s; int idx = ((Number)i).intValue(); if (idx < 0) idx = str.length() + idx; return String.valueOf(str.charAt(idx)); }\n");
        sb.append("  private static String __flx_slc(Object s, Object a, Object b) { String str = (String)s; int len = str.length(); int si = a==null?0:((Number)a).intValue(); int ei = b==null?len:((Number)b).intValue(); if (si<0) si = len+si; if (ei<0) ei=len+ei; if (si<0) si=0; if (ei>len) ei=len; if (ei<si) return \"\"; return str.substring(si, ei); }\n");

        // cast helpers: handle Numbers and Strings (from input) safely
        sb.append("  private static Object __flx_cast_int(Object v) {\n");
        sb.append("    if (v == null) throw new RuntimeException(\"Cannot cast null to int\");\n");
        sb.append("    if (v instanceof Number) return Integer.valueOf(((Number)v).intValue());\n");
        sb.append("    if (v instanceof String) {\n");
        sb.append("      String s = ((String)v).trim();\n");
        sb.append("      if (s.isEmpty()) throw new RuntimeException(\"Cannot cast empty string to int\");\n");
        sb.append("      try { return Integer.valueOf(Integer.parseInt(s)); }\n");
        sb.append("      catch (NumberFormatException e) {\n");
        sb.append("        try { return Integer.valueOf((int)Double.parseDouble(s)); }\n");
        sb.append("        catch (NumberFormatException e2) { throw new RuntimeException(String.format(\"Cannot cast to int: '%s'\", s)); }\n");
        sb.append("      }\n");
        sb.append("    }\n");
        sb.append("    throw new RuntimeException(\"Cannot cast to int: \" + v);\n");
        sb.append("  }\n");

        sb.append("  private static Object __flx_cast_double(Object v) {\n");
        sb.append("    if (v == null) throw new RuntimeException(\"Cannot cast null to double\");\n");
        sb.append("    if (v instanceof Number) return Double.valueOf(((Number)v).doubleValue());\n");
        sb.append("    if (v instanceof String) {\n");
        sb.append("      String s = ((String)v).trim();\n");
        sb.append("      if (s.isEmpty()) throw new RuntimeException(\"Cannot cast empty string to double\");\n");
        sb.append("      try { return Double.valueOf(Double.parseDouble(s)); }\n");
        sb.append("      catch (NumberFormatException e) { throw new RuntimeException(String.format(\"Cannot cast to double: '%s'\", s)); }\n");
        sb.append("    }\n");
        sb.append("    throw new RuntimeException(\"Cannot cast to double: \" + v);\n");
        sb.append("  }\n");

        sb.append("  private static Object __flx_cast_bool(Object v) {\n");
        sb.append("    if (v == null) throw new RuntimeException(\"Cannot cast null to bool\");\n");
        sb.append("    if (v instanceof Boolean) return v;\n");
        sb.append("    if (v instanceof Number) return ((Number)v).doubleValue() != 0.0;\n");
        sb.append("    if (v instanceof String) {\n");
        sb.append("      String s = ((String)v).trim().toLowerCase();\n");
        sb.append("      if (s.equals(\"true\") || s.equals(\"1\")) return true;\n");
        sb.append("      if (s.equals(\"false\") || s.equals(\"0\")) return false;\n");
        sb.append("      throw new RuntimeException(String.format(\"Cannot cast to bool: '%s'\", s));\n");
        sb.append("    }\n");
        sb.append("    throw new RuntimeException(\"Cannot cast to bool: \" + v);\n");
        sb.append("  }\n");

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
                case PLUS: return "__flx_add(" + generateExpr(b.left()) + ", " + generateExpr(b.right()) + ")";
                case MINUS: return "__flx_sub(" + generateExpr(b.left()) + ", " + generateExpr(b.right()) + ")";
                case STAR: return "__flx_mul(" + generateExpr(b.left()) + ", " + generateExpr(b.right()) + ")";
                case SLASH: return "__flx_div(" + generateExpr(b.left()) + ", " + generateExpr(b.right()) + ")";
                case GREATER: return "__flx_gt(" + generateExpr(b.left()) + ", " + generateExpr(b.right()) + ")";
                case LESS: return "__flx_lt(" + generateExpr(b.left()) + ", " + generateExpr(b.right()) + ")";
                case EQUAL_EQUAL: return "__flx_eq(" + generateExpr(b.left()) + ", " + generateExpr(b.right()) + ")";
                default: return "null";
            }
        }
        if (e instanceof Expr.Unary u) {
            if (u.op().type() == TokenType.MINUS) return "__flx_sub(Integer.valueOf(0), " + generateExpr(u.right()) + ")";
        }
        if (e instanceof Expr.Call c) {
            StringBuilder sb = new StringBuilder();
            // If calling the builtin `input`, route to the internal helper to avoid name collisions
            if (c.callee() instanceof Expr.Variable v && v.name().equals("input")) {
                sb.append("__flx_input(");
            } else {
                sb.append(c.callee() instanceof Expr.Variable v ? v.name() : generateExpr(c.callee())).append("(");
            }
            for (int i = 0; i < c.args().size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(generateExpr(c.args().get(i)));
            }
            sb.append(")");
            return sb.toString();
        }
        if (e instanceof Expr.Index idx) return "__flx_idx(" + generateExpr(idx.target()) + ", " + generateExpr(idx.index()) + ")";
        if (e instanceof Expr.Slice sl) return "__flx_slc(" + generateExpr(sl.target()) + ", " + (sl.start()==null?"null":generateExpr(sl.start())) + ", " + (sl.end()==null?"null":generateExpr(sl.end())) + ")";
        if (e instanceof Expr.Cast c) {
            String t = c.typeName().toLowerCase();
            switch (t) {
                case "int": return "__flx_cast_int(" + generateExpr(c.expr()) + ")";
                case "double": return "__flx_cast_double(" + generateExpr(c.expr()) + ")";
                case "str": return "String.valueOf(" + generateExpr(c.expr()) + ")";
                case "bool": return "__flx_cast_bool(" + generateExpr(c.expr()) + ")";
                default: return generateExpr(c.expr());
            }
        }
        return "null";
    }
}
