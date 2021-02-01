/* Copyright (C) 2005, University of Massachusetts, Multi-Agent Systems Lab
 * See LICENSE for license information
 */

/************************************************************
 * MaxQAF.java
 ************************************************************/

package taems;

/* Global Includes */
import java.util.*;
import java.io.*;
import java.awt.*;

import utilities.Distribution;

/**
 * MaxQAF class
 */
public class MaxQAF extends QAF implements Serializable, Cloneable {

  /**
   * Default constructor
   */
  public MaxQAF() {
    super("q_max");
  }

  /**
   * Uses QAF-specific methods to calculate the actual
   * quality for the task using it.
   * @param e An enumeration of the tasks/methods which 
   * fall under this QAF.
   * @return The quality
   */
  public float calculateQuality(Enumeration e) {
    float q = (float)0;
    float weight;

    while (e.hasMoreElements()) {
      TaskBase t = (TaskBase)e.nextElement();
      weight = 1;
      if (t.getCurrentQuality() > q) {
	  if (t.getAttribute("weighting_factor") != null) 
	      weight = ((Float)t.getAttribute("weighting_factor")).floatValue();
	q = (weight * t.getCurrentQuality());
      }
    }

    return q;
  }

   /**
   * Uses QAF-specific methods to calculate the maximum
   * quality for the task using it.
   * @param e An enumeration of the tasks/methods which 
   * fall under this QAF.
   * @return The maximum quality
   */
  public float calculateMaximumQuality(Enumeration e) {
    float q = (float)0;
    float weight;
    
    while (e.hasMoreElements()) {
      TaskBase t = (TaskBase)e.nextElement();
      weight = 1;
      if (t.getMaximumQuality() > q) {
	  if (t.getAttribute("weighting_factor") != null) 
	      weight = ((Float)t.getAttribute("weighting_factor")).floatValue();
	  q = (weight * t.getMaximumQuality());
      }
    }
    
    return q;
  }
}
