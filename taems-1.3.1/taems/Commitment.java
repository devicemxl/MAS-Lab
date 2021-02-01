/* Copyright (C) 2005, University of Massachusetts, Multi-Agent Systems Lab
 * See LICENSE for license information
 */

/************************************************************
 * Commitment.java
 ************************************************************/

package taems;

import java.util.*;
import java.io.*;
import java.awt.*;

import utilities.*;

/**
 * Commitments are used by an agent to represent the quantitative aspects of
 * negotiation and coordination.
 * <p> 
 * @see <A HREF="http://mas.cs.umass.edu/research/taems/white/">Taems White Paper</A>
 * @see <A HREF="http://mas.cs.umass.edu/~wagner/research/commitments.html">Commitments</A>
 */
public class Commitment implements Serializable, Cloneable {
  /**
   * Importance Constants
   */
  public static final int WHATIF = -1;
  public static final int GIVEN = 0;
  public static final int HARD = 1;

  /**
   * Type Constants
   */
  public static final String DO = "do";
  public static final String DONT = "dont";
  public static final String DEADLINE = "deadline";
  public static final String EST = "earliest_start_time";
  public static final String EST_DL_DO = "est_and_dl_and_do";
  public static final String SPECIAL = "special";

  /**
   * existing_or_proposed constants
   */
  public static final String EXISTING = "existing";
  public static final String PROPOSED = "proposed";

  protected String label = null; 
  protected Agent from_agent = null;
  protected Agent to_agent = null;
  protected Vector tasks = new Vector(1);

  protected String type;
  protected int importance = GIVEN;
  protected float min_quality = 0;
  protected int earliest_start_time = -1;
  protected int deadline = -1;
  protected int time_satisfied = -1;
  protected int dont_interval_start = -1;
  protected int dont_interval_end = -1;

  protected Distribution quality = null;
  protected Distribution time = null;

  protected boolean nonlocal = false;
  protected Hashtable attributes = new Hashtable();

  /**
   * Default constructor, fields are filled with default values.
   */
  public Commitment() {
      super();
  }

  /**
   * Normal Constructor
   *
   * @param l The commitment's label
   * @param ty The type (DO, DONT, DEADLINE, EST, EST_DL_DO, SPECIAL)
   * @param fa The from agent
   * @param ta The to agent
   * @param tsk The commitment's vector of tasks (Task or Method objects)
   * @param imp The importance value (WHATIF, GIVEN, HARD, etc.)
   * @param minq The minimum quality value
   * @param est Earliest start time
   * @param dl Deadline
   * @param sat The commitment's time satisfied
   */
  public Commitment(String l, String ty, Agent fa, Agent ta, Vector tsk, int imp, float minq, int est, int dl, int sat) {

      label = l;
      type = ty;

      from_agent = fa;
      to_agent = ta;
      tasks = tsk;

      importance = imp;
      min_quality = minq;
      earliest_start_time = est;
      deadline = dl;
      time_satisfied = sat;

      dont_interval_start = -1;
      dont_interval_end = -1;
  }

  /**
   * Don't Commitment Constructor
   * @param l The commitment's label
   * @param fa The from agent
   * @param ta The to agent
   * @param tsk The commitment's vector of tasks (Task or Method objects)
   * @param imp The importance value (WHATIF, GIVEN, HARD, etc.)
   * @param ds The don't interval start time
   * @param de The don't interval end time
   */
  public Commitment(String l, Agent fa, Agent ta, TaskBase tsk, int imp, int ds, int de) {
      this(l, "dont", fa, ta, new Vector(), imp, 0, -1, -1, -1);

      tasks.addElement(tsk);

      dont_interval_start = ds;
      dont_interval_end = de;
  }

  /**
   * Nonlocal Commitment Constructor
   * @param l The commitment's label
   * @param fa The from agent
   * @param ta The to agent
   * @param tsk The commitment's vector of tasks (Task or Method objects)
   * @param q The quality distribution
   * @param t The time distribution
   */
  public Commitment(String l, Agent fa, Agent ta, Vector tsk, Distribution q, Distribution t) {
      this(l, null, fa, ta, tsk, 0, 0, -1, -1, -1);

      quality = q;
      time = t;
      nonlocal = true;
  }

