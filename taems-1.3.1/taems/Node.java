/* Copyright (C) 2005, University of Massachusetts, Multi-Agent Systems Lab
 * See LICENSE for license information
 */

/************************************************************
 * Node.java
 ************************************************************/

package taems;

/* Global imports */
import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;

import utilities.*;

/**
 * A node superclass.  This forms the basis for all tangible
 * elements of the Taems graph.
 * <P>
 * You shouldn't have to instantiate one of these directly,
 * except when using them to find other matching nodes.
 */
public class Node implements Serializable, Cloneable, NodeUpdateEventListener, Comparable {
  /**
   * Status constants
   */
  public    static final int    NORMAL = 0;
  public    static final int    EXECUTING = 1;
  public    static final int    COMPLETED = 2;
  public    static final int    ACTIVE = 3;
  public    static final int    OVERLOADED = 4;
  public    static final int    UNDERLOADED = 5;
  public    static final int    DISABLED = 6;

  /**
   * Display flags
   */
  public    static final int    HIDDEN    = 1;
  public    static final int    SELECTED  = 2;
  public    static final int    COLLAPSED = 3;

  protected String label = "";
  protected Agent agent = null;
  protected Vector outirs = new Vector();
  protected Vector inirs = new Vector();
  protected Hashtable attributes = new Hashtable();

  protected int status;
  protected transient long flags = 0;
  protected transient Vector listeners = new Vector();

  /**
   * Default constructor
   * @param l The node's label
   * @param a The agent the node belongs to
   */
  public Node(String l, Agent a) {

    label = l;
    agent = a;
  }

  /**
   * Default constructor
   * @param l The node's label
   */
  public Node(String l) {
    this (l, null);
  }

  /**
   * Blank constructor
   */
  public Node() {
    this(null, null);
  }

  /**
   * Accessors
   */
  public String getLabel() { return label; }
  public void setLabel(String l) {
      if (getLabel() != null)
          if (getLabel().equals(l)) return;
      label = l;
      rect = null;
      fireNodeUpdateEvent(new NodeUpdateEvent(this, NodeUpdateEvent.VALUE));
  }
  public Agent getAgent() { return agent; }
  public void setAgent(Agent a) {
      if (getAgent() != null) {
          if (a != null && getAgent().matches(a)) return;
      }
      agent = a;
      fireNodeUpdateEvent(new NodeUpdateEvent(this, NodeUpdateEvent.VALUE));
  }
  public boolean isVirtual() { return false; }

    /**
     * Listener accessors
     */
    public void addNodeUpdateEventListener(NodeUpdateEventListener o) {
        if (o == this)
            System.err.println("Warning: Node " + getLabel() + " just started listening to itself");
        listeners.addElement(o);
    }
    public void removeNodeUpdateEventListener(NodeUpdateEventListener o) {
        listeners.removeElement(o);
    }

    protected void fireNodeUpdateEvent(NodeUpdateEvent ev) {
        Enumeration e = listeners.elements();

        while (e.hasMoreElements()) {
            NodeUpdateEventListener l = (NodeUpdateEventListener)e.nextElement();

            switch (ev.getID()) {
            case NodeUpdateEvent.VALUE:
                l.valueUpdate(ev);
                break;
            case NodeUpdateEvent.GRAPHIC:
                l.graphicUpdate(ev);
                break;
            case NodeUpdateEvent.PLACEMENT:
                l.placementUpdate(ev);
                break;
            case NodeUpdateEvent.STRUCTURE:
                l.structureUpdate(ev);
                break;
            }
        }
    }

