/* Copyright (C) 2005, University of Massachusetts, Multi-Agent Systems Lab
 * See LICENSE for license information
 */

/************************************************************
 * MLCSchedule.java
 * created by Francois Mechkak, 12/10/1999
 * debugged by Regis Vincent (1999-2001)
 ************************************************************/

package taems;

import java.util.*;
import java.io.*;
import java.awt.*;
import utilities.Distribution;
import utilities.Log;

/**
 * MLCSchedule object.  This should contain some data describing the quantifiable
 * characteristics of the schedule (e.g. Expected QCD, start time) in addition
 * to the list of Schedules that are to be executed.
 */
public class MLCSchedule extends Schedule implements Serializable, Cloneable {

  // Global variables used during the parallelizing process 
 protected Hashtable index_methods = new Hashtable();
    protected Hashtable constraints;
  protected Vector schedules = new Vector();
  protected Vector mlc = new Vector();
  protected Log log = Log.getDefault();


//********************************************************************************
// TOOLS
//********************************************************************************

  private Vector append(Vector v1, Vector v2) {
    Vector v = new Vector();
    Enumeration e1 = new Vector(0).elements();
    Enumeration e2 = new Vector(0).elements();

    if (v1!=null) { e1 = v1.elements(); }
    while (e1.hasMoreElements()) { v.addElement(e1.nextElement()); }

    if (v2!=null) { e2 = v2.elements(); }
    while (e2.hasMoreElements()) { v.addElement(e2.nextElement()); }

    return v;
  }

//********************************************************************************
// DEBUGGING DISPLAY METHODS
//********************************************************************************

    public String stringifyMLC() {     
	String print = new String();
	Enumeration e = mlc.elements();
	while (e.hasMoreElements()) {
	    Schedule s = (Schedule)e.nextElement();
	    print += stringifySchedule(s) + "\n";
	}
	return print;
    }

    public String toString() {
        return stringifyMLC();
	    //toTTaems(Taems.VCUR);  }
    }

  public String stringifySchedules() {
    String print = new String();
    Enumeration e = schedules.elements();
    while (e.hasMoreElements()) {
	Schedule s = (Schedule)e.nextElement();
	print += stringifySchedule(s) + "\n";
    }
    return print;
  }

  /**
   * stringifySchedule
   */
  public String stringifySchedule(Schedule s) {
      return(s.stringify());
  }


  //********************************************************************************
  // PARALLEL SCHEDULING
  //********************************************************************************

  /**
   * delinearize
   * @Create a copy of a linear ScheduleElement. The Start and Finish values are set 
   * @at the earliest time possible to respect interrelationships, QAFs and resources
   */
  protected ScheduleElement delinearize(ScheduleElement se,int start) {

    if (!se.hasFinished()) {
	buildPreconditions(se,start);
	se.setFinishTime(null);
	se.setSchedule(null);
	//	log.log("delinearize :" + se.stringify(), Log.LOG_WARNING);
    }
    Method m = se.getMethod();
    // Building index
    index_methods.put(m, se);

    return se;

  }

   /**
   * subBuildPrecedencePC
   * recursive part of buildPrecedencePC
   */
  protected void subBuildPrecedencePC(Hashtable precedences, TaskBase tb) { 

    String className = tb.getClass().getName();

    if (className.equals("taems.Method")) {
	Method m = (Method)tb;
	ScheduleElement se = (ScheduleElement)index_methods.get(m);
	if (se != null) { precedences.put(se,"0"); }
    }

    if (className.equals("taems.Task")) {
	Task t = (Task)tb;
	Enumeration e = t.getSubtasks();
	while (e.hasMoreElements()) {
	    TaskBase new_tb = (TaskBase)e.nextElement();
	    subBuildPrecedencePC(precedences, new_tb);
	}
    }
  }

    protected Vector findSequenceDependency(TaskBase tb) {
	Vector dependence = new Vector();
	Enumeration e = tb.getSupertasks();
	while (e.hasMoreElements()) {
	    Task t = (Task)e.nextElement();
	    String qaf = t.getQAF().getClass().getName();
	    if (qaf.startsWith("taems.Seq")) {
		Enumeration e1 = t.getSubtasks();
		boolean precedence = true;
		while(precedence && e1.hasMoreElements()){
		    TaskBase t1 = (TaskBase)e1.nextElement();
		    if (t1.getLabel().equals(tb.getLabel())) 
			precedence = false;
		    else {
			if (t1 instanceof Method) 
			    dependence.addElement(t1);
			else { 
			    for (Enumeration e2= ((Task)t1).getAllSubtasks() ; e2.hasMoreElements(); ){
				Object o = e2.nextElement();
				if (o instanceof Method) 
				    dependence.addElement(o);
			    }
			}
		    }
		}
	    }
	    Vector v3 = findSequenceDependency(t);
	    for (Enumeration e3 = v3.elements(); e3.hasMoreElements(); ) 
		dependence.addElement(e3.nextElement());
	}
	return(dependence);
    }

