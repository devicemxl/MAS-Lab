/* Copyright (C) 2005, University of Massachusetts, Multi-Agent Systems Lab
 * See LICENSE for license information
 */

/************************************************************
 * VirtualScheduleElement.java
 ************************************************************/

package taems;

/* Global imports */
import java.awt.*;
import java.io.*;

import taems.*;

/**
 * Virtual schedule elements are used in the cloning sequence of
 * of ScheduleElement. They are place holder.
 */
public class VirtualScheduleElement extends ScheduleElement implements Serializable, Cloneable {

  /**
   * Default constructor
   * @param id The scheduleElement ID
   */
  public VirtualScheduleElement(int id) {
    super(id);
  }

  /** 
   * Accessors
   */
  public boolean isVirtual() { return true; }

    /**
     * Determines if an object matches this one.
     * @see Node#matches
     */
    public boolean matches(ScheduleElement se) {
	int ID = se.getID();
	if (se.hasAttribute("Cloned")) 
	    ID = ((Integer)se.getAttribute("Cloned")).intValue();
	if (getID() == ID)
	    return true;
	else
	    return false;
    }

 /**
   * Stringify
   */

  public String stringify() {
      String print = "- @->" + getID() +" - ";
      return print;
  }
      
   
  /**
   * Returns the ttaems version of the node
   * @param v The version number output style to use
   */
  public String toTTaems(float v) {
    StringBuffer sb = new StringBuffer("");

    sb.append(";(spec_virt_sched_element\n");
    sb.append(";   (referenceto " + getID() + ")\n");
    sb.append(";)\n");

    return sb.toString();
  }
}
