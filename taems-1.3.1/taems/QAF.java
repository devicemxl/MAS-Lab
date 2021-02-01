/* Copyright (C) 2005, University of Massachusetts, Multi-Agent Systems Lab
 * See LICENSE for license information
 */

/************************************************************
 * QAF.java
 ************************************************************/

package taems;

/* Global Includes */
import java.util.*;
import java.io.*;
import java.awt.*;

import utilities.Distribution;

/**
 * QAF class
 */
public abstract class QAF implements Serializable, Cloneable {
  protected String label = "";
  protected static final Vector qafs = new Vector();

    static {
      QAF.registerQAF(new MinQAF());
      QAF.registerQAF(new MaxQAF());
      QAF.registerQAF(new SumQAF());
      QAF.registerQAF(new SumAllQAF());
      QAF.registerQAF(new ExactlyOneQAF());
      QAF.registerQAF(new LastQAF());
      QAF.registerQAF(new SeqMinQAF());
      QAF.registerQAF(new SeqMaxQAF());
      QAF.registerQAF(new SeqSumQAF());
      QAF.registerQAF(new SeqLastQAF());
    }

  /**
   * Default constructor
   */
  public QAF(String l) {
    label = l;
  }

  /**
   * Accessors
   */
  public String getLabel() { return label; }
  public void setLabel(String l) { label = l; }

    /**
     * Use this to get a list of the available QAFs.
     */
  public static Enumeration getQAFs() { return qafs.elements(); }
  protected static void registerQAF(QAF q) { qafs.addElement(q); }
  public static QAF getQAF(String s) { 
      Enumeration e = getQAFs();
      while (e.hasMoreElements()) {
          QAF q = (QAF)e.nextElement();
          if (q.getLabel().equals(s))
              return q;
      }
      return null;
  }

  /**
   * Uses QAF-specific methods to calculate the actual
   * quality for the task using it.
   * @param e An enumeration of the tasks/methods which * fall under this QAF.
   * @return The quality
   */
  public float calculateQuality(Enumeration e) {
	System.err.println("Warning: QAF " + getLabel() + " is not functional");
	return 0;
  }

  /**
   * Uses QAF-specific methods to calculate the actual
   * maximum quality for the task using it.
   * @param e An enumeration of the tasks/methods which * fall under this QAF.
   * @return The quality
   */
  public float calculateMaximumQuality(Enumeration e) {
	System.err.println("Warning: QAF " + getLabel() + " is not functional");
	return 0;
  }

  /**
   * Determines if an object matches this one.
   * <BR>
   * This matches against:
   * <UL>
   * <LI> Label
   * </UL>
   * @see Node#matches
   */
  public boolean matches(QAF q) {

      if (q != null) {
          if (!Node.matches(q.getLabel(), getLabel())) return false;
      }

      return true;
  }

  /**
   * Clone
   */
  public Object clone() {
    QAF cloned = null;

    try {
      cloned = (QAF)super.clone();
    } catch (Exception e) {System.out.println("Clone Error: " + e);}

    cloned.setLabel(new String(label));

    return cloned;
  }
}