  /**
   * buildPreconditions
   * builds the preconditions vector of a ScheduleElement
   */
  protected void buildPreconditions(ScheduleElement se, int start) {

    Method m = se.getMethod();

    Enumeration e1 = m.getAffectingInterrelationships();
    while (e1.hasMoreElements()) {
	Interrelationship ir = (Interrelationship)e1.nextElement();
	log.log("Looking at " +  ir.getClass().getName(),Log.LOG_SPAM);
	// ---- Adding affecting Enables and Facilitates to preconditions
	if ((ir instanceof taems.EnablesInterrelationship) ||
	    (ir instanceof taems.FacilitatesInterrelationship)) {

	    Hashtable precedences = new Hashtable(); 
	    subBuildPrecedencePC(precedences, (TaskBase)ir.getFrom());
	    
	    Enumeration e3 = precedences.keys();
	    while (e3.hasMoreElements()) {
		ScheduleElement se2 = (ScheduleElement)e3.nextElement();
		PrecedencePrecondition pp1 = new PrecedencePrecondition(this, se2, ir);
		se.addPrecondition(pp1);
	    }
	}
    }

    
    e1 = findSequenceDependency(se.getMethod()).elements();
    while (e1.hasMoreElements()) {
	Method m2 = (Method)e1.nextElement();
	log.log("Found that : " + m2.getLabel(),Log.LOG_SPAM);
	ScheduleElement se2 = (ScheduleElement)index_methods.get(m2);
	if (se2 != null) {
	    PrecedencePrecondition pp1 = new PrecedencePrecondition(this, se2,true);
	    se.addPrecondition(pp1);
	}
    }

    // ---- Adding affected Disables and Hinders to preconditions
    Enumeration e2 = m.getAffectedInterrelationships();
    while (e2.hasMoreElements()) {
	boolean isValid = false;
	Interrelationship ir = (Interrelationship)e2.nextElement();
	log.log("Looking at " +  ir.getClass().getName(),Log.LOG_SPAM);
	if ((ir instanceof taems.DisablesInterrelationship) ||
	   (ir instanceof taems.HindersInterrelationship)) {

	    Hashtable precedences = new Hashtable(); 
	    subBuildPrecedencePC(precedences, (TaskBase)ir.getFrom());
	    
	    Enumeration e3 = precedences.keys();
	    while (e3.hasMoreElements()) {
		ScheduleElement se3 = (ScheduleElement)e3.nextElement();
		PrecedencePrecondition pp3 = new PrecedencePrecondition(this, se3, ir);
		se.addPrecondition(pp3);
	    }
	}
    }

    if (se.getMethod().getEarliestStartTime() != Integer.MIN_VALUE) {
	EarliestStartTimePrecondition estp = new EarliestStartTimePrecondition(this,new Distribution(se.getMethod().getEarliestStartTime()+start,1.0));
	se.addPrecondition(estp);
    }
  }

  /**
   * insertsInMLC
   * @inserts a schedule element in the MLC 
   * The existing schedules are checked for possible integration of the new element,
   * then, if none is available, a new schedule is created.
   * Note : the schedule to which belongs a given element has no meaning in the MLC.
   * Consider that each element is in a separate schedule.
   */
  protected void insertInMLC(Vector MLC, ScheduleElement se, int start) {

    setupElement(se,start);
    Schedule s = se.getSchedule();
    if (!MLC.contains(s)) { 
	MLC.addElement(s); 
    }
  }

  protected boolean isLastOfSchedule(ScheduleElement se) {
    Schedule s = se.getSchedule();
    if (s.getScheduleElementIndex(se) == s.size() - 1) { return true; }
    else { return false; }
  }

