/* Copyright (C) 2005, University of Massachusetts, Multi-Agent Systems Lab
 * See LICENSE for license information
 */

/************************************************************
 * TaskBase.java
 ************************************************************/

package taems;

/* Global imports */
import java.util.*;
import java.io.*;
import java.awt.*;

import utilities.*;

/**
 * TaskBase node.  This is used as a base class for both tasks
 * and methods, which together make up the bulk of the 
 * action based knowledge.
 * <P>
 * You shouldn't have to instantiate one of these directly,
 * except when using them to find other matching nodes.
 */
public class TaskBase extends Node implements Serializable, Cloneable {
  protected int arrival_time = Integer.MIN_VALUE;
  protected int earliest_start_time = Integer.MIN_VALUE;
  protected int deadline = Integer.MIN_VALUE;
  protected Boolean nonlocal = null;

  protected Vector supertasks = new Vector();

  /**
   * Constructor
   * @param l The node's label
   * @param a The agent the node belongs to
   * @param at Arrival time
   * @param est Earliest start time
   * @param dl Deadline
   */
  public TaskBase(String l, Agent a, int at, int est, int dl) {
    super(l, a);

    arrival_time = at;
    earliest_start_time = est;
    deadline = dl;

    setCurrentQuality(0);
    setCurrentDuration(0);
    setCurrentCost(0);
  }

