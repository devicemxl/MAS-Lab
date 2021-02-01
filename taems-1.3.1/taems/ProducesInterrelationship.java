/* Copyright (C) 2005, University of Massachusetts, Multi-Agent Systems Lab
 * See LICENSE for license information
 */

/************************************************************
 * ProducesInterrelationship.java
 ************************************************************/

package taems;

/* Global Includes */
import java.util.*;
import java.io.*;

import utilities.Distribution;
import utilities.TaemsRandom;

/**
 * ProducesInterrelationship
 */
public class ProducesInterrelationship extends Interrelationship implements Serializable, Cloneable {

  /**
   * Constructor
   * @param c The produces distribution
   * @param m The model to use (DURATION_INDEPENDENT or PER_TIME_UNIT)
   */
  public ProducesInterrelationship(String l, Agent a, Distribution c, String m) {
    super(l, a, null, null, null, null);
    setProduces(c);
    setModel(m);
  }

  /**
   * Constructor
   */
  public ProducesInterrelationship(String l, Agent a, Distribution c) {
    this(l, a, c, PER_TIME_UNIT);
  }

  /**
   * Blank Constructor
   */
  public ProducesInterrelationship() {
    super();
  }

  /**
   * Gets and sets the produces distribution 
   * (right now, just a cost pass-thru)
   */
  public void setProduces(Distribution p) {
     setCost(p);
  }
  public Distribution getProduces() {
     return getCost();
  }

  public boolean hasModel() { return true; }

  public Outcome applyNLEonOutcome(Outcome o, boolean complex, TaemsRandom tr) {
    return(o);
  }

  /**
   * Returns the ttaems version of the node
   * @param v The version number output style to use
   */
  public String toTTaems(float v) {
    StringBuffer sb = new StringBuffer("");

    if ((getFrom() != null) && (! getFrom().isVirtual())) {
      if (!(getFrom() instanceof Method))
         utilities.Log.getDefault().log("Warning: From node on IR " + getLabel() + " is not a Method", 1);
    }
    if ((getTo() != null) && (! getTo().isVirtual())) {
      if (!(getTo() instanceof Resource))
         utilities.Log.getDefault().log("Warning: To node on IR " + getLabel() + " is not a Resource", 1);
    }

    sb.append("(spec_produces\n");
    sb.append(super.toTTaems(v));
    if (getCost() != null)
      sb.append("   (produces " + getProduces().output() + ")\n");
    sb.append(")\n");

    return sb.toString();
  }
}


