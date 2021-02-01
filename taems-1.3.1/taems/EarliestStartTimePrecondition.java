/* Copyright (C) 2005, University of Massachusetts, Multi-Agent Systems Lab
 * See LICENSE for license information
 */

/************************************************************
 * EarliestStartTimePrecondition.java
 * created by Regis Vincent (1999-2000)
 ************************************************************/

package taems;

import java.util.*;
import java.io.*;
import java.awt.*;
import utilities.Distribution;
import utilities.Log;

/**
 * EarliestStartTimePrecondition object.
 */
public class EarliestStartTimePrecondition extends Precondition implements Serializable, Cloneable {

    protected Distribution earliestStartTime;
    protected Log log = Log.getDefault();

    public EarliestStartTimePrecondition(MLCSchedule p, Distribution d) {
      parentMLC = p;
      earliestStartTime = d;
    }

  public Distribution getEarliestStartDistribution() { 
      return earliestStartTime; }
    public int getEarliestStartTime() { 
	return((int)(earliestStartTime.calculateMin()));}
  public void setgetEarliestStartDistribution(Distribution d) {
      earliestStartTime = d;
  }

  public boolean check(int time) {
      boolean answer=true;
      String reason="";
      if (optional)
	  return true;

      // Linked method must have finished
      if (time < getEarliestStartTime()) {
	  answer = false;
	  reason = "time is not right " + time + " < " + getEarliestStartTime();
      }
      log.log("#### Checking : " + output() + " is " + answer + " [" + reason +"]",Log.LOG_INFO);
      return(answer);
  }

    public void completed(int time) { }
    public void clear() { }

  public String output() {
    // put everything into a StringBuffer
    StringBuffer sb = new StringBuffer();
    sb.append("EarliestStartTime : " + getEarliestStartDistribution());
    return sb.toString();
  }

   /**
     * The clone function
     */
    public Object clone() {
	EarliestStartTimePrecondition  cloned = new EarliestStartTimePrecondition(null, getEarliestStartDistribution());
	cloned.setOptional(isOptional());
	return(cloned);
    }
   

  /**
   * Returns a textual Taems version of the precondition
   * @param v The version number output style to use
   * @return String containing TTaems representation
   */
  public String toTTaems(float v) {
    StringBuffer sb = new StringBuffer("           (spec_earlieststarttime_precondition\n");

    sb.append("              (earliest_start_time " + getEarliestStartDistribution() + ")\n");
    sb.append("           )\n");
    
    return sb.toString();
  }
}
