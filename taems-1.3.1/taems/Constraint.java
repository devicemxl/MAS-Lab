/* Copyright (C) 2005, University of Massachusetts, Multi-Agent Systems Lab
 * See LICENSE for license information
 */

/************************************************************
 * Constraint.java
 * created by Regis Vincent (1999-2000)
 ************************************************************/

package taems;

import java.util.*;
import java.io.*;
import java.awt.*;
import utilities.Distribution;

/**
 * Constraint object, for use with MLCSchedule. 
 */
public abstract class Constraint implements Serializable, Cloneable {

  protected MLCSchedule parentMLC;

  /**
   * Checks if precondition is satisfied, ie. if linked method can be started
   * @param int time (current time);
   */
  public abstract boolean check(int time);

  /**
   * Returns a textual version of the precondition
   * Debugging purpose
   */
  public abstract String output();

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

}
