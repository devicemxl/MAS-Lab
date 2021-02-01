/* Copyright (C) 2005, University of Massachusetts, Multi-Agent Systems Lab
 * See LICENSE for license information
 */

/************************************************************
 * Schedule.java
 ************************************************************/

package taems;

import java.util.*;
import java.io.*;
import java.awt.*;
import utilities.Distribution;
import utilities.Log;

/**
 * Schedule object.  This should contain some data describing the quantifiable
 * characteristics of the schedule (e.g. Expected QCD, start time) in addition
 * to the list of ScheduleElements that are to be executed.
 */
public class Schedule implements Serializable, Cloneable {
  protected final int CLUSTERING=3;
  protected Vector schedule_elements = new Vector();
  protected Vector task_quality_info;
  protected Vector commitment_info;
  protected Distribution quality;
  protected Distribution duration;
  protected Distribution cost;
  protected Distribution finish;
  protected Distribution start;
  protected double rating = Double.NEGATIVE_INFINITY;
  protected ScheduleDisplay display;

  /**
   * Constructor.
   * @param se The list of SceduleElements comprising the actual schedule
   * @param tqi Task quality information
   * @param ci Commitment information
   * @param qd Expected quality distribution
   * @param dd Expected duration distribution
   * @param cd Expected cost distribution
   */
  public Schedule(Vector se, Vector tqi, Vector ci, Distribution qd, Distribution dd, Distribution cd) {
    schedule_elements = se; 
    task_quality_info = tqi;
    commitment_info = ci;
    quality = qd;
    duration = dd;
    cost = cd;
  }

  /**
   * Blank constructor
   */
  public Schedule() {
    schedule_elements = new Vector();
    task_quality_info = null;
    commitment_info = null;
    quality = null;
    duration = null;
    cost = null;
  }

  /**
   * Accessor for the task quality info.  This data is currently not produced by
   * the DTC scheduler.
   */
  public void setTaskQualityInfo(Vector s) {
    task_quality_info = s;
  }
  public Vector getTaskQualityInfo() {
    return task_quality_info;
  }


  /**
   * Accessor for the commitment info.  This data is currently not produced by
   * the DTC scheduler.
   */
  public void setCommitmentInfo(Vector s) {
    commitment_info = s;
  }
  public Vector getCommitmentInfo() {
    return commitment_info;
  }

  /** 
   * Accessor for quality 
   */
  public void setQuality(Distribution d){ quality = d; }
  public Distribution getQuality(){ return quality; }
  /** 
   * Accessor for duration 
   */
  public void setDuration(Distribution d){ duration = d; }
  public Distribution getDuration(){ return duration; }

  /** 
   * Accessor for cost 
   */
  public void setCost(Distribution d){ cost = d; }
  public Distribution getCost(){ return cost; }

  /** 
   * Accessor for rating
   */
  public void setRating(double d) { rating = d; }
  public double getRating() {return rating; }

  /**
   * Returns an Enumeration of Schedule Element for this schedule.
   */ 
  public Enumeration getElements() {
    if (getScheduleElements() != null)
      return getScheduleElements().elements();
    else
      return (new Vector(0)).elements();
  }

  /**
   * Returns a sorted Enumeration of Schedule Element for this schedule.
   */ 
  public Enumeration getElementsSorted() {
          
      return sortElements(getScheduleElements());
  }

    /**
     * returns a sorted Enumeration of ScheduleElements for the given vector
     */ 
    protected Enumeration sortElements(Vector se) {
      if (se == null)
          return (new Vector(0)).elements();
      Vector v = new Vector(se.size());
      
      for (int i = 0; i < se.size(); i++) {
          int j;
          ScheduleElement n = (ScheduleElement)se.elementAt(i);
          for (j = 0; j < v.size(); j++) {
              ScheduleElement c = (ScheduleElement)v.elementAt(j);
              if (n.getStart() <= c.getStart())
                  break;
          }
          v.insertElementAt(n, j);
      }

      return v.elements();
  }


  /**
   * clears out the schedule
   */
  public void removeAllElements() {
    schedule_elements.removeAllElements();
  }

