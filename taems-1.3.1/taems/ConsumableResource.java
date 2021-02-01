/* Copyright (C) 2005, University of Massachusetts, Multi-Agent Systems Lab
 * See LICENSE for license information
 */

/************************************************************
 * ConsumableResource.java
 ************************************************************/

package taems;

import java.util.*;

/**
 * Used to represent resources who's level may be reduced
 * by some action, and does not replenish until some other
 * action explicitly does so (e.g. paper in a printer, or
 * gas in a car).
 */
public class ConsumableResource extends Resource implements Cloneable {

  /**
   * Constructor
   * @param l The node's label
   * @param a The agent who manages the resource
   * @param s The current state of the resource
   * @param d The resource depletion level
   * @param o The resource overload level
   */
  public ConsumableResource(String l, Agent a, double s, double d, double o) {
    super(l, a, s, d, o);
  }

  /**
   * Blank Constructor
   */
  public ConsumableResource() {
    this (null, null, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
  }

  /**
   * Returns the ttaems version of the node
   * @param v The version number output style to use
   */
  public String toTTaems(float v) {
    StringBuffer sb = new StringBuffer("");

    sb.append("(spec_consumable_resource\n");
    sb.append(super.toTTaems(v));
    sb.append(")\n");

    return sb.toString();
  }
}