  /**
   * Accessors
   */
  public String getLabel() { return label; }
  public void setLabel(String l) { label = l; }
  public Agent getFromAgent() { return from_agent; }
  public void setFromAgent(Agent a) { from_agent = a; }
  public Agent getToAgent() { return to_agent; }
  public void setToAgent(Agent a) { to_agent = a; }
  public TaskBase getTask() {
      if ((tasks != null) && (tasks.size() > 0))
          return (TaskBase)tasks.firstElement();
      else
          return null;
  }
  public void setTask(TaskBase t) {
      tasks = new Vector();
      tasks.addElement(t);
  }
  public Enumeration getTasks() {
      if (tasks != null)
          return tasks.elements();
      else
          return new Vector().elements();
  }
  public Vector getTaskVector() { return tasks; }
  public void setTaskVector(Vector v) { tasks = v; }
  public String getType() { return type; }
  public void setType(String t) { type = t; }
  public int getImportance() { return importance; }
  public void setImportance(int i) { importance = i; }
  public float getMinimumQuality() { return min_quality; }
  public void setMinimumQuality(float m) { min_quality = m; }
  public int getEarliestStartTime() { return earliest_start_time; }
  public void setEarliestStartTime(int e) { earliest_start_time = e; }
  public int getDeadline() { return deadline; }
  public void setDeadline(int d) { deadline = d; }
  public int getDontIntervalStart() { return dont_interval_start; }
  public void setDontIntervalStart(int d) { dont_interval_start = d; }
  public int getDontIntervalEnd() { return dont_interval_end; }
  public void setDontIntervalEnd(int d) { dont_interval_end = d; }
  public int getTimeSatisfied() { return time_satisfied; }
  public void setTimeSatisfied(int t) { time_satisfied = t; }

  public Distribution getQualityDistribution() { return quality; }
  public void setQualityDistribution(Distribution d) { quality = d; }
  public Distribution getTimeDistribution() { return time; }
  public void setTimeDistribution(Distribution d) { time = d; }

  public boolean isNonLocal() { return getNonLocal(); }
  public boolean getNonLocal() { return nonlocal; }
  public void setNonLocal(boolean n) { nonlocal = n; }

  public void setCostDistribution(Distribution c) { setAttribute("cost_distribution", c); }
  public Distribution getCostDistribution() { return((Distribution)getAttribute("cost_distribution")); }
  public void setResource(String r) { setAttribute("Resource", r); }
  public String getResource() { return((String)getAttribute("Resource")); }
  public void setQuantity(float f) { setAttribute("Quantity", new Float(f)); }
  public float getQuantity() {
	Float f = (Float)getAttribute("Quantity");
	return (f != null) ? f.floatValue() : Float.NEGATIVE_INFINITY;
  }
  public void setID(long l) { setAttribute("CoordinationID", new Long(l)); }
  public long getID() {
	Long l = (Long)getAttribute("CoordinationID");
	return (l != null) ? l.longValue() : Long.MIN_VALUE;
  }

  public void setExistingOrProposed(String ep) { setAttribute("existing_or_proposed", ep); }
  public String getExistingOrProposed() { return (String)getAttribute("existing_or_proposed"); }
  public void setQualityForCommitmentSatisfaction(Distribution d) { setAttribute("quality_for_commitment_satisfaction", d); }
  public Distribution getQualityForCommitmentSatisfaction() { return (Distribution)getAttribute("quality_for_commitment_satisfaction"); }

  /**
   * This function determines if the Commitment has one of the 
   * predefined types given above (e.g. DO, DONT, etc.).  Note
   * that this does not include the SPECIAL type.
   * @return True if the type is one of the predefined standards
   * given above.
   */
  public boolean isNormalType() {

     if (getType() == null) return false;

     if ( getType().equals(DO) ||
          getType().equals(DONT) ||
          getType().equals(DEADLINE) ||
          getType().equals(EST) ||
          getType().equals(EST_DL_DO) )
	return true;
     else
	return false;
  }

