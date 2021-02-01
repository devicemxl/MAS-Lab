/* Copyright (C) 2005, University of Massachusetts, Multi-Agent Systems Lab
 * See LICENSE for license information
 */

/************************************************************
 * SortedEnumerationByTimeStamp.java
 ************************************************************/

package taems;

/* Global imports */
import java.awt.*;
import java.io.*;
import java.util.*;

import taems.*;

/**
 * Sorted a vector of enumerations
 */
class SortedEnumerationByTimeStamp implements Enumeration {
  protected Vector enums = new Vector();
  protected Enumeration e;

  public SortedEnumerationByTimeStamp(Enumeration ens) {
    Interrelationship rel;

    while (ens.hasMoreElements()) {
      enums.addElement(ens.nextElement());
    }
    
    if (enums.size() > 1) {
      // sorting by duration
      for (int k = 0; k < enums.size()-1; k++) {
        int enumr = k;
        rel = (Interrelationship)enums.elementAt(enumr);
        for (int j = k; j < enums.size(); j++) {
          if (((Interrelationship)enums.elementAt(j)).getTimeStamp()
              < rel.getTimeStamp()) {
            enumr = j;
            rel = (Interrelationship)enums.elementAt(enumr);
          }
        }
        if (k != enumr) { // swap
          enums.setElementAt(enums.elementAt(k), enumr);
          enums.setElementAt(rel, k);
        }
      }
    }
    e = enums.elements();
  }

  public boolean hasMoreElements() {
    return (e.hasMoreElements());
  }
  
  public Object nextElement() {
    if (e != null)
      return e.nextElement();
    else
      return null;
  }
}

  
