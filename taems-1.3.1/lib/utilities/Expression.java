package utilities;

import java.text.ParseException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

public class Expression {
  Object a = null;
  
  Object b = null;
  
  Object op = null;
  
  private int index;
  
  public Object getOperand1() {
    return this.a;
  }
  
  public Object getOperand2() {
    return this.b;
  }
  
  public Object getOperator() {
    return this.op;
  }
  
  public boolean isTrue(Hashtable h) {
    Object o = evaluate(h);
    if (o == null)
      return false; 
    if (o instanceof Boolean)
      return ((Boolean)o).booleanValue(); 
    System.err.println("Warning: Top level expression returned non-boolean value");
    return false;
  }
  
  public boolean isFalse(Hashtable h) {
    return !isTrue(h);
  }
  
  public Object evaluate(Hashtable h) {
    Object ao = null, bo = null;
    ao = convertOperand(this.a, h);
    if (this.op == null)
      return ao; 
    if (this.op.equals("||")) {
      if (ao instanceof Boolean && ((Boolean)ao).booleanValue())
        return new Boolean(true); 
      bo = convertOperand(this.b, h);
      if (bo instanceof Boolean && ((Boolean)bo).booleanValue())
        return new Boolean(true); 
      if (ao != null && !(ao instanceof Boolean)) {
        System.err.println("Error, " + this.op + " has incorrect operand types");
      } else if (bo != null && !(bo instanceof Boolean)) {
        System.err.println("Error, " + this.op + " has incorrect operand types");
      } else {
        return new Boolean(false);
      } 
    } else if (this.op.equals("&&")) {
      if (ao instanceof Boolean && !((Boolean)ao).booleanValue())
        return new Boolean(false); 
      bo = convertOperand(this.b, h);
      if (ao instanceof Boolean && bo instanceof Boolean)
        return new Boolean((((Boolean)ao).booleanValue() && ((Boolean)bo).booleanValue())); 
      if (ao != null && !(ao instanceof Boolean)) {
        System.err.println("Error, " + this.op + " has incorrect operand types");
      } else if (bo != null && !(bo instanceof Boolean)) {
        System.err.println("Error, " + this.op + " has incorrect operand types");
      } else {
        return new Boolean(false);
      } 
    } 
    if (bo == null)
      bo = convertOperand(this.b, h); 
    if (ao instanceof Number && bo instanceof String)
      bo = new Double(bo.hashCode()); 
    if (bo instanceof Number && ao instanceof String)
      ao = new Double(ao.hashCode()); 
    if (this.op.equals("==")) {
      if (ao == null || bo == null)
        return new Boolean((ao == bo)); 
      if (ao instanceof Number && bo instanceof Number) {
        double ad = ((Number)ao).doubleValue();
        double bd = ((Number)bo).doubleValue();
        return new Boolean((ad == bd));
      } 
      return new Boolean(ao.toString().equals(bo.toString()));
    } 
    if (this.op.equals("!=")) {
      if (ao == null || bo == null)
        return new Boolean((ao != bo)); 
      if (ao instanceof Number && bo instanceof Number) {
        double ad = ((Number)ao).doubleValue();
        double bd = ((Number)bo).doubleValue();
        return new Boolean((ad != bd));
      } 
      return new Boolean(!ao.toString().equals(bo.toString()));
    } 
    if (bo != null)
      if (this.op.equals("!")) {
        if (bo instanceof Boolean)
          return new Boolean(!((Boolean)bo).booleanValue()); 
        System.err.println("Error, " + this.op + " has incorrect operand types");
      } else if (ao != null) {
        if (this.op.equals(">")) {
          if (ao instanceof Number && bo instanceof Number) {
            double ad = ((Number)ao).doubleValue();
            double bd = ((Number)bo).doubleValue();
            return new Boolean((ad > bd));
          } 
          System.err.println("Error, " + this.op + " has incorrect operand types");
        } else if (this.op.equals("<")) {
          if (ao instanceof Number && bo instanceof Number) {
            double ad = ((Number)ao).doubleValue();
            double bd = ((Number)bo).doubleValue();
            return new Boolean((ad < bd));
          } 
          System.err.println("Error, " + this.op + " has incorrect operand types");
        } else if (this.op.equals(">=")) {
          if (ao instanceof Number && bo instanceof Number) {
            double ad = ((Number)ao).doubleValue();
            double bd = ((Number)bo).doubleValue();
            return new Boolean((ad >= bd));
          } 
          System.err.println("Error, " + this.op + " has incorrect operand types");
        } else if (this.op.equals("<=")) {
          if (ao instanceof Number && bo instanceof Number) {
            double ad = ((Number)ao).doubleValue();
            double bd = ((Number)bo).doubleValue();
            return new Boolean((ad <= bd));
          } 
          System.err.println("Error, " + this.op + " has incorrect operand types");
        } else if (this.op.equals("+")) {
          if (ao instanceof Number && bo instanceof Number) {
            double ad = ((Number)ao).doubleValue();
            double bd = ((Number)bo).doubleValue();
            return new Double(ad + bd);
          } 
          System.err.println("Error, " + this.op + " has incorrect operand types");
        } else if (this.op.equals("*")) {
          if (ao instanceof Number && bo instanceof Number) {
            double ad = ((Number)ao).doubleValue();
            double bd = ((Number)bo).doubleValue();
            return new Double(ad * bd);
          } 
          System.err.println("Error, " + this.op + " has incorrect operand types");
        } else if (this.op.equals("-")) {
          if (ao instanceof Number && bo instanceof Number) {
            double ad = ((Number)ao).doubleValue();
            double bd = ((Number)bo).doubleValue();
            return new Double(ad - bd);
          } 
          System.err.println("Error, " + this.op + " has incorrect operand types");
        } else if (this.op.equals("/")) {
          if (ao instanceof Number && bo instanceof Number) {
            double ad = ((Number)ao).doubleValue();
            double bd = ((Number)bo).doubleValue();
            return new Double(ad / bd);
          } 
          System.err.println("Error, " + this.op + " has incorrect operand types");
        } else if (this.op.equals("%")) {
          if (ao instanceof Number && bo instanceof Number) {
            double ad = ((Number)ao).doubleValue();
            double bd = ((Number)bo).doubleValue();
            return new Double(ad % bd);
          } 
          System.err.println("Error, " + this.op + " has incorrect operand types");
        } 
      }  
    return null;
  }
  
