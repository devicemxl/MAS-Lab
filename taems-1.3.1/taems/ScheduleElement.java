/* Copyright (C) 2005, University of Massachusetts, Multi-Agent Systems Lab
 * See LICENSE for license information
 */

/************************************************************
 * ScheduleElement.java
 ************************************************************/

package taems;

import java.util.*;
import java.io.*;
import java.awt.*;
import utilities.Distribution;
import utilities.Log;

/**
 * ScheduleElements populate Schedule objects.  Each schedule element
 * describes a particular execution instance in the schedule.  This
 * includes the method to perform, as well as expected performance
 * characteristics and monitoring information (if any).
 */
public class ScheduleElement implements Serializable, Cloneable, Comparable {
    static int currentID = 0;
    protected int ID;
  protected Method method = null;
  protected Distribution start_time = null;
  protected Distribution finish_time = null;
  protected Distribution quality = null;
  protected Distribution duration = null;
  protected Distribution cost = null;
  protected Vector quality_monitoring_info = null;
  protected Vector duration_monitoring_info = null;
  protected Schedule schedule = null;
  private ScheduleElementDisplay display = null;
  protected int actual_start = Integer.MIN_VALUE;
  protected int actual_finish = Integer.MIN_VALUE;
  protected Vector preconditions = new Vector();
  protected Vector dependingof = new Vector();
  protected Vector dependanceto = new Vector();
  protected boolean bindings=false;
  protected Hashtable attributes = new Hashtable();
  protected transient Log log = Log.getDefault();

  /**
   * Simple constructor.
   * @param m - Method object
   * @param st - distribution for the start time
   * @param ft - distribution for the finish time
   * @param qd - Qualitty distribution
   * @param dd - Duration distribution
   * @param cd - Cost distribution
   * @param qm - Quality Monitoring information
   * @param dm - Duration Monitoring information
   */
  public ScheduleElement(Method m, Distribution st, Distribution ft, Distribution qd, Distribution dd, Distribution cd, Vector qm, Vector dm) {
    setID(currentID++);
    method = m;
    start_time = st;
    finish_time = ft;
    quality = qd;
    duration = dd;
    cost = cd;
    quality_monitoring_info = qm;
    duration_monitoring_info = dm;
    display = new ScheduleElementDisplay(this);
  }

  /**
   * Blank constructor
   */
  public ScheduleElement() {
    setID(currentID++);
  }

  /**
   * Blank constructor
   */
  public ScheduleElement(int id) {
    setID(id);
  }

    /**
     * Serialization stuff
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        ScheduleElementDisplay temp = display;
	Log templog = log;
        display = null;
	log = null;
        out.defaultWriteObject();
        display = temp;
	log = templog;
    }
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.display = new ScheduleElementDisplay(this);
        log = log.getDefault();
    }

  /**
   * Accessor for label, start_time, finish_time, quality, duration, cost, 
   * quality_monitoring_info, duration_monitoring_info Distribution
   */
  public Method getMethod() { return method; }
    /**
     * Get the Schedule Element label (in fact the Method label)
     * @return String
     */
  public String getLabel() {
      if (getMethod() == null) return null;
      else return getMethod().getLabel();
  }

    /**
     * Set the unique ID for the ScheduleElement
     */
    protected void setID(int i) {
	ID = i;
    }

    /**
     * Get the unique ID for this instance
     */
    public int getID() { return(ID); }

