/* Copyright (C) 2005, University of Massachusetts, Multi-Agent Systems Lab
 * See LICENSE for license information
 */

/************************************************************
 * MinQAF.java
 ************************************************************/

package taems;

/* Global Includes */
import java.util.*;
import java.io.*;
import java.awt.*;

import utilities.Distribution;

/**
 * MinQAF class
 */
public class MinQAF extends QAF implements Serializable, Cloneable {

  /**
   * Default constructor
   */
  public MinQAF() {
    super("q_min");
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
    float weight;
 
    while (e.hasMoreElements()) {
      TaskBase t = (TaskBase)e.nextElement();
      weight = 1;
      if (t.getCurrentQuality() < q) {
	  if (t.getAttribute("weighting_factor") != null) 
	      weight = ((Float)t.getAttribute("weighting_factor")).floatValue();
	  q = (weight * t.getCurrentQuality());
      }
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
      weight = 1;
      if (t.getMaximumQuality() < q) {
	  if (t.getAttribute("weighting_factor") != null) 
	      weight = ((Float)t.getAttribute("weighting_factor")).floatValue();
	  q = (weight * t.getMaximumQuality());
      }
    }

    if (q == Float.MAX_VALUE)
      q = 0;

    return q;
  }
}