  protected Object convertOperand(Object o, Hashtable h) {
    Object r;
    if (o instanceof Expression) {
      r = ((Expression)o).evaluate(h);
    } else if (o instanceof String) {
      String s = (String)o;
      if (s.charAt(0) == '"') {
        r = s.substring(1, s.length() - 1);
      } else if (s.equalsIgnoreCase("null")) {
        r = null;
      } else {
        r = h.get(s);
      } 
      if (r instanceof String)
        try {
          Double d = Double.valueOf((String)r);
          r = d;
        } catch (Exception e) {} 
    } else {
      r = o;
    } 
    return r;
  }
  
  protected void parseExpression(Vector v) throws ParseException {
    boolean neg = false;
    String t = nextToken(v);
    if (t == null)
      throw new ParseException("Unexpected null token", this.index); 
    if (t.equals("(")) {
      this.a = new Expression(v);
      t = nextToken(v);
      if (t == null)
        throw new ParseException("Unexpected null token", this.index); 
      if (!t.equals(")"))
        throw new ParseException("Token " + t + " is out of place", this.index); 
    } else if (OperatorTest.isSign(t)) {
      v.insertElementAt(t, 0);
      v.insertElementAt("0", 0);
      this.a = new Expression(v);
    } else if (t.charAt(0) == '"') {
      this.a = t;
    } else if (Character.isDigit(t.charAt(0))) {
      this.a = Double.valueOf(t);
    } else if (Character.isLetter(t.charAt(0)) || t.charAt(0) == '_') {
      this.a = t;
    } else if (t.charAt(0) == '!') {
      this.op = t;
    } else {
      throw new ParseException("Token " + t + " is out of place", this.index);
    } 
    if (v.isEmpty())
      return; 
    if (this.op == null) {
      t = nextToken(v);
      if (t == null)
        throw new ParseException("Unexpected null token", this.index); 
      if (t.equals(")"))
        return; 
      if (OperatorTest.isOperator(t)) {
        this.op = t;
      } else {
        throw new ParseException("Token " + t + " is out of place", this.index);
      } 
    } 
    t = nextToken(v);
    if (t == null)
      throw new ParseException("Unexpected null token", this.index); 
    if (t.equals("(")) {
      this.b = new Expression(v);
      t = nextToken(v);
      if (t == null)
        throw new ParseException("Unexpected null token", this.index); 
      if (!t.equals(")"))
        throw new ParseException("Token " + t + " is out of place", this.index); 
    } else if (OperatorTest.isSign(t)) {
      v.insertElementAt(t, 0);
      v.insertElementAt("0", 0);
      this.b = new Expression(v);
    } else if (t.charAt(0) == '"') {
      this.b = t;
    } else if (Character.isDigit(t.charAt(0))) {
      this.b = Double.valueOf(t);
    } else if (Character.isLetter(t.charAt(0)) || t.charAt(0) == '_') {
      this.b = t;
    } else {
      throw new ParseException("Token " + t + " is out of place", this.index);
    } 
  }
  
