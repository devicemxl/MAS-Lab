/* Copyright (C) 2005, University of Massachusetts, Multi-Agent Systems Lab
 * See LICENSE for license information
 */

/************************************************************
 * Agent.java
 ************************************************************/

package taems;

/* Global imports */
import java.io.*;
import java.util.*;

/**
 * A node superclass
 */
public class Agent implements Serializable, Cloneable {
  protected String label = "";
  protected Hashtable attributes = new Hashtable();

  /**
   * Default constructor
   */
  public Agent(String l) {
    label = l;
  }

  /**
   * Accessors
   */
  public String getLabel() { return label; }
  public void setLabel(String l) { label = l; }

  /**
   * Note that this uses the toString() method for the key
   * to actually store the object.
   * @param k The key identifying the desired attribute
   * @return The object, or null if not found
   */
  public Object getAttribute(Object k) {
    return attributes.get(k.toString());
  }

  /**
   * Sets an attribute's data.  Attribute keys should not
   * contain whitespace.
   * <P>
   * If a key is added with a null data object, the key will
   * be removed from the attribute set.
   * @param k The key identifying the desired attribute
   * @param d The attribute data
   */
  public void setAttribute(Object k, Object d) { 
    if (d == null)
      removeAttribute(k);
    else
      attributes.put(k.toString(), d);
  }

  /**
   * Removes an attribute field from the object.
   * @param k The attribute to remove.
   */
  public void removeAttribute(Object k) {
    attributes.remove(k.toString());
  }

  /**
   * Returns the attribute names
   * @return An enumeration of all the attribute keys
   */
  public Enumeration getAttributes() { return attributes.keys(); }

  /**
   * Determines if the node has a particular attribute
   * @return True if the attribute is present
   */
  public boolean hasAttribute(Object k) {
    return attributes.containsKey(k.toString());
  }

  /**
   * Returns a reference to the raw attribute table.  Not
   * for public consumption.
   * @return The attribute table, or null if not found
   */
  protected Hashtable getAttributesTable() {
      return attributes;
  }

  /**
   * Determines if an object matches this one.
   * <BR>
   * This matches against:
   * <UL>
   * <LI> Label
   * </UL>
   * @see Node#matches
   */
  public boolean matches(Agent a) {
      
      if (a != null) {
          if (!Node.matches(a.getLabel(), getLabel())) return false;
      }

      return true;
  }

  /**
   * Clone me
   */
  public Object clone() {
    Agent cloned = null;

    try {
      cloned = (Agent)super.clone();
    } catch (Exception e) {
      System.out.println("Clone Error: " + e);
    }

    if (attributes != null)
        cloned.attributes = (Hashtable)attributes.clone();

    if (label != null) cloned.setLabel(new String(label));
    else cloned.setLabel(null);

    return cloned;
  }

  /**
   * Copies the fields into the provided agent.
   * @param a The agent to copy stuff into
   */
    public void copy(Agent a) {

        if (label != null) a.setLabel(new String(label));
        else a.setLabel("");

        if (attributes != null)
            a.attributes = (Hashtable)attributes.clone();
    }

  /**
   * Returns the ttaems version of the node
   * @param v The version number output style to use
   */
  public String toTTaems(float v) {
    StringBuffer sb = new StringBuffer("");

    sb.append("(spec_agent\n");

    sb.append(Node.attributesToString(attributes, v));

    sb.append("  (label " + getLabel() + ")\n");
    sb.append(")\n");
    
    return sb.toString();
  }

  /**
   * Stringify
   */
  public String toString() {
    return toTTaems(Taems.VCUR);
  }
}

