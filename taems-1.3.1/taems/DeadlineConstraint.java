/* Copyright (C) 2005, University of Massachusetts, Multi-Agent Systems Lab
 * See LICENSE for license information
 */

/*********************************************************/
/* DeadlineConstraint.java                               */
/*********************************************************/

package taems;

import java.util.*;
import java.io.*;
import java.awt.*;
import utilities.Distribution;
import utilities.Log;
/**
 * DeadlineConstraint object.
 */
public class DeadlineConstraint extends Precondition implements Serializable, Cloneable {

    protected Schedule schedule;
    protected Log log = Log.getDefault();
    protected int deadline;

    public DeadlineConstraint(MLCSchedule p, Schedule s, int d) {
      setParentMLC(p);
      setSchedule(s);
      setDeadline(d);
    }

    public void setSchedule(Schedule s) {
	schedule=s;
    }

    public Schedule getSchedule() {
	return(schedule);
    }

    public void setDeadline(int d) {
	deadline = d;
    }
    
    public int getDeadline() {
	return(deadline);
    }

  public boolean check(int time) {
      boolean answer=true;
      String reason = "unknown";
      log.log("#### Constraint : " + output() + " is " + answer + " [" + reason +"]",Log.LOG_INFO);
      return(answer);
  }

    public Object clone() { 
	DeadlineConstraint cloned = new DeadlineConstraint(null,null,getDeadline());
	System.err.println("WARNING, This Function DeadlineConstraint.clone() is NOT WORKING");
	return cloned;  }

  public void completed(int i) { }

  public void clear() { }

  public String output() {
    // put everything into a StringBuffer
    StringBuffer sb = new StringBuffer();
    return sb.toString();
  }


   

  /**
   * Returns a textual Taems version of the precondition
   * @param v The version number output style to use
   * @return String containing TTaems representation
   */
  public String toTTaems(float v) {
    StringBuffer sb = new StringBuffer("           (spec_deadline_constraint\n");

    sb.append("              (schedule " + schedule.toTTaems(v) + ")\n");
    sb.append("              (deadline " + deadline + ")\n");  
    sb.append("           )\n");
    
    return sb.toString();
  }
}