  public Expression(String s) throws ParseException {
    this.index = 0;
    Vector v = new Vector();
    StringTokenizer e = new StringTokenizer(s, " \t\r\n\"()&!|=<>-+*/%", true);
    while (e.hasMoreElements()) {
      String t = e.nextToken();
      v.addElement(t);
    } 
    try {
      parseExpression(v);
    } catch (ParseException ex) {
      throw new ParseException(ex.getMessage() + " \"" + s + "\"", ex.getErrorOffset());
    } 
  }
  
  protected Expression(Vector v) throws ParseException {
    this.index = 0;
    parseExpression(v);
  }
  
  protected String nextToken(Vector v) {
    String t = null;
    while (!v.isEmpty()) {
      String s = v.firstElement();
      v.removeElementAt(0);
      this.index += s.length();
      if (s.equals(" ") || 
        s.equals("\t") || 
        s.equals("\n") || 
        s.equals("\r"))
        continue; 
      if (s.equals("\"")) {
        t = s;
        while (!v.isEmpty()) {
          s = v.firstElement();
          v.removeElementAt(0);
          this.index += s.length();
          t = t + s;
          if (s.equals("\""))
            break; 
        } 
        break;
      } 
      if (s.equals("=")) {
        t = s;
        s = v.firstElement();
        if (s.equals("=")) {
          v.removeElementAt(0);
          this.index += s.length();
          t = t + s;
          break;
        } 
        System.err.println("Error: Unknown token \"" + t + "\"");
        break;
      } 
      if (s.equals("|")) {
        t = s;
        s = v.firstElement();
        if (s.equals("|")) {
          v.removeElementAt(0);
          this.index += s.length();
          t = t + s;
          break;
        } 
        System.err.println("Error: Unknown token \"" + t + "\"");
        break;
      } 
      if (s.equals("&")) {
        t = s;
        s = v.firstElement();
        if (s.equals("&")) {
          v.removeElementAt(0);
          this.index += s.length();
          t = t + s;
          break;
        } 
        System.err.println("Error: Unknown token \"" + t + "\"");
        break;
      } 
      if (s.equals("!")) {
        t = s;
        s = v.firstElement();
        if (s.equals("=")) {
          v.removeElementAt(0);
          this.index += s.length();
          t = t + s;
        } 
        break;
      } 
      if (s.equals(">")) {
        t = s;
        s = v.firstElement();
        if (s.equals("=")) {
          v.removeElementAt(0);
          this.index += s.length();
          t = t + s;
        } 
        break;
      } 
      if (s.equals("<")) {
        t = s;
        s = v.firstElement();
        if (s.equals("=")) {
          v.removeElementAt(0);
          this.index += s.length();
          t = t + s;
        } 
        break;
      } 
      if (s.endsWith("E") && Character.isDigit(s.charAt(0))) {
        t = s;
        s = v.firstElement();
        if (s.equals("-") || s.equals("+")) {
          v.removeElementAt(0);
          this.index += s.length();
          t = t + s;
          s = v.firstElement();
          if (Character.isDigit(s.charAt(0))) {
            v.removeElementAt(0);
            this.index += s.length();
            t = t + s;
            break;
          } 
          System.err.println("Error: Unknown token \"" + t + "\"");
          break;
        } 
        System.err.println("Error: Unknown token \"" + t + "\"");
        break;
      } 
      t = s;
    } 
    return t;
  }
  
