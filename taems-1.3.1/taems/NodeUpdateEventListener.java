/* Copyright (C) 2005, University of Massachusetts, Multi-Agent Systems Lab
 * See LICENSE for license information
 */

/***************************************************************************************
 * NodeUpdateEventListener.java
 ***************************************************************************************/

package taems;

import java.util.*;

/**
 * Interface for listening to graphic update events
 */
public interface NodeUpdateEventListener extends EventListener {

  /**
   */
  public void valueUpdate(NodeUpdateEvent e);

  /**
   */
  public void graphicUpdate(NodeUpdateEvent e);

  /**
   */
  public void placementUpdate(NodeUpdateEvent e);

  /**
   */
  public void structureUpdate(NodeUpdateEvent e);
}
