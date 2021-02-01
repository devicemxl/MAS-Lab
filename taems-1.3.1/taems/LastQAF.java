/* Copyright (C) 2005, University of Massachusetts, Multi-Agent Systems Lab
 * See LICENSE for license information
 */

/************************************************************
 * LastQAF.java
 ************************************************************/

package taems;

/* Global Includes */
import java.util.*;
import java.io.*;
import java.awt.*;

import utilities.Distribution;

/**
 * LastQAF class
 */
public class LastQAF extends QAF implements Serializable, Cloneable {

  /**
   * Default constructor
   */
  public LastQAF() {
    super("q_last");
  }

  /**
   * Uses QAF-specific methods to calculate the actual
   * quality for the task using it.
   * @param e An enumeration of the tasks/methods which 
   * fall under this QAF.
   * @return The quality
   */
    public float calculateQuality(Enumeration e) {
        float q = 0;
        float weight;
        int f = Integer.MIN_VALUE;

        while (e.hasMoreElements()) {
            TaskBase t = (TaskBase)e.nextElement();
            weight = 1;
            if (t.getAttribute("weighting_factor") != null) 
                weight = ((Float)t.getAttribute("weighting_factor")).floatValue();
            float cq = weight * t.getCurrentQuality();
            if (t.getFinishTime() > f) {
                f = t.getFinishTime();
                q = cq;
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