  /**
   * setupElement
   * @organizes the elements in a structured list of schedules, 
   * and calculates the Start and Finish distributions
   */
  protected void setupElement(ScheduleElement se, int start) {

    // CASE 1 : Element already scheduled
    if (se.getSchedule() != null) {
        //log.log("(" + se.getMethod().getLabel() + " already set up)",Log.LOG_SPAM);
	return;
    }

    // CASE 2 : se has no preconditions
    if (se.numPreconditions() == 0) {

	// ---- Calculating Start and Finish time
	se.setStartTime(new Distribution(new Float(start), new Float(1)));
	Vector v_end = new Vector();
	v_end.addElement(se.getStartTime());
	v_end.addElement(se.getDurationDistribution());
	Distribution d1 = Distribution.computeJointDistribution(v_end);
	d1.cluster(CLUSTERING);
	se.setFinishTime(d1);

	// ---- Creating new schedule
	Schedule s = new Schedule();
	s.addScheduleElement(se);
    }

    // CASE 3 : se has preconditions
    if (se.numPreconditions() > 0) {

	// ---- Recursing through preconditions - collecting Finish times and Schedules
	Vector v_starttime = new Vector();
	Schedule selected = null;
	Enumeration e = se.getPreconditionsElement();
	while (e.hasMoreElements()) {
 	    Precondition p = (Precondition)e.nextElement();
	    if (p instanceof PrecedencePrecondition) {
		ScheduleElement se2 = p.getScheduleElement();
	    
		// If p isn't ScheduleElement-dependant, se2 is null (eg resource)

		if (se2 != null) {
		    if (se2.getSchedule() == null) { setupElement(se2,start); }
		    if (isLastOfSchedule(se2)) { selected = se2.getSchedule(); }
		    v_starttime.addElement(se2.getFinishTime());
		}
	    }
	    if (p instanceof EarliestStartTimePrecondition) {
		v_starttime.addElement(((EarliestStartTimePrecondition)p).getEarliestStartDistribution());
	    }
	}
	// ---- If no available schedule has been found, creating our own
	if (selected == null) { selected = new Schedule(); }

	// ---- Calculating Start and Finish time
	Distribution d2 = Distribution.computeMaxJointDistribution(v_starttime);
	d2.cluster(CLUSTERING);
	se.setStartTime(d2);

	Vector v_end = new Vector();
	v_end.addElement(se.getStartTime());
	v_end.addElement(se.getDurationDistribution());
	Distribution d3 = Distribution.computeJointDistribution(v_end);
	d3.cluster(CLUSTERING);
	se.setFinishTime(d3);
	selected.addScheduleElement(se);
    }

  }


  /**
   * createMLC
   * @creates a Multiple Lines of Control (MLC) vector from a linear schedule
   */
  public Vector createMLC(Vector linearSchedules, int start) {
    index_methods = new Hashtable();

    Vector MLC = new Vector();
    Vector allSE = new Vector();
    ScheduleElement nse;

    // ---- Creating a list of delinearized ScheduleElement, fit for insertion in the Mlc
    Enumeration e1 = linearSchedules.elements();
    while (e1.hasMoreElements()) {
	Schedule s = (Schedule)e1.nextElement();
	Enumeration linear = s.getElements();
	while (linear.hasMoreElements()) {
	    ScheduleElement se = (ScheduleElement)linear.nextElement();
	    allSE.addElement(delinearize(se,start));
	}
    }

    // ---- Linking these elements in a structured list of Schedules
    Enumeration e2 = allSE.elements();
    while (e2.hasMoreElements()) {
	ScheduleElement se = (ScheduleElement)e2.nextElement();
	//	log.log("Inserting " + se.getMethod().getLabel(),Log.LOG_SPAM);
	insertInMLC(MLC, se,start);
    }
    return MLC;
  }


//********************************************************************************
// STANDARD SCHEDULE FUNCTIONS
//********************************************************************************

  /**
   * Constructor.
   * @param se The list of ScheduleElements comprising the actual schedule
   * @param tqi Task quality information
   * @param ci Commitment information
   * @param qd Expected quality distribution
   * @param dd Expected duration distribution
   * @param cd Expected cost distribution
   */
  public MLCSchedule(Vector se, Vector tqi, Vector ci, Distribution qd, Distribution dd, Distribution cd) {
    Schedule s = new Schedule(se, tqi, ci, qd, dd, cd);
    constraints = new Hashtable();
    addSchedule(s,0,0.5f);
  }

  /**
   * Constructor.
   * @param s The schedule to add
   * @param t The current time (or 0 to not offset anything)
   * @param compact rate (between 0 to 1).
   */
  public MLCSchedule(Schedule s, int t, float compact) {
    super();
    constraints = new Hashtable();
    addSchedule(s, t, compact);
  }

  /**
   * Blank constructor
   */
  public MLCSchedule() { 
      super();
      constraints = new Hashtable();
  }

  protected void setSchedules(Vector v) { schedules = v; }
  protected void setMLC(Vector v) { mlc = v; }


    /**
     * addConstraint to the current MLCSchedule.
     * @param Object key
     * @param Constraint 
     */
    