  /**
   * give me the vector
   */
  public Vector getScheduleElements() {
    return schedule_elements;
  }

  /**
   * set the vector
   */
  public void setScheduleElements(Vector v) {
    schedule_elements = v;
    Enumeration e = getScheduleElements().elements();
    while (e.hasMoreElements()) {
      ScheduleElement se = (ScheduleElement)e.nextElement();
      se.setSchedule(this);
    }
  }

  /**
   * sets a schedule item, shifting those behind it back
   * one space
   */
  public void insertScheduleElement(ScheduleElement e, int i) {
    schedule_elements.insertElementAt(e, i);
    e.setSchedule(this);
  }

  /**
   * adds a scheduled item
   */
  public void addScheduleElement(ScheduleElement e) {
    schedule_elements.addElement(e);
    e.setSchedule(this);
  }

  /**
   * removes a scheduled item
   */
  public void removeScheduleElement(ScheduleElement e) {
    schedule_elements.removeElement(e);
    e.setSchedule(null);
  }

  /**
   * return the schedule element that has the matched label
   */
  public ScheduleElement getScheduleElement(String l) {
    Enumeration list = getElements(); 
    while ( list.hasMoreElements() ) {
      ScheduleElement se = (ScheduleElement) list.nextElement();
      if (se.getLabel().equals(l)) { 
	return se;
      }
    }	
    return null;
  }

  /**
   * Return the first schedule element which has not completed.
   * @param time The current time
   * @return The element, or null if none found
   */
  public ScheduleElement getNextTask(int time) {
      Enumeration list = getNextTasks(time);
      if ((list != null) && list.hasMoreElements())
	  return((ScheduleElement)list.nextElement());
      return null;
  }

  /**
   * Return all schedule element which have not been started
   * and have start times >= current time;
   * @param time The current time
   * @return An enumeration of elements to be run (may just
   *  be an empty enumeration).
   */
  public Enumeration getNextTasks(int time) {
    Enumeration list = getElements(); 
    Vector answer = new Vector();
    while ( list.hasMoreElements() ) {
      ScheduleElement se = (ScheduleElement) list.nextElement();
      if (!se.hasStarted()) {
          // If no start times are specified, just add one and stop
          if ((se.getStartTime() == null) || se.getStartTime().containsValue(-1, 0)) {
              answer.addElement(se);
              break;

          // otherwise figure out which ones should be run
          } else if (se.getStartTime().containsValue(time, 0) || (time >= se.getStart())) {
              answer.addElement(se);
          }
      }
    }
    return(sortElements(answer));
  }

  /**
   * Returns true if the schedule is started (that is,
   * any of the schedule elements have been started).
   */
  public boolean isStarted() {
    ScheduleElement se;

    // Check
    Enumeration list = getElements(); 
    while ( list.hasMoreElements() ) {
      se = (ScheduleElement) list.nextElement();
      if (se.hasStarted()) {
	  return true;
      }
    }

    return false;
  }

  /**
   * Returns true if the schedule is completed (that is,
   * all the schedule elements have finished).
   */
  public boolean isCompleted() {
    ScheduleElement se;

    // Make simple check of last element
    Vector v = getScheduleElements();
    if ((v != null) && (v.size() > 0)) {
      se = (ScheduleElement)v.lastElement();
      if (!se.hasFinished()) return false;
    }

    // If that's ok, check em all
    Enumeration list = getElements(); 
    while ( list.hasMoreElements() ) {
      se = (ScheduleElement) list.nextElement();
      if (!se.hasFinished()) return false;
    }

    return true;
  }

  /**
   * Returns true if the schedule is blocked (that is,
   * all the schedule elements have finished or all those
   * that haven't finished are blocked).
   */
  public boolean isBlocked() {
    ScheduleElement se;

    if (isCompleted()) {
        return true;
    }

    // If that's ok, check em all
    Enumeration list = getElements(); 
    while ( list.hasMoreElements() ) {
      se = (ScheduleElement) list.nextElement();
      if ((!se.hasFinished()) && (!se.isBlocked())) {
          return false;
      }
    }

    return true;
  }

  /**
   * return the schedule element that has the given index.
   */
  public ScheduleElement getScheduleElement(int i) {
    return (ScheduleElement)schedule_elements.elementAt(i);
  }

