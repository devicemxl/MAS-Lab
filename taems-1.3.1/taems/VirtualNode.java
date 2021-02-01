/* Copyright (C) 2005, University of Massachusetts, Multi-Agent Systems Lab
 * See LICENSE for license information
 */

/************************************************************
 * VirtualNode.java
 ************************************************************/

package taems;

/* Global imports */
import java.awt.*;
import java.io.*;

import taems.*;

/**
 * Virtual nodes are used in the taems graph to indicate
 * nodes for which we have no real information yet, but
 * still need to be represented in some manner.  For instance,
 * if we have an interrelationship with a target who's type
 * we don't yet know, a VN can be used as a placeholder to
 * mark that spot.
 */
public class VirtualNode extends Node implements Serializable, Cloneable {

  /**
   * Default constructor
   * @param l The node's label
   * @param a The agent the node belongs to
   */
  public VirtualNode(String l, Agent a) {
    super(l, a);
  }

  /**
   * Default constructor
   * @param l The node's label
   */
  public VirtualNode(String l) {
    super(l, null);
  }

  /**
   * Blank Constructor
   */
  public VirtualNode() {
    this (null, null);
  }

  /** 
   * Accessors
   */
  public boolean isVirtual() { return true; }
  public int getStatus() { return DISABLED; }

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

  /**
   * Returns the ttaems version of the node
   * @param v The version number output style to use
   */
  public String toTTaems(float v) {
    StringBuffer sb = new StringBuffer("");

    sb.append(";(spec_virt_node\n");
    if (getLabel() != null)
      sb.append(";   (label " + getLabel() + ")\n");
    if (getAgent() != null)
      sb.append(";   (agent " + agent.getLabel() + ")\n");
    sb.append(";)\n");

    return sb.toString();
  }
}