    public void addConstraint(Object key, Constraint c) {
	if (c.check(0)) {
	    constraints.put(key,c);
	}
	else
	    log.log("Can't add a constrainst that is not valid, dumb ass",Log.LOG_ERR);
    }

    /**
     * getConstrainsts returns an Enumeration of Constraints for the current MLCSchedule
     @ return an Enumeration
     */
    
    public Enumeration getConstraints() {
	return(constraints.elements());
    }

    /**
     * SetDeadline 
     */
//     public void setDeadline(Schedule s, int deadline) {
// 	Vector v =new Vector();
// 	for (int i = s.getScheduleElements().size() - 1 ;
// 	     i > 0 ; i--) {
// 	    ScheduleElement se = (ScheduleElement)s.getScheduleElements().elementAt(i);
// 	}}

// 	    protected Vector setDeadlineRecursive(ScheduleElement se, int deadline, Vector v) {
// 		for (Enumeration e = se.getDependanceTo();
// 		     e.hasMoreElement(); ) {
// 		}}
		    
			
		
    /**
     * removeConstraint, remove one constraint.
     * @param Constraint
     */
    public void removeConstraint(Constraint c) {
	for (Enumeration e = constraints.keys() ; e.hasMoreElements() ; ) {
	    Object key = e.nextElement();
	    if (constraints.get(key) == c)
		constraints.remove(key);
	}
    }
    
	    
    
//--------------------------------------------------------------------------------
// trying real duration again, with clustering
//--------------------------------------------------------------------------------

  /**
   * calculateDuration
   * @calculates the duration distribution of the MLC
   */
  protected void calculateDuration() {

      Vector v_duration = new Vector();
      Vector s_duration = new Vector();
      float value = 0;
      Enumeration e = mlc.elements();
      while (e.hasMoreElements()) {
	  Schedule s = (Schedule)e.nextElement();
	  ScheduleElement se = (ScheduleElement)s.getScheduleElement(s.size()-1);

	if (!se.hasFinished()) {
	    v_duration.addElement(se.getFinishTime());
	}
	se = (ScheduleElement)s.getScheduleElement(0);
	s_duration.addElement(se.getStartTime());
    }

    Distribution d = Distribution.computeMaxJointDistribution(v_duration);
    Distribution d1 = Distribution.computeMinJointDistribution(s_duration);
    Distribution d2 = Distribution.computeDifferenceJointDistribution(d,d1);
    d2.cluster(CLUSTERING);
    setDuration(d2);

  }

  /**
   * calculateQualityCostRating
   */
  public void calculateQualityCostRating() {

    Vector v_quality = new Vector();
    Vector v_cost = new Vector();
    double sum_rating = 0;
    Enumeration e = schedules.elements();
    while (e.hasMoreElements()) {
	Schedule s2 = (Schedule)e.nextElement();
	v_quality.addElement(s2.getQuality());
	v_cost.addElement(s2.getCost());
	sum_rating += s2.getRating();
    }
    Distribution d = Distribution.computeJointDistribution(v_quality);
    d.cluster(CLUSTERING);
    setQuality(d);
    d = Distribution.computeJointDistribution(v_cost);
    d.cluster(CLUSTERING);
    setCost(d);
    setRating(sum_rating / schedules.size());
  }

    /**
     * add already "MLCfied" schedule
     */ 
        public void addScheduleSimple(Schedule s) {
         schedules.addElement(s);
    }

  /**
   * Adds and parallelize a schedule
   **/
  public void addSchedule(Schedule s, int startingtime, float compacting) {
      boolean added = false;
    if (schedules.size() == 0) {
        //task_quality_info = new Vector();
	//commitment_info = new Vector();
	quality = new Distribution(new Float(0.0), new Float(1.0));
	duration = new Distribution(new Float(0.0), new Float(1.0));
	cost = new Distribution(new Float(0.0), new Float(1.0));

	schedules.addElement(s);
	
	// ---- Calling the whole "parallel scheduling" process
	mlc = createMLC(schedules,startingtime);
	added = true;
    }
    else {
	if (!schedules.contains(s)) {
	    added = true;
	    schedules.addElement(s);
	    Enumeration e2 = s.getElements();
	    while (e2.hasMoreElements()) {
		ScheduleElement se = (ScheduleElement)e2.nextElement();
		insertInMLC(mlc, delinearize(se,startingtime),startingtime);
	    }
	}
    }

    if (added) {
	compactSchedule(s,startingtime, compacting);
    }

    s.calculateDuration();

    // ---- Calculating Quality, Cost and Rating
    calculateQualityCostRating();
    log.log("Mlc - Quality : " + quality.output(),Log.LOG_SPAM);
    log.log("Mlc - Cost : " + cost.output(),Log.LOG_SPAM);
    calculateDuration();
    log.log("Mlc - Duration : " + duration.output(),Log.LOG_SPAM);
    if ((getTaskQualityInfo() != null) || (s.getTaskQualityInfo() != null))
        setTaskQualityInfo(append(getTaskQualityInfo(), s.getTaskQualityInfo()));
    if ((getCommitmentInfo() != null) || (s.getCommitmentInfo() != null))
        setCommitmentInfo(append(getCommitmentInfo(), s.getCommitmentInfo()));

    log.log("Mlc - Rating : " + rating,Log.LOG_SPAM);

  }


