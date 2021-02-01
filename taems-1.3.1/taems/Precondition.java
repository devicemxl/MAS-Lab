/* Copyright (C) 2005, University of Massachusetts, Multi-Agent Systems Lab
 * See LICENSE for license information
 */

/************************************************************
 * Precondition.java
 * created by Francois Mechkak, 01/12/00
 * debugged by Regis Vincent (1999-2000)
 ************************************************************/

package taems;

import java.util.*;
import java.io.*;
import java.awt.*;
import utilities.Distribution;

/**
 * Precondition object, for use with MLCSchedule. 
 */
public abstract class Precondition implements Serializable, Cloneable {

  protected ScheduleElement conditioningSE;
  protected MLCSchedule parentMLC;
    protected boolean optional=false;
  /**
   * Checks if precondition is satisfied, ie. if linked method can be started
   * @param int time (current time);
   */
  public abstract boolean check(int time);


  /**
   * Update the precondition when the method is completed
   * @param int time (current time);
   */
  public abstract void completed(int time);


    /**
     * clear the precondition, before being removed
     */
    public abstract void clear();

  /**
   * Returns a textual version of the precondition
   * Debugging purpose
   */
  public abstract String output();

  /**
   * Returns the ScheduleElement that the PC links TO (ie the one that 
   * conditions the valid/invalid state of the PC), or null if not 
   * dependant on one
   */
  public ScheduleElement getScheduleElement() { 
    return conditioningSE;
  }

  public void setScheduleElement(ScheduleElement se) { conditioningSE = se; }


     /**
     * Check the dependency of this precondition over another ScheduleElement
     * @param ScheduleElement
     * @return boolean true
     */
    public boolean dependentOf(ScheduleElement s) {
	if (s==getScheduleElement())
	    return true;
	else
	    return false;
    }

  /**
   * Returns the parent MLCSchedule
   */
  public MLCSchedule getParentMLC() {
    return parentMLC;
  }

  /**
   * Sets the parent MLCSchedule
   */
  public void setParentMLC(MLCSchedule s) {
      parentMLC = s;
  }

    public String toString() { return toTTaems(Taems.VCUR); }

  /**
   * Returns a textual Taems version of the precondition
   * @param v The version number output style to use
   * @return String containing TTaems representation
   */
  public abstract String toTTaems(float v);
    
    /**
     * Retuns true if this precondition is optional
     * @return boolean
     */
    public boolean isOptional() { return optional; }
    
    /**
     * retargetVirtual
     */
    public void retargetVirtual(MLCSchedule s, Vector clonedSE) {
	setParentMLC(s);
	if ((getScheduleElement() != null) && 
	    (getScheduleElement() instanceof VirtualScheduleElement)){
	    for (Enumeration e = clonedSE.elements() ; e.hasMoreElements(); ) {
		ScheduleElement se = (ScheduleElement)e.nextElement();
		if (getScheduleElement().matches(se)) 
		    setScheduleElement(se);
	    }
	}
    }
	
    /**
     * Define this precondition has optional or not
     * @param v - boolean
     */
    public void setOptional(boolean v) { optional = v; }

    /**
     * Clone function
     */
    public abstract Object clone();

}

