public class FluxProgram {
  private static Object add(Object a, Object b) {
    if (a instanceof String || b instanceof String) return String.valueOf(a) + String.valueOf(b);
    if (a instanceof Double || b instanceof Double) return ((Number)a).doubleValue() + ((Number)b).doubleValue();
    return ((Number)a).intValue() + ((Number)b).intValue();
  }
  private static Object sub(Object a, Object b) {
    if (a instanceof Double || b instanceof Double) return ((Number)a).doubleValue() - ((Number)b).doubleValue();
    return ((Number)a).intValue() - ((Number)b).intValue();
  }
  private static Object mul(Object a, Object b) {
    if (a instanceof Double || b instanceof Double) return ((Number)a).doubleValue() * ((Number)b).doubleValue();
    return ((Number)a).intValue() * ((Number)b).intValue();
  }
  private static Object div(Object a, Object b) {
    if (a instanceof Double || b instanceof Double) return ((Number)a).doubleValue() / ((Number)b).doubleValue();
    return ((Number)a).intValue() / ((Number)b).intValue();
  }
  private static boolean gt(Object a, Object b) {
    if (a instanceof Number && b instanceof Number) return ((Number)a).doubleValue() > ((Number)b).doubleValue();
    return String.valueOf(a).compareTo(String.valueOf(b)) > 0;
  }
  private static boolean lt(Object a, Object b) {
    if (a instanceof Number && b instanceof Number) return ((Number)a).doubleValue() < ((Number)b).doubleValue();
    return String.valueOf(a).compareTo(String.valueOf(b)) < 0;
  }
  private static boolean eq(Object a, Object b) {
    if (a == null) return b == null;
    return a.equals(b);
  }
  private static String input(Object prompt) {
    if (prompt != null) System.out.print(String.valueOf(prompt));
    try { java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(System.in)); return br.readLine(); } catch (Exception e) { return null; }
  }
  private static String idx(Object s, Object i) { String str = (String)s; int idx = ((Number)i).intValue(); if (idx < 0) idx = str.length() + idx; return String.valueOf(str.charAt(idx)); }
  private static String slc(Object s, Object a, Object b) { String str = (String)s; int len = str.length(); int si = a==null?0:((Number)a).intValue(); int ei = b==null?len:((Number)b).intValue(); if (si<0) si = len+si; if (ei<0) ei=len+ei; if (si<0) si=0; if (ei>len) ei=len; if (ei<si) return ""; return str.substring(si, ei); }
  public static void main(String[] args) {
  Object a = input("test");
  System.out.println(a);
  }
}
