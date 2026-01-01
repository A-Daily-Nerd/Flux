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
  private static Object add(Object a, Object b) {
    return __flx_add(a, b);
  }
  public static void main(String[] args) {
  Object name = __flx_input("Enter your name: ");
  System.out.println(__flx_add("Hello, ", name));
  Object i = Integer.valueOf(0);
  while ((Boolean) (__flx_lt(i, Integer.valueOf(3))) )
  {
    System.out.println(__flx_add("i = ", i));
    i = __flx_add(i, Integer.valueOf(1));
  }
  Object s = "Hello, Flux!";
  System.out.println(__flx_add("s[0] = ", __flx_idx(s, Integer.valueOf(0))));
  System.out.println(__flx_add("s[1:5] = ", __flx_slc(s, Integer.valueOf(1), Integer.valueOf(5))));
  Object v = add(Integer.valueOf(2), Double.valueOf(3.5));
  System.out.println(__flx_add("add(2, 3.5) = ", v));
  Object n = Integer.valueOf(5);
  Object d = __flx_div(Double.valueOf(((Number)n).doubleValue()), Integer.valueOf(2));
  System.out.println(__flx_add("5 / 2 = ", d));
  }
}