  /**
   * This function determines if the Commitment has a non-standard
   * or special type.
   * @return True if the type is not normal
   */
  public boolean isSpecialType() {

     if (getType() == null) return true;

     if ( getType().equals(SPECIAL) ||
          (! isNormalType()) )
	return true;
     else
	return false;
  }

  /**
   * Note that this uses the toString() method for the key
   * to actually store the object.
   * @param k The key identifying the desired attribute
   * @return The object, or null if not found
   */
  public Object getAttribute(Object k) {
    return attributes.get(k.toString());
  }

  /**
   * Sets an attribute's data.  Attribute keys should not
   * contain whitespace.
   * <P>
   * If a key is added with a null data object, the key will
   * be removed from the attribute set.
   * @param k The key identifying the desired attribute
   * @param d The attribute data
   */
  public void setAttribute(Object k, Object d) { 
    if (d == null)
      removeAttribute(k);
    else
      attributes.put(k.toString(), d);
  }

  /**
   * Removes an attribute field from the object.
   * @param k The attribute to remove.
   */
  public void removeAttribute(Object k) {
    attributes.remove(k.toString());
  }

  /**
   * Returns the attribute names
   * @return An enumeration of all the attribute keys
   */
  public Enumeration getAttributes() { return attributes.keys(); }

  /**
   * Determines if the node has a particular attribute
   * @return True if the attribute is present
   */
  public boolean hasAttribute(Object k) {
    return attributes.containsKey(k.toString());
  }

  /**
   * Returns a reference to the raw attribute table.  Not
   * for public consumption.
   * @return The attribute table, or null if not found
   */
  protected Hashtable getAttributesTable() {
      return attributes;
  }

  /**
   * Returns true if the dont_interval contains the interval [start,end]
   * This is used if the scheduler changed the time of the method execution,
   * and we want to see if the new method interval is a subset of the old
   * interval.  
   * @return TRUE if dont_start <= start && end <= dont_end.
   */
  public boolean containsInterval(int start, int end) {
      if (dont_interval_start <= start && end <= dont_interval_end)
          return true;
      return false;
  }

  /**
   * Clone me.
   */
  public Object clone() {
      Commitment cloned = new Commitment();

      if (getLabel() != null)
          cloned.setLabel(new String(getLabel()));
      if (getFromAgent() != null)
          cloned.setFromAgent((Agent)getFromAgent().clone());
      if (getToAgent() != null)
          cloned.setToAgent((Agent)getToAgent().clone());
      if (getTaskVector() != null)
          cloned.setTaskVector((Vector)tasks.clone());

      if (getType() != null)
          cloned.setType(getType());
      cloned.setImportance(getImportance());
      cloned.setMinimumQuality(getMinimumQuality());
      cloned.setEarliestStartTime(getEarliestStartTime());
      cloned.setDeadline(getDeadline());
      cloned.setTimeSatisfied(getTimeSatisfied());
      
      cloned.setDontIntervalStart(getDontIntervalStart());
      cloned.setDontIntervalEnd(getDontIntervalEnd());

      if (getQualityDistribution() != null)
          cloned.setQualityDistribution((Distribution)getQualityDistribution().clone());
      if (getTimeDistribution() != null)
          cloned.setTimeDistribution((Distribution)getTimeDistribution().clone());

      if (getCostDistribution() != null)
          cloned.setCostDistribution((Distribution)getCostDistribution().clone());

      cloned.setNonLocal(getNonLocal());
      if (attributes != null)
          cloned.attributes = (Hashtable)attributes.clone();

      return cloned;
  }