  /**
   * return the schedule element's location in the schedule
   * (starting with 0) or -1 if not found.
   */
  public int getScheduleElementIndex(ScheduleElement se) {
    return schedule_elements.indexOf(se);
  }

  /**
   * Returns the number of methods in this schedule.
   */
  public int size() {
    if (schedule_elements != null)
      return(schedule_elements.size());
    else
      return 0;
  }

  /**
   * is the method in the schedule?
   */
  public boolean containsTask(String l) {
    Enumeration list = getElements(); 
    while ( list.hasMoreElements() ) {
      ScheduleElement se = (ScheduleElement) list.nextElement();
      if (se.getLabel().equals(l)) { 
	return true;
      }
    }	
    return false;
  }

    public boolean containsTask(ScheduleElement se) {
	return(schedule_elements.contains(se));
    }

  /**
   * Sets the start time, offsetting the element start and finish
   * times appropriately.
   * <p>
   * This function assumes the initial start time of the schedule
   * is whatever start is set to by default (usually 0).
   */
  public void setStartTime(int s) {
      Enumeration e = getScheduleElements().elements();
      if (e.hasMoreElements()) {
          int offset = s - getScheduleElement(0).getStart();
	  if (start != null) 
	      setStart(getStart().applyOffset((float)offset));
          while (e.hasMoreElements()) {
              ScheduleElement se = (ScheduleElement)e.nextElement();
              se.setStartTime(se.getStartTime().applyOffset((float)offset));
              se.setFinishTime(se.getFinishTime().applyOffset((float)offset));
          }
      }
  }
    
    /**
     * Set the Start Time Distribution of a schedule.
     */
    public void setStart(Distribution d) { start = d; }


    /**
     * Delaying the finish time of a defined ScheduleElement 
     * @param ScheduleElement schelmt, the starting point of the delay
     * @param int delay
     * @return Vector of ScheduleElement delayed
     */
     public Vector delayFinishTime(ScheduleElement schelement, int s) {
	 boolean needToDelayed = false;
	 Vector v = new Vector();
	 Enumeration e = getScheduleElements().elements();
	 if (e.hasMoreElements()) {
	     int offset = s;
	     while (e.hasMoreElements()) {
		 ScheduleElement se = (ScheduleElement)e.nextElement();
		 if (se == schelement) {
		     needToDelayed=true;
		     if (!schelement.hasAttribute("FinishTime"))
			 se.setAttribute("FinishTime", se.getFinishTime());
		     se.setFinishTime(se.getFinishTime().applyOffset((float)offset));
		     if (!schelement.hasAttribute("DurationDistribution"))
			 se.setAttribute("DurationDistribution", se.getDurationDistribution());
		     se.setDurationDistribution(se.getDurationDistribution().applyOffset((float)offset));
		 }
		 else {
		     if (needToDelayed) {
			 if (!schelement.hasAttribute("StartTime"))
			     se.setAttribute("StartTime", se.getStartTime());
			 se.setStartTime(se.getStartTime().applyOffset((float)offset));
			 if (!schelement.hasAttribute("FinishTime"))
			     se.setAttribute("FinishTime", se.getFinishTime());
			 se.setFinishTime(se.getFinishTime().applyOffset((float)offset));
			 v.addElement(se);
		     }
		 }
	     }
	 }
	 return(v);
     }


    /**
     * Delaying from a defined ScheduleElement from an offset
     * @param ScheduleElement schelmt, the starting point of the delay
     * @param int delay
     * @return Vector of ScheduleElement delayed
     */
     public Vector delayStartTime(ScheduleElement schelement, int s) {
	 boolean needToDelayed = false;
	 Vector v = new Vector();
	 Enumeration e = getScheduleElements().elements();
	 if (e.hasMoreElements()) {
	     int offset = s;
	     while (e.hasMoreElements()) {
		 ScheduleElement se = (ScheduleElement)e.nextElement();
		 if (se == schelement) 
		     needToDelayed=true;
		 if (needToDelayed) {
		     se.setAttribute("StartTime", se.getStartTime());
		     se.setStartTime(se.getStartTime().applyOffset((float)offset));
		     se.setAttribute("FinishTime", se.getFinishTime());
		     se.setFinishTime(se.getFinishTime().applyOffset((float)offset));
		     v.addElement(se);
		 }
	     }
	 }
	 return(v);
     }

