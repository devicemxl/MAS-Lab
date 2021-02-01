/* Copyright (C) 2005, University of Massachusetts, Multi-Agent Systems Lab
 * See LICENSE for license information
 */

/************************************************************
 * FacilitatesInterrelationship.java
 ************************************************************/

package taems;

/* Global Includes */
import java.util.*;
import java.io.*;

import utilities.Distribution;
import utilities.TaemsRandom;

/**
 * FacilitatesInterrelationship
 */
public class FacilitatesInterrelationship extends Interrelationship implements Serializable, Cloneable {

  /**
   * Constructor
   */
  public FacilitatesInterrelationship(String l, Agent a, Distribution q, Distribution d, Distribution c, Distribution delay) {
    super(l, a, q, d, c, delay);
  }

  /**
   * Blank Constructor
   */
  public FacilitatesInterrelationship() {
    super();
  }

 /**
   * This is the function you have to specify in the different subclasses
   * in order to compute the effects of this NLE on the outcome.
   * As the NLE is facilitate, we choose a power value from
   * the distribution and multiply the distribution with it.
   * So if you put a power distribution value < <B>1</B>, you will 
   * <B> REDUCE</B> the final value.<BR>
   * If you put a power distribution value > <B>1</B>, you will 
   * <B>INCREASE</B> the final value.<BR>
   * @param o original Outcome 
   * @param complex describe if you take in account the quality of 
   *        the originator.
   * @param tr is the TaemsRandom generator
   * @ return an outcome #see Outcome.
   */
  public Outcome applyNLEonOutcome(Outcome o, boolean complex, TaemsRandom tr){
    TaskBase task = (TaskBase)getFrom();
    Outcome outc = (Outcome)o.clone();
    if (getActive()) {
      float powerQ = 1;
      if (complex == true) { /* apply quality dependance */
	powerQ = task.getCurrentQuality() / task.getMaximumQuality();
      }
      if (this.quality != null) {
	/* Set distribution */
	tr.setDistribution(quality);
	/* Get a value */
	float power_q = tr.nextValue();
	tr.unsetDistribution();
	/* implement this f*cking stupid model */
	power_q = 1 + power_q; 
	/* Get the outcome and return it */	
	outc.setQuality(o.getQuality().applyPower(powerQ*power_q));
      }
      if (this.duration != null) {
	/* Set distribution */
	tr.setDistribution(duration);
	/* Get a value */
	float power_d = tr.nextValue();
	tr.unsetDistribution();
	/* implement this f*cking stupid model */
	power_d = 1 - power_d; 
	if (power_d < 0) 
	    power_d = 0;
	/* Get the outcome and return it */	
	outc.setDuration(o.getDuration().applyPower(powerQ*power_d));
      }
      if (this.cost != null) {
	/* Set distribution */
	tr.setDistribution(cost);
	/* Get a value */
	float power_c = tr.nextValue();
	tr.unsetDistribution();
	/* implement this f*cking stupid model */
	power_c = 1 - power_c; 
	if (power_c < 0) 
	    power_c = 0;
	/* Get the outcome and return it */	
	outc.setCost(o.getCost().applyPower(powerQ*power_c));
      }
    }
    else {
      setActive(false);
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

    sb.append("(spec_facilitates\n");
    sb.append(super.toTTaems(v));
    if (getQuality() != null)
      sb.append("   (quality_power " + getQuality().output() + ")\n");
    if (getDuration() != null)
      sb.append("   (duration_power " + getDuration().output() + ")\n");
    if (getCost() != null)
      sb.append("   (cost_power " + getCost().output() + ")\n");
    if (getDelay() != null)
      sb.append("   (delay " + getDelay().output() + ")\n");
    sb.append(")\n");

    return sb.toString();
  }
}


