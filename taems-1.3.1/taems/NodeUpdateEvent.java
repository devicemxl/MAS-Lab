/* Copyright (C) 2005, University of Massachusetts, Multi-Agent Systems Lab
 * See LICENSE for license information
 */

/***************************************************************************************
 * NodeUpdateEvent.java
 ***************************************************************************************/

package taems;

import java.awt.*;

/**
 * Generic node event
 */
public class NodeUpdateEvent extends AWTEvent {

  // the integer IDs
  public static final int VALUE = 0;
  public static final int GRAPHIC = 1;
  public static final int PLACEMENT = 2;
  public static final int STRUCTURE = 3;

  /** 
   * Constructor
   * @param source The source of the event
   * @param id The id of the event
   */
  public NodeUpdateEvent(Object source, int id) {
    super(source, id);
  }

  /**
   * a simplistic toString function 
   */ 
  public String toString() {
    return "(NodeUpdateEvent) " + getID();
  }
}