  /**
   * Matches stuff
   * <BR>
   * This matches against:
   * <UL>
   * <LI> Label
   * <LI> From agent
   * <LI> To agent
   * <LI> Type
   * <LI> Tasks
   * <LI> Importance
   * <LI> MinimumQuality
   * <LI> EarliestStartTime
   * <LI> Deadline
   * <LI> TimeSatisfied
   * <LI> CostDistribution
   * <LI> Resource
   * <LI> Quantity
   * <LI> ID
   * <LI> DontIntervalStart
   * <LI> DontIntervalEnd
   * <LI> QualityDistribution
   * <LI> TimeDistribution
   * </UL>
   * @param c The commitment to match against
   * @return true if they match
   * @see Node#matches
   */
  public boolean matches(Commitment c) {

      if (!Node.matches(c.getLabel(), getLabel())) return false;
      if (!Node.matches(c.getFromAgent(), getFromAgent())) return false;
      if (!Node.matches(c.getToAgent(), getToAgent())) return false;

      if (!Node.matches(c.getType(), getType())) return false;

      Enumeration e1 = c.getTasks();
      while (e1.hasMoreElements()) {
          TaskBase ct = (TaskBase)e1.nextElement();
          boolean found = false;
          Enumeration e2 = getTasks();
          while (e2.hasMoreElements()) {
              TaskBase mt = (TaskBase)e2.nextElement();
              if (ct.matches(mt)) {
                  found = true;
                  break;
              }
          }
          if (!found) return false;
      }

      if (!Node.matches(c.getImportance(), getImportance())) return false;
      if (!Node.matches(c.getMinimumQuality(), getMinimumQuality())) return false;
      if (!Node.matches(c.getEarliestStartTime(), getEarliestStartTime())) return false;
      if (!Node.matches(c.getDeadline(), getDeadline())) return false;
      if (!Node.matches(c.getTimeSatisfied(), getTimeSatisfied())) return false;

      if (!Node.matches(c.getCostDistribution(), getCostDistribution())) return false;
      if (!Node.matches(c.getResource(), getResource())) return false;
      if (!Node.matches(c.getQuantity(), getQuantity())) return false;
      if (!Node.matches(c.getID(), getID())) return false;
   
      if (!Node.matches(c.getDontIntervalStart(), getDontIntervalStart())) return false;
      if (!Node.matches(c.getDontIntervalEnd(), getDontIntervalEnd())) return false;

      if (!Node.matches(c.getQualityDistribution(), getQualityDistribution())) return false;
      if (!Node.matches(c.getTimeDistribution(), getTimeDistribution())) return false;

      return true;
  }

