/* Copyright (C) 2005, University of Massachusetts, Multi-Agent Systems Lab
 * See LICENSE for license information
 */

/************************************************************
 * VirtualResource.java
 ************************************************************/

package taems;

/* Global imports */
import java.util.*;
import java.io.*;
import java.awt.*;

/**
 * VirtualResource node.
 */
public class VirtualResource extends Resource implements Serializable, Cloneable {

  /**
   * Simple constructor
   * @param l The node's label
   * @param a The agent the node belongs to
   */
  public VirtualResource(String l, Agent a) {
    super();

    setLabel(l);
    setAgent(a);
  }

  /**
   * Simple Constructor
   * @param l The node's label
   */
  public VirtualResource(String l) {
    this(l, null);
  }

  /**
   * Blank Constructor
   */
  public VirtualResource() {
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

    sb.append(";(spec_virt_resource\n");
    if (getLabel() != null)
      sb.append(";   (label " + getLabel() + ")\n");
    if (getAgent() != null)
      sb.append(";   (agent " + agent.getLabel() + ")\n");
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
