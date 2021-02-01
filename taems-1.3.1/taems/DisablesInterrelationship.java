/* Copyright (C) 2005, University of Massachusetts, Multi-Agent Systems Lab
 * See LICENSE for license information
 */

/************************************************************
 * DisablesInterrelationship.java
 ************************************************************/

package taems;

/* Global Includes */
import java.util.*;
import java.io.*;

import utilities.Distribution;
import utilities.TaemsRandom;

/**
 * DisablesInterrelationship
 */
public class DisablesInterrelationship extends Interrelationship implements Serializable, Cloneable {

  /**
   * Constructor
   */
  public DisablesInterrelationship(String l, Agent a, Distribution delay) {
    super(l, a, null, null, null, delay);
  }

  /**
   * Blank Constructor
   */
  public DisablesInterrelationship() {
    super();
  }

   /**
   * This is the function you have to specify in the different subclasses
   * in order to compute the effects of this NLE on the outcome.
   * In this context, disables will set the quality to zero if
   * it's actif. <BR>
   * @param o original Outcome 
   * @param complex describe if you take in account the quality of 
   *        the originator.
   * @param tr is the TaemsRandom generator
   * @ return an outcome #see Outcome.
   
   */
  public Outcome applyNLEonOutcome(Outcome o, boolean complex, TaemsRandom tr){
      Outcome outc = (Outcome)o.clone();
    if (getActive()) {
      outc.setQuality(new Distribution(new Float(0.0), new Float(1.0)));
    }
    return(outc);
  }
  

  /**
   * Update the state of the NLE depending of the source quality.
   * @param time is the current time (useful for the timestamp).
   */
  public void update(int time) {
    TaskBase task = (TaskBase)getFrom();
    if ((task.getCurrentQuality() != 0) && (getActive() != true)) {
      setActive(true);
      super.update(time);
    }
    else if ((getActive() == true) && (task.getCurrentQuality() == 0)) {
       setActive(false);
       super.update(time);
    }
  }

  /**
   * Returns the ttaems version of the node
   * @param v The version number output style to use
   */
  public String toTTaems(float v) {
    StringBuffer sb = new StringBuffer("");

    sb.append("(spec_disables\n");
    sb.append(super.toTTaems(v));
    if (getDelay() != null)
      sb.append("   (delay " + getDelay().output() + ")\n");
    sb.append(")\n");

    return sb.toString();
  }
}


