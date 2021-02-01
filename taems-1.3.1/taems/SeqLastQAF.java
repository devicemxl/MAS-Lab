/* Copyright (C) 2005, University of Massachusetts, Multi-Agent Systems Lab
 * See LICENSE for license information
 */

/************************************************************
 * SeqLastQAF.java
 ************************************************************/

package taems;

/* Global Includes */
import java.util.*;
import java.io.*;
import java.awt.*;

import utilities.Distribution;

/**
 * SeqLastQAF class
 */
public class SeqLastQAF extends QAF implements Serializable, Cloneable {

  /**
   * Default constructor
   */
  public SeqLastQAF() {
    super("q_seq_last");
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
            q = cq;
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

      if (t.getAttribute("weighting_factor") != null) 
          weight = ((Float)t.getAttribute("weighting_factor")).floatValue();
      q = (weight * t.getMaximumQuality());
    }
    
    return q;
  }
}

