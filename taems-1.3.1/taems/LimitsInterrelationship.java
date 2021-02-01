/* Copyright (C) 2005, University of Massachusetts, Multi-Agent Systems Lab
 * See LICENSE for license information
 */

/************************************************************
 * LimitsInterrelationship.java
 ************************************************************/

package taems;

/* Global Includes */
import java.util.*;
import java.io.*;

import utilities.Distribution;
import utilities.TaemsRandom;

/**
 * LimitsInterrelationship
 */
public class LimitsInterrelationship extends Interrelationship implements Serializable, Cloneable {

  /**
   * Constructor
   * @param m The model to use (DURATION_INDEPENDENT or PER_TIME_UNIT)
   */
  public LimitsInterrelationship(String l, Agent a, Distribution q, Distribution d, Distribution c, String m) {
    this(l, a, q, d, c);
    setModel(m);
  }

  /**
   * Constructor
   */
  public LimitsInterrelationship(String l, Agent a, Distribution q, Distribution d, Distribution c) {
    super(l, a, q, d, c, null);
  }

  /**
   * Blank Constructor
   */
  public LimitsInterrelationship() {
    super();
  }

  public Outcome applyNLEonOutcome(Outcome o, boolean complex, TaemsRandom tr){
    return o;
  }

  public boolean hasModel() { return true; }

  /**
   * Returns the ttaems version of the node
   * @param v The version number output style to use
   */
  public String toTTaems(float v) {
    StringBuffer sb = new StringBuffer("");

    if ((getFrom() != null) && (! getFrom().isVirtual())) {
      if (!(getFrom() instanceof Resource))
         utilities.Log.getDefault().log("Warning: From node on IR " + getLabel() + " is not a Resource", 1);
    }
    if ((getTo() != null) && (! getTo().isVirtual())) {
      if (!(getTo() instanceof Method))
         utilities.Log.getDefault().log("Warning: From node on IR " + getLabel() + " is not a Method", 1);
    }

    sb.append("(spec_limits\n");
    sb.append(super.toTTaems(v));
    if (getQuality() != null)
      sb.append("   (quality_power " + getQuality().output() + ")\n");
    if (getDuration() != null)
      sb.append("   (duration_power " + getDuration().output() + ")\n");
    if (getCost() != null)
      sb.append("   (cost_power " + getCost().output() + ")\n");
    sb.append(")\n");
    /** Resource Model  is missing but DTC doesn't
	handle that very well, so I move it here for now 
	remove it when DTC works with it = RV =  **/
    return sb.toString();
  }
}