  public Hashtable generateLinearForm() {
    Hashtable atable;
    Hashtable btable;
    if (isBooleanExpression())
      return null; 
    if (this.a == null || this.b == null)
      return null; 
    if (this.a instanceof Expression) {
      atable = ((Expression)this.a).generateLinearForm();
      if (atable == null)
        return null; 
    } else {
      Object ao = simplifyOperand(this.a);
      if (ao == null)
        return null; 
      atable = new Hashtable();
      if (ao instanceof String) {
        atable.put(ao, new Double(1.0D));
      } else if (ao instanceof Double) {
        atable.put("CONSTANT", ao);
      } else {
        return null;
      } 
    } 
    if (this.b instanceof Expression) {
      btable = ((Expression)this.b).generateLinearForm();
      if (btable == null)
        return null; 
    } else {
      Object bo = simplifyOperand(this.b);
      if (bo == null)
        return null; 
      btable = new Hashtable();
      if (bo instanceof String) {
        btable.put(bo, new Double(1.0D));
      } else if (bo instanceof Double) {
        btable.put("CONSTANT", bo);
      } else {
        return null;
      } 
    } 
    if (this.op.equals("+")) {
      Enumeration e = btable.keys();
      while (e.hasMoreElements()) {
        String key = e.nextElement();
        if (atable.containsKey(key)) {
          Double ad = (Double)atable.get(key);
          Double bd = (Double)btable.get(key);
          Double rd = new Double(ad.doubleValue() + bd.doubleValue());
          atable.put(key, rd);
          continue;
        } 
        atable.put(key, btable.get(key));
      } 
    } else if (this.op.equals("-") || isComparisonExpression()) {
      Enumeration e = btable.keys();
      while (e.hasMoreElements()) {
        String key = e.nextElement();
        if (atable.containsKey(key)) {
          Double ad = (Double)atable.get(key);
          Double double_1 = (Double)btable.get(key);
          Double double_2 = new Double(ad.doubleValue() - double_1.doubleValue());
          atable.put(key, double_2);
          continue;
        } 
        Double bd = (Double)btable.get(key);
        Double rd = new Double(-1.0D * bd.doubleValue());
        atable.put(key, rd);
      } 
    } else if (this.op.equals("*") || this.op.equals("/")) {
      if (btable.size() == 1 && btable.containsKey("CONSTANT")) {
        Double bd = (Double)btable.get("CONSTANT");
        Enumeration e = new SafeEnumeration(atable.keys());
        while (e.hasMoreElements()) {
          Double rd;
          String key = e.nextElement();
          Double ad = (Double)atable.get(key);
          if (this.op.equals("*")) {
            rd = new Double(ad.doubleValue() * bd.doubleValue());
          } else {
            rd = new Double(ad.doubleValue() / bd.doubleValue());
          } 
          atable.put(key, rd);
        } 
      } else if (atable.size() == 1 && atable.containsKey("CONSTANT")) {
        Double ad = (Double)atable.get("CONSTANT");
        Enumeration e = new SafeEnumeration(btable.keys());
        while (e.hasMoreElements()) {
          Double rd;
          String key = e.nextElement();
          Double bd = (Double)btable.get(key);
          if (this.op.equals("*")) {
            rd = new Double(ad.doubleValue() * bd.doubleValue());
          } else {
            rd = new Double(ad.doubleValue() / bd.doubleValue());
          } 
          btable.put(key, rd);
        } 
        atable = btable;
      } else {
        return null;
      } 
    } else if (this.op.equals("%")) {
      return null;
    } 
    if (isComparisonExpression())
      atable.put("OPERATOR", this.op); 
    return atable;
  }
  
  protected Object simplifyOperand(Object o) {
    Object r = null;
    boolean var = false;
    if (o instanceof String) {
      String s = (String)o;
      if (s.charAt(0) == '"') {
        r = s.substring(1, s.length() - 1);
      } else if (s.equalsIgnoreCase("null")) {
        r = "null";
      } else {
        r = s;
        var = true;
      } 
      if (r == null)
        r = "null"; 
      try {
        Double d = Double.valueOf((String)r);
        r = d;
      } catch (Exception e) {
        if (!var)
          r = new Double(o.hashCode()); 
      } 
    } else if (o instanceof Number) {
      r = new Double(((Number)o).doubleValue());
    } 
    return r;
  }
  