    /**
     * Returns the ttaems version of the node
     * @param v The version number output style to use
     */
    public String toTTaems(float v) {
        StringBuffer sb = new StringBuffer("");
        Enumeration e;
        
        if (v >= Taems.V1_1) {
            sb.append("(spec_commitment\n");

            sb.append(Node.attributesToString(attributes, v));

            if (getLabel() != null)
                sb.append("   (label " + getLabel() + ")\n");        
            if (getType() != null)
                sb.append("   (type " + getType() + ")\n");
            // Should probably add a simple agent field here as well
            if (getFromAgent() != null)
                sb.append("   (from_agent " + getFromAgent().getLabel() + ")\n");
            if (getToAgent() != null)
                sb.append("   (to_agent " + getToAgent().getLabel() + ")\n");

            sb.append("   (task");
            e = getTasks();
            String notes = "\n";
            while (e.hasMoreElements()) {
                TaskBase t = (TaskBase)e.nextElement();
                sb.append(" " + t.getLabel());
                if (t.isVirtual())
                    notes += ";  ** Note: node " + t.getLabel() + " is virtual.\n";
            }
            sb.append(")" + notes);


            if (isNonLocal()) {
                if (getQualityDistribution() != null)
                    sb.append("   (quality_distribution " + getQualityDistribution().output() + ")\n");
                if (getTimeDistribution() != null)
                    sb.append("   (time_distribution " + getTimeDistribution().output() + ")\n");
                sb.append("   (nonlocal)\n");

            } else {
                // These don't really belong here, that's why they're commented out
                if (getQualityDistribution() != null)
                    sb.append(";   (quality_distribution " + getQualityDistribution().output() + ")\n");
                if (getTimeDistribution() != null)
                    sb.append(";   (time_distribution " + getTimeDistribution().output() + ")\n");

                sb.append("   (importance " + getImportance() + ")\n");
                sb.append("   (minimum_quality " + getMinimumQuality() + ")\n");
                sb.append("   (earliest_start_time " + getEarliestStartTime() + ")\n");
                sb.append("   (deadline " + getDeadline() + ")\n");
                
                // DTC has a fit if you specify a dont_interval for a non DONT commitment.
                //  Some people (me) like to use the dont_interval for Do commitments
                //  in the coordination protocol code.
                if ((getType() != null) && (getType().equalsIgnoreCase("dont"))) {
                    sb.append("   (dont_interval_start " + getDontIntervalStart() + ")\n");
                    sb.append("   (dont_interval_end " + getDontIntervalEnd() + ")\n");
                }

                sb.append("   (time_satisfied " + getTimeSatisfied() + ")\n");
            }

            sb.append(")\n");

        } else if (! isNonLocal()) {
            // Taems V1_0 "local"
            sb.append("(spec_commitment\n");

            if (v >= Taems.V1_0A) {
                sb.append(Node.attributesToString(attributes, v));
            }

            sb.append("   (label " + getLabel() + ")\n");
        
            sb.append("   (type " + getType() + ")\n");
            if (getFromAgent() != null)
                sb.append("   (agent " + getFromAgent().getLabel() + ")\n");
            sb.append("   (task");
            e = getTasks();
            String notes = "\n";
            while (e.hasMoreElements()) {
                TaskBase t = (TaskBase)e.nextElement();
                sb.append(" " + t.getLabel());
                if (t.isVirtual())
                    notes += ";  ** Note: node " + t.getLabel() + " is virtual.\n";
            }
            sb.append(")" + notes);

            sb.append("   (importance " + getImportance() + ")\n");
            sb.append("   (minimum_quality " + getMinimumQuality() + ")\n");
            sb.append("   (earliest_start_time " + getEarliestStartTime() + ")\n");
            sb.append("   (deadline " + getDeadline() + ")\n");
            
            // DTC has a fit if you specify a dont_interval for a non DONT commitment.
            //  Some people (me) like to use the dont_interval for Do commitments
            //  in the coordination protocol code.
            if ((getType() != null) && (getType().equalsIgnoreCase("dont"))) {
                sb.append("   (dont_interval_start " + getDontIntervalStart() + ")\n");
                sb.append("   (dont_interval_end " + getDontIntervalEnd() + ")\n");
            }
            
            sb.append("   (time_satisfied " + getTimeSatisfied() + ")\n");
            
            sb.append(")\n");

        } else {
            // Taems V1_0 "nonlocal"
            sb.append("(spec_nonlocal_commitment\n");

            if (v >= Taems.V1_0A) {
                sb.append(Node.attributesToString(attributes, v));
            }

            sb.append("   (label " + getLabel() + ")\n");

            sb.append("   (task");
            e = getTasks();
            String notes = "\n";
            while (e.hasMoreElements()) {
                TaskBase t = (TaskBase)e.nextElement();
                sb.append(" " + t.getLabel());
                if (t.isVirtual())
                    notes += ";  ** Note: node " + t.getLabel() + " is virtual.\n";
            }
            sb.append(")" + notes);
            
            if (getFromAgent() != null)
                sb.append("   (from_agent " + getFromAgent().getLabel() + ")\n");
            if (getToAgent() != null)
                sb.append("   (to_agent " + getToAgent().getLabel() + ")\n");
            
            if (getQualityDistribution() != null)
                sb.append("   (quality_distribution " + getQualityDistribution().output() + ")\n");
            if (getTimeDistribution() != null)
                sb.append("   (time_distribution " + getTimeDistribution().output() + ")\n");

            sb.append(")\n");
        }
        
        return sb.toString();
    }

  /**
   * Stringify
   */
  public String toString() {
    return toTTaems(Taems.VCUR);
  }
}
