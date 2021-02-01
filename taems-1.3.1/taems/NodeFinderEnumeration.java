/* Copyright (C) 2005, University of Massachusetts, Multi-Agent Systems Lab
 * See LICENSE for license information
 */

/************************************************************
 * NodeFinderEnumeration.java
 ************************************************************/

package taems;

/* Global imports */
import java.awt.*;
import java.io.*;
import java.util.*;

import taems.*;

/**
 * Node finder
 */
class NodeFinderEnumeration implements Enumeration {
  Vector matches = new Vector();
  Vector looked = new Vector();
  Node match;
  
  public NodeFinderEnumeration(Enumeration es, Node m) {
    match = m;

    while (es.hasMoreElements()) {
      Node s = (Node)es.nextElement();
      findMatches(s);
    }
  }

  public NodeFinderEnumeration(Node s, Node m) {
    match = m;
    findMatches(s);
  }
  
  public boolean hasMoreElements() {
    return (!matches.isEmpty());
  }
  
  public Object nextElement() {
    Object o = matches.firstElement();
    matches.removeElement(o);
    return o;
  }
  
  private boolean err = false;
  private void findMatches(Node n) {
    Enumeration e;
    
    // Check
    if (n == null) {
        err = true;
        utilities.Log.getDefault().log("Warning: NodeFinderEnumeration.findMatches() called with null node", 1);
        return;
    }

    // Have we already seen this node
    if (looked.contains(n)) return;
    looked.addElement(n);

    // Does it match?
    if (n.matches(match)) {
        matches.addElement(n);
    }
    
    // Look at out IRs
    e = n.getOutInterrelationships();
    while (e.hasMoreElements()) {
        findMatches((Node)e.nextElement());
    }    

    // Look at subtasks
    if (n instanceof Task) {
      e = ((Task)n).getSubtasks();
      while (e.hasMoreElements()) {
          findMatches((Node)e.nextElement());
      }

    // Look at IR endpoints
    } else if (n instanceof Interrelationship) {
      Node f = ((Interrelationship)n).getFrom();
      findMatches(f);
      Node t = ((Interrelationship)n).getTo();
      findMatches(t);
    }
    
    // Look at in IRs, just in case
    e = n.getInInterrelationships();
    while (e.hasMoreElements()) {
      findMatches((Node)e.nextElement());
    }

    // Err check
    if (err) {
        err = false;
        utilities.Log.getDefault().log("Source of last error may be node " + n.getLabel(), 1);
    }
  }
}
