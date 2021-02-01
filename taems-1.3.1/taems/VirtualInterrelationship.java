/* Copyright (C) 2005, University of Massachusetts, Multi-Agent Systems Lab
 * See LICENSE for license information
 */

/************************************************************
 * VirtualInterrelationship.java
 ************************************************************/

package taems;

/* Global imports */
import java.util.*;
import java.io.*;
import java.awt.*;

/**
 * VirtualInterrelationship node.
 */
public class VirtualInterrelationship extends Interrelationship implements Serializable, Cloneable {

  /**
   * Simple constructor
   * @param l The node's label
   * @param a The agent the node belongs to
   */
  public VirtualInterrelationship(String l, Agent a) {
    super();

    setLabel(l);
    setAgent(a);
  }

  /**
   * Blank Constructor
   */
  public VirtualInterrelationship() {
    this(null, null);
  }

  /** 
   * Accessors
   */
  public boolean isVirtual() { return true; }
  public int getStatus() { return DISABLED; }

  /**
   * Returns the ttaems version of the node
   * @param v The version number output style to use
   */
  public String toTTaems(float v) {
    StringBuffer sb = new StringBuffer("");

    sb.append(";(spec_virt_interrelationship\n");
    if (getLabel() != null)
      sb.append(";   (label " + getLabel() + ")\n");
    if (getAgent() != null)
      sb.append(";   (agent " + agent.getLabel() + ")\n");
    if (getFrom() != null)
      sb.append(";   (from " + getFrom().getLabel() + ")\n");
    if (getFromOutcome() != null)
      sb.append(";   (from_outcome " + getFromOutcome().getLabel() + ")\n");   
    if (getTo() != null)
      sb.append(";   (to " + getTo().getLabel() + ")\n");
    sb.append(";)\n");

    return sb.toString();
  }

  /**
   * Determines if an object matches this one.
   * @see Node#matches
   */
  public boolean matches(Node n) {

    if (n.getClass().isInstance(this)) {
      return super.matches(n);
    }

    return false;
  }
}