    /**
     * undeleteTiming, when you reset the start time of 
     * a schedule and for any reasons wants to go back, this function
     * will do the jobs.
     */
      public void undeleteTiming(Vector v) {
        Enumeration e = v.elements();
        while(e.hasMoreElements()) {
            ScheduleElement se = (ScheduleElement)e.nextElement();
            undeleteTiming(se);
        }
    }
  
    /**
     * undeleteTiming, when you reset the start time of 
     * a schedule and for any reasons wants to go back, this function
     * will do the jobs.
     */
    public void undeleteTiming(ScheduleElement se) {

        if (se.hasAttribute("StartTime")) {
            Distribution d = (Distribution)se.getAttribute("StartTime");
            se.setStartTime(d);
            se.removeAttribute("StartTime");
        }
        if (se.hasAttribute("FinishTime")) {
            Distribution d = (Distribution)se.getAttribute("FinishTime");
            se.setFinishTime(d);
            se.removeAttribute("FinishTime");
        }
    }

    public void deleteHistory(Vector v) {
        Enumeration e = v.elements();
        while(e.hasMoreElements()) {
            ScheduleElement se = (ScheduleElement)e.nextElement();
            deleteHistory(se);
        }
    }

    public void deleteHistory(ScheduleElement se) {
     if (se.hasAttribute("StartTime")) {
	 se.removeAttribute("StartTime");
     }
     if (se.hasAttribute("FinishTime")) {
	 se.removeAttribute("FinishTime");
     }
     if (se.hasAttribute("DurationDistribution")) {
	 se.removeAttribute("DurationDistribution");
     }
 }

    
    /**
     * Sets the finish time, this is read from the scheduler
     */
    public void setFinish(Distribution d) { finish = d;}

  /**
   * Gets the supposed schedule start time
   * @return the start time, or -1 if not found
   */
  public int getStartTime() { 
      if (start == null) {
	  if (size() > 0) {
		  ScheduleElement se = 
		      (ScheduleElement)getScheduleElements().firstElement();
		  return (se.getStart());
	  } else {
		return -1;
	  }
      }
      return((int)start.calculateMin()); 
  }

  public Distribution getStart() { return start; }

  /**
   * Gets the supposed scheduler finish time
   */
    public int getFinishTime() {  
	if (finish == null) {
	    ScheduleElement se = 
		(ScheduleElement)getScheduleElements().lastElement();
	    return (se.getFinish());
	}
	return((int)finish.calculateMax()); 
    }
    public Distribution getFinish() { return finish; }


  /**
   * Gets the total duration for the this schedule
   */
    public int getTotalDuration() {
	if (duration == null) {
	  ScheduleElement se = (ScheduleElement)getScheduleElements().lastElement();
	  return (se.getFinish() - getStartTime()); 
	}
	return ((int)getDuration().calculateMax());
    }	

    /**
     * Fixes up pre and post conditions of all the schedule elements,
     * to the extent that it is possible at this scope.
     * @param The schedule to query for link fixes, or null to use
     * this schedule.
     */
    public void fixConditions(Schedule query) {
        if (query == null) query = this;

        Enumeration e = getElements();
        while (e.hasMoreElements()) {
            ScheduleElement se = (ScheduleElement)e.nextElement();

            Enumeration pree = se.getPreconditionsElement();
            while (pree.hasMoreElements()) {
                Precondition p = (Precondition)pree.nextElement();
                if (this instanceof MLCSchedule)
                    p.setParentMLC((MLCSchedule)this);
                if (p instanceof PrecedencePrecondition) {
                    ScheduleElement x = query.getScheduleElement(((PrecedencePrecondition)p).getScheduleElement().getLabel());
                    if (x != null)
                        ((PrecedencePrecondition)p).setScheduleElement(x);
                }
            }

	}
    }