    /**
     * If the schedule has an Earliest Start Time or a deadline
     * the function will compute the execution window and 
     * set the start time of the schedule.
     * @param Schedule s
     * @param int start time
     * @param float security, this value between 0 and 1 define
     * the security range you want on the start time.<P>
     * For example if security=0 then start time = earliest start time
     * if security=1 then start time = latest start time 
     */
    public void compactSchedule(Schedule s, int start, float probability) {
	boolean compact = true;
	if (probability == -1)
	    compact = false;
	else if (probability > 1) 
	    probability =1;
	else if (probability < 0)
	    probability=0;
	
	for (Enumeration e = s.getElements(); e.hasMoreElements(); ) {
	    ScheduleElement se = (ScheduleElement)e.nextElement();
	    if (se.getMethod().getComposedEarliestStartTime() != Integer.MIN_VALUE) {
		//		log.log("Settinng EST = " + (start + se.getMethod().getComposedEarliestStartTime()) + " for " + se.getLabel());
		se.setEarliestStartTime(start + se.getMethod().getComposedEarliestStartTime());
		if (compact)
		    se.setOptimalEarliestStartTime(start + se.getMethod().getComposedEarliestStartTime(),true);
	    }
	    if (se.getMethod().getComposedDeadline() != Integer.MIN_VALUE) {
		//		log.log("Settinng LFT = " + (start + se.getMethod().getComposedDeadline()) + " for " + se.getLabel());
		se.getMethod().setActualLatestFinishTime(start + se.getMethod().getComposedDeadline());
		se.setLatestFinishTime(start + se.getMethod().getComposedDeadline());
		if (compact)
		    se.setOptimalLatestFinishTime(start + se.getMethod().getComposedDeadline(), true);
	    }
	}
	if (compact) {
	    //	    log.log("compacting Schedule = " + s.stringify(), Log.LOG_WARNING);
	    for (Enumeration e = s.getElements(); e.hasMoreElements(); ) {
		ScheduleElement se = (ScheduleElement)e.nextElement();
 		int est = se.getOptimalEarliestStartTime();
		int lst = se.getOptimalLatestStartTime();
 		if (est == Integer.MIN_VALUE) 
 		    est = se.getStart();
		if (lst == Integer.MIN_VALUE) 
 		    lst = se.getStart();
		int offset = Math.round(est+probability*(lst-est))  - se.getStart();    
		//		log.log("est = " + est + " lst = " + lst + " start = " + se.getStart(), Log.LOG_WARNING);
		//		log.log("Delaying " + se.getLabel() + " of " + offset, Log.LOG_WARNING);
		if (offset != 0) 
		    delayStartTime(se,offset);
		//		log.log("compact Schedule = " + s.stringify(), Log.LOG_WARNING);
 	    }
	}
    }

  /**
   * Removes a schedule and resets the MLC
   **/
  public void removeSchedule(Schedule s) {
    schedules.removeElement(s);
    Enumeration e = s.getScheduleElements().elements();
    while (e.hasMoreElements()) {
	ScheduleElement se = (ScheduleElement)e.nextElement();	
	for (Enumeration e1 = se.getDependanceTo().elements();
	     e1.hasMoreElements(); ) {
	    ScheduleElement se1 = (ScheduleElement)e1.nextElement();
	    if(!s.getScheduleElements().contains(se1)) {
		for (Enumeration e2 = se1.getPreconditionsElement();
		     e2.hasMoreElements(); ) {
		    Precondition pre = (Precondition)e2.nextElement();
		    if (pre.dependentOf(se)) {
			se1.removePrecondition(pre);
			se1.removeDependingOf(se);
			se.removeDependanceTo(se1);
		    }
		}
	    }
	}
	for (Enumeration e1 = se.getDependingOf().elements();
	     e1.hasMoreElements(); ) {
	    ScheduleElement se1 = (ScheduleElement)e1.nextElement();
	    if(!s.getScheduleElements().contains(se1)) {
		se1.removeDependanceTo(se);
	    }
	}
	se.getSchedule().removeScheduleElement(se);
    }
    Vector tmp = new Vector();
    Enumeration e4 = mlc.elements();
    while (e4.hasMoreElements()) {
	Schedule s4 = (Schedule)e4.nextElement();
	if (s4.size() == 0) 
	    tmp.addElement(s4);
    }
    e4 =tmp.elements();
    while (e4.hasMoreElements()) 
	mlc.removeElement(e4.nextElement());
  }

