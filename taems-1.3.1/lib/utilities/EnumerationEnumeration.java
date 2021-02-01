package utilities;

import java.util.Enumeration;
import java.util.Vector;

public class EnumerationEnumeration implements Enumeration {
  Vector enums = new Vector();
  
  Enumeration e;
  
  public EnumerationEnumeration(Vector ens) {
    this.enums = ens;
    prime();
  }
  
  public EnumerationEnumeration(Vector ens, boolean uniq) {
    if (uniq) {
      Vector v = new Vector();
      Enumeration t1 = ens.elements();
      while (t1.hasMoreElements()) {
        Enumeration t2 = t1.nextElement();
        while (t2.hasMoreElements()) {
          Object o = t2.nextElement();
          if (!v.contains(o))
            v.addElement(o); 
        } 
      } 
      this.enums = new Vector();
      this.enums.addElement(v.elements());
    } else {
      this.enums = ens;
    } 
    prime();
  }
  
  private void prime() {
    while (this.e == null || !this.e.hasMoreElements()) {
      if (this.enums.isEmpty()) {
        this.e = null;
        return;
      } 
      this.e = this.enums.firstElement();
      this.enums.removeElement(this.e);
    } 
  }
  
  public boolean hasMoreElements() {
    prime();
    return (this.e != null);
  }
  
  public Object nextElement() {
    prime();
    if (this.e != null)
      return this.e.nextElement(); 
    return null;
  }
}