    /**
     * Get the start time (Distribution !!!)
     * @return A Distribution
     */
  public Distribution getStartTime() { return start_time; }
    /**
     * Get the finish time distribution
     * @return A Distribution
     */
  public Distribution getFinishTime() { return finish_time; }
    /**
     * Get the quality distribution
     * @return A Distribution
     */
  public Distribution getQualityDistribution() { return quality; }
    /**
     * @param Get the duration distribution
     * @return A Distribution
     */
  public Distribution getDurationDistribution() { return duration; }
    /**
     * Get the cost distribution
     * @return A Distribution
     */
  public Distribution getCostDistribution() { return cost; }
    /**
     * return the earliest start time (computed by partial order scheduler).<P>
     * This function will not work unless the partial order scheduler has built the dependency.
     * @return int 
     */
  public int getEarliestStartTime() { 
      return(getMethod().getActualEarliestStartTime()); }
    /**
     * return the latest start time (computed by partial order scheduler).<P>
     * This function will not work unless the partial order scheduler has built the dependency.
     * @return int
     */
  public int getLatestStartTime() { return(getMethod().getActualLatestStartTime()); }
    /**
     * return the earliest finish time (computed by partial order scheduler).<P>
     * This function will not work unless the partial order scheduler has built the dependency.
     * @return int
     */
  public int getEarliestFinishTime() { return(getMethod().getActualEarliestFinishTime()); }
    /**
     * return the latest finish time (computed by partial order scheduler).<P>
     * This function will not work unless the partial order scheduler has built the dependency.
     * @return int
     */
  public int getLatestFinishTime() { return(getMethod().getActualLatestFinishTime()); }

    /**
     * @param
     * @return
     */
  public Vector getQualityMonitoringInfo() { return quality_monitoring_info; }
    /**
     * @param
     * @return
     */
  public Vector getDurationMonitoringInfo() { return duration_monitoring_info; }

    /**
     * Get the display of this element.
     */
  public ScheduleElementDisplay getDisplay() { return display; }

    /**
     * Set earliest start time
     * @param time to start
     */
    public void setEarliestStartTime(int time) { 
	int est = getEarliestStartTime();
	// Limitation of not exceeding EST
	est = Math.max(est,getMethod().getEarliestStartTime());
	est = Math.max(est,time);
	 if (est != getEarliestStartTime()) 
	     getMethod().setActualEarliestStartTime(est);
	 for (Enumeration e = dependanceto.elements(); 
	      e.hasMoreElements();){
	     ScheduleElement se = (ScheduleElement)e.nextElement();
	     se.setEarliestStartTime(getEarliestFinishTime());
	 }
     }
    

    /**
     * Set latest finish time
     * @param time to start
     */
    public void setLatestFinishTime(int time) { 
	int lft = getLatestFinishTime();
	// Limitation of not exceeding LFT
	lft = Math.min(lft,time);
	 if (lft != getLatestFinishTime()) 
	     getMethod().setActualLatestFinishTime(lft);
	 for (Enumeration e = dependingof.elements(); 
	      e.hasMoreElements();){
	     ScheduleElement se = (ScheduleElement)e.nextElement();
	     se.setLatestFinishTime(getLatestStartTime());
	 }
    }
    
    /**
     * @param
     * @return
     */
    public void setMethod(Method m) { method = m; }
    /**
     * @param
     * @return
     */
    public void setStartTime(Distribution d) { start_time = d; }
    /**
     * @param
     * @return
     */
    public void setFinishTime(Distribution d) { finish_time = d; }
    /**
     * @param
     * @return
     */
    public void setQualityDistribution(Distribution d) { quality = d; }
    /**
     * @param
     * @return
     */
    public void setDurationDistribution(Distribution d) { duration = d; }
    /**
     * @param
     * @return
     */
    public void setCostDistribution(Distribution d) { cost = d; }
    /**
     * @param
     * @return
     */
    public void setQualityMonitoringInfo(Vector v) { quality_monitoring_info = v; }
    /**
     * @param
     * @return
     */
    public void setDurationMonitoringInfo(Vector v) { duration_monitoring_info = v; }
    /**
     * @param
     * @return
     */
    public void addPrecondition(Precondition p) {
	if (preconditions == null) 
	    preconditions = new Vector();
	preconditions.addElement(p);
	if (p.getScheduleElement() != null) {
	    addDependingOf(p.getScheduleElement());
	    p.getScheduleElement().addDependanceTo(this);
	}
	else 
	    log.log("Hey why this precondition pointer is NULL",2);
    }

    /**
     * @param
     * @return
     */
    public void removePrecondition(Precondition p) {
	preconditions.removeElement(p);
	p.clear();
	if (p.getScheduleElement() != null) {
	    removeDependingOf(p.getScheduleElement());
	    p.getScheduleElement().removeDependanceTo(this);
	}
    }