  /**
   * Simple constructor
   * @param l The node's label
   * @param a The agent the node belongs to
   */
  public TaskBase(String l, Agent a) {
    this(l, a, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
  }

  /**
   * Blank Constructor
   */
  public TaskBase() {
    this (null, null);
  }

  /**
   * Accessors
   */
  public int getArrivalTime() { return arrival_time; }
  public void setArrivalTime(int a) {
      if (getArrivalTime() == a) return;
      arrival_time = a;
      fireNodeUpdateEvent(new NodeUpdateEvent(this, NodeUpdateEvent.VALUE));
  }
  public int getEarliestStartTime() { return earliest_start_time; }
  public void setEarliestStartTime(int e) {
      if (getEarliestStartTime() == e) return;
      earliest_start_time = e;
      fireNodeUpdateEvent(new NodeUpdateEvent(this, NodeUpdateEvent.VALUE));
  }
  public int getComposedDeadline() { 
      int dl = getDeadline();
      Enumeration e = getSupertasks();
      while (e.hasMoreElements()) {
	  TaskBase n = (TaskBase)e.nextElement();
	  int dl1 = n.getComposedDeadline();
	  if (dl1 != Integer.MIN_VALUE)
	      if (deadline != Integer.MIN_VALUE)
		  dl = Math.min(dl,dl1);
	      else
		  dl = dl1;
      }
      return(dl);
  }
  
    public int getComposedEarliestStartTime() {
        int estime = getEarliestStartTime();
        Enumeration e = getSupertasks();
        while (e.hasMoreElements()) {
            TaskBase n = (TaskBase)e.nextElement();
            estime = Math.max(estime,n.getComposedEarliestStartTime());
        }
        return(estime);
    }

  public int getDeadline() { return deadline; }
  public void setDeadline(int d) {
      if (getDeadline() == d) return;
      deadline = d;
      fireNodeUpdateEvent(new NodeUpdateEvent(this, NodeUpdateEvent.VALUE));
  }
  public void unsetDeadline() { setDeadline(Integer.MIN_VALUE); }

  public boolean getNonLocal() { if (nonlocal == null) return false; else return nonlocal.booleanValue(); }
  public boolean isNonLocal() { return getNonLocal(); }
  public void setNonLocal(boolean n) {
      if (nonlocal != null)
          if (nonlocal.booleanValue() == n) return;
      nonlocal = new Boolean(n);
      fireNodeUpdateEvent(new NodeUpdateEvent(this, NodeUpdateEvent.VALUE));
  }
  public int getStartTime() { return Integer.MIN_VALUE; }
  public void setStartTime(int s) { updateParentQualities(); }
  public int getFinishTime() { return Integer.MIN_VALUE; }
  public void setFinishTime(int f) { updateParentQualities(); }
  public boolean hasFinished() { return (getFinishTime() >= 0); }
  public boolean hasStarted() { return (getStartTime() >= 0); }
  
  /**
   * Attribute based accessors
   */
  public float getCurrentQuality() {
    if (hasAttribute("cur_quality"))
      return ((Float)getAttribute("cur_quality")).floatValue();
    else
      return 0;
  }
  public void setCurrentQuality(float q) {
    if (getCurrentQuality() == q) return;
    setAttribute("cur_quality", new Float(q));
    updateParentQualities();
  }

  /**
   * Calls the updateQuality function on parent tasks
   */
  public void updateParentQualities() {
    Enumeration e = getSupertasks();
    while (e.hasMoreElements()) {
      Node t = (Node)e.nextElement();
      if (t instanceof Task) {
	((Task)t).updateQuality();
      }
    }
  }

  /** 
   * get the maximum quality you can get for this method.
   * Used to compute the facilitator factors.
   */
   public float getMaximumQuality() {
    float maximumQuality;
    if (hasAttribute("max_quality"))
      return ((Float)getAttribute("max_quality")).floatValue();
    return 0;
   }

  public void setMaximumQuality(float q) {
    setAttribute("max_quality", new Float(q));
  }
  
  public float getCurrentDuration() {
    if (hasAttribute("cur_duration"))
      return ((Float)getAttribute("cur_duration")).floatValue();
    else
      return 0;
  }

  public void setCurrentDuration(float d) {
    if (getCurrentDuration() == d) return;
    setAttribute("cur_duration", new Float(d));
    Enumeration e = getSupertasks();
    while (e.hasMoreElements()) {
      Node t = (Node)e.nextElement();
      if (t instanceof Task) {
	((Task)t).updateDuration();
      }
    }
  }
  public float getCurrentCost() {
    if (hasAttribute("cur_cost"))
      return ((Float)getAttribute("cur_cost")).floatValue();
    else
      return 0;
  }
  public void setCurrentCost(float c) {
    if (getCurrentCost() == c) return;
    setAttribute("cur_cost", new Float(c));
    Enumeration e = getSupertasks();
    while (e.hasMoreElements()) {
      Node t = (Node)e.nextElement();
      if (t instanceof Task) {
	((Task)t).updateCost();
      }
    }
  }

  /**
   * Gets the affecting IRs by gathering those obtained from
   * the supertasks.
   * @return An enumeration of the affecting IRs
   */
  public Enumeration getAffectingInterrelationships() {
    Vector v = new Vector();

    v.addElement(getInInterrelationships());
    
    Enumeration e = getSupertasks();
    while (e.hasMoreElements()) {
      Node n = (Node)e.nextElement();
      v.addElement(n.getAffectingInterrelationships());
    }

    return new EnumerationEnumeration(v);
  }

  /**
   * Gets the affected IRs by gathering those obtained from
   * the supertasks.
   * @return An enumeration of the affected IRs
   */
  public Enumeration getAffectedInterrelationships() {
    Vector v = new Vector();

    v.addElement(getOutInterrelationships());
    
    Enumeration e = getSupertasks();
    while (e.hasMoreElements()) {
      Node n = (Node)e.nextElement();
      v.addElement(n.getAffectedInterrelationships());
    }

    return new EnumerationEnumeration(v);
  }

  /**
   * Detaches the node from the structure it is in, be careful with
   * this.  Note you may have to also call Taems.removeNode to remove
   * it from the enclosing Taems structure.
   */
  public Node excise() {
    Node n = super.excise();

    VirtualTaskBase vn = new VirtualTaskBase(n.getLabel(), n.getAgent());
    n.transferInterrelationships(vn);

    while (hasSupertasks()) {
      Task t = (Task)supertasks.firstElement();
      t.replaceSubtask(this, vn);
    }

    return vn;
  }

  /**
   * Destroys this node, and any other nodes that are dependent on it.
   * Note you may have to also call Taems.removeNode to remove
   * it from the enclosing Taems structure.
   */
  public void delete() {

      while (hasSupertasks()) {
          Task t = (Task)supertasks.firstElement();
          t.removeSubtask(this);
      }

      super.delete();
  }
    
  /**
   * Adds a supertask to this node.  Used internally to maintain 
   * graph state.
   * @param st The task to add
   */
  protected void addSupertask(Task st) {
    supertasks.addElement(st);
  }

  /**
   * Removes a supertask from this node.  Used internally to maintain 
   * graph state.
   * @param st The task to remove
   */
  protected void removeSupertask(Task st) {
    supertasks.removeElement(st);
  }

    /**
     * Transfers all the supertasks of this node to another node.
     * Essentially swaps the new node for this one in each of the
     * supertask's subtask lists.
     * @param n The node to transfer supertasks to
     */
    protected void transferSupertasks(TaskBase n) {

        if (this == n) return;

        while (hasSupertasks()) {
            Task t = firstSupertask();
            t.replaceSubtask(this, n);
        }
    }

  /**
   * Returns a list of the node's supertasks.
   * @return The supertasks
   */
  public Enumeration getSupertasks() {
    return supertasks.elements();
  }

  /**
   * Returns the node's first supertask.
   * @return The supertask, or null if none
   */
  public Task firstSupertask() {
      try {
          return (Task)supertasks.firstElement();
      } catch (NoSuchElementException e) {
          return null;
      }
  }

  /**
   * Returns the node's first visible supertask.
   * @return The first visible supertask, or null if none
   */
  public Task firstVisibleSupertask() {
      try {
          for (Enumeration e = getSupertasks(); e.hasMoreElements(); ) {
              Task t = (Task)e.nextElement();
              if (t.isVisible() && !t.isCollapsed())
                  return t;
          }
      } catch (NoSuchElementException e) {
          return null;
      }
      return null;
  }

  /**
   * Determines the number of supertasks
   * @return The number of supertasks
   */
  public int numSupertasks() {
    return supertasks.size();
  }

  /**
   * Determines if the node has supertasks
   * @return True if it has supertasks
   */
  public boolean hasSupertasks() {
    return (numSupertasks() > 0);
  }

    /**
     * Convenience function that removes this node as a subtask from all
     * its supertasks.
     */
    public void removeAllSupertasks() {

        while (hasSupertasks()) {
            Task t = (Task)supertasks.firstElement();
            t.removeSubtask(this);
        }

        fireNodeUpdateEvent(new NodeUpdateEvent(this, NodeUpdateEvent.VALUE));
        fireNodeUpdateEvent(new NodeUpdateEvent(this, NodeUpdateEvent.PLACEMENT));
    }

    /**
     * Calculates the tree depth of the node
     */
    public int calculateTreeDepth() {
        int d = 1;

        if (hasSupertasks()) {
            Enumeration e = getSupertasks();
            while (e.hasMoreElements()) {
                TaskBase t = (TaskBase)e.nextElement();
                d = Math.max(d, 1 + t.calculateTreeDepth());
            }
        }

        return d;
    }

  /**
   * Returns the ttaems version of the node
   * @param v The version number output style to use
   */
  public String toTTaems(float v) {
    StringBuffer sb = new StringBuffer("");
    
    sb.append(super.toTTaems(v));
    if (arrival_time != Integer.MIN_VALUE)
      sb.append("   (arrival_time " + arrival_time + ")\n");
    if (earliest_start_time != Integer.MIN_VALUE)
      sb.append("   (earliest_start_time " + earliest_start_time + ")\n");
    if (deadline != Integer.MIN_VALUE)
      sb.append("   (deadline " + deadline + ")\n");
    if (isNonLocal()) {
        if (v >= Taems.V1_1) {
            sb.append("   (nonlocal)\n");
        } else {
            sb.append("   (method_is_non_local)\n");
        } 
    }

   
    if (hasSupertasks()) {
      sb.append("   (supertasks ");
      Enumeration e = getSupertasks();
      while(e.hasMoreElements()) {
	sb.append(((Node)e.nextElement()).getLabel());
	if (e.hasMoreElements()) sb.append(" ");
      }
      sb.append(")\n");
    }

    return sb.toString();
  }

  /**
   * Determines if an object matches this one.
   * <BR>
   * This matches against:
   * <UL>
   * <LI> Arrival Time
   * <LI> Earliest Start Time
   * <LI> Deadline
   * </UL>
   * Check the matches function for the parent classes of this
   * class for more details.
   * @see Node#matches
   */
    public boolean matches(Node n) {

        if (n.getClass().isInstance(this)) {
            if (! super.matches(n))
                return false;
        } else
            return false;

        if (n instanceof TaskBase) {
            if (!matches(((TaskBase)n).getArrivalTime(), getArrivalTime())) return false;
            if (!matches(((TaskBase)n).getEarliestStartTime(), getEarliestStartTime())) return false;
            if (!matches(((TaskBase)n).getDeadline(), getDeadline())) return false;
        }

        return true;
    }

  /**
   * Clone.  Careful, this doesn't fill in supertasks, we expect
   * derived classes to handle this in their clone methods.
   */
  public Object clone() {
    TaskBase cloned = null;

    try {
      cloned = (TaskBase)super.clone();
    } catch (Exception e) {System.out.println("Clone Error: " + e);}

    cloned.setArrivalTime(getArrivalTime());
    cloned.setEarliestStartTime(getEarliestStartTime());
    cloned.setDeadline(getDeadline());
    cloned.setNonLocal(getNonLocal());
    cloned.supertasks = new Vector();

    return cloned;
  }

  /**
   * Copies the node's fields into the provided node.
   * Does not do anything with endpoints.
   * @param n The node to copy stuff into
   */
    public void copy(Node n) {

        if (n instanceof TaskBase) {
            TaskBase t = (TaskBase)n;
            t.setArrivalTime(getArrivalTime());
            t.setEarliestStartTime(getEarliestStartTime());
            t.setDeadline(getDeadline());
            t.setNonLocal(getNonLocal());
        }

        super.copy(n);
    }

  /***********************************************************
   *                     Drawing Junk                        *
   ***********************************************************/

    public boolean isVisible() {
        if (!super.isVisible()) return false;
        if (!hasSupertasks()) return true;

        boolean c = true;
        for (Enumeration e = getSupertasks(); e.hasMoreElements(); ) {
            TaskBase t = (TaskBase)e.nextElement();
            if (!t.isCollapsed()) { c = false; break; }
        }
        if (c) return false;

        boolean i = true;
        for (Enumeration e = getSupertasks(); e.hasMoreElements(); ) {
            TaskBase t = (TaskBase)e.nextElement();
            if (t.isVisible()) { i = false; break; }
        }
        if (i) return false;

        return true;
    }

  /**
   * Get the background color
   */
  protected Color getBackground() {

    if (isSelected())
      return super.getBackground();

    else switch(getStatus()) {
    case NORMAL:
        if (isNonLocal())
            return Color.lightGray;
        break;
    default:
    }

    return super.getBackground();
  }


  /**
   * Get the foreground color
   */
  protected Color getForeground() {

      if (isNonLocal())
          return Color.darkGray;
      else
          return super.getForeground();
  }
}