    /**
     * Gets an enumeration of the schedules
     */
    public Enumeration getSchedules() {
        return schedules.elements();
    }

    /**
     * Check if a schedule is in the MLCSchedule
     */
    public boolean contains(Schedule s) {
	return(schedules.contains(s));
    }

    /**
     * Return the number of schedules present
     */
    public int numberOfSchedules() {
	return(schedules.size());
    }
  /**
   * returns an Enumeration of Schedule Element for this schedule.
   */ 
  public Enumeration getElements() {
    return getScheduleElements().elements();
  }

  /**
   * clears out the schedule
   */
  public void removeAllElements() {
    schedules.removeAllElements();
    schedules = new Vector();
    mlc.removeAllElements();
    mlc = new Vector();
    task_quality_info = null;
    commitment_info = null;
    quality = null;
    duration = null;
    cost = null;
  }

  /**
   * give me the vector
   * Should not be used externally - method should be protected
   */
  public Vector getScheduleElements() {

    Vector sev = new Vector();
    if (mlc == null) { return (new Vector(0)); }
    Enumeration e1 = mlc.elements();
    while (e1.hasMoreElements()) {
	Schedule s = (Schedule)e1.nextElement();
	Enumeration e2 = s.getElements();
	while (e2.hasMoreElements()) { sev.addElement(e2.nextElement()); }
    }
    return sev;
  }


  /**
   * return the schedule element that has the matched label.
   * Searches first for local SE's, the looks inside the 
   * schedules it knows about
   */
  public ScheduleElement getScheduleElement(String l) {
      ScheduleElement se = super.getScheduleElement(l);

      if (se == null) {
          Enumeration list = getSchedules(); 
          while (list.hasMoreElements() ) {
              Schedule s = (Schedule) list.nextElement();
              se = s.getScheduleElement(l);
              if (se != null) break;
          }	
      }

      return se;
  }

  /**
   * set the vector
   * Use at your own risk
   */
  public void setScheduleElements(Vector v) {
    Schedule s = new Schedule();
    s.setScheduleElements(v);
    addSchedule(s,0,0.5f);
  }

  /**
   * sets a schedule item as a lone schedule
   * Use at your own risk
   */
  public void insertScheduleElement(ScheduleElement e, int i) {
    addScheduleElement(e);
  }

  /**
   * adds a scheduled item
   * Use at your own risk
   */
  public void addScheduleElement(ScheduleElement e) {
    Schedule s = new Schedule();
    Vector v = new Vector();
    v.addElement(e);
    s.setScheduleElements(v);
    addSchedule(s,0,0.5f);
  }

  /**
   * removes a scheduled item
   * Use at your own risk
   */
  public void removeScheduleElement(ScheduleElement e) {
    Schedule s = e.getSchedule();
    s.removeScheduleElement(e);
  }



  /**
   * True if all prerequisite methods to ScheduleElement have been executed.
   * Every time a precondition is checked valid, 
   * @param target - ScheduleElement to check
   * @param time - current time
   */
    public boolean checkPreconditions(ScheduleElement target, int time) {
      return target.checkPreconditions(time);
    }
 /**
   * Returns true if the schedule is completed (that is,
   * all the schedule elements have finished).
   */
  public boolean isCompleted() {
      boolean answer = true;
      Enumeration e = schedules.elements();
      while (e.hasMoreElements() && answer) {
	  Schedule s = (Schedule)e.nextElement();
	  answer = answer && s.isCompleted();
      }
      return(answer);
  }
    
  /**
   * Return the first schedule element which has not completed in each line of control.
   * @param time The current time
   * @return a list of these elements, empty if none found
   */
    public Enumeration getNextTasks(int time) {
        Vector result = new Vector();
        boolean foundNewElements = true;
        Vector listElements = getScheduleElements();
        while (foundNewElements) {
            foundNewElements = false;

            for (int i = 0; i < listElements.size(); i++) {
                ScheduleElement se = (ScheduleElement)listElements.elementAt(i);
                //boolean pre = false, finish = false;
//System.err.println(" ---------> " + "Examining " + se.getLabel());
                if (!se.hasStarted()) {
                    if (time >= se.getStart()) {
                        if (checkPreconditions(se,time)) {
                            listElements.removeElementAt(i);
                            result.addElement(se);
                            foundNewElements = true;
                        } else {
//System.err.println(" (preconditions failed)");
                        }
                    } else {
//System.err.println(" (not yet start time)");
                    }
                } else {
//System.err.println(" (already started)");
                }
            } 
        }
	
        return(sortElements(result));
    }