    /**
     * @param
     * @return
     */
    public void addDependingOf(ScheduleElement se) {
	if (dependingof == null) 
	     dependingof = new Vector();
	if (!dependingof.contains(se))
	    dependingof.addElement(se);
    }

    /**
     * @param
     * @return
     */
    public Vector getDependingOf() { return dependingof; }

    /**
     * @param
     * @return
     */
    public Vector getDependanceTo() { return dependanceto; }

    /**
     * @param
     * @return
     */
    public void addDependanceTo(ScheduleElement se) {
	if (dependanceto == null) 
	     dependanceto = new Vector();
	if (!dependanceto.contains(se))
	    dependanceto.addElement(se);
    }
    /**
     * @param
     * @return
     */
    public void removeDependingOf(ScheduleElement se) {
	if (dependingof.contains(se))
	    dependingof.removeElement(se);
    }


    /**
     * @param
     * @return
     */
    public void removeDependanceTo(ScheduleElement se) {
	if (dependanceto.contains(se))
	    dependanceto.removeElement(se);
    }

    /**
     * @param
     * @return
     */
    public Enumeration getPreconditionsElement() { 
        if (numPreconditions() <= 0) return new Vector().elements();
        else return(preconditions.elements());
    }
    /**
     * @param
     * @return
     */
    public Vector getPreconditions() { return(preconditions); }
    /**
     * @param
     * @return
     */
    public void setPreconditions(Vector v) {
	for (Enumeration e = v.elements() ; e.hasMoreElements();) 
	    addPrecondition((Precondition)e.nextElement());
    }
    /**
     * @param
     * @return
     */
    public int numPreconditions() { return (getPreconditions() == null) ? 0 : getPreconditions().size(); }

    /**
     * True if all prerequisites of the ScheduleElement have been satisfied.
     * @param time - current time
     */
    public boolean checkPreconditions(int time) {
        Enumeration e = getPreconditionsElement();
        while (e.hasMoreElements()) {
            Precondition p = (Precondition)e.nextElement();
            boolean check = p.check(time);
            if (! check) {
                return false;
            } else {
                //System.err.println(" -> true");
            }
        }
        return true;
    }

    /**
     * Start and finish accessors
     */
    public void setActualStart(int s) { actual_start = s; }
    /**
     * @param
     * @return
     */
    public int getActualStart() { return actual_start; }
    /**
     * @param
     * @return
     */
    public void setActualFinish(int f) { 
	actual_finish = f; 
	Enumeration e = getPreconditionsElement();
        while (e.hasMoreElements()) {
            Precondition p = (Precondition)e.nextElement();
	    p.completed(f);
	}
    }
    /**
     * @param
     * @return
     */
    public int getActualFinish() { return actual_finish; }
    /**
     * @param
     * @return
     */
    public boolean hasStarted() { return (getActualStart() != Integer.MIN_VALUE); }
    /**
     * @param
     * @return
     */
    public boolean hasFinished() { return (getActualFinish() != Integer.MIN_VALUE); }

    /**
     * @param
     * @return
     */
    public boolean isBlocked() {
        if (hasFinished()) {
            if (getMethod() == null) {
                System.err.println("Warning: Method on " + getLabel() + " is null");
                return false;
            }
            if (getMethod().getCurrentQuality() <= 0) {
                return true;
            } else {
                return false;
            }

        } else {
            Enumeration e = getDependingOf().elements();
            while (e.hasMoreElements()) {
                ScheduleElement se = (ScheduleElement)e.nextElement();
                if (se.isBlocked()) {
                    return true;
                }
            }
        }

        return false;
    }

  /**
   * Gets the start time of the action
   * See getStartTime if you want the real expected start
   *  time distribution.
   * @see #getStartTime 
   */
  public int getStart() {
    if (start_time == null) 
      return -1;
    return ((int)start_time.calculateMin());
  }

  /**
   * Set the Start time
   * @param time The new start time
   */
  public void setStart(int time) {
    start_time = new Distribution(new Float(time), new Float(1.0));
  }

  /**
   * Returns the current known finish time of the action.
   * See getFinishTime if you want the real expected finish
   *  time distribution.
   * @return The finish time, or -1 if none specified
   * @see #getFinishTime
   */
  public int getFinish() {
    if (finish_time == null) 
      return -1;
    return ((int)finish_time.calculateMax());
  }

