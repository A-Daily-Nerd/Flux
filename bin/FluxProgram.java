public class FluxProgram {
  private static Object __flx_add(Object a, Object b) {
    if (a instanceof String || b instanceof String) return String.valueOf(a) + String.valueOf(b);
    if (a instanceof Double || b instanceof Double) return ((Number)a).doubleValue() + ((Number)b).doubleValue();
    return ((Number)a).intValue() + ((Number)b).intValue();
  }
  private static Object __flx_sub(Object a, Object b) {
    if (a instanceof Double || b instanceof Double) return ((Number)a).doubleValue() - ((Number)b).doubleValue();
    return ((Number)a).intValue() - ((Number)b).intValue();
  }
  private static Object __flx_mul(Object a, Object b) {
    if (a instanceof Double || b instanceof Double) return ((Number)a).doubleValue() * ((Number)b).doubleValue();
    return ((Number)a).intValue() * ((Number)b).intValue();
  }
  private static Object __flx_div(Object a, Object b) {
    if (a instanceof Double || b instanceof Double) return ((Number)a).doubleValue() / ((Number)b).doubleValue();
    return ((Number)a).intValue() / ((Number)b).intValue();
  }
  private static boolean __flx_gt(Object a, Object b) {
    if (a instanceof Number && b instanceof Number) return ((Number)a).doubleValue() > ((Number)b).doubleValue();
    return String.valueOf(a).compareTo(String.valueOf(b)) > 0;
  }
  private static boolean __flx_lt(Object a, Object b) {
    if (a instanceof Number && b instanceof Number) return ((Number)a).doubleValue() < ((Number)b).doubleValue();
    return String.valueOf(a).compareTo(String.valueOf(b)) < 0;
  }
  private static boolean __flx_eq(Object a, Object b) {
    if (a == null) return b == null;
    return a.equals(b);
  }
  private static String __flx_input(Object prompt) {
    if (prompt != null) System.out.print(String.valueOf(prompt));
    try { java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(System.in)); return br.readLine(); } catch (Exception e) { return null; }
  }
  private static String __flx_idx(Object s, Object i) { String str = (String)s; int idx = ((Number)i).intValue(); if (idx < 0) idx = str.length() + idx; return String.valueOf(str.charAt(idx)); }
  private static String __flx_slc(Object s, Object a, Object b) { String str = (String)s; int len = str.length(); int si = a==null?0:((Number)a).intValue(); int ei = b==null?len:((Number)b).intValue(); if (si<0) si = len+si; if (ei<0) ei=len+ei; if (si<0) si=0; if (ei>len) ei=len; if (ei<si) return ""; return str.substring(si, ei); }
  private static Object __flx_cast_int(Object v) {
    if (v == null) throw new RuntimeException("Cannot cast null to int");
    if (v instanceof Number) return Integer.valueOf(((Number)v).intValue());
    if (v instanceof String) {
      String s = ((String)v).trim();
      if (s.isEmpty()) throw new RuntimeException("Cannot cast empty string to int");
      try { return Integer.valueOf(Integer.parseInt(s)); }
      catch (NumberFormatException e) {
        try { return Integer.valueOf((int)Double.parseDouble(s)); }
        catch (NumberFormatException e2) { throw new RuntimeException(String.format("Cannot cast to int: '%s'", s)); }
      }
    }
    throw new RuntimeException("Cannot cast to int: " + v);
  }
  private static Object __flx_cast_double(Object v) {
    if (v == null) throw new RuntimeException("Cannot cast null to double");
    if (v instanceof Number) return Double.valueOf(((Number)v).doubleValue());
    if (v instanceof String) {
      String s = ((String)v).trim();
      if (s.isEmpty()) throw new RuntimeException("Cannot cast empty string to double");
      try { return Double.valueOf(Double.parseDouble(s)); }
      catch (NumberFormatException e) { throw new RuntimeException(String.format("Cannot cast to double: '%s'", s)); }
    }
    throw new RuntimeException("Cannot cast to double: " + v);
  }
  private static Object __flx_cast_bool(Object v) {
    if (v == null) throw new RuntimeException("Cannot cast null to bool");
    if (v instanceof Boolean) return v;
    if (v instanceof Number) return ((Number)v).doubleValue() != 0.0;
    if (v instanceof String) {
      String s = ((String)v).trim().toLowerCase();
      if (s.equals("true") || s.equals("1")) return true;
      if (s.equals("false") || s.equals("0")) return false;
      throw new RuntimeException(String.format("Cannot cast to bool: '%s'", s));
    }
    throw new RuntimeException("Cannot cast to bool: " + v);
  }
  public static void main(String[] args) {
  System.out.println("Welcome to my calculator!");
  Object numberOne = __flx_input("Whats the first number?: ");
  Object numberTwo = __flx_input("Whats the second number?: ");
  numberOne = __flx_cast_int(numberOne);
  numberTwo = __flx_cast_int(numberTwo);
  Object Op = __flx_input("What operation would you like to do? (1: add, 2: sub, 3: mult, 4:div): ");
  Op = __flx_cast_int(Op);
  if ((Boolean) (__flx_eq(Op, Integer.valueOf(1))) )
  {
    System.out.println(__flx_add(numberOne, numberTwo));
  }
  else
  if ((Boolean) (__flx_eq(Op, Integer.valueOf(2))) )
  {
    System.out.println(__flx_sub(numberOne, numberTwo));
  }
  else
  if ((Boolean) (__flx_eq(Op, Integer.valueOf(3))) )
  {
    System.out.println(__flx_mul(numberOne, numberTwo));
  }
  else
  if ((Boolean) (__flx_eq(Op, Integer.valueOf(4))) )
  {
    System.out.println(__flx_div(__flx_mul(numberOne, Double.valueOf(1.0)), numberTwo));
  }
  else
  {
    System.out.println("Invalid Operation");
  }
  }
}
