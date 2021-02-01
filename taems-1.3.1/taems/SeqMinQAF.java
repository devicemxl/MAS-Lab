/* Copyright (C) 2005, University of Massachusetts, Multi-Agent Systems Lab
 * See LICENSE for license information
 */

/************************************************************
 * SeqMinQAF.java
 ************************************************************/

package taems;

/* Global Includes */
import java.util.*;
import java.io.*;
import java.awt.*;

import utilities.Distribution;

/**
 * SeqMinQAF class
 */
public class SeqMinQAF extends QAF implements Serializable, Cloneable {

  /**
   * Default constructor
   */
  public SeqMinQAF() {
    super("q_seq_min");
  }

  /**
   * Uses QAF-specific methods to calculate the actual
   * quality for the task using it.
   * @param e An enumeration of the tasks/methods which 
   * fall under this QAF.
   * @return The quality
   */
  public float calculateQuality(Enumeration e) {
    float q = (float)Float.MAX_VALUE;
    int lf = Integer.MIN_VALUE;
    float weight;

    while (e.hasMoreElements()) {
      TaskBase t = (TaskBase)e.nextElement();
      if (!t.hasFinished()) {
          q = (float)0;
          break;
      }
      if (lf > t.getStartTime()) {
	q = (float)0;
	break;
      }
      lf = t.getFinishTime();
      weight = 1;
      if (t.getAttribute("weighting_factor") != null) 
	  weight = ((Float)t.getAttribute("weighting_factor")).floatValue();
      float cq = weight * t.getCurrentQuality();
      if (cq < q)
	q = cq;
    }

    if (q == Float.MAX_VALUE)
      q = 0;

    return q;
  }

  /**
   * Uses QAF-specific methods to calculate the minimum
   * quality for the task using it.
   * @param e An enumeration of the tasks/methods which 
   * fall under this QAF.
   * @return The minimum quality
   */
  public float calculateMaximumQuality(Enumeration e) {
    float q = (float)Float.MAX_VALUE;
    float weight;

    while (e.hasMoreElements()) {
      TaskBase t = (TaskBase)e.nextElement();
      weight=1;
      if (t.getMaximumQuality() < q) {
	  if (t.getAttribute("weighting_factor") != null) 
	      weight = ((Float)t.getAttribute("weighting_factor")).floatValue();
	  q = weight * t.getMaximumQuality();
      }
    }

    if (q == Float.MAX_VALUE)
      q = 0;

    return q;
  }

}