  /**
   * Set the Finish time 
   */
   public void setFinish(int time) {
    finish_time = new Distribution(new Float(time), new Float(1.0));
  }

  /**
   * Returns the mean expected quality of the schedule Element
   * @return The quality, or -1 if none specified
   */
  public float getQuality() {
    if (quality == null) 
      return -1;
    return (quality.calculateAvg());
   }

  /**
   * Returns the mean expected cost of the schedule Element
   * @return The cost, or -1 if none specified
   */
  public float getCost() {
    if (cost == null) 
      return -1;
    return (cost.calculateAvg());
   }

  /**
   * Returns the max expected duration of the schedule Element
   * @return The duration, or -1 if none specified
   */
  public int getDuration() {
    if (duration == null) 
      return -1;
    return ((int)duration.calculateMax());
   }

  /**
   * Sets the container schedule
   */
  public void setSchedule(Schedule s) { schedule = s; }

  /**
   * Gets the container schedule
   */
  public Schedule getSchedule() { return schedule; }

 	
  /**
   * Clone me.  Note if you are planning to use this elsewhere, you'll
   * have to retarget the schedule pointer.
   */
  public Object clone() {
      ScheduleElement cloned = new ScheduleElement();
      Enumeration e;

      cloned.setMethod(new VirtualMethod(getMethod().getLabel(), getMethod().getAgent()));

      if (getStartTime() != null)
          cloned.setStartTime((Distribution)getStartTime().clone());
      if (getFinishTime() != null)
          cloned.setFinishTime((Distribution)getFinishTime().clone());
      if (getQualityDistribution() != null)
          cloned.setQualityDistribution((Distribution)getQualityDistribution().clone());
      if (getDurationDistribution() != null)
          cloned.setDurationDistribution((Distribution)getDurationDistribution().clone());
      if (getCostDistribution() != null)
          cloned.setCostDistribution((Distribution)getCostDistribution().clone());

      cloned.setActualStart(getActualStart());
      cloned.setActualFinish(getActualFinish());

      if (getQualityMonitoringInfo() != null)
          cloned.setQualityMonitoringInfo((Vector)getQualityMonitoringInfo().clone());
      if (getDurationMonitoringInfo() != null)
          cloned.setDurationMonitoringInfo((Vector)getDurationMonitoringInfo().clone());

      cloned.setSchedule(getSchedule());
      // attributes
      for (e=getAttributes() ; e.hasMoreElements() ; ) {
	  Object key = e.nextElement();
	  Object value = getAttribute(key);
	  cloned.setAttribute(key,value);
      }
      cloned.setAttribute("Cloned",new Integer(getID()));
      // preconditions
      if (preconditions != null) {
	  Vector v = new Vector();
	  for (e = preconditions.elements(); e.hasMoreElements() ;) 
	      v.addElement(((Precondition)e.nextElement()).clone());
	  cloned.setPreconditions(v);
      }

      // dependingof
      if(dependingof.size() != 0) {
	  for (e = dependingof.elements(); e.hasMoreElements() ;) {
	      ScheduleElement se = (ScheduleElement)e.nextElement();
	      cloned.addDependingOf(new VirtualScheduleElement(se.getID()));
	  }
      }
      // dependanceto
      if(dependanceto.size() != 0) {
	  for (e = dependanceto.elements(); e.hasMoreElements() ;) {
	      ScheduleElement se = (ScheduleElement)e.nextElement();
	      cloned.addDependanceTo(new VirtualScheduleElement(se.getID()));
	  }
      }
      // bindings
      cloned.bindings = false;
      cloned.display = new ScheduleElementDisplay(cloned);
      return cloned;
  }

