/* Copyright (C) 2005, University of Massachusetts, Multi-Agent Systems Lab
 * See LICENSE for license information
 */

/************************************************************
 * ConsumesInterrelationship.java
 ************************************************************/

package taems;

/* Global Includes */
import java.util.*;
import java.io.*;

import utilities.Distribution;

/**
 * ConsumesInterrelationship
 */
public class ConsumesInterrelationship extends Interrelationship implements Serializable, Cloneable {

  /**
   * Constructor
   * @param c The consumes distribution
   * @param m The model to use (DURATION_INDEPENDENT or PER_TIME_UNIT)
   */
  public ConsumesInterrelationship(String l, Agent a, Distribution c, String m) {
    super(l, a, null, null, null, null);
    setConsumes(c);
    setModel(m);
  }

  /**
   * Constructor
   */
  public ConsumesInterrelationship(String l, Agent a, Distribution c) {
    this(l, a, c, PER_TIME_UNIT);
  }

  /**
   * Blank Constructor
   */
  public ConsumesInterrelationship() {
    super();
  }

  /**
   * Gets and sets the consumes distribution 
   * (right now, just a cost pass-thru)
   */
  public void setConsumes(Distribution c) {
     setCost(c);
  }
  public Distribution getConsumes() {
     return getCost();
  }

  public boolean hasModel() { return true; }

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

    if (v == Taems.V1)
      sb.append("(spec_uses\n");
    else
      sb.append("(spec_consumes\n");
    sb.append(super.toTTaems(v));
    if (getCost() != null)
      sb.append("   (consumes " + getConsumes().output() + ")\n");
    sb.append(")\n");

    return sb.toString();
  }
}