  /**
   * Clone me.
   */
  public Object clone() {
    Schedule cloned = new Schedule();

    Enumeration e = getElements();
    while (e.hasMoreElements()) {
        cloned.addScheduleElement((ScheduleElement)((ScheduleElement)e.nextElement()).clone());
    }

    if (task_quality_info != null)
        cloned.setTaskQualityInfo((Vector)task_quality_info.clone());
    if (commitment_info != null)
        cloned.setCommitmentInfo((Vector)commitment_info.clone());

    if (getQuality() != null)
        cloned.setQuality((Distribution)getQuality().clone());
    if (getDuration() != null)
        cloned.setDuration((Distribution)getDuration().clone());
    if (getCost() != null)
        cloned.setCost((Distribution)getCost().clone());

    cloned.setRating(getRating());
    //cloned.setStartTime(getStartTime());
    //cloned.start = getStartTime();
    if (getStart() != null)
        cloned.setStart((Distribution)getStart().clone());
    if (getFinish() != null)
        cloned.setFinish((Distribution)getFinish().clone());
    cloned.display = display;

    e = cloned.getElements();
    while (e.hasMoreElements()) {
        ScheduleElement se = (ScheduleElement)e.nextElement();
	se.retargetVirtual(cloned,cloned.schedule_elements);
    }
    return cloned;
  }

    /**
     * clone with Virtual clones only the schedule and use replace
     * the set of ScheduleElement by VirtualScheduleElement 
     * to replace them instead of cloning all the ScheduleElement
     * Do not use, this method is intended for internal use only.
     */ 
    protected Object cloneWithVirtual() {
	Schedule cloned = new Schedule();
	
	Enumeration e = getElements();
	while (e.hasMoreElements()) {
	    cloned.addScheduleElement(new VirtualScheduleElement(((ScheduleElement)e.nextElement()).getID()));
	}
	
	if (task_quality_info != null)
	    cloned.setTaskQualityInfo((Vector)task_quality_info.clone());
	if (commitment_info != null)
	    cloned.setCommitmentInfo((Vector)commitment_info.clone());
	
	if (getQuality() != null)
	    cloned.setQuality((Distribution)getQuality().clone());
	if (getDuration() != null)
	    cloned.setDuration((Distribution)getDuration().clone());
	if (getCost() != null)
        cloned.setCost((Distribution)getCost().clone());
	
	cloned.setRating(getRating());
	//cloned.setStartTime(getStartTime());
    //cloned.start = getStartTime();
	if (getStart() != null)
	    cloned.setStart((Distribution)getStart().clone());
	if (getFinish() != null)
	    cloned.setFinish((Distribution)getFinish().clone());
	cloned.display = display;
	return cloned;
    }

    /**
     * This function retarget the virtual Schedule Element
     */
    protected void retargetVirtual(Vector clonedSE) {
	retargetVirtualDependancy(schedule_elements,clonedSE);
    }

    /**
     * Replace the VirtualSchedulementElement in this dependance vector
     * Side Effect: change the Vector dependance.
     */
    protected void retargetVirtualDependancy(Vector dependance, Vector clonedSE) {
	Vector toberemoved = new Vector();
	Vector tobereplaced = new Vector();
	Enumeration e1;
	for (e1 = dependance.elements(); e1.hasMoreElements() ;) {
	    ScheduleElement se1 = (ScheduleElement)e1.nextElement();
	    if (se1 instanceof VirtualScheduleElement) {
		for (Enumeration e = clonedSE.elements() ; e.hasMoreElements(); ) {
		    ScheduleElement se = (ScheduleElement)e.nextElement();
		    if (((VirtualScheduleElement)se1).matches(se)) {
			if (!tobereplaced.contains(se))
			    tobereplaced.add(se);
			toberemoved.addElement(se1);
		    }
		}
	    }
	}
	for (e1 = toberemoved.elements() ; e1.hasMoreElements(); )
	    dependance.removeElement(e1.nextElement());
	for (e1 = tobereplaced.elements() ; e1.hasMoreElements(); )
	    dependance.addElement(e1.nextElement());
    }
	

