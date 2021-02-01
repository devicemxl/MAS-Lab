/* Copyright (C) 2005, University of Massachusetts, Multi-Agent Systems Lab
 * See LICENSE for license information
 */

/************************************************************
 * VirtualMethod.java
 ************************************************************/

package taems;

/* Global imports */
import java.util.*;
import java.io.*;
import java.awt.*;

/**
 * VirtualMethod node.
 */
public class VirtualMethod extends Method implements Serializable, Cloneable {

  /**
   * Simple constructor
   * @param l The node's label
   * @param a The agent the node belongs to
   */
  public VirtualMethod(String l, Agent a) {
    super(l, a);
  }

  /**
   * Blank Constructor
   */
  public VirtualMethod() {
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

    sb.append(";(spec_virt_method\n");
    if (getLabel() != null)
      sb.append(";   (label " + getLabel() + ")\n");
    if (getAgent() != null)
      sb.append(";   (agent " + agent.getLabel() + ")\n");
    if (supertasks.size() > 0) {
      sb.append(";   (supertasks ");
      Enumeration e = getSupertasks();
      while(e.hasMoreElements()) {
          sb.append(((Node)e.nextElement()).getLabel());
          if (e.hasMoreElements()) sb.append(" ");
      }
      sb.append(";)\n");
    }
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