  /**
   * return the schedule element that has the given index.
   * Meaningless - should not be used
   */
  public ScheduleElement getScheduleElement(int i) {
    return null;
  }

  /**
   * return the schedule element's location in the schedule
   * Meaningless - should not be used
   */
  public int getScheduleElementIndex(ScheduleElement se) {
    return -1;
  }

  /**
   * Returns the number of methods in this schedule.
   */
  public int size() {
    if (schedules != null)
      return(getScheduleElements().size());
    else
      return 0;
  }

  /**
   * Sets the start time, offsetting the element start and finish
   * times appropriately.
   * <p>
   * This function assumes the initial start time of the schedule
   * is whatever start is set to by default (usually 0).
   * <p>
   * <b> DO NOT USE THIS FUNCTION for delaying any schedule with
   * resources bindings. Use the function PartialOrderScheduler.setStartTime
   */
  public void setStartTime(int time) {
    Enumeration list = mlc.elements();
    int offset = time - getStartTime();
    if (offset > 0) {
	while (list.hasMoreElements()) {
	    Schedule s = (Schedule)list.nextElement();
	    s.setStartTime(s.getStartTime()+offset);
	}
    }
  }


    /**
     * Delaying the finishTime of a ScheduleElement
     * @param ScheduleElement schelmt, the starting point of the delay
     * @param int delay 
     * @return Vector of all ScheduleElements delayed
     */
    public Vector delayFinishTime(ScheduleElement schelement, int delay) {
	Vector v = delayTime(schelement,delay,false);
	schelement.setAttribute("DurationDistribution", schelement.getDurationDistribution());
	schelement.setDurationDistribution(schelement.getDurationDistribution().applyOffset((float)delay));
	return(v);
    }
    /**
     * Delaying from a defined ScheduleElement to the end of the
     * schedule. 
     * @param ScheduleElement schelmt, the starting point of the delay
     * @param int delay 
     * @return Vector of all ScheduleElements delayed
     */
    public Vector delayStartTime(ScheduleElement schelement, int delay) {
	return(delayTime(schelement,delay,true));
    }

    /**
     * Delay the timing of the method if the flag is true, it
     * will offset the start time and the finishtime. If the
     * flag is false it will affect the duration of the method
     * @param ScheduleElement schelmt, the starting point of the delay
     * @param int delay 
     * @param boolean flag
     * @return Vector of all ScheduleElements delayed
     */
    protected Vector delayTime(ScheduleElement schelement, int delay, boolean flag) {
	    if (flag) {
		if (!schelement.hasAttribute("StartTime"))
		    schelement.setAttribute("StartTime", schelement.getStartTime());
		schelement.setStartTime(schelement.getStartTime().applyOffset((float)delay));
		
	    }
	    if (!schelement.hasAttribute("FinishTime"))
		schelement.setAttribute("FinishTime", schelement.getFinishTime());
	    schelement.setFinishTime(schelement.getFinishTime().applyOffset((float)delay));
	    
        // Calculate new finish time
	    int finish = (int)schelement.getFinishTime().calculateMax();

	Vector tmp = new Vector();
	tmp.addElement(schelement);
	ScheduleElement se = schelement;
	//	Enumeration list = getElementsSorted();
	Enumeration list = se.getDependanceTo().elements();
	while (list.hasMoreElements()) {
	    ScheduleElement s = (ScheduleElement)list.nextElement();
	    int start = (int)s.getStartTime().calculateMin();
	    Vector tv = new Vector();
	    if (finish-start > 0)
		tv = delayStartTime(s, finish-start);
	    if (!tv.contains(s)) 
		tv.addElement(s);
	    Enumeration e1 = tv.elements();
	    while (e1.hasMoreElements()) {
		ScheduleElement se2 = (ScheduleElement)e1.nextElement();
		if (!tmp.contains(se2)) 
		    tmp.addElement(se2);
	    }
	}
	if (flag) 
	    setDuration(getDuration().applyOffset(delay));
        return(tmp);
    }