  public boolean isSatisfiable() {
    try {
      Expression exp = new Expression(toString());
      Hashtable comparisons = new Hashtable();
      System.err.println(exp);
      exp.replaceComparisons(comparisons);
      System.err.println(exp);
      Enumeration e = comparisons.keys();
      while (e.hasMoreElements()) {
        String k = e.nextElement();
        Expression cexp = (Expression)comparisons.get(k);
        System.err.println("\t" + k + " -> " + cexp);
        System.err.println("\t\t" + cexp.generateLinearForm());
      } 
      Vector variables = new Vector();
      e = exp.findVariables();
      while (e.hasMoreElements())
        variables.addElement(e.nextElement()); 
      Vector satisfies = new Vector();
      boolean[] vals = new boolean[variables.size()];
      int[] mods = new int[variables.size()];
      int num = (int)Math.pow(2.0D, variables.size());
      for (int j = 0; j < variables.size(); j++) {
        vals[j] = false;
        mods[j] = (int)Math.pow(2.0D, j);
      } 
      for (int i = 0; i < num; i++) {
        Hashtable hash = new Hashtable();
        for (int k = 0; k < variables.size(); k++) {
          if (i % mods[k] == 0)
            vals[k] = !vals[k]; 
          hash.put(variables.elementAt(k), new Boolean(vals[k]));
        } 
        if (exp.isTrue(hash))
          satisfies.addElement(hash); 
      } 
      e = (Enumeration)satisfies.elements();
      while (e.hasMoreElements())
        System.err.println(e.nextElement()); 
    } catch (ParseException ex) {
      System.err.println("Error parsing expression: " + ex.toString());
    } 
    return false;
  }
  
  public void replaceComparisons(Hashtable h) {
    int variable = 0;
    if (OperandTest.isExpression(this.a))
      if (((Expression)this.a).isComparisonExpression()) {
        while (h.containsKey("__" + variable))
          variable++; 
        h.put("__" + variable, this.a);
        this.a = "__" + variable;
      } else {
        ((Expression)this.a).replaceComparisons(h);
      }  
    if (OperandTest.isExpression(this.b))
      if (((Expression)this.b).isComparisonExpression()) {
        while (h.containsKey("__" + variable))
          variable++; 
        h.put("__" + variable, this.b);
        this.b = "__" + variable;
      } else {
        ((Expression)this.b).replaceComparisons(h);
      }  
  }
  
  public Enumeration findVariables() {
    Hashtable h = new Hashtable();
    if (OperandTest.isVariable(this.a)) {
      h.put(this.a, new Boolean(true));
    } else if (OperandTest.isExpression(this.a)) {
      Enumeration e = ((Expression)this.a).findVariables();
      while (e.hasMoreElements())
        h.put(e.nextElement(), new Boolean(true)); 
    } 
    if (OperandTest.isVariable(this.b)) {
      h.put(this.b, new Boolean(true));
    } else if (OperandTest.isExpression(this.b)) {
      Enumeration e = ((Expression)this.b).findVariables();
      while (e.hasMoreElements())
        h.put(e.nextElement(), new Boolean(true)); 
    } 
    return h.keys();
  }
  
  public boolean isBooleanExpression() {
    if (this.op == null && 
      convertOperand(this.a, new Hashtable()) instanceof Boolean)
      return true; 
    return OperatorTest.isBoolean(this.op);
  }
  
  public boolean isComparisonExpression() {
    return OperatorTest.isComparison(this.op);
  }
  
  public boolean isMathematicalExpression() {
    return OperatorTest.isMathematical(this.op);
  }
  
  public String toString() {
    if (this.op == null)
      return this.a.toString(); 
    if (this.a == null)
      return "(" + this.op + " " + this.b + ")"; 
    return "(" + this.a + " " + this.op + " " + this.b + ")";
  }
  