  /**
   * Matches stuff
   * <BR>
   * This matches against:
   * <UL>
   * <LI> Start
   * <LI> Finish
   * <LI> Quality
   * <LI> Duration
   * <LI> Cost
   * <LI> Rating
   * <LI> Schedule elements (order counts)
   * </UL>
   * @param s The schedule to match against
   * @return true if they match
   */
  public boolean matches(Schedule s) {

      if (s.size() != size()) return false;

      if (!Node.matches(s.getStart(), getStart())) return false;
      if (!Node.matches(s.getFinish(), getFinish())) return false;

      if (!Node.matches(s.getQuality(), getQuality())) return false;
      if (!Node.matches(s.getDuration(), getDuration())) return false;
      if (!Node.matches(s.getCost(), getCost())) return false;
      if (!Node.matches(s.getRating(), getRating())) return false;

      Enumeration e1 = getElements();
      Enumeration e2 = s.getElements();
      while (e1.hasMoreElements()) {
          ScheduleElement se1 = (ScheduleElement)e1.nextElement();
          ScheduleElement se2 = (ScheduleElement)e2.nextElement();

          if (! se1.matches(se2)) return false;
      }

      return true;
  }

    /**
     * calculateDuration
     * @calculates the duration distribution of the MLC
     */
    protected void calculateDuration() {
	
      Vector v_duration = new Vector();
      Vector s_duration = new Vector();
      float value = 0;
      Enumeration e = getElements();
      while (e.hasMoreElements()) {
	  ScheduleElement se = (ScheduleElement)e.nextElement();
	  if (!se.hasFinished()) {
	      v_duration.addElement(se.getFinishTime());
	  }
	  s_duration.addElement(se.getStartTime());
      }

    Distribution d = Distribution.computeMaxJointDistribution(v_duration);
    Distribution d1 = Distribution.computeMinJointDistribution(s_duration);
    Distribution d2 = Distribution.computeDifferenceJointDistribution(d,d1);
    d2.cluster(CLUSTERING);
    setStart(d1);
    setFinish(d);
    setDuration(d2);
  }

  /**
   * Stringify
   */
  public String toString() { return(toTTaems(Taems.VCUR)); }

    /**
     * stringifySchedule
     */
    public String stringify() {
	String print = "";
	
	Enumeration e = getElements();
	while (e.hasMoreElements()) {
	    ScheduleElement schElement = (ScheduleElement)e.nextElement();
	    print = print + schElement.stringify();
	}
	return print;
    }


   /**
    * Returns the ttaems version of the schedule
    * @param v The version number output style to use
    */
    public String toTTaems(float v) {
        // put everything into a StringBuffer
        StringBuffer sb = new StringBuffer("(spec_schedule\n");
        sb.append("; Schedule contains " + size() + " schedule element(s)\n");
    
        if (schedule_elements != null) {
            sb.append("   (schedule_elements\n");
            Enumeration e;
            e = getElements();
            while (e.hasMoreElements())
                sb.append(((ScheduleElement)e.nextElement()).toTTaems(v));
            sb.append("   )\n");
        }    
        if (task_quality_info != null) {
            sb.append("   (task_quality_infos ");
            Enumeration e;
            e = task_quality_info.elements();
            while (e.hasMoreElements())
                sb.append(e.nextElement().toString() + " ");
            sb.append(")\n");
        }

        if (commitment_info != null)
            sb.append("   (commitment_info "+commitment_info.toString()+")\n");
        
        if (quality != null)
            sb.append("   (quality_distribution "+quality.output()+")\n");
    
        if (duration != null)
            sb.append("   (duration_distribution "+duration.output()+")\n");
    
        if (cost != null)
            sb.append("   (cost_distribution "+cost.output()+")\n");
    
        if (rating != Double.NEGATIVE_INFINITY)
            sb.append("   (rating "+ rating+")\n");
            
        sb.append(")\n");
    
        return sb.toString();
    }
  
  public int getTimeIncrement() {
    return(display.getTimeIncrement());
  }

  public ScheduleDisplay getDisplay() {
    return display;
  }
  
  public void setDisplay(ScheduleDisplay sd) {
    display = sd;
  }

  public void init() {
    //    display = new ScheduleDisplay(this);
    // display.setSize(display.getSize());
  }
  

}