  /**
   * Gets the supposed schedule start time
   * @return the start time, or -1 if not found
   */
  public int getStartTime() {
    int earliest = Integer.MAX_VALUE;
    if (start == null) {
	if (size() > 0) {
	    Enumeration e = mlc.elements();
	    while (e.hasMoreElements()) {
		Schedule s = (Schedule)e.nextElement();
		ScheduleElement se = (ScheduleElement)s.getScheduleElements().firstElement();
		if (se.getStart() < earliest) earliest = se.getStart();
	    }
	    return earliest;
    	} else { return -1; }
    }
    return((int)start.calculateMin()); 
  }

    
    /**
     * Undelete all the history
     */ 
    public void deleteHistory() {
	Enumeration e = getElements();
        while(e.hasMoreElements()) {
            ScheduleElement se = (ScheduleElement)e.nextElement();
            deleteHistory(se);
        }
    }

  /**
   * Gets the supposed scheduler finish time
   */
  public int getFinishTime() {  
    int latest = Integer.MIN_VALUE;
    if (finish == null) {
	if (size() > 0) {
	    Enumeration e = mlc.elements();
	    while (e.hasMoreElements()) {
		Schedule s = (Schedule)e.nextElement();
		ScheduleElement se = (ScheduleElement)s.getScheduleElements().lastElement();
		if (se.getFinish() > latest) latest = se.getFinish();
	    }
	    return latest;
    	} else { return -1; }
    }
    return((int)finish.calculateMax()); 
  }

  public Distribution getFinish() { return finish; }

  /**
   * Gets the total duration for the this schedule
   */
  public int getTotalDuration() {
    if (duration == null) {
	return (getFinishTime() - getStartTime()); 
    }
    return ((int)getDuration().calculateMax());
  }	

    /**
     * Gets the sum of all schedule duration
     */
    public int getMaxFinishTime() {
	int max =0;
	for(Enumeration e =getSchedules() ; e.hasMoreElements();) {
	    Schedule s= (Schedule)e.nextElement();
	    if (max < s.getFinishTime())
		max = s.getFinishTime();
	}
	return(max);
    }

    /**
     * Fixes up preconditions of all the schedule elements,
     * to the extent that it is possible at this scope.
     * @param The schedule to query for link fixes, or null to use
     * this schedule.
     */
    public void fixConditions(Schedule query) {
        if (query == null) query = this;

        super.fixConditions(query);

        Enumeration e = getSchedules();
        while (e.hasMoreElements()) {
            Schedule s = (Schedule)e.nextElement();
            s.fixConditions(query);
        }
    }

  /**
   * Clone me.
   */
  public Object clone() {
    MLCSchedule cloned = new MLCSchedule();

    Enumeration e = schedules.elements();
    Vector v = new Vector();
    while (e.hasMoreElements()) {
	v.addElement(((Schedule)e.nextElement()).clone());
    }
    cloned.setSchedules(v);
    e = cloned.getSchedules();
    v = new Vector();
    while (e.hasMoreElements()) {
	Schedule s = (Schedule)e.nextElement();
	for (Enumeration e1 = s.getElements() ; e1.hasMoreElements(); ) 
	    v.addElement(e1.nextElement());
    }
    e = v.elements();
    while (e.hasMoreElements()) {
	ScheduleElement se = (ScheduleElement)e.nextElement();
	se.retargetVirtual(cloned,v);
    }

    Vector newmlc = new Vector();
    e =  mlc.elements();
    while (e.hasMoreElements()) {
	Schedule s = (Schedule)e.nextElement();
	Schedule clonedS = (Schedule)s.cloneWithVirtual();
	clonedS.retargetVirtual(v);
	newmlc.addElement(clonedS);
    }
    cloned.setMLC(newmlc);
    
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

    if (getStart() != null)
        cloned.setStart((Distribution)getStart().clone());
    if (getFinish() != null)
        cloned.setFinish((Distribution)getFinish().clone());
    cloned.display = display;
    return cloned;
  }


   /**
    * Returns the ttaems version of the schedule
    * @param v The version number output style to use
    */
    public String toTTaems(float v) {
        // put everything into a StringBuffer
        StringBuffer sb = new StringBuffer("(spec_mlc_schedule\n");
        sb.append("; Schedule contains " + schedules.size() + " schedule(s)\n");
    
        if (schedules != null) {
            sb.append("   (schedules\n");
            Enumeration e;
            e = getSchedules();
            while (e.hasMoreElements())
                sb.append(((Schedule)e.nextElement()).toTTaems(v));
            sb.append("   )\n");
        }    
        if (schedule_elements != null) {
            sb.append("; Schedule contains " + schedule_elements.size() + " schedule elements(s)\n");
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
}