  public static void main(String[] argv) {
    Hashtable h = new Hashtable();
    h.put("a", "a");
    h.put("b", "b");
    h.put("c", "c");
    h.put("n1", "1");
    h.put("n2", "2");
    h.put("n3", "3");
    h.put("d", "abba gold");
    h.put("Y", "16");
    h.put("X", "64");
    h.put("R", "20");
    try {
      Expression e = new Expression("(a == \"a\")");
      System.err.println(e.toString() + " -> " + e.isTrue(h));
      e = new Expression("(b == \"a\")");
      System.err.println(e.toString() + " -> " + e.isTrue(h));
      e = new Expression("((b == \"a\") || (a != \"a\"))");
      System.err.println(e.toString() + " -> " + e.isTrue(h));
      e = new Expression("((((b != \"a\") && (a != \"b\")) && (c == c)) && (d == \"abba gold\"))");
      System.err.println(e.toString() + " -> " + e.isTrue(h));
      e.isSatisfiable();
      e = new Expression("(((1 == n1) && (n1 == 1)) && (n1 != n2))");
      System.err.println(e.toString() + " -> " + e.isTrue(h));
      e.isSatisfiable();
      e = new Expression("(1 == n2)");
      System.err.println(e.toString() + " -> " + e.isTrue(h));
      e = new Expression("(1 != n2)");
      System.err.println(e.toString() + " -> " + e.isTrue(h));
      e = new Expression("(1 < n2)");
      System.err.println(e.toString() + " -> " + e.isTrue(h));
      e = new Expression("(1 <= n2)");
      System.err.println(e.toString() + " -> " + e.isTrue(h));
      e = new Expression("(1 <= n1)");
      System.err.println(e.toString() + " -> " + e.isTrue(h));
      e = new Expression("(1 > n2)");
      System.err.println(e.toString() + " -> " + e.isTrue(h));
      e = new Expression("(1 >= n2)");
      System.err.println(e.toString() + " -> " + e.isTrue(h));
      e = new Expression("(2 >= n2)");
      System.err.println(e.toString() + " -> " + e.isTrue(h));
      e.isSatisfiable();
      e = new Expression("((a == \"a\") && ((n1 >= 0) && ((n1 < 5) || (n1 < 0))))");
      System.err.println(e.toString() + " -> " + e.isTrue(h));
      e.isSatisfiable();
      e = new Expression("((b == \"a\") && ((n1 >= 0) && (n1 < 5)))");
      System.err.println(e.toString() + " -> " + e.isTrue(h));
      e = new Expression("(fake == null)");
      System.err.println(e.toString() + " -> " + e.isTrue(h));
      e = new Expression("(fake != NULL)");
      System.err.println(e.toString() + " -> " + e.isTrue(h));
      e = new Expression("(!(a == NuLl))");
      System.err.println(e.toString() + " -> " + e.isTrue(h));
      e.isSatisfiable();
      e = new Expression("((n1 + 5) > 5)");
      System.err.println(e.toString() + " -> " + e.isTrue(h));
      e = new Expression("((n1 / 5) > 5)");
      System.err.println(e.toString() + " -> " + e.isTrue(h));
      e = new Expression("(((n1 + n2) - n3) == 0)");
      System.err.println(e.toString() + " -> " + e.isTrue(h));
      e.isSatisfiable();
      e = new Expression("((n3 % n2) == n1)");
      System.err.println(e.toString() + " -> " + e.isTrue(h));
      e = new Expression("(_n3s == null)");
      System.err.println(e.toString() + " -> " + e.isTrue(h));
      Enumeration en = e.findVariables();
      while (en.hasMoreElements())
        System.err.println("\t" + en.nextElement()); 
      e = new Expression("(3 + 2)");
      System.err.println(((Expression)e.getOperand1()).toString() + " -> " + ((Expression)e.getOperand1()).generateLinearForm());
      e = new Expression("(a == 3)");
      System.err.println(((Expression)e.getOperand1()).toString() + " -> " + ((Expression)e.getOperand1()).generateLinearForm());
      e = new Expression("((3 + 2) * a)");
      System.err.println(((Expression)e.getOperand1()).toString() + " -> " + ((Expression)e.getOperand1()).generateLinearForm());
      e = new Expression("((((a + (2 - b)) * 5) - (d + 6)) == (5 * (4 + d)))");
      System.err.println(((Expression)e.getOperand1()).toString() + " -> " + ((Expression)e.getOperand1()).generateLinearForm());
      e = new Expression("(((((21.0 + R) >= X) && ((21.0 - R) <= X)) && ((24.0 + R) >= Y)) && ((24.0 - R) <= Y))");
      System.err.println(e.toString() + " -> " + e.isTrue(h));
      e = new Expression("(-21.0 < (-21 --1))");
      System.err.println(e.toString() + " -> " + e.isTrue(h));
      e = new Expression("(-n1 == (+0 +-1))");
      System.err.println(e.toString() + " -> " + e.isTrue(h));
      e = new Expression("(3.0E-1 == 0.3)");
      System.err.println(e.toString() + " -> " + e.isTrue(h));
      e = new Expression("(3.2E-5 == 0.3)");
      System.err.println(e.toString() + " -> " + e.isTrue(h));
      e = new Expression("(3.2E-5 == 0.32E-4)");
      System.err.println(e.toString() + " -> " + e.isTrue(h));
      e = new Expression("(3.2E+1 == 320.0E-1)");
      System.err.println(e.toString() + " -> " + e.isTrue(h));
    } catch (ParseException ex) {
      System.err.println(ex.toString());
    } 
  }
}