  /**
   * Listener code
   */
  public void valueUpdate(NodeUpdateEvent e) { fireNodeUpdateEvent(e); }
  public void graphicUpdate(NodeUpdateEvent e) { fireNodeUpdateEvent(e); }
  public void placementUpdate(NodeUpdateEvent e) { fireNodeUpdateEvent(e); }
  public void structureUpdate(NodeUpdateEvent e) { fireNodeUpdateEvent(e); }

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
    else {
        Object o = getAttribute(k);
        if ((o == null) || (!o.toString().equals(d.toString()))) {
            attributes.put(k.toString(), d);
            fireNodeUpdateEvent(new NodeUpdateEvent(this, NodeUpdateEvent.VALUE));
        }
    }
  }

  /**
   * Removes an attribute field from the object.
   * @param k The attribute to remove.
   */
  public void removeAttribute(Object k) {
      if (attributes.remove(k.toString()) != null)
          fireNodeUpdateEvent(new NodeUpdateEvent(this, NodeUpdateEvent.VALUE));
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
   * Returns the number of attributes.
   * @return The number of attributes posessed by the object
   */
    public int numAttributes() {
      return attributes.size();
  }

  /**
   * Adds an interrelationship, using this node as the source, and
   * the given node as the target.  Note that if you try to add an
   * IR that has previously been added, it will be retargeted (e.g.
   * the original one will be removed).
   * @param i The interrelationship to add
   * @param o The source outcome (may be null)
   * @param t The target of the relationship
   */
  public void addInterrelationship(Interrelationship i, Outcome o, Node t) {

    i.setEndpoints(this, o, t);
  }
  public void addInterrelationship(Interrelationship i, Node t) {

    i.setEndpoints(this, null, t);
  }

  /**
   * Removes an interrelationship from both this node and
   * from the other end node.  If you wish to move an IR around,
   * you must first remove it, then re-add it.
   * the original one will be removed).
   * @param i The interrelationship to add
   */
  public void removeInterrelationship(Interrelationship i) {

    if ((outirs.contains(i)) || (inirs.contains(i)))
      i.excise();
  }

  /**
   * Interrelationship accessors
   */
  public Enumeration getOutInterrelationships() {
      Vector v = new Vector();
      v.addElement(outirs.elements());
      return new EnumerationEnumeration(v, true);
  }
  public Enumeration getOutInterrelationships(Interrelationship i) {
    return new NodeFinderEnumeration(outirs.elements(), i);
  }
  protected void addOutInterrelationship(Interrelationship i) {
    if (this instanceof Interrelationship) {
      System.err.println("Error: Cannot attach an IR to another IR");
      return;
    }
    outirs.addElement(i);
    i.addNodeUpdateEventListener(this);
  }
  protected void removeOutInterrelationship(Interrelationship i) {
    outirs.removeElement(i);
    i.removeNodeUpdateEventListener(this);
  }
  public boolean hasOutInterrelationships() {
    return (outirs.size() > 0);
  }
  public int numOutInterrelationships() {
    return outirs.size();
  }
  public Enumeration getInInterrelationships() {
      Vector v = new Vector();
      v.addElement(inirs.elements());
      return new EnumerationEnumeration(v, true);
  }
  public Enumeration getInInterrelationships(Interrelationship i) {
    return new NodeFinderEnumeration(inirs.elements(), i);
  }
  protected void addInInterrelationship(Interrelationship i) {
    if (this instanceof Interrelationship) {
      System.err.println("Error: Cannot attach an IR to another IR");
      return;
    }
    inirs.addElement(i);
  }
  protected void removeInInterrelationship(Interrelationship i) {
    inirs.removeElement(i);
  }
  public boolean hasInInterrelationships() {
    return (inirs.size() > 0);
  }
  public int numInInterrelationships() {
    return inirs.size();
  }
  public boolean hasInterrelationships() {
    return (hasInInterrelationships() || hasOutInterrelationships());
  }

  /**
   * Detaches the node from the structure it is in, be careful with
   * this.  Note you may have to also call Taems.removeNode to remove
   * it from the enclosing Taems structure.
   * <P>
   * If necessary, this node will return a virtual node that can
   * be used as glue in the task structure it is coming from.
   */
  public Node excise() {
    VirtualNode vme = new VirtualNode(getLabel());

    transferInterrelationships(vme);

    return vme;
  }

  /**
   * Destroys this node, and any other nodes that are dependent on it.
   * Note you may have to also call Taems.removeNode to remove
   * it from the enclosing Taems structure.
   */
  public void delete() {

      while (hasInInterrelationships()) {
          Interrelationship i = (Interrelationship)inirs.firstElement();
          i.delete();
      }
      while (hasOutInterrelationships()) {
          Interrelationship i = (Interrelationship)outirs.firstElement();
          i.delete();
      }
  }

  /**
   * Transfers all interrelationships going in or out of this
   * node to another node
   * @param n The node to move all IRs to
   */
  public void transferInterrelationships(Node n) {
   
      if (this == n) return;

      while (hasInInterrelationships()) {
          Interrelationship i = (Interrelationship)inirs.firstElement();
          i.setTo(n);
      }
      while (hasOutInterrelationships()) {
          Interrelationship i = (Interrelationship)outirs.firstElement();
          if ((i.getFromOutcome() != null) && (n instanceof Method))
              i.setFrom(n, ((Method)n).getOutcome(i.getFromOutcome().getLabel()));
          else
              i.setFrom(n);
      }
  }

  /**
   * Returns an enumeration of all the IRs which may affect this
   * node.  For tasks and methods, this should include all those
   * IRs which affect their supertasks as well.
   * @return An enumeration of the affecting IRs
   */
  public Enumeration getAffectingInterrelationships() {
    return getInInterrelationships();
  }

  /**
   * Returns an enumeration of all the IRs which are affected by this
   * node.  For tasks and methods, this should include all those
   * IRs which are affected by their supertasks as well.
   * @return An enumeration of the affected IRs
   */
  public Enumeration getAffectedInterrelationships() {
    return getOutInterrelationships();
  }

  /**
   * Determines if an object matches this one.  The matching
   * details are as follows:
   * <UL>
   * <LI>[Reference]Null == anything
   * <LI>[String]ABC == any string starting with ABC (case sensitive)
   * <LI>[integer]Integer.MIN_VALUE == any integer
   * <LI>[long]Long.MIN_VALUE == any long
   * <LI>[double]Double.NEGATIVE_INFINITY == any double
   * <LI>[float]Float.NEGATIVE_INFINITY == any float
   * <LI>[boolean]Strict, no wildcards available for booleans
   *  (** except if stored as Boolean, where null is wild)
   * <LI>[Distribution]Null == anything
   * </UL>
   * This function will return true if all the fields match within
   * this node.
   * <!-- (e.g. it will not consider actual subtasks, supertasks or
   * interrelationships). -->
   * Interrelationships are not checked (you can do that explicitly
   * yourself if this is important).  Matching in derived classes
   * should work analagously (and should also check instance type).
   * <BR>
   * This matches against:
   * <UL>
   * <LI> Label
   * <LI> Agent
   * </UL>
   * Note that attributes are <I>not</I> matched against, unless otherwise
   * noted.
   * @param n The node to match against
   * @return true if they match
   */
    public boolean matches(Node n) {

        if (!matches(n.getLabel(), getLabel())) return false;
        if (!matches(n.getAgent(), getAgent())) return false;

        return true;
    }

    /**
     * Matches helper function.
     * @see #matches
     */
    protected static boolean matches(String check, String current) {
        if (check != null) {
            if (current == null) {
                return false;
            } else {
                /*
                  if (n.getLabel().endsWith("*")) {
                  String realn = n.getLabel().substring(0, n.getLabel().indexOf("*"));
                  if (! getLabel().startsWith(realn)) return false;
                  } else if (! getLabel().equalsIgnoreCase(n.getLabel())) return false;
                */
                //if (! getLabel().equalsIgnoreCase(n.getLabel())) return false;
                if (! current.equals(check)) return false;
            }
        }
        return true;
    }
    /**
     * Matches helper function.
     * @see #matches
     */
    protected static boolean matches(Agent check, Agent current) {
        if (check != null) {
            if (current == null) {
                return false;
            } else {
                if (! current.matches(check)) return false;
            }
        }
        return true;
    }
    /**
     * Matches helper function.
     * @see #matches
     */
    protected static boolean matches(int check, int current) {
        if (check != Integer.MIN_VALUE) {
            if (current != check) return false;
        }
        return true;
    }
    /**
     * Matches helper function.
     * @see #matches
     */
    protected static boolean matches(long check, long current) {
        if (check != Long.MIN_VALUE) {
            if (current != check) return false;
        }
        return true;
    }
    /**
     * Matches helper function.
     * @see #matches
     */
    protected static boolean matches(float check, float current) {
        if (check != Float.NEGATIVE_INFINITY) {
            if (current != check) return false;
        }
        return true;
    }
    /**
     * Matches helper function.
     * @see #matches
     */
    protected static boolean matches(double check, double current) {
        if (check != Double.NEGATIVE_INFINITY) {
            if (current != check) return false;
        }
        return true;
    }
    /**
     * Matches helper function.
     * @see #matches
     */
    protected static boolean matches(Boolean check, Boolean current) {
        if (check != null) {
            if (current == null) {
                return false;
            } else {
                if (current.booleanValue() != check.booleanValue()) return false;
            }
        }
        return true;
    }
    /**
     * Matches helper function.
     * @see #matches
     */
    protected static boolean matches(Distribution check, Distribution current) {
        if (check != null) {
            if (current == null) {
                return false;
            } else {
                if (! current.equals(check)) return false;
            }
        }
        return true;
    }

  /**
   * Finds matching nodes using this node as the source point,
   * given another node using the specification given 
   * in the match function.  Most object will just determine
   * if they themselves match or not, but container objects
   * (such as Task and Taems) should also search through
   * the other nodes they contain for matches.
   * <P>
   * Here's some examples:
   * <DL>
   * <DT><TT>findNodes(new Node())</TT>
   * <DD>Returns all the nodes (inc Methods, Tasks, IRs, etc.)
   * <DT><TT>findNodes(new Method())</TT>
   * <DD>Returns all the accessible methods.
   * <DT><TT>findNodes(new Node(myNode.getLabel()))</TT>
   * <DD>Returns all the nodes who's label equals myNode's label.
   * <!-- <DT><TT>findNodes(new Node("foo*"))</TT> -->
   * <!-- <DD>Returns all the nodes who's label starts with foo -->
   * <DT><TT>findNodes(new Node(null, new Agent("Agent1")))</TT>
   * <DD>Returns all the nodes owned by Agent1.
   * <DT><TT>findNodes(new Method("Foo", new Agent("Agent1")))</TT>
   * <DD>Returns all the methods who's label starts with "Foo" and is
   * owned by Agent1.
   * <DT><TT>findNodes(new EnablesInterrelationship(null, null, new Distribution(1.0, 1.0)))</TT>
   * <DD>Returns all the enables relations which have the given distribution.
   * <DT><TT>findNodes(new Task(null, new Agent("Agent1"), new SumQAF()))</TT>
   * <DD>Returns all the Tasks owned by Agent1 which have a sum QAF.
   * <DT><TT>findNodes(new Resource(null, 0, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY))</TT>
   * <DD>Returns all the Resources with a current level of 0.
   * </DL>
   * @param n The node to match on
   * @return An enumeration of matching nodes
   * @see #matches
   */
  public Enumeration findNodes(Node n) {
    return new NodeFinderEnumeration(this, n);
  }

  /**
   * Finds the first matching node using this node as the source point.
   * @param n The node to match on
   * @return A matching node, or null if none found
   * @see #matches
   * @see #findNodes
   */
  public Node findNode(Node n) {
    Enumeration e = findNodes(n);

    if (e.hasMoreElements())
      return (Node)e.nextElement();
    else
      return null;
  }

  /**
   * Copies the nodes fields into the provided node.  Does
   * not do anything with IRs.
   * @param n The node to copy stuff into
   */
    public void copy(Node n) {

        if (label != null) n.setLabel(new String(label));
        else n.setLabel("");
        if (agent != null) n.agent = (Agent)agent.clone();
        else n.setAgent(null);

        if (attributes != null)
            n.attributes = (Hashtable)attributes.clone();

        n.setStatus(getStatus());
    }

  /**
   * Clone me, will attach irs going out from this node, but
   * not those coming in.  (Gotta do that elsewhere, probably in Task
   * and Taems clone).
   */
  public Object clone() {
    Node cloned = null;

    try {
      cloned = (Node)super.clone();
      cloned.listeners = new Vector();
    } catch (Exception e) {
      System.out.println("Clone Error: " + e);
    }

    copy(cloned);

    cloned.outirs = new Vector();
    Enumeration e = getOutInterrelationships();
    while (e.hasMoreElements()) {
      Interrelationship i = (Interrelationship)((Interrelationship)e.nextElement()).clone();
      i.setFrom(cloned, null);
    }
    cloned.inirs = new Vector();

    return cloned;
  }

  /**
   * Writes out the attributes block in a textual taems
   * kind of way.
   * @param attrs The attributes hashtable
   * @param v The version number output style to use
   */
    public static String attributesToString(Hashtable attrs, float v) {
        StringBuffer sb = new StringBuffer("");

        if (v > Taems.V1_0) {
            Enumeration e = attrs.keys();
            boolean gotsome = e.hasMoreElements();
            if (gotsome)
                sb.append("   (spec_attributes\n");
            while (e.hasMoreElements()) {
                Object k = e.nextElement();
                String key = k.toString();
                if ((key.indexOf(" ")  != -1) ||
                    (key.indexOf("\t") != -1) ||
                    (key.indexOf("\n") != -1) ||
                    (key.indexOf("\r") != -1)) {
                    sb.append(";     (<multi-line key ommitted>)\n");
                    continue;
                }
                Object d = attrs.get(k);
                String type = null;
                //            String data = d.toString();
                String data = Converter.unTypeProperty(d,null);
                if (data == null) {
                    if (d instanceof Node) {
                        data = ((Node)d).getLabel();
                        type = "Node ";
                        if (((Node)d).isVirtual())
                            sb.append(";   ** Note: Node " + data + " is virtual\n");  
                    } else {
                        sb.append(";     (" + key + " <un-convertible " + d.getClass().toString() + " ommitted>)\n");
                        continue;
                    }
                }
                if ((data.indexOf("\n") != -1) ||
                    (data.indexOf("\r") != -1)) {
                    sb.append(";     (" + key + " <multi-line data ommitted>)\n");
                    continue;
                }
                if (v >= Taems.V1_1) {
                    if (type == null) {
                        type = d.getClass().getName();
                        type = type.substring(type.lastIndexOf(".") + 1) + " ";
                    }
                } else {
                    type = "";
                }
                sb.append("      (");
                sb.append(key);
                sb.append(" ");
                sb.append(type);
                sb.append(data);
                sb.append(")\n");
            }
            if (gotsome)
                sb.append("   )\n");
        }

        return sb.toString();
    }

  /**
   * Returns the ttaems version of the node
   * @param v The version number output style to use
   */
  public String toTTaems(float v) {
    StringBuffer sb = new StringBuffer("");

    sb.append(attributesToString(attributes, v));

    if (getLabel() != null) {
      sb.append("   (label " + getLabel() + ")\n");
    }
    if (getAgent() != null) {
      sb.append("   (agent " + agent.getLabel() + ")\n");
    }
    
    //sb.append(";  ** Listeners: " + listeners.size() + "\n");
    sb.append(";  ** In IRs: ");
    sb.append(numInInterrelationships());
    sb.append("\n");
    sb.append(";  ** Out IRs: ");
    sb.append(numOutInterrelationships());
    sb.append("\n");

    return sb.toString();
  }

    /**
     * Stringify
     */
    public String toString() {
        return toTTaems(Taems.VCUR);
    }

    public int compareTo(Object o) {
        return getLabel().compareTo(((Node)o).getLabel());
    }

  /***********************************************************
   *                     Drawing Junk                        *
   ***********************************************************/
  protected static final int H_MARGIN = 5;
  protected static final int V_MARGIN = 2;

  protected transient Rectangle rect = null;
  protected transient Point loc = null;

  protected static Font normalFont = new Font("SansSerif", Font.PLAIN, 12);
  protected static Font mediumFont = new Font("SansSerif", Font.PLAIN, 10);
  protected static Font smallFont = new Font("SansSerif", Font.PLAIN, 8);
    protected static Stroke normalstroke = new BasicStroke();
    protected static Stroke collapsedstroke = new BasicStroke(2);

  /**
   * Accessors
   */
  protected void setLocation(Point p) {
      loc = p;
      fireNodeUpdateEvent(new NodeUpdateEvent(this, NodeUpdateEvent.GRAPHIC));
  }
  protected void offsetLocation(Point p) {
      Point np = new Point(getLocation().x + p.x, getLocation().y + p.y);
      setLocation(np);
  }
  public Point getLocation() { return loc; }
  public void setStatus(int s) {
      if (status != s) {
          status = s;
          fireNodeUpdateEvent(new NodeUpdateEvent(this, NodeUpdateEvent.GRAPHIC));
      }
  }
  public int getStatus() { return status; }

  public void setSelected(boolean s) {
      if (s) setFlag(SELECTED);
      else unsetFlag(SELECTED);
  }
  public boolean isSelected() {
      return getFlag(SELECTED);
  }
  public void setHidden(boolean s) {
      if (s) setFlag(HIDDEN);
      else unsetFlag(HIDDEN);
  }
  public boolean isHidden() {
      return getFlag(HIDDEN);
  }
  public void setCollapsed(boolean s) {
      if (s) setFlag(COLLAPSED);
      else unsetFlag(COLLAPSED);
  }
  public boolean isCollapsed() {
      return getFlag(COLLAPSED);
  }
    public void toggleCollapsed() {
        setCollapsed(!isCollapsed());
    }

    public boolean isVisible() {
        return !isHidden();
    }

  public void setFlag(int f) {
      if (! getFlag(f)) {
          flags |= (1 << f);
          fireNodeUpdateEvent(new NodeUpdateEvent(this, NodeUpdateEvent.GRAPHIC));
      }
  }
  public void unsetFlag(int f) {
      if (getFlag(f)) {
          flags &= ~(1 << f);
          fireNodeUpdateEvent(new NodeUpdateEvent(this, NodeUpdateEvent.GRAPHIC));
      }
  }
  public boolean getFlag(int f) {
      int mask = (1 << f);
      return ((flags & mask) == mask);
  }  

  public Rectangle getBounds() { return rect; }

  /**
   * Determines if a point is contained by this object.
   */
  public boolean contains(Point p) {

    if (isHidden()) return false;

    if (rect != null)
      return rect.contains(p);
    else
      return false;
  }

  /**
   * Calculates the width of the object.
   */
    public int calculateWidth(FontMetrics fm) {

        if (getLabel() == null) {
            return 0;
        } else {
            if ((fm == wsavefm) && (rect != null)) {
                return rect.width;
            } else {
                wsavefm = fm;
                return fm.stringWidth(getLabel()) + H_MARGIN * 2;
            }
        }
    }
    private FontMetrics wsavefm;

  /**
   * Calculates the width of the object, plus any subnodes.
   */
  public int calculateTreeWidth(FontMetrics fm) {
      return calculateWidth(fm);
  }

  /**
   * Calculates the height of the object.
   */
    public int calculateHeight(FontMetrics fm) {

        if (getLabel() == null) {
            return 0;
        } else {
            if ((fm == hsavefm) && (rect != null)) {
                return rect.height;
            } else {
                hsavefm = fm;
                return fm.getHeight() + V_MARGIN * 2;
            }
        }
    }
  private FontMetrics hsavefm;

  /**
   * Calculates the height of the object, plus any subnodes.
   */
    public int calculateTreeHeight(FontMetrics fm) {

        int height = calculateHeight(fm);

        /*Enumeration e = getOutInterrelationships();
          while (e.hasMoreElements()) {
          height += ((Node)e.nextElement()).calculateHeight(fm);
          }*/

        return height;
    }

    /**
     * Paints any connection lines that need to be painted
     * @param g Where to draw them
     */
    public void paintLines(Graphics g) {

        Enumeration e = getOutInterrelationships();
        while (e.hasMoreElements()) {
            Node n = (Node)e.nextElement();
            n.paintLines(g);
        }
    }

    /**
     * Updates the object's bounds
     */
    public void updateBounds(FontMetrics fm) {
        if (getLocation() == null) {
            System.err.println("Warning, updateBounds for node " + getLabel() + " ran into null location");
            rect = new Rectangle(0, 0, 1, 1);
            return;
        }
        int x = (int)loc.x;
        int y = (int)loc.y;
        int w = calculateWidth(fm);
        int h = calculateHeight(fm);
				
        // Cache the location
        rect = new Rectangle(x - w/2, y - h/2, w, h);
    }

  /**
   * Paints the node in its current location
   */
  public void paint(Graphics g) {
      g.setFont(normalFont);
      FontMetrics fm = g.getFontMetrics();
      updateBounds(fm);
      
      if (! isHidden()) {
          // Draw bounding rect
          g.setColor(getBackground());
          g.fillRect(rect.x, rect.y, rect.width, rect.height);
          g.setColor(Color.black);
          g.drawRect(rect.x, rect.y, rect.width, rect.height);
          
          // Draw label
          try {
              g.setColor(getForeground());
              g.drawString(getLabel(), rect.x + H_MARGIN, rect.y + rect.height - (V_MARGIN + fm.getDescent()));
          } catch (java.lang.NullPointerException e) {
              System.err.println("Error drawing label");
              e.printStackTrace();
          }
      }
      
      // Draw Interrelationships
      Enumeration e = getOutInterrelationships();
      while (e.hasMoreElements()) {
          Node n = (Node)e.nextElement();
          n.paint(g);
      }
  }

  /**
   * Returns the size of the node
   */
  public Dimension getSize(Graphics g) {
    return new Dimension(calculateWidth(g.getFontMetrics()), calculateHeight(g.getFontMetrics()));
  }

  /**
   * Get the background color.  These may be overridden with attributes named "Color_<STATUS>"
   * which contain a Vector object containing three Strings, which are numbers in the range
   * of 0-255 for the RGB values of the color.  See the Color object for what these mean.
   * <P>
   * For example:
   * <PRE>
   * (spec_task_group
   *    (spec_attributes
   *       (Color_NORMAL    Vector 0,128,255)
   *       (Color_EXECUTING Vector 128,255,0)
   *       (Color_COMPLETED Vector 255,0,128)
   *    )
   *   (label MyTask)
   *   (agent A)
   *   (qaf q_sum)
   * )
   * </PRE>
   */
    protected Color getBackground() {
        Color c = Color.white;
        Object o = null;

        // Get the default colors if selected
        if (isSelected()) {
            o = getAttribute("Color_SELECTED");
            c = Color.yellow;
        }

        // ...or not
        else switch(getStatus()) {
        case UNDERLOADED:
            o = getAttribute("Color_UNDERLOADED");
            c = Color.cyan;
            break;
        case EXECUTING:
            o = getAttribute("Color_EXECUTING");
            c = Color.green;
            break;
        case ACTIVE:
            o = getAttribute("Color_ACTIVE");
            c = Color.green;
            break;
        case COMPLETED:
            o = getAttribute("Color_COMPLETED");
            c = Color.pink;
            break;
        case OVERLOADED:
            o = getAttribute("Color_OVERLOADED");
            c = Color.pink;
            break;
        case DISABLED:
            o = getAttribute("Color_DISABLED");
            c = Color.lightGray;
            break;
        case NORMAL:
            o = getAttribute("Color_NORMAL");
            c = Color.white;
            break;
        }

        // See if it was overridden by an attribute
        // (note there isn't much error checking going on here)
        if (o != null && o instanceof Vector) {
            Vector v = (Vector)o;
            String r = (String)v.elementAt(0);
            String g = (String)v.elementAt(1);
            String b = (String)v.elementAt(2);
            c = new Color(Integer.parseInt(r), Integer.parseInt(g), Integer.parseInt(b));
        }

        // And return it
        return c;
    }

  /**
   * Get the foreground color
   */
  protected Color getForeground() {

    return Color.black;
  }
}
