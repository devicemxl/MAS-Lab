/* Copyright (C) 2005, University of Massachusetts, Multi-Agent Systems Lab
 * See LICENSE for license information
 */

/************************************************************
 * PrecedencePrecondition.java
 * created by Francois Mechkak, 01/12/00
 * debugged by Regis Vincent (1999-2000)
 ************************************************************/

package taems;

import java.util.*;
import java.io.*;
import java.awt.*;
import utilities.Distribution;
import utilities.Log;
/**
 * PrecedencePrecondition object.
 */
public class PrecedencePrecondition extends Precondition implements Serializable, Cloneable {

    protected Interrelationship ir;
    protected Log log = Log.getDefault();
    protected boolean fake=false;
    public PrecedencePrecondition(MLCSchedule p, ScheduleElement se, Interrelationship i) {
      parentMLC = p;
      setScheduleElement(se);
      if (i != null){
	  setInterrelationship(i);
      }
    }
    
    public PrecedencePrecondition(MLCSchedule p, ScheduleElement se, boolean SEQ) {
	parentMLC = p;
	setScheduleElement(se);
	fake=SEQ;
      
    }
  public Interrelationship getInterrelationship() { return ir; }
  public void setInterrelationship(Interrelationship i) { ir = i; }
    
    public boolean isArtificialIR() {
	return fake;
    }
    public void setArtificialIR() {
	fake = true;
    }

  public boolean check(int time) {
      boolean answer=true;
      String reason="";
      if (optional)
	  return(true);

      // Linked method must have finished
      if (!conditioningSE.hasFinished()) {
	  answer = false;
	  reason = "Method not yet finished";
      }
      else {
	  // IR delay must have been completed
	  if ((ir !=null) && (!ir.getActive())) {
	      answer = false;
	      reason = "Delay not yet activated";
	  }
      }
      log.log("#### Checking : " + output() + " is " + answer + " [" + reason +"]",Log.LOG_INFO);
      return(answer);
  }

    public void completed(int time) { }
    public void clear() { }

  public String output() {
    // put everything into a StringBuffer
    StringBuffer sb = new StringBuffer();
    sb.append("Precedence : Method " + conditioningSE.getMethod().getLabel());
    if (ir != null) {
	sb.append(" IR " + ir.getLabel());
    }
    return sb.toString();
  }

    /**
     * The clone function
     */
    public Object clone() {
	PrecedencePrecondition cloned = new PrecedencePrecondition(null, new VirtualScheduleElement(conditioningSE.getID()),getInterrelationship());
	cloned.setOptional(isOptional());
	cloned.fake = isArtificialIR();
	return(cloned);
    }
   

  /**
   * Returns a textual Taems version of the precondition
   * @param v The version number output style to use
   * @return String containing TTaems representation
   */
  public String toTTaems(float v) {
    StringBuffer sb = new StringBuffer("           (spec_precedence_precondition\n");
    if (getScheduleElement() instanceof VirtualScheduleElement) 
	sb.append("             (virtual_schedule_element " + getScheduleElement().getID() + ")\n");
    else {
	sb.append("              (schedule_element " + getScheduleElement().getLabel() + ")\n");
	sb.append("; Reference " + getScheduleElement().getID() + "\n");
	if ((getScheduleElement().getStartTime() == null) && (getScheduleElement().getDurationDistribution() == null))
	    sb.append(";             ** Note: schedule_element may be virtual\n");  
    }
    if (ir != null)
	sb.append("              (interrelationship " + getInterrelationship().getLabel() + ")\n");
    if (fake)
	sb.append(";             ** Note: interrelationship is virtual\n;              ** from SEQuence QAF\n");
    

    if (isOptional())
	sb.append(";; Optional Precedence relation");
	
    sb.append("           )\n");
    
    return sb.toString();
  }
}
