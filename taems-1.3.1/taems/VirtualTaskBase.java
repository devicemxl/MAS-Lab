/* Copyright (C) 2005, University of Massachusetts, Multi-Agent Systems Lab
 * See LICENSE for license information
 */

/************************************************************
 * VirtualTaskBase.java
 ************************************************************/

package taems;

/* Global imports */
import java.util.*;
import java.io.*;
import java.awt.*;

/**
 * VirtualTaskBase node.
 */
public class VirtualTaskBase extends TaskBase implements Serializable, Cloneable {

  /**
   * Simple constructor
   * @param l The node's label
   * @param a The agent the node belongs to
   */
  public VirtualTaskBase(String l, Agent a) {
    super(l, a);
  }

  /**
   * Blank Constructor
   */
  public VirtualTaskBase() {
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

    sb.append(";(spec_virt_task_base\n");
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
      sb.append(")\n");
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