    /**
     * retargetVirtual
     */
    public void retargetVirtual(Schedule s, Vector clonedSE) {
	setSchedule(s);
	Enumeration e;
	if (s instanceof MLCSchedule) {
	    MLCSchedule mlc = (MLCSchedule)s;
	    // preconditions
	    for (e = preconditions.elements(); e.hasMoreElements() ;) {
		Precondition p = (Precondition)e.nextElement();
		p.retargetVirtual(mlc,clonedSE);
	}

	    // dependingof
	    s.retargetVirtualDependancy(dependingof,clonedSE);

	    // dependanceto
	    s.retargetVirtualDependancy(dependanceto,clonedSE);
	}
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
     * isResourceBind tell if this schedule element has a resource bindings
     * attached.
     */
    public boolean isResourceBind() {return bindings;}

    /**
     * setResourceBind set the resource bindings flags
     */
    public void setResourceBind(boolean b) { bindings = b ; }

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
   * Matches stuff
   * <BR>
   * This matches against:
   * <UL>
   * <LI> Method
   * <LI> Start time
   * <LI> Finish time
   * <LI> QualityDistribution
   * <LI> DurationDistribution
   * <LI> CostDistribution
   * <LI> ActualStart
   * <LI> ActualFinish
   * </UL>
   * @param s The schedule element to match against
   * @return true if they match
   */
  public boolean matches(ScheduleElement s) {

      if (s.getMethod() != null) {
          if (getMethod() == null) {
              return false;
          } else {
              if (! getMethod().matches(s.getMethod())) return false;
          }
      }

      if (!Node.matches(s.getStartTime(), getStartTime())) return false;
      if (!Node.matches(s.getFinishTime(), getFinishTime())) return false;
      if (!Node.matches(s.getQualityDistribution(), getQualityDistribution())) return false;
      if (!Node.matches(s.getDurationDistribution(), getDurationDistribution())) return false;
      if (!Node.matches(s.getCostDistribution(), getCostDistribution())) return false;

      if (!Node.matches(s.getActualStart(), getActualStart())) return false;
      if (!Node.matches(s.getActualFinish(), getActualStart())) return false;

      //if (!Node.matches(s.getQualityMonitoringInfo(), getQualityMonitoringInfo())) return false;
      //if (!Node.matches(s.getDurationMonitoringInfo(), getDurationMonitoringInfo())) return false;

      return true;
  }
  
  /**
   * Returns the ttaems version of the node
   * @param v The version number output style to use
   */
  public String toTTaems(float v) {
    StringBuffer sb = new StringBuffer("     (" + getLabel() + "\n");
    if (getMethod() == null) {
        sb.append(";     ** Note: method is null\n");  
    } else if (getMethod().isVirtual()) {
        sb.append(";     ** Note: method " + getLabel() + " is virtual\n");  
    }
    //sb.append(";         ** ID = " + getID() + "\n");

    if (v >= Taems.V1_0A) {
        sb.append(Node.attributesToString(attributes, v));
    }

    if (start_time != null)
      sb.append("        (start_time_distribution "+start_time.output()+")\n");

    if (hasStarted())
      sb.append(";        (actual_start "+getActualStart()+")\n");

    if (finish_time != null)
      sb.append("        (finish_time_distribution "+finish_time.output()+")\n");

    if (hasFinished())
      sb.append(";        (actual_finish "+getActualFinish()+")\n");
    
    if (quality != null)
      sb.append("        (quality_distribution "+quality.output()+")\n");
    
    if (duration != null)
      sb.append("        (duration_distribution "+duration.output()+")\n");
    
    if (cost != null)
      sb.append("        (cost_distribution "+cost.output()+")\n");
    
    if (quality_monitoring_info != null) {
        sb.append("        (quality_monitoring_info ");
        Enumeration e;
        e = quality_monitoring_info.elements();
        while (e.hasMoreElements())
            sb.append(e.nextElement().toString()+" ");
        sb.append(")\n");
    }
    
    if (duration_monitoring_info != null) { 
        sb.append("        (duration_monitoring_info ");
        Enumeration e;
        e = duration_monitoring_info.elements();
        while (e.hasMoreElements())
            sb.append(e.nextElement().toString()+" ");
        sb.append(")\n");
    }
 
    if ((numPreconditions() > 0) && (v >= Taems.V1_1)) {
        sb.append("        (preconditions\n");
        sb.append(";         " + numPreconditions() + " precondition(s)\n");
	Enumeration e = getPreconditionsElement();
	while (e.hasMoreElements()) {
		Precondition p = (Precondition)e.nextElement();
		sb.append(p.toTTaems(v));
	}
        sb.append("        )\n");
    }
    sb.append("     )\n");
 
    return sb.toString();
  }

  /**
   * Stringify
   */

//   public String stringify() {
//       String print = "";
//       boolean ANSI=false;
//       if (hasFinished()) {
// 	  ANSI =true;
// 	  print = print + Log.ANSI_S + Log.ANSI_C_PNK;
//       }
//       else if (hasStarted()) {
// 	  ANSI =true;
// 	  print = print + Log.ANSI_S + Log.ANSI_C_GRN;
//       }
//       print = print + "-" + dependingof.size() + "@" + getID() + " [ (";
//       if (getEarliestStartTime() == Integer.MIN_VALUE)
// 	  print = print + "?";
//       else
// 	  print = print + getEarliestStartTime();
//       if (getOptimalEarliestStartTime() == Integer.MIN_VALUE)
// 	  print = print + "{?}, ";
//       else
// 	  print = print + "{"+getOptimalEarliestStartTime()+"}, ";
//       if (getLatestStartTime() == Integer.MIN_VALUE)
// 	  print = print + "?";
//       else
// 	  print = print + getLatestStartTime();
//       if (getOptimalLatestStartTime() == Integer.MIN_VALUE)
// 	  print = print + "{?}) ";
//       else
// 	  print = print + "{" + getOptimalLatestStartTime() +"}) ";
//       print = print + getStart() + " - " + 
// 	  getLabel() + " - " + getFinish() + " (" ;
//       if (getEarliestFinishTime() ==  Integer.MAX_VALUE)
// 	  print = print + "?";
//       else
// 	  print = print + getEarliestFinishTime();
//       if (getOptimalEarliestFinishTime() == Integer.MAX_VALUE)
// 	  print = print + "{?}, ";
//       else
// 	  print = print + "{"+getOptimalEarliestFinishTime()+"}, ";
//       if (getLatestFinishTime() ==  Integer.MAX_VALUE)
// 	  print = print + "?";
//       else 
// 	  print = print + getLatestFinishTime();
//       if (getOptimalLatestFinishTime() == Integer.MAX_VALUE)
// 	  print = print + "{?}) ] ";
//       else
// 	  print = print + "{" + getOptimalLatestFinishTime() +"}) ] ";
//       if (ANSI)
// 	  print = print + Log.ANSI_F ;
//       return (print+ dependanceto.size() +"-");
//   }

  public String stringify() {
       String print = "";
       boolean ANSI=false;
       if (hasFinished()) {
 	  ANSI =true;
 	  print = print + Log.ANSI_S + Log.ANSI_C_PNK;
       }
       else if (hasStarted()) {
 	  ANSI =true;
 	  print = print + Log.ANSI_S + Log.ANSI_C_GRN;
       }
       print = print + " [ (";
       if (getEarliestStartTime() == Integer.MIN_VALUE)
 	  print = print + "?, ";
       else
 	  print = print + getEarliestStartTime() + ", " ;
       if (getLatestStartTime() == Integer.MIN_VALUE)
 	  print = print + "?) ";
       else
 	  print = print + getLatestStartTime()  + ") ";
       print = print + getStart() + " - " + 
 	  getLabel() + " - " + getFinish() + " (" ;
       if (getEarliestFinishTime() ==  Integer.MAX_VALUE)
 	  print = print + "?, ";
       else
 	  print = print + getEarliestFinishTime() + ", ";
       if (getLatestFinishTime() ==  Integer.MAX_VALUE)
 	  print = print + "?) ]";
       else 
 	  print = print + getLatestFinishTime() + ") ] ";
       if (ANSI)
 	  print = print + Log.ANSI_F ;
       return (print);
   }

  public String toString() {
    return toTTaems(Taems.VCUR);
  }
 
  /**
   * compares this ScheduleElement to another, based on start time and finish
   * time.
   */
  public int compareTo(Object o)
  {
      if (!(o instanceof ScheduleElement))
          throw new ClassCastException();
      ScheduleElement e = (ScheduleElement) o;
      if (getStart() < e.getStart())
          return -1;
      if (getStart() > e.getStart())
          return 1;
      if (getFinish() < e.getFinish())
          return -1;
      if (getFinish() > e.getFinish())
          return 1;
      return getLabel().compareTo(e.getLabel());
  }

    // Optimal Computation of EST and LST
    // Crap crap crap
        //  protected int oest = Integer.MIN_VALUE;
    //  protected int olst = Integer.MIN_VALUE;
    //  protected int oeft = Integer.MAX_VALUE;
    //  protected int olft = Integer.MAX_VALUE;

  public int getOptimalEarliestStartTime() { 
      if(hasAttribute("OEST"))
	  return(((Integer)getAttribute("OEST")).intValue());
      else
	  return(Integer.MIN_VALUE);
  }
  public int getOptimalLatestStartTime() { 
      if(hasAttribute("OLST"))
	  return(((Integer)getAttribute("OLST")).intValue());
      else
	  return(Integer.MIN_VALUE);
  }
  public int getOptimalEarliestFinishTime() { 
      if(hasAttribute("OEFT"))
	  return(((Integer)getAttribute("OEFT")).intValue());
      else
	  return(Integer.MAX_VALUE);
  }
  public int getOptimalLatestFinishTime() { 
      if(hasAttribute("OLFT"))
	  return(((Integer)getAttribute("OLFT")).intValue());
      else
	  return(Integer.MAX_VALUE);
  }

    protected static Vector propagationList = new Vector();
    protected boolean updated  = false;

    public void resetPropagation() {
	if (!propagationList.contains(this)) {
	    propagationList.add(this);
	    int recursion = propagationList.size();
	    String print = "\t";
	    for (int i = 0; i < recursion ; i ++)
		print += "\t";
	    if (updated) {
	    	updated = false;
	    }
		
	    int est = getOptimalEarliestStartTime();
	    int eft = getOptimalEarliestFinishTime();
	    int lst = getOptimalLatestStartTime();
	    int lft = getOptimalLatestFinishTime();
	    if (log.getLogLevel() > 2) 
		log.log(print +"This = " + stringify(),3);
	    for (Enumeration e = dependingof.elements(); 
		 e.hasMoreElements();){
		ScheduleElement se = (ScheduleElement)e.nextElement();
		if (log.getLogLevel() > 2) 
		    log.log(print + "<- se = " + se.stringify());
		se.resetPropagation();
		if (log.getLogLevel() > 2) 
		    log.log(print + "<- se = " + se.stringify());
		    if (se.getOptimalEarliestFinishTime() != Integer.MAX_VALUE)
			est = Math.max(est,se.getOptimalEarliestFinishTime());
		    if (se.getOptimalLatestFinishTime() != Integer.MAX_VALUE)
			lst = Math.max(lst,se.getOptimalLatestFinishTime());
	    }
	    for (Enumeration e = dependanceto.elements(); 
		 e.hasMoreElements();){
		ScheduleElement se = (ScheduleElement)e.nextElement();
		if (log.getLogLevel() > 2) 
		    log.log(print + "-> se = " + se.stringify());
		se.resetPropagation();
		if (log.getLogLevel() > 2) 
		    log.log(print + "-> se = " + se.stringify());
		if (se.getOptimalEarliestStartTime() != Integer.MIN_VALUE)
		    eft = Math.min(eft,se.getOptimalEarliestStartTime());
		if (se.getOptimalLatestStartTime() != Integer.MIN_VALUE)
			lft = Math.min(lft,se.getOptimalLatestStartTime());
	    }

	    if (log.getLogLevel() > 2) {
		log.log(print + "est = " + est);
		log.log(print + "eft = " + eft);
		log.log(print + "lst = " + lst);
		log.log(print + "lft = " + lft);
	    }
	    if (est != getOptimalEarliestStartTime()) {
		setOEST(est);
	    }
	    else if (eft != getOptimalEarliestFinishTime()) {
		setOEFT(eft);
	    }
	    if (lst != getOptimalLatestStartTime()) {
		setOLST(lst);
	    }
	    else if (lft != getOptimalLatestFinishTime()) {
		setOLFT(lft);
	    }
	    propagationList.removeElement(this);
	}
    }
    
    private void setOptimalEarliestStartTime(int time) {
	setOEST(time);
	for (Enumeration e = dependingof.elements(); 
	     e.hasMoreElements();){
	    ScheduleElement se = (ScheduleElement)e.nextElement();
	    se.setOptimalEarliestFinishTime(getOptimalEarliestStartTime(),false);
	}
    }

    public void setOptimalEarliestStartTime(int time, boolean propagation) { 
	if (!updated) {
	    updated = true;
	    setOptimalEarliestStartTime(time);
	    setOptimalEarliestFinishTime(getOptimalEarliestFinishTime());
	}
	if (propagation)
	    resetPropagation();
    }
    
    private void setOptimalEarliestFinishTime(int time) {
	setOEFT(time);
	for (Enumeration e = dependanceto.elements(); 
	     e.hasMoreElements();){
	    ScheduleElement se = (ScheduleElement)e.nextElement();
	    se.setOptimalEarliestStartTime(getOptimalEarliestFinishTime(),false);
	}
    }

    public void setOptimalEarliestFinishTime(int time,boolean propagation) {
	if (!updated) {
	    updated = true;
	    setOptimalEarliestFinishTime(time);
	    setOptimalEarliestStartTime(getOptimalEarliestStartTime());
	}
	if (propagation)
	    resetPropagation();
    }
    
    private void setOptimalLatestStartTime(int time) {
	setOLST(time);
	for (Enumeration e = dependingof.elements(); 
	     e.hasMoreElements();){
	    ScheduleElement se = (ScheduleElement)e.nextElement();
	    se.setOptimalLatestFinishTime(getOptimalLatestStartTime(),false);
	}
    }


    public void setOptimalLatestStartTime(int time, boolean propagation) {
	if (!updated) {
	    updated = true;
	    setOptimalLatestStartTime(time);
	    setOptimalLatestFinishTime(getOptimalLatestFinishTime());
	}
	if (propagation)
	    resetPropagation();
    }

    private void setOptimalLatestFinishTime(int time) {
	setOLFT(time);
	for (Enumeration e = dependanceto.elements(); 
	     e.hasMoreElements();){
	    ScheduleElement se = (ScheduleElement)e.nextElement();
	    se.setOptimalLatestStartTime(getOptimalLatestFinishTime(),false);
	}
    }
    
    public void setOptimalLatestFinishTime(int time,boolean propagation) { 
	if (!updated) {
	    updated = true;
	    setOptimalLatestFinishTime(time);
	    setOptimalLatestStartTime(getOptimalLatestStartTime());
	}
	if (propagation)
	    resetPropagation();
    }

    private void setOEST(int time) {
	int value = time;
	if (time < 0) {
	    log.log("Warning someone is trying to set a negative OEST (" + time +") Ignoring it OEST set to 0 for SE " + getLabel(), Log.LOG_WARNING);
	    value = 0;
	}
	setAttribute("OEST", new Integer(value));
	setAttribute("OEFT", new Integer(value + (int) (getMethod().getGlobalOutcome().getDuration().calculateMin())));
    }

    private void setOEFT(int time) {
	int valueEFT = time;
	int valueEST = time - (int) (getMethod().getGlobalOutcome().getDuration().calculateMin());
	if (valueEST < 0) {
	    log.log("Warning someone is trying to set a incorrect OEFT (" + time +") that will create an negative OEST (" + valueEST + ") Ignoring it OEFT set to " + (time - valueEST) + " for SE " + getLabel(), Log.LOG_WARNING);
	    valueEFT = (int) (getMethod().getGlobalOutcome().getDuration().calculateMin());
	    valueEST = 0;
	}
	setAttribute("OEFT", new Integer(valueEFT));
	setAttribute("OEST", new Integer(valueEST));
    }
    
    private void setOLST(int time) {
	setAttribute("OLST", new Integer(time));
	setAttribute("OLFT", new Integer(time + (int) (getMethod().getGlobalOutcome().getDuration().calculateMax())));
    }

    private void setOLFT(int time) {
	setAttribute("OLFT", new Integer(time));
	setAttribute("OLST", new Integer(time - (int) (getMethod().getGlobalOutcome().getDuration().calculateMax())));
    }
}
