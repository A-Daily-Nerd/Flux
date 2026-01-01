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
  private static Object foot_to_meter = Double.valueOf(0.3048);
  private static Object inch_to_cm = Double.valueOf(2.54);
  private static Object kg_to_pound = Double.valueOf(2.20462);
  private static Object running = Boolean.TRUE;
  private static Object feet_to_meters(Object f) {
    return __flx_mul(f, foot_to_meter);
  }
  private static Object meters_to_feet(Object m) {
    return __flx_div(m, foot_to_meter);
  }
  private static Object inches_to_cm(Object i) {
    return __flx_mul(i, inch_to_cm);
  }
  private static Object cm_to_inches(Object c) {
    return __flx_div(c, inch_to_cm);
  }
  private static Object kg_to_pounds(Object kg) {
    return __flx_mul(kg, kg_to_pound);
  }
  private static Object pounds_to_kg(Object lb) {
    return __flx_div(lb, kg_to_pound);
  }
  private static Object celsius_to_fahrenheit(Object c) {
    return __flx_add(__flx_div(__flx_mul(c, Double.valueOf(9.0)), Double.valueOf(5.0)), Double.valueOf(32.0));
  }
  private static Object fahrenheit_to_celsius(Object f) {
    return __flx_div(__flx_mul(__flx_sub(f, Double.valueOf(32.0)), Double.valueOf(5.0)), Double.valueOf(9.0));
  }
  public static void main(String[] args) {
  while ((Boolean) (running) )
  {
    System.out.println("Choose conversion: F2M, M2F, I2CM, CM2I, KG2LB, LB2KG, C2F, F2C, or EXIT:");
    Object choice = __flx_input("> ");
    if ((Boolean) (__flx_eq(choice, "EXIT")) )
    {
      running = Boolean.FALSE;
    }
    else
    {
      Object value = __flx_input("Enter value: ");
      Object num = __flx_cast_double(value);
      if ((Boolean) (__flx_eq(choice, "F2M")) )
      {
        System.out.println(__flx_add("Result: ", feet_to_meters(num)));
      }
      else
      if ((Boolean) (__flx_eq(choice, "M2F")) )
      {
        System.out.println(__flx_add("Result: ", meters_to_feet(num)));
      }
      else
      if ((Boolean) (__flx_eq(choice, "I2CM")) )
      {
        System.out.println(__flx_add("Result: ", inches_to_cm(num)));
      }
      else
      if ((Boolean) (__flx_eq(choice, "CM2I")) )
      {
        System.out.println(__flx_add("Result: ", cm_to_inches(num)));
      }
      else
      if ((Boolean) (__flx_eq(choice, "KG2LB")) )
      {
        System.out.println(__flx_add("Result: ", kg_to_pounds(num)));
      }
      else
      if ((Boolean) (__flx_eq(choice, "LB2KG")) )
      {
        System.out.println(__flx_add("Result: ", pounds_to_kg(num)));
      }
      else
      if ((Boolean) (__flx_eq(choice, "C2F")) )
      {
        System.out.println(__flx_add("Result: ", celsius_to_fahrenheit(num)));
      }
      else
      if ((Boolean) (__flx_eq(choice, "F2C")) )
      {
        System.out.println(__flx_add("Result: ", fahrenheit_to_celsius(num)));
      }
      else
      {
        System.out.println("Unknown choice!");
      }
    }
  }
  }
}
