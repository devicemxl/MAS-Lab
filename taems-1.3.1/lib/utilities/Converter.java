package utilities;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

public class Converter {
  public static Object reTypeProperty(String property, String type) {
    Hashtable hashtable;
    Object newp = null;
    String delim = ",";
    if (property instanceof String) {
      if (property.startsWith("DELIM=")) {
        StringTokenizer tmp = new StringTokenizer(property);
        delim = tmp.nextToken();
        property = property.substring(delim.length());
        property = property.trim();
        delim = delim.substring(6);
      } 
      if (type == null || type.equalsIgnoreCase("String")) {
        newp = (Object)property;
      } else if (type.equalsIgnoreCase("Long")) {
        newp = (Object)Long.valueOf(property);
      } else if (type.equalsIgnoreCase("Integer")) {
        newp = (Object)Integer.valueOf(property);
      } else if (type.equalsIgnoreCase("Float")) {
        newp = (Object)Float.valueOf(property);
      } else if (type.equalsIgnoreCase("Double")) {
        newp = (Object)Double.valueOf(property);
      } else if (type.equalsIgnoreCase("Short")) {
        newp = (Object)Short.valueOf(property);
      } else if (type.equalsIgnoreCase("Boolean")) {
        newp = (Object)Boolean.valueOf(property);
      } else if (type.equalsIgnoreCase("Distribution")) {
        StringTokenizer t = new StringTokenizer(property, delim);
        Distribution temp = new Distribution();
        while (t.hasMoreTokens()) {
          try {
            Float v = Float.valueOf(t.nextToken());
            Float d = Float.valueOf(t.nextToken());
            temp.appendTerm(v, d);
          } catch (Exception e) {
            System.err.println(e);
            break;
          } 
        } 
        newp = (Object)temp;
      } else if (type.equalsIgnoreCase("Vector")) {
        StringTokenizer t = new StringTokenizer(property, delim);
        Vector temp = new Vector();
        while (t.hasMoreTokens())
          temp.addElement(t.nextToken().trim()); 
        newp = (Object)temp;
      } else if (type.equalsIgnoreCase("Hashtable")) {
        StringTokenizer t = new StringTokenizer(property, delim);
        Hashtable temp = new Hashtable();
        while (t.hasMoreTokens()) {
          String o, k = t.nextToken().trim();
          if (t.hasMoreTokens()) {
            o = t.nextToken().trim();
          } else {
            o = "Invalid hashtable specification";
          } 
          temp.put(k, o);
        } 
        hashtable = temp;
      } else {
        Log.getDefault().log("Error, unsupported conversion type " + type, 2);
      } 
    } 
    return hashtable;
  }
  
  public static String getPropertyType(Object property) {
    if (property == null)
      return null; 
    String type = null;
    if (property instanceof String || property instanceof Long || property instanceof Integer || property instanceof Float || property instanceof Double || property instanceof Short || property instanceof Boolean || property instanceof Distribution || property instanceof Vector || property instanceof Hashtable) {
      type = property.getClass().getName();
      type = type.substring(type.lastIndexOf(".") + 1);
    } 
    return type;
  }
  
  public static String unTypeProperty(Object property, String delim) {
    String newp = null;
    if (delim == null)
      delim = ","; 
    if (property == null)
      return null; 
    if (property instanceof String || property instanceof Long || property instanceof Integer || property instanceof Float || property instanceof Double || property instanceof Short || property instanceof Boolean) {
      newp = property.toString();
    } else if (property instanceof Distribution) {
      newp = "";
      delim = " ";
      Enumeration e = ((Vector)property).elements();
      while (e.hasMoreElements()) {
        newp = newp + e.nextElement().toString();
        if (e.hasMoreElements())
          newp = newp + delim; 
      } 
    } else if (property instanceof Vector) {
      newp = "DELIM=" + delim + " ";
      Enumeration e = ((Vector)property).elements();
      while (e.hasMoreElements()) {
        newp = newp + e.nextElement().toString();
        if (e.hasMoreElements())
          newp = newp + delim; 
      } 
    } else if (property instanceof Hashtable) {
      newp = "DELIM=" + delim + " ";
      Enumeration e = ((Hashtable)property).keys();
      while (e.hasMoreElements()) {
        Object key = e.nextElement();
        newp = newp + key.toString() + delim + ((Hashtable)property).get(key).toString();
        if (e.hasMoreElements())
          newp = newp + delim; 
      } 
    } else {
      Log.getDefault().log("Error, unsupported conversion type " + property.getClass(), 2);
    } 
    return newp;
  }
}
