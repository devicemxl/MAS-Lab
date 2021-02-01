/* Copyright (C) 2005, University of Massachusetts, Multi-Agent Systems Lab
 * See LICENSE for license information
 */

/************************************************************
 * Taems.java
 ************************************************************/

package taems;

/* Global imports */
import javax.swing.*;
import java.beans.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.awt.geom.*;
import java.io.*;
import java.util.*;
import java.awt.datatransfer.*;

import javax.swing.filechooser.FileFilter;

import utilities.Log;
import utilities.SafeEnumeration;
import utilities.Distribution;
import utilities.VFlowLayout;
import utilities.Converter;

import taems.parser.*;

/**
 * A Taems structure will typically be wrapped with a Taems
 * class such as this.  The Taems class essentially provides
 * organization and functionality to the graph, by providing
 * access to the data itself from various points and implementing
 * utility functions useful when working with the graph.
 */
public class Taems extends JPanel implements Serializable, Cloneable, NodeUpdateEventListener {
    /**
     * Version constants
     */
    public static final float V1    = (float)1.0;
    public static final float V1_0  = V1;
    public static final float V1_0A = (float)1.01; // V1_0 + Attributes
    public static final float V1_1  = (float)1.1;
    public static final float VCUR  = V1_1;
    
    /**
     * scheduling_mode constants
     */
    public static final String EXHAUSTIVE_PRUNED_ALTERNATIVES = "exhaustive_from_pruned_alternatives";
    public static final String EXHAUSTIVE_ALTERNATIVES = "exhaustive_from_alternatives";
    public static final String EXHAUSTIVE = "pure_exhaustive";
    public static final String HEURISTIC = "heuristic";

    protected Vector nodes = new Vector();
    protected Vector agents = new Vector(1);
    protected Hashtable attributes = new Hashtable();
    protected Vector schedules = null;
    protected Vector commitments = new Vector();
    protected Criteria criteria ;
    
    protected Log log = Log.getDefault();
    
    protected transient Vector listeners = new Vector();
    protected transient boolean replace = true;
    protected transient boolean revalue = true;
    protected transient static boolean printing = false;

    /**
     * Default constructor
     * @param l The structure's label
     * @param a The owning agent
     */
    public Taems(String l, Agent a) {
        super();

        initGraphics();
        setLabel(l);
        setAgent(a);

        //setSize(250, 350);
    
        addNodeUpdateEventListener(this);
    }

    /**
     * Label constructor
     * @param l The structure's label
     */
    public Taems(String l) {
        this(l, null);
    }

    /**
     * Blank constructor
     */
    public Taems() {
        this(null);
    }

    /**
     * Gets the Taems structure's label.
     * @return The structure's label, or null if it has none
     * @see #setLabel
     */
    public String getLabel() {
        return (String)getAttribute("label");
    }

    /**
     * Sets the label on the task structure.  Currently, this
     * is stored as the structure's "<TT>label</TT>" attribute,
     * if you need to specify it textually.
     * @param l The new label
     */
    public void setLabel(String l) {
        if (getLabel() != null)
            if (getLabel().equals(l)) return;
        setAttribute("label", l);
        if (l == null) l = "";
    }

    /**
     * Other attribute-based accessors
     */
    public void setSchedulingMode(String s) { setAttribute("scheduling_mode", s); }
    public String getSchedulingMode() { return (String)getAttribute("scheduling_mode"); }
    public void setPruningSetting(String s) { setAttribute("pruning_setting", s); }
    public String getPruningSetting() { return (String)getAttribute("pruning_setting"); }

    /**
     * Gets the first agent, who is presumably the owner of the task
     * structure.  A convenience method.
     * @return The agent, or null if none specified
     */
    public Agent getAgent() {
        if (numAgents() > 0)
            return (Agent)agents.firstElement();
        else
            return null;
    }
    
    /**
     * Removes the previous owning agent (the first one on the list), and 
     * replaces it with the given one.  Note this does not modify any of the
     * nodes in the task structure.  If you want to reset the agents of those
     * nodes you must do it manually.
     * @param a The agent
     */
    public void setAgent(Agent a) {
        if (numAgents() > 0)
            removeAgent(getAgent());
        
        if (a != null)
            agents.insertElementAt(a, 0);
    }
    
    /**
     * Returns all the agent objects associated with the task structure
     * @return e An enumeration of Agent objects
     */
    public Enumeration getAgents() { return agents.elements(); }

    /**
     * Adds an agent to the Task structure.  If an existing Agent object
     * matches the one to be added, it is not added.
     * @param a The Agent to add
     */
    public void addAgent(Agent a) {
        if ((a != null) && (findAgent(a) == null)) {
            agents.addElement(a);
            fireNodeUpdateEvent(new NodeUpdateEvent(this, NodeUpdateEvent.VALUE));
        }
    }

    /**
     * Removes an agent from the Task structure.
     * @param a The Agent to remove
     */
    public void removeAgent(Agent a) {
        if (a != null) {
            agents.removeElement(a);
            fireNodeUpdateEvent(new NodeUpdateEvent(this, NodeUpdateEvent.VALUE));
        }
    }

    /**
     * Returns the number of agents associated with the structure
     * @return The number of agents
     */
    public int numAgents() { return agents.size(); }

    /**
     * A search function, to find a particular Agent object, based on
     * a "template" that is passed in.
     * @param a The template Agent object, which is used to find
     *  a matching one.
     * @return The matching Agent object, or null if none was found
     */
    public Agent findAgent(Agent a) {

        Enumeration e = getAgents();
        while (e.hasMoreElements()) {
            Agent agent = (Agent)e.nextElement();
            if (agent.matches(a)) return agent;
        }

        return null;
    }

    /**
     * Sets the schedule criteria object.
     * @param v The Criteria to set
     */
    public void setScheduleCriteria(Criteria v) {
        criteria = v;
    }

    /**
     * Gets the structure's schedule criteria object.
     * @return The Criteria, or null if none is specified
     */
    public Criteria getScheduleCriteria() {
        if (criteria != null) 
            return criteria;
        return null;
    }

    /**
     * Sets the schedule for the Taems object, replacing
     * all existing Schedules with the new one.
     * @param s The schedule to be assigned to the structure
     */
    public void setSchedule(Schedule s) {
        Vector v = new Vector();
        if (s != null)
            v.addElement(s);
        setScheduleVector(v);
    }

    /**
     * Sets the raw schedule vector for the task structure.
     * You should normally not need to make use of this function.
     * retargetSchedules is called, and the first schedule is
     * set as displayed.
     * @param sv The new vector of schedules.
     * @see #addSchedule
     * @see #removeSchedule
     */
    public void setScheduleVector(Vector sv) {
        if ((numSchedules() > 0) && (sv != schedules))
            removeAllSchedules();
        schedules = sv;
        retargetSchedules();
        setDisplayedSchedule(getFirstSchedule());
    }

    /**
     * Gets the raw schedule vector for the task structure.
     * You should normally not need to make use of this function.
     * @return The vector of schedules
     * @see #addSchedule
     * @see #removeSchedule
     */
    public Vector getScheduleVector() {
        if (schedules == null) 
            schedules = new Vector();
        return schedules;
    }

    /**
     * Returns the structure's schedules.  If it has none,
     * an empty enumeration is returned.
     * @return An enumeration of Schedule objects.
     */
    public Enumeration getSchedules() {
        return getScheduleVector().elements();
    }
  
    /**
     * Returns the first or active schedule, if the structure
     * has one.
     * @return The active schedule, or null if none found.
     */
    public Schedule getFirstSchedule() {
        if (numSchedules() < 1)
            return null;
        else
            return (Schedule)getScheduleVector().firstElement();
    }

    /**
     * If necessary, first adds the schedule, and then sets it
     * as the "first" or active schedule for the structure.  It
     * is also set as the displayed schedule for the structure.
     * @param s the schedule to make active
     */
    public void setFirstSchedule(Schedule s) {
        if (numSchedules() < 1) {
            setSchedule(s);
        } else {
            if (!schedules.contains(s)) {
                addSchedule(s);
            }
            Vector v = getScheduleVector();
            v.remove(s);
            v.insertElementAt(s, 0);
            setDisplayedSchedule(getFirstSchedule());
        }
    }

    /**
     * Adds the schedule to the task structure.  If the schedule
     * is the first to be added, it is also set as the displayed
     * schedule for the structure.  retargetSchedules is called.
     * @param s
     * @see #retargetSchedules
     */
    public void addSchedule(Schedule s) {
        Vector v = getScheduleVector();
        v.addElement(s);
        retargetSchedules();
        if (numSchedules() <= 1)
            setDisplayedSchedule(getFirstSchedule());
    }

    /**
     * Returns the number of schedules the task structure contains.
     * @return The number of schedules
     */
    public int numSchedules() {
        return getScheduleVector().size();
    }

    /**
     * Removes a schedule from the Taems structure, replacing
     * method references in its ScheduleElements with VirtualMethod
     * objects.
     * @param s The schedule to remove
     */
    public void removeSchedule(Schedule s) {
        
        if (schedules.contains(s)) {
            schedules.remove(s);
            Enumeration e = s.getElements();
            while (e.hasMoreElements()) {
                ScheduleElement se = (ScheduleElement)e.nextElement();
                Method m = se.getMethod();
                se.setMethod(new VirtualMethod(m.getLabel(), m.getAgent()));
            }
        }
    }

    /**
     * Removes all the schedules the structure posesses.
     */
    public void removeAllSchedules() {
        Enumeration e = new SafeEnumeration(getSchedules());
        while (e.hasMoreElements()) {
            Schedule s = (Schedule)e.nextElement();
            removeSchedule(s);
        }
    }

    /**
     * Uses the provided schedule to mark the methods which are to be executed.
     * Note that this doesn't add the schedule to the structure, it simply uses
     * it to set the schedule numbers on the individual methods, typically
     * for display purposes
     * @see #setSchedule
     * @see #setFirstSchedule
     * @param s The schedule to use
     */
    public void setDisplayedSchedule(Schedule s) {
      
        // Unset numbers
        Enumeration e = findNodes(new Method());
        while (e.hasMoreElements()) {
            Method m = (Method)e.nextElement();
            m.setScheduleNumber(-1);
        }

        // Update nodes
        if (s != null) {

            // Set numbers
            ScheduleElement old = null;
            e = s.getElements();
            for (int num = 1; e.hasMoreElements(); num++) {
                ScheduleElement se = (ScheduleElement)e.nextElement();
                if (old != null) {
                    if ((old.getStartTime() != null) && (se.getStartTime() != null)) {
                        if (se.getStartTime().containsValue(-1, 0)) {
                            // do not use times if they are bogus
                        } else if (old.getStartTime().equals(se.getStartTime())) {
                            num--;
                        }
                    }
                }
                se.getMethod().setScheduleNumber(num);
                old = se;
            }
        }
    }

    /**
     * Adds a commitment to the Task structure.  retargetCommitments
     * is called.
     * @param c The Commitment to add
     */
    public void addCommitment(Commitment c) {
        commitments.addElement(c);
        retargetCommitments();
    }

    /**
     * Removes a commitment from the task structure, replacing any
     * tasks it references with VirtualTaskBase objects.
     * @param c The commitment to remove
     */
    public void removeCommitment(Commitment c) {
        if (commitments.contains(c)) {
            commitments.removeElement(c);
            Vector v = new Vector();
            Enumeration e = c.getTasks();
            while (e.hasMoreElements()) {
                TaskBase t = (TaskBase)e.nextElement();
                v.addElement(new VirtualTaskBase(t.getLabel(), t.getAgent()));
            }
            c.setTaskVector(v);
        }
    }

    /**
     * Removes all the commitments the structure posesses.
     */
    public void removeAllCommitments() {
        Enumeration e = new SafeEnumeration(getCommitments());
        while (e.hasMoreElements()) {
            Commitment c = (Commitment)e.nextElement();
            removeCommitment(c);
        }
    }

    /**
     * Returns an enumeration of the task structure's commitments.
     * @return an Enumeration of Commitment objects
     */
    public Enumeration getCommitments() {
        return commitments.elements();
    }

    /**
     * Returns the first Commitment posessed by the Task structure.
     * This is a convenience function, other functions offer
     * access to all the Commitments associated with the structure.
     * @return The structure's first Commitment, or null if it has none
     * @see #getCommitments
     */
    public Commitment getCommitment() {
        if (numCommitments() > 0)
            return (Commitment)commitments.firstElement();
        else
            return null;
    }

    /**
     * Get the raw vector of Commitment objects.  Normally you should
     * not use this function, use the other acessors to manipulate this
     * listing.
     * @return A Vector of Commitment objects
     * @see #getCommitments
     * @see #addCommitment
     * @see #removeCommitment
     */
    public Vector getCommitmentVector() {
        return commitments;
    }

    /**
     * Set the raw vector of Commitment objects.  Normally you should
     * not use this function, use the other acessors to manipulate this
     * listing.  retargetCommitments is called.
     * @return A Vector of Commitment objects
     * @see #getCommitments
     * @see #addCommitment
     * @see #removeCommitment
     */
    public void setCommitmentVector(Vector v) {
        if ((numCommitments() > 0) && (v != commitments))
            removeAllCommitments();
        commitments = v;
        retargetCommitments();
    }

    /**
     * Returns the number of Commitment objects assigned to the structure.
     * @return a number
     */
    public int numCommitments() {
        return commitments.size();
    }

    /**
     * Listener accessors.  The Taems object uses an event-based system to
     * recognize when it should graphically update itself.  These functions
     * are used to support this system.
     * @param o The listener to add
     */
    public void addNodeUpdateEventListener(NodeUpdateEventListener o) {
        listeners.addElement(o);
    }

    /**
     * @see #addNodeUpdateEventListener
     */
    public void removeNodeUpdateEventListener(NodeUpdateEventListener o) {
        listeners.removeElement(o);
    }

    /**
     * Fires node update events for the structure
     * @param ev The event to fire
     */
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
                l.placementUpdate(ev);
                break;
            }
        }
    }

    /**
     * Called as part of the node event system.  This indicates that
     * a value has changed within the task structure.
     * @param e The event which was fired
     * @see #addNodeUpdateEventListener
     */
    public void valueUpdate(NodeUpdateEvent e) {
        if (display != null) {
            revalue = true;
            display.repaint();
        }
    }

    /**
     * Called as part of the node event system.  This indicates that
     * something graphical has changed within the task structure.
     * @param e The event which was fired
     * @see #addNodeUpdateEventListener
     */
    public void graphicUpdate(NodeUpdateEvent e) {
        if (display != null) {
            display.repaint(); 
            display.updatePreferredSize();
        }
    }

    /**
     * Called as part of the node event system.  This indicates that
     * a placement event has occurred within the task structure.
     * @param e The event which was fired
     * @see #addNodeUpdateEventListener
     */
    public void placementUpdate(NodeUpdateEvent e) {
        if (display != null) {
            replace = true;
            display.repaint(); 
        }
    }

    /**
     * Called as part of the node event system.  This indicates that
     * a structural change has occurred within the task structure.
     * @param e The event which was fired
     * @see #addNodeUpdateEventListener
     */
    public void structureUpdate(NodeUpdateEvent e) {
        if (display != null) {
            retargetVirtuals();
            replace = true;
            revalue = true;
            display.repaint(); 
        }
    }
  
  /**
   * Gets a Taems-level attribute.
   * @param k The key identifying the desired attribute
   * @return The object, or null if not found
   * @see #setAttribute
   */
  public Object getAttribute(Object k) {
    return attributes.get(k.toString());
  }

  /**
   * Sets a Taems-level attribute.  The key's toString() function is
   * used to store the data, and should not contain whitespace
   * or parentheses.
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
    fireNodeUpdateEvent(new NodeUpdateEvent(this, NodeUpdateEvent.VALUE));
  }

  /**
   * Removes an attribute field from the object.
   * @param k The attribute to remove.
   * @see #setAttribute
   */
  public void removeAttribute(Object k) {
    attributes.remove(k.toString());
    fireNodeUpdateEvent(new NodeUpdateEvent(this, NodeUpdateEvent.VALUE));
  }

  /**
   * Returns the attribute names
   * @return An enumeration of all the attribute keys
   */
  public Enumeration getAttributes() { return attributes.keys(); }

  /**
   * Determines if the node has a particular attribute
   * @param k The key to check for
   * @return True if the attribute is present
   * @see #setAttribute
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
   * Add a node to the taems structure.  Use this to add
   * things like task groups, virtual nodes or resources.
   * Pretty much anything that actually belongs in the graph
   * itself (like methods, tasks, IRs) should be added using
   * the accessors provided in the nodes themselves.
   * @see Task#addSubtask
   * @see Node#addInterrelationship
   * @param n The node to add
   */
  public void addNode(Node n) {
    if (n == null) return;

    if (nodes.contains(n)) {
      log.log("Warning: You just addded the node " + n.getLabel() + " to a Taems object that already had it", 1);
    }

    nodes.addElement(n);
    n.addNodeUpdateEventListener(this);

    if (n instanceof Interrelationship) {
        log.log("***************************************************************", Log.LOG_WARNING);
        log.log("Warning: You just addded an Interrelationship to a Taems object", Log.LOG_WARNING);
        log.log("While you can technically do this, you will experience wierdness", Log.LOG_WARNING);
        log.log("if you do so.  Instead, we recommend you add the from and to", Log.LOG_WARNING);
        log.log("nodes to the structure, which will make it much happier. E.g.", Log.LOG_WARNING);
        log.log("   BAD: Taems.addNode(Interrelationship);", Log.LOG_WARNING);
        log.log("  GOOD: Taems.addNode(Interrelationship.getTo());", Log.LOG_WARNING);
        log.log("        Taems.addNode(Interrelationship.getFrom());", Log.LOG_WARNING);
        log.log("***************************************************************", Log.LOG_WARNING);
        try { Thread.currentThread().sleep(1000); } catch (InterruptedException ex) { }
    }

    fireNodeUpdateEvent(new NodeUpdateEvent(this, NodeUpdateEvent.PLACEMENT));
  }

  /**
   * Removes a node from the taems structure.  This just
   * takes the node off of the high level list, it does not
   * actually disconnect it from its surroundings.
   * @param n The node to remove
   */
  public void removeNode(Node n) {
    nodes.removeElement(n);
    n.removeNodeUpdateEventListener(this);

    fireNodeUpdateEvent(new NodeUpdateEvent(this, NodeUpdateEvent.PLACEMENT));
  }

  /**
   * Excises a node from the taems structure.  This should
   * both remove the node from the high level list and chop
   * off all its connections to other nodes (child-parent
   * and NLE relations).
   * @param n The node to excise
   * @return The virtual node replacing the excised node
   */
  public Node exciseNode(Node n) {
    removeNode(n);
    return n.excise();
  }

  /**
   * Returns the number of resources that are known.
   */
  public int numResources() {
      int n = 0;
      Enumeration e = getNodes();
      while (e.hasMoreElements()) {
          if (e.nextElement() instanceof Resource)
              n++;
      }
      return n;
  }

  /**
   * Returns the number of task groups that are known.
   */
  public int numTaskGroups() {
      int n = 0;
      Enumeration e = getNodes();
      while (e.hasMoreElements()) {
          Node node = (Node)e.nextElement();
          if (node instanceof Task)
              if (((Task)node).isTaskGroup())
                  n++;
      }
      return n;
  }

  /**
   * Gets the nodes.  Note this only returns the highest level
   * nodes, if you want <I>all</I> the nodes contained in the
   * task structure, you should use findNodes.
   * @return An enumeration of all the high level nodes
   * @see #findNodes
   * @see #getAllNodes
   */
  public Enumeration getNodes() {
    return nodes.elements();
  }

  /**
   * Gets all the reachable nodes in the task structure.
   * @return An enumeration of all the nodes
   */
  public Enumeration getAllNodes() {
      return findNodes(new Node());
  }

  /**
   * Historical convenience function that searches for the first task
   * group node available and returns it.
   * <BR>
   * Note that this is <I>not</I> the same set that is added via addNode.
   * This will search through all the root level nodes and return the
   * first <I>task group</I> that is found.  So any other type of node
   * that is encountered will be skipped (including Methods and Resources).
   * If you really want to get the first Node in the task structure, 
   * regardless of its class type, you should use getNodes.
   * @return A task group node, or null if none found.
   * @see #getNodes
   */
  public Task getNode() {
    Enumeration e = getNodes();

    while (e.hasMoreElements()) {
      Node n = (Node)e.nextElement();
      if (n instanceof Task) {
	if (((Task)n).isTaskGroup())
	  return (Task)n;
      }
    }

    return null;
  }

  /**
   * Finds matching nodes using this node as the source point,
   * given another node using the specification given 
   * in the match function.
   * @return An enumeration of matching nodes
   * @see Node#matches
   * @see Node#findNodes
   */
  public Enumeration findNodes(Node n) {
    return new NodeFinderEnumeration(getNodes(), n);
  }

  /**
   * Finds the first matching node using this node as the source point.
   * @return A matching node, or null if none found
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
     * Unions another task structure with this one.  All unique
     * nodes will be present in the new structure.  If two nodes
     * are identical matches then the original is kept.  If two
     * nodes match only in name and agent, the new node is
     * used.  A clone of the new task structure is used during the
     * union, so you may safely use it afterwards.
     * @param t The new task structure to add
     * @see Node#matches
     */
    public void unionTaems(Taems t) {
        unionTaems(t, true);
    }

    /**
     * Same as above, except gives you the option of cloning 
     * the task structure before unioning it.  If the structure
     * is not cloned, you should not attempt to use the structure
     * passed in afterwards.
     * @param t The new task structure to union
     * @param clone Clone it before the union
     */
    public void unionTaems(Taems t, boolean clone) {
        if (clone)
            t = (Taems)t.clone();

        synchronized (addLock) {
            // It's smashin' time!
            smash();
            t.smash();
            
            // Find matches
            Enumeration oe = new SafeEnumeration(getNodes());
            while (oe.hasMoreElements()) {
                Node on = (Node)oe.nextElement();

                // Because they are smashed, I know IRs will only appear
                // attached to virtual nodes we don't care about.
                if (on.hasInInterrelationships())
                    continue;

                // Look for matches
                Enumeration ne = new SafeEnumeration(t.getNodes());
                while (ne.hasMoreElements()) {
                    Node nn = (Node)ne.nextElement();
                    
                    // Interrelationships are wierd
                    // Identify them by their from nodes
                    if (on.hasOutInterrelationships()) {
                        if (nn.hasOutInterrelationships()) {
                            Interrelationship oi = (Interrelationship)on.getOutInterrelationships().nextElement();
                            Interrelationship ni = (Interrelationship)nn.getOutInterrelationships().nextElement();

                            if (oi.matches(ni)) {
                                t.removeNode(ni.getTo());
                                t.removeNode(ni.getFrom());
                                break;
                                
                            } else if (Node.matches(ni.getLabel(), oi.getLabel()) &&
                                       Node.matches(ni.getAgent(), oi.getAgent())) {
                                removeNode(oi.getTo());
                                removeNode(oi.getFrom());
                                break;
                            }
                        }
                        continue;
                    }
                    if (nn.hasOutInterrelationships() || nn.hasInInterrelationships()) {
                        continue;
                    }

                    // Check class type
                    //if (! nn.getClass().isInstance(on)) continue;

                    // Nodes are normal
                    if (on.matches(nn)) {
                        // Exact match keeps old
                        t.removeNode(nn);
                        break;

                    } else if (Node.matches(nn.getLabel(), on.getLabel()) &&
                               Node.matches(nn.getAgent(), on.getAgent())) {
                        // Partial match keeps new
                        removeNode(on);
                        break;
                    }
                }
            }

            // Now add them
            addTaems(t, false);

            // Remove orphans
            removeOrphans();
        }
    }

    /**
     * Imports a taems structure into an existing one.  This essentially
     * means that the original Taems structure is deleted, and replaced 
     * with the new one, with one important difference.  Any nodes in the
     * old structure which exactly match those in the new are not replaced,
     * so references to those nodes won't break.  A clone of the new task
     * structure is used during the import, so you may safely use it afterwards.
     * @param t The new task structure to import
     * @see Node#matches
     */
    public void importTaems(Taems t) {
        importTaems(t, true);
    }

    /**
     * Same as above, except gives you the option of cloning 
     * the task structure before importing it.  If the structure
     * is not cloned, you should not attempt to use the structure
     * passed in afterwards.
     * @param t The new task structure to import
     * @param clone Clone it before the import
     */
    public void importTaems(Taems t, boolean clone) {
        if (clone)
            t = (Taems)t.clone();

        synchronized (addLock) {

            // It's smashin' time!
            smash();
            t.smash();
            
            // Find matches
            Enumeration oe = new SafeEnumeration(getNodes());
            while (oe.hasMoreElements()) {
                Node on = (Node)oe.nextElement();
                boolean found = false;

                // Because they are smashed, I know IRs will only appear
                // attached to virtual nodes we don't care about.
                if (on.hasInInterrelationships())
                    continue;

                // Look for matches
                Enumeration ne = new SafeEnumeration(t.getNodes());
                while (ne.hasMoreElements()) {
                    Node nn = (Node)ne.nextElement();
                    
                    // Interrelationships are wierd
                    // Identify them by their from nodes
                    if (on.hasOutInterrelationships()) {
                        if (nn.hasOutInterrelationships()) {
                            Interrelationship oi = (Interrelationship)on.getOutInterrelationships().nextElement();
                            Interrelationship ni = (Interrelationship)nn.getOutInterrelationships().nextElement();

                            if (oi.matches(ni)) {
                                t.removeNode(ni.getTo());
                                t.removeNode(ni.getFrom());
                                found = true;
                                break;
                            }
                        }
                        continue;
                    }
                    if (nn.hasOutInterrelationships() || nn.hasInInterrelationships()) {
                        continue;
                    }

                    // Check class type
                    //if (! nn.getClass().isInstance(on)) continue;

                    // Nodes are normal
                    if (on.matches(nn)) {
                        // Exact match keeps old
                        t.removeNode(nn);
                        found = true;
                        break;
                    }
                }

                // Remove ones that aren't found
                if (!found) {
                    if (on.hasOutInterrelationships()) {
                        Interrelationship oi = (Interrelationship)on.getOutInterrelationships().nextElement();
                        removeNode(oi.getTo());
                        removeNode(oi.getFrom());
                    } else
                        removeNode(on);
                }
            }

            // Now add them
            addTaems(t, false);
        }
    }

    /**
     * Adds another Taems task structure to this one.  The
     * incoming task structure is cloned before being added,
     * so the added structure is unaffected by the procedure
     * (so don't expect changes to it to be reflected in the
     * combined structure).
     * <P>
     * Essentially what happens is clones of all the high level
     * nodes from the incoming structure will be added to this one.
     * Schedules and Commitments are also transferred.  The schedule
     * criteria from the new structure is used if it has one.
     * Taems-level attributes are added, with matching attributes
     * in the add structure replacing those in the existing one.
     * @param t The structure to add.
     */
    public synchronized void addTaems(Taems t) {
        addTaems(t, true);
    }

    /**
     * Same as addTaems, except gives you the option of cloning 
     * the task structure before adding it.  If the structure
     * is not cloned, you should not attempt to use the structure
     * passed in afterwards.
     * @param t The structure to add.
     * @param clone Clone it before the add
     * @see #addTaems(Taems)
     */
    public synchronized void addTaems(Taems t, boolean clone) {
        if (clone)
            t = (Taems)t.clone();

        synchronized (addLock) {
            // Add agents
            Enumeration e = t.getAgents();
            while (e.hasMoreElements()) {
                Agent a = (Agent)e.nextElement();
                if (findAgent(a) == null)
                    addAgent(a);
            }

            // Reorganize misplaced resources
            e = t.findNodes(new Resource());
            while (e.hasMoreElements()) {
                Resource r = (Resource)e.nextElement();
                if (!t.nodes.contains(r)) {
                    t.addNode(r);
                }
            }

            // Add nodes
            e = t.getNodes();
            while (e.hasMoreElements()) {
                mergeNode((Node)e.nextElement());
            }

            // Add schedules
            e = new SafeEnumeration(t.getSchedules());
            while (e.hasMoreElements()) {
                Schedule s = (Schedule)e.nextElement();
                t.removeSchedule(s);
                addSchedule(s);
            }

            // Add schedule criteria, if the new one has one
            if (t.getScheduleCriteria() != null)
                setScheduleCriteria(t.getScheduleCriteria());

            // Add commitments
            e = new SafeEnumeration(t.getCommitments());
            while (e.hasMoreElements()) {
                Commitment c = (Commitment)e.nextElement();
                t.removeCommitment(c);
                addCommitment(c);
            }

            // Attributes
            e = t.getAttributes();
            while (e.hasMoreElements()) {
                Object key = e.nextElement();
                Object data = t.getAttribute(key);
                if (data instanceof Node) {
                    Node n = (Node)data;
                    data = new VirtualNode(n.getLabel(), n.getAgent());
                }
                setAttribute(key, data);
            }

            // Now fix it all
            retargetVirtuals();
        }
    }
    private Integer addLock = new Integer(0);

  /**
   * Merges a node into the taems structure.
   * @param n The node to merge
   */
  private void mergeNode(Node n) {
      if (n == null) return;

      if (n instanceof Resource) {
          mergeResource((Resource)n);
          
      } else {
          addNode(n);
      }
  }

  /**
   * Merges a resource.  If the resource already exists, the old
   * one is kept and all interrelationships associated with the
   * duplicate are retargeted to it.  If it does not exist, it
   * is added as usual.
   * @param r The resource to add
   */
  private void mergeResource(Resource r) {
    Enumeration e;
    Resource or = null;

    /*
    e = findNodes(new Resource(r.getLabel(), null));
    if (e.hasMoreElements())
        or = (Resource)e.nextElement();

    if ((or == r) && (!e.hasMoreElements())) {
        // Move it to the top?
        if (! nodes.contains(or)) {
            addNode(or);
        }
        return;

    } if (e.hasMoreElements()) {
        or = (Resource)e.nextElement();
    }

    if (e.hasMoreElements()) {
        log.log("Warning: mergeResource found too many matches", 1);
    }
    */

    // Only look for resources at the top
    e = getNodes();
    Resource matcher = new Resource(r.getLabel(), null);
    while (e.hasMoreElements()) {
        Node n = (Node)e.nextElement();
        if (n.matches(matcher)) {
            or = (Resource)n;
            break;
        }
    }

    // Add it in one way or another
    if (or != null) {
        r.transferInterrelationships(or);
        fireNodeUpdateEvent(new NodeUpdateEvent(this, NodeUpdateEvent.PLACEMENT));

    } else {
        addNode(r);
    }
  }

  /**
   * Removes another Taems task structure from this one. Is is
   * expected that the incoming task structure is a clone of
   * a structure that has previously been added (e.g. added in
   * the way that addTaems does it).
   * <P>
   * Note that resources and agents are not removed, as it is 
   * assumed that extra items of these types do no harm, and if
   * they are removed inconsistencies may occur.
   * @param t The structure to remove
   * @return The actual Taems structure that was removed
   * (note Resources will be cloned in the returned structure)
   */
  public Taems removeTaems(Taems t) {
    Taems removed = new Taems();
    Vector toremove = new Vector();
    Enumeration e;

    synchronized (addLock) {
        // Get all the nodes
        e = t.getAllNodes();
        while (e.hasMoreElements()) {
            toremove.addElement(e.nextElement());
        }

        // Heave-ho the IRs first to make things easier
        e = new SafeEnumeration(toremove.elements());
        while (e.hasMoreElements()) {
            Node n = (Node)e.nextElement();
            if (!(n instanceof Interrelationship)) continue;
            toremove.removeElement(n);
            Node matcher = new Node(n.getLabel(), n.getAgent());
            Interrelationship i = (Interrelationship)unmergeNode(findNode(matcher));
            if (i != null) {
                removed.addNode(i.getFrom());
                removed.addNode(i.getTo());
            }
        }

        // Tasks
        while (!toremove.isEmpty()) {
            e = new SafeEnumeration(toremove.elements());
            while (e.hasMoreElements()) {
                Node n = (Node)e.nextElement();
                if (n instanceof Task) {
                    // Don't excise tasks until all their subtasks
                    // are gone, too, otherwise we won't be able
                    // to find those subtasks later.
                    boolean ignore = false;
                    Enumeration te = ((Task)n).getSubtasks();
                    while (te.hasMoreElements()) {
                        TaskBase st = (TaskBase)te.nextElement();
                        if (toremove.contains(st)) {
                            ignore = true;
                            break;
                        }
                    }
                    if (ignore) continue;
                }
                toremove.removeElement(n);
                Node matcher;
                if (n.isVirtual())
                    // Virtuals only remove other virtuals
                    matcher = n;
                else
                    // Otherwise remove based on name and agent
                    matcher = new Node(n.getLabel(), n.getAgent());

                Node matched = findNode(matcher);

                if ((n instanceof TaskBase) && ((TaskBase)n).isNonLocal()) {
                    // Nonlocals only remove other nonlocals
                    if ((matched instanceof TaskBase) && !((TaskBase)matched).isNonLocal()) {
                        continue;
                    }
                }

                removed.addNode(unmergeNode(matched));
            }
        }

        /*
        e = t.findNodes(new Interrelationship());
        while (e.hasMoreElements()) {
            Node n = (Node)e.nextElement();
            if (seen.contains(n)) continue;
            seen.addElement(n);
            Interrelationship i = (Interrelationship)unmergeNode(findNode(n));
            if (i != null) {
                removed.addNode(i.getFrom());
                removed.addNode(i.getTo());
            }
        }
        */
        // This should get most of them
        /*e = t.getNodes();
        while (e.hasMoreElements()) {
            Node n = (Node)e.nextElement();
            if (seen.contains(n)) continue;
            seen.addElement(n);
            removed.addNode(unmergeNode(findNode(n)));
            }*/
        // And do some final cleaning
        /*
        e = t.getAllNodes();
        while (e.hasMoreElements()) {
            Node n = (Node)e.nextElement();
            if (seen.contains(n)) continue;
            seen.addElement(n);
            removed.addNode(unmergeNode(findNode(n)));
        }
        */
    }

    removed.retargetVirtuals();
    return removed;
  }

  /**
   * Unmerges a node from the taems structure.
   * @param n The node to unmerge
   */
    private Node unmergeNode(Node n) {
        if (n == null) return n;

        if (n instanceof Resource) {
            // Keep it, return copy so it don't exist in two structures
            return (Node)n.clone();

        } else {
            removeNode(n);
            if (n.hasInterrelationships()) {
                addNode(n.excise());
            } else {
                n.excise();
            }
        }

        return n;
    }

    /**
     * Clone, this makes a copy the Taems structure.
     * @reutrn A clone of the Taems structure
     */
    public Object clone() {
        Taems cloned = new Taems(getLabel(), getAgent());
        Enumeration e;

        log.log("Starting Taems clone...", Log.LOG_DEBUG);

        // Prelim stuff
        cloned.listeners = (Vector)listeners.clone();

        // Set the label
        if (getLabel() != null) cloned.setLabel(new String(getLabel()));
        else cloned.setLabel(null);

        // Clone the agents
        e = getAgents();
        cloned.agents = new Vector();
        while(e.hasMoreElements()) {
            cloned.addAgent((Agent)((Agent)e.nextElement()).clone());
        }

        // Clone the nodes
        e = getNodes();
        cloned.nodes = new Vector();
        while(e.hasMoreElements()) {
            cloned.addNode((Node)((Node)e.nextElement()).clone());
        }

        // Clone the schedules
        e = getSchedules();
        cloned.schedules = new Vector();
        while(e.hasMoreElements()) {
            cloned.addSchedule((Schedule)((Schedule)e.nextElement()).clone());
        }

        // Clone the schedule criterea
        if (getScheduleCriteria() != null)
            cloned.setScheduleCriteria((Criteria)getScheduleCriteria().clone());

        // Clone the commitments
        e = getCommitments();
        cloned.commitments = new Vector();
        while(e.hasMoreElements()) {
            cloned.addCommitment((Commitment)((Commitment)e.nextElement()).clone());
        }

        // Clone the attribs
        if (attributes != null)
            cloned.attributes = (Hashtable)attributes.clone();

        // Finish up
        cloned.retargetVirtuals();

        log.log("Taems clone finished...", Log.LOG_DEBUG2);

        return cloned;
    }

    /**
     * This function uses the copy function of Node and its subclasses to
     * copy the non-structural characteristics of this structure into the
     * one provided.  To do this, findNode is used to find the counterpart
     * in the provided structure for each node in this structure (matching
     * by label and agent only).  No structural changes are made (subtasks
     * and interrelationships remain in place), and nodes which do not have
     * a counterpart in either structure are not affected.
     * <P>
     * Note this function does not current copy agents, commitments,
     * schedules or criteria.  This may change in the future.
     * @param n The taems structure to copy into
     */
    public void copy(Taems t) {

        Enumeration e = getAllNodes();
        while (e.hasMoreElements()) {
            Node n = (Node)e.nextElement();
            Node matcher = new Node(n.getLabel(), n.getAgent());
            Node matched = t.findNode(matcher);
            if (matched != null) {
                n.copy(matched);
            }
        }

        if (attributes != null)
            t.attributes = (Hashtable)attributes.clone();

        t.setLabel(getLabel());
    }
    
    /**
     * Used during clone and add, this function is called at the
     * end to hunt down virtual nodes dangling from Interrelationships,
     * Tasks, ScheduleElements or Commitments and replace them with their
     * real counterparts, if they can be found.
     * <P>
     * retargetSchedules and retargetCommitments are automatically
     * called by this function.
     */
    public synchronized void retargetVirtuals() {
        Vector allnodes = new Vector();
        Vector virtuals = new Vector();
        Enumeration e;

        log.log("Starting retarget...", Log.LOG_DEBUG);

        // Cache all nodes
        e = new SafeEnumeration(getAllNodes());
        while (e.hasMoreElements()) {
            Node n = (Node)e.nextElement();

            // Condense virtuals and NL stuff
            if (n.isVirtual() || ((n instanceof TaskBase) && ((TaskBase)n).isNonLocal())) {
                boolean found = false;
                Node matcher = new Node(n.getLabel(), n.getAgent());

                Enumeration ve = virtuals.elements();
                while (ve.hasMoreElements()) {
                    Node vn = (Node)ve.nextElement();
                    // Mmm, I'm not sure if I should be matching both of these
                    // as it may be overly aggressive in cases where you have
                    // multiple potential matches.  I suppose I could write
                    // that off as the person who wrote the structure being
                    // too loose with their virtual nodes, however...
                    if (vn.matches(matcher) || matcher.matches(vn)) {
                        boolean replace = false;
                        found = true;

                        // If equivalent classes, first in vector is kept,
                        // otherwise see if it should be replaced.

                        if ((n instanceof TaskBase) && (((TaskBase)n).isNonLocal())) {
                            // Nonlocals replace everything
                            if (!((vn instanceof TaskBase) && (((TaskBase)vn).isNonLocal()))) {
                                replace = true;
                            }

                        } else if (n instanceof VirtualMethod) {
                            // VMethods replace everything else
                            if ((vn instanceof VirtualNode) || (vn instanceof VirtualTaskBase))
                                replace = true;
                            if ((vn instanceof TaskBase) && (((TaskBase)vn).isNonLocal())) {
                                replace = false;
                            }

                        } else if (n instanceof VirtualTaskBase) {
                            // VTaskBases replace VNodes
                            if (vn instanceof VirtualNode) {
                                replace = true;
                            }

                        } else if (n instanceof VirtualResource) {
                            // VResource replace VNodes
                            if (vn instanceof VirtualNode) {
                                replace = true;
                            }
                        }

                        if (replace) {
                            vn.transferInterrelationships(n);
                            if ((vn instanceof TaskBase) && (n instanceof TaskBase))
                                ((TaskBase)vn).transferSupertasks((TaskBase)n);
                            else if ((vn instanceof Interrelationship) && (n instanceof Interrelationship))
                                ((Interrelationship)n).replace((Interrelationship)vn);
                            virtuals.removeElement(vn);
                            removeNode(vn);
                            virtuals.addElement(n);
                            if (!nodes.contains(n))
                                addNode(n);

                        } else {
                            n.transferInterrelationships(vn);
                            if ((n instanceof TaskBase) && (vn instanceof TaskBase))
                                ((TaskBase)n).transferSupertasks((TaskBase)vn);
                            else if ((vn instanceof Interrelationship) && (n instanceof Interrelationship))
                                ((Interrelationship)vn).replace((Interrelationship)n);
                            removeNode(n);
                            if (!nodes.contains(vn))
                                addNode(vn);
                        }

                        break;
                    }
                }

                if (!found) {
                    virtuals.addElement(n);
                }

            } else {
                allnodes.addElement(n);
            }
        }

        // Retarget them
        Enumeration ve = virtuals.elements();
        while (ve.hasMoreElements()) {
            Node vn = (Node)ve.nextElement();
            Node matcher = new Node(vn.getLabel(), vn.getAgent());
	    boolean found = false;

            e = allnodes.elements();
            while (e.hasMoreElements()) {
                Node n = (Node)e.nextElement();
                if (n.matches(matcher)) {
	 	    found = true;

                    // Transfer everything
                    vn.transferInterrelationships(n);
                    if (vn instanceof TaskBase) {
                        if (n instanceof TaskBase)
                            ((TaskBase)vn).transferSupertasks((TaskBase)n);
                        else
                            System.err.println("Warning, " + n.getLabel() + " is not a TaskBase as expected");
                    } else if (vn instanceof Interrelationship) {
                        if (n instanceof Interrelationship)
                            ((Interrelationship)n).replace((Interrelationship)vn);
                        else
                            System.err.println("Warning, " + n.getLabel() + " is not an Interrlationship as expected");
                    }

                    // Then ditch the virtual node
                    removeNode(vn);

                    // And remove the one we found from the root task
                    // list if it is part of a subtree
                    if ((n instanceof TaskBase) && ((TaskBase)n).hasSupertasks()) {
                        removeNode(n);
                    }
                    break;
                }
            }

            // To avoid over-cloning, remove VNodes which appear elsewhere in
            // the task structure (e.g. have supertasks) from the high level
            // node list.
            if (!found) {
                if ((vn instanceof TaskBase) && (((TaskBase)vn).hasSupertasks())) {
                    removeNode(vn);
                }
            }
        }

        // IRs
        /*
        e = findNodes(new Interrelationship());
        while(e.hasMoreElements()) {
            // Find all IRs
            Interrelationship i = (Interrelationship)e.nextElement();
            Node n = i.getTo();
            if (n.isVirtual() || ((n instanceof TaskBase) && (((TaskBase)n).isNonLocal()))) {
                // Virtual to?
                Node matcher = new Node(n.getLabel(), n.getAgent());
                Enumeration ne = allnodes.elements();
                while (ne.hasMoreElements()) {
                    // Look for a match
                    Node temp = (Node)ne.nextElement();
                    if (temp == n) continue;
                    if (temp.isVirtual()) continue;
                    if (! temp.matches(matcher)) continue;
                    i.setTo(temp);
                    removeNode(n);
                    if ((temp instanceof TaskBase) && ((TaskBase)temp).isNonLocal())
                        n = temp;
                    else
                        break;
                }
            }
            n = i.getFrom();
            if (n.isVirtual() || ((n instanceof TaskBase) && (((TaskBase)n).isNonLocal()))) {

                Enumeration ne = findNodes(new Node(n.getLabel(), n.getAgent()));
                while (ne.hasMoreElements()) {
                    // Look for a match
                    Node temp = (Node)ne.nextElement();
                    if (temp == n) continue;
                    if (temp.isVirtual()) continue;
                    if (i.getFromOutcome() != null)
                        i.setFrom(temp, ((Method)temp).getOutcome(i.getFromOutcome().getLabel()));
                    else
                        i.setFrom(temp, null);
                    removeNode(i);
                    removeNode(n);
                    if ((temp instanceof TaskBase) && ((TaskBase)temp).isNonLocal())
                        n = temp;
                    else
                        break;
                }
                while (ne.hasMoreElements()) {
                    // Warn if multiple matches
                    Node temp = (Node)ne.nextElement();
                    if (temp.isVirtual()) continue;
                    log.log("Warning: Duplicate match(es) found for IR from " + temp.getLabel() + "\n" + temp, 1);
                    break;
                }
            }
            }*/

        // Tasks
        /*e = findNodes(new Task());
        while(e.hasMoreElements()) {
            // Look at all tasks
            Task t = (Task)e.nextElement();
            Enumeration te = t.getSubtasks();
            while (te.hasMoreElements()) {
                TaskBase n = (TaskBase)te.nextElement();
                if (n.isVirtual() || (n.isNonLocal())) {
                    // Found a virtual or nonlocal task
                    Node matcher = new Node(n.getLabel(), n.getAgent());
                    Enumeration ne = allnodes.elements();
                    while (ne.hasMoreElements()) {
                        // Look for a match
                        Node temp = (Node)ne.nextElement();
                        if (temp == n) continue;
                        if (temp.isVirtual()) continue;
                        if (! temp.matches(matcher)) continue;
                        //if (temp.isNonLocal()) continue;
if (n.isNonLocal()) {
System.err.println("retargeting nl: " + n.getLabel());
}
                        int index = t.getSubtaskPosition(n);
                        t.setSubtask((TaskBase)temp, index);
                        removeNode(n);
                        removeNode(temp);
                        if (!((TaskBase)temp).isNonLocal())
                            break;
			else
			    n = (TaskBase)temp;
                    }
                }
            }
        }*/

        // Tasks and methods
        /*
        e = findNodes(new TaskBase());
        while(e.hasMoreElements()) {
            // Look at all task bases
            TaskBase n = (TaskBase)e.nextElement();
            if (n.isVirtual() || (n.isNonLocal())) {
                // Found a virtual or nonlocal task
                Node matcher = new Node(n.getLabel(), n.getAgent());
                Enumeration ne = allnodes.elements();
                while (ne.hasMoreElements()) {
                    // Look for a match
                    Node temp = (Node)ne.nextElement();
                    if (temp == n) continue;
                    if (temp.isVirtual()) continue;
                    if (! temp.matches(matcher)) continue;
                    //if (temp.isNonLocal()) continue;
                    Enumeration pe = new SafeEnumeration(n.getSupertasks());
                    while(pe.hasMoreElements()) {
                        Task t = (Task)pe.nextElement();
                        int index = t.getSubtaskPosition(n);
                        t.setSubtask((TaskBase)temp, index);
                    }
                    removeNode(n);
                    n = (TaskBase)temp;
                    if (n.hasSupertasks())
                        removeNode(n);
                    if (!n.isNonLocal())
                        break;
                }
            }
            }*/

        // Schedule Elements
        retargetSchedules();

        // Commitments
        retargetCommitments();

        log.log("Retarget finished", Log.LOG_DEBUG2);
    }

    /**
     * Handles schedule retargetting
     */
    public synchronized void retargetSchedules() {
        retargetSchedules(null);
    }

    /**
     * Handles schedule retargetting
     */
    public synchronized void retargetSchedules(Enumeration schede) {
        Enumeration e = (schede == null) ? getSchedules() : schede;

        while (e.hasMoreElements()) {
            Schedule s = (Schedule)e.nextElement();

            if (s instanceof MLCSchedule) {
                retargetSchedules(((MLCSchedule)s).getSchedules());
            }

            Enumeration e2 = s.getElements();
            while (e2.hasMoreElements()) {
                ScheduleElement se = (ScheduleElement)e2.nextElement();
                if ((se.getMethod() != null) && (se.getMethod().isVirtual())) {
                    Enumeration ne = findNodes(new Method(se.getMethod().getLabel(), se.getMethod().getAgent()));
                    while (ne.hasMoreElements()) {
                        // Look for a match
                        Method temp = (Method)ne.nextElement();
                        if (temp.isVirtual()) continue;
                        se.setMethod(temp);
                    }
                    while (ne.hasMoreElements()) {
                        // Warn if multiple matches
                        Node temp = (Node)ne.nextElement();
                        if (temp.isVirtual()) continue;
                        log.log("Warning: Duplicate match(es) found for ScheduleElement " + temp.getLabel() + "\n" + temp, 1);
                        break;
                    }
                }
                if (se.numPreconditions() > 0) {
                    // Fix up preconditions
                    Enumeration en = se.getPreconditionsElement();
                    while (en.hasMoreElements()) {
                        Precondition p = (Precondition)en.nextElement();
                        if (p instanceof PrecedencePrecondition) {
                            PrecedencePrecondition pp = (PrecedencePrecondition)p;
			    if (pp.getInterrelationship() == null) {
				if (!pp.isArtificialIR())
				    log.log("Warning: No interrelationship found on precedence precondition on element " + se.getLabel(), 1);
                            } else if (pp.getInterrelationship().isVirtual()) {
                                Interrelationship i = pp.getInterrelationship();
                                Node n = findNode(new Interrelationship(i.getLabel(), i.getAgent()));
                                if ((n != null) && (n instanceof Interrelationship)) {
                                    pp.setInterrelationship((Interrelationship)n);
                                } else {
                                    System.err.println("Nuts, can't find the ir");
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Handles attribute retargetting.  This can potentially be very
     * expensive given the number of things it has to look at.  It isn't
     * normally invoked by retargetVirtuals, so if you think you'll need
     * it you'll have to call it explicitly.
     */
    public synchronized void retargetAttributes() {
        Enumeration e;
        
        // Taems
        retargetAttributes(getAttributesTable());

        // Nodes
        e = getAllNodes();
        while (e.hasMoreElements()) {
            Node n = (Node)e.nextElement();
            retargetAttributes(n.getAttributesTable());
        }

        // Agents
        e = getAgents();
        while (e.hasMoreElements()) {
            Agent a = (Agent)e.nextElement();
            retargetAttributes(a.getAttributesTable());
        }

        // Commitments
        e = getCommitments();
        while (e.hasMoreElements()) {
            Commitment c = (Commitment)e.nextElement();
            retargetAttributes(c.getAttributesTable());
        }

        // Schedule Elements
        e = getSchedules();
        while (e.hasMoreElements()) {
            Schedule s = (Schedule)e.nextElement();
            Enumeration en = s.getElements();
            while (en.hasMoreElements()) {
                ScheduleElement se = (ScheduleElement)en.nextElement();
                retargetAttributes(se.getAttributesTable());
            }
        }
    }

    /**
     * Well, actually this handles attribute retargetting
     * @param table The attribute table to munch on
     */
    protected synchronized void retargetAttributes(Hashtable table) {
        Enumeration e = table.keys();

        while (e.hasMoreElements()) {
            Object key = e.nextElement();
            Object value = table.get(key);
            if (value instanceof Node) {
                Node n = (Node)value;
                if (n.isVirtual()) {
                    Enumeration ne = findNodes(new Node(n.getLabel(), n.getAgent()));
                    while (ne.hasMoreElements()) {
                        // Look for a match
                        Method temp = (Method)ne.nextElement();
                        if (temp.isVirtual()) continue;
                        table.put(key, temp);
                    }
                    while (ne.hasMoreElements()) {
                        // Warn if multiple matches
                        Node temp = (Node)ne.nextElement();
                        if (temp.isVirtual()) continue;
                        log.log("Warning: Duplicate match(es) found for attribute " + temp.getLabel() + "\n" + temp, 1);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Handles commitment element retargetting.
     */
    public synchronized void retargetCommitments() {
        Enumeration e = getCommitments();

        while (e.hasMoreElements()) {
            Commitment c = (Commitment)e.nextElement();
            Enumeration e2 = c.getTasks();
            Vector v = new Vector();
            while (e2.hasMoreElements()) {
                TaskBase t = (TaskBase)e2.nextElement();
                if (t.isVirtual() || (t.isNonLocal())) {
                    Enumeration ne = findNodes(new TaskBase(t.getLabel(), t.getAgent()));
                    boolean added = false;
                    TaskBase nl = null;
                    while (ne.hasMoreElements()) {
                        // Look for a match
                        TaskBase temp = (TaskBase)ne.nextElement();
                        if (temp.isVirtual()) continue;
                        if (temp.isNonLocal()) {
                           nl = temp;
                           continue;
			}
                        added = true;
                        v.addElement(temp);
                    }
                    while (ne.hasMoreElements()) {
                        // Warn if multiple matches
                        TaskBase temp = (TaskBase)ne.nextElement();
                        if (temp.isVirtual()) continue;
                        if (temp.isNonLocal()) continue;
                        log.log("Warning: Duplicate match(es) found for Commitment " + temp.getLabel() + "\n" + temp, 1);
                        break;
                    }
                    if (! added) {
                        if (nl != null)
                           v.addElement(nl);
                        else
                           v.addElement(t);
                    }
                } else {
                    v.addElement(t);
                }
            }
            c.setTaskVector(v);
        }
    }

    /**
     * This function more or less rips apart the tree structure
     * in the Taems object.  All links are removed, and virtual 
     * nodes inserted to represent where those nodes were.  Each
     * of the resulting pile of nodes is then added to the top
     * level of the structure.  It also removes direct references
     * from the schedules and commitments.  Theoretically, this is
     * the inverse of retargetVirtuals.
     * <BR>
     * Why would you want to do such a thing?  Well, sometimes
     * its easier to deal with all the little pieces than one
     * big glump.  In these cases, you smash it, pick through 
     * the remenants, and then put it back together.
     * @see #retargetVirtuals
     */  
    public void smash() {
        Node n;
        Enumeration e = new SafeEnumeration(getAllNodes());

        // Do the nodes
        nodes = new Vector(); // yow
        while(e.hasMoreElements()) {
            n = (Node)e.nextElement();

            if (n instanceof Task) {
                Task t = (Task)n;
                Enumeration te = new SafeEnumeration(t.getSubtasks());
                while (te.hasMoreElements()) {
                    TaskBase tb = (TaskBase)te.nextElement();
                    t.replaceSubtask(tb, new VirtualTaskBase(tb.getLabel(), tb.getAgent()));
                }

                addNode(n);

            } else if (n instanceof TaskBase) {
                addNode(n);

            } else if (n instanceof Interrelationship) {
                Interrelationship i = (Interrelationship)n;
                
                i.setEndpoints(new VirtualNode(i.getFrom().getLabel(), i.getFrom().getAgent()),
                               i.getFromOutcome(),
                               new VirtualNode(i.getTo().getLabel(), i.getTo().getAgent()));

                addNode(i.getFrom());
                addNode(i.getTo());

            } else if (n instanceof Resource) {
                addNode(n);
            }

            if (n.isVirtual()) removeNode(n);
        }

        // Do the schedules
        e = getSchedules();
        while (e.hasMoreElements()) {
            Schedule s = (Schedule)e.nextElement();
            Enumeration se = s.getElements();
            while (se.hasMoreElements()) {
                ScheduleElement sem = (ScheduleElement)se.nextElement();
                if (sem.getMethod() != null) {
                    sem.setMethod(new VirtualMethod(sem.getMethod().getLabel(), sem.getMethod().getAgent()));
                }
            }
        }

        // Do the commitments
        e = getCommitments();
        while (e.hasMoreElements()) {
            Commitment c = (Commitment)e.nextElement();
            Enumeration ce = c.getTasks();
            Vector v = new Vector();
            while (ce.hasMoreElements()) {
                TaskBase t = (TaskBase)ce.nextElement();
                v.addElement(new VirtualTaskBase(t.getLabel(), t.getAgent()));
            }
        }
    }

    /**
     * This function removes items from the root level that don't
     * look like they belong there.  Things like tasks with no
     * real subtasks and singleton nodes with no interrelationship
     * ties will be removed
     */
    public void removeOrphans() {

        synchronized (addLock) {
            Enumeration e = new SafeEnumeration(getNodes());
            while (e.hasMoreElements()) {
                boolean remove;;
                Node n = (Node)e.nextElement();

                // Resource?
                remove = (n instanceof Resource ? false : true);
            
                // Real task children?
                if (remove) {
                    if (n instanceof Task) {
                        Task t = (Task)n;
                        if (t.hasSubtasks()) {
                            Enumeration te = t.getSubtasks();
                            while (te.hasMoreElements()) {
                                Node st = (Node)te.nextElement();
                                if (! st.isVirtual()) {
                                    remove = false;
                                    break;
                                }
                            }
                        }
                    }
                }

                // Interrelationships?
                if (remove) {
                    if (n.hasOutInterrelationships()) {
                        remove = false;
                    }
                    if (n.hasInInterrelationships()) {
                        remove = false;
                    }
                }

                // TaskBase with supertask?
                if (n instanceof TaskBase) {
                    if (((TaskBase)n).hasSupertasks())
                        remove = true;
                }

                // Kill it?
                if (remove)
                    removeNode(n);
            }
        }
    }

    /**
     * Returns the ttaems version of the node
     * @param v The version number output style to use
     */
    public String toTTaems(float v) {
        StringBuffer sb = new StringBuffer("");
        Vector seen = new Vector();
        Enumeration e;

        sb.append(";;;;\n;; Begin Task Structure " + getLabel() + " (Output v." + v + ")\n;;;;\n\n");
    
        // Agents
        e = getAgents();
        while (e.hasMoreElements()) {
            Agent a = (Agent)e.nextElement();
            sb.append(a.toTTaems(v));
            sb.append("\n");
        } 
   
        // Nodes
        e = getNodes();
        while (e.hasMoreElements()) {
            Node n = (Node)e.nextElement();
            //sb.append("; Start new base structure\n");
            if (n instanceof Task) {
                if (!((Task)n).isTaskGroup()) {
                    log.log("Warning: High level task is not a task group.", 1);
                }
                Enumeration ne = n.findNodes(new Node(null, null));
                while (ne.hasMoreElements()) {
                    Node cn = (Node)ne.nextElement();
                    if (seen.contains(cn)) {
                        sb.append("; Node " + cn.getLabel() + " previously defined\n");
                    } else {
                        seen.addElement(cn);
                        sb.append(cn.toTTaems(v));
                    }
                    sb.append("\n");
                }
            } else {
                if (seen.contains(n)) {
                    //sb.append("; Node " + n.getLabel() + " previously defined\n");
                } else {
                    seen.addElement(n);
                    sb.append(n.toTTaems(v));
                    Enumeration irs = n.getOutInterrelationships();
                    while (irs.hasMoreElements()) {
                        Node cn = (Node)irs.nextElement();
                        if (seen.contains(cn)) {
                            sb.append("; Node " + cn.getLabel() + " previously defined\n");
                        } else {
                            seen.addElement(cn);
                            sb.append("\n");
                            sb.append(cn.toTTaems(v));
                        }
                    }
                }
                sb.append("\n");
            }
        }

        // Scheduler's Criteria 
        if (criteria != null) {
            sb.append(criteria.toTTaems(v));
            sb.append("\n");
        }

        // Schedules
        if (schedules != null) {
            e = getSchedules();
            while (e.hasMoreElements()) {
                Schedule s = (Schedule)e.nextElement();
                sb.append(s.toTTaems(v));
                sb.append("\n");
            }
        }

        // Commitments
        if (commitments != null) {
            e = getCommitments();
            while (e.hasMoreElements()) {
                Commitment c = (Commitment)e.nextElement();
                sb.append(c.toTTaems(v));
                sb.append("\n");
            }
        }

        // Attributes
        sb.append(Node.attributesToString(attributes, v));

        //sb.append(";;;;\n;; End Task Structure " + getLabel() + "\n;;;;\n\n");				
        return sb.toString();
    }
		
    /**
     * Stringify
     */
    public String toString() {
        return toTTaems(Taems.VCUR);
    }

    private Object last_attrib;
    private Outcome last_outcome;
    private String last_selectnode;
    class TaemsEdit extends JPanel {
        Node node;
        JComponent c;
        JComboBox cb;
        JButton b;
        GridBagLayout grid = new GridBagLayout();
        GridBagConstraints cons = new GridBagConstraints();
        String none = "<none specified>";

        public TaemsEdit(Node n) {
            node = n;
            generatePanel();
        }

        public void generatePanel() {
            setLayout(grid);

            cons.fill = GridBagConstraints.BOTH;
            cons.weightx = 1.0;

            if (node == null) {
                generateTaemsEdits();

            } else if (node instanceof Task) {
                generateNodeEdits();
                generateTaskBaseEdits();
                generateTaskEdits();
                generateOtherNodeEdits();

            } else if (node instanceof Method) {
                generateNodeEdits();
                generateTaskBaseEdits();
                generateMethodEdits();
                generateOtherNodeEdits();

            } else if (node instanceof Interrelationship) {
                generateNodeEdits();
                generateInterrelationshipEdits();
                generateOtherNodeEdits();

            } else if (node instanceof Resource) {
                generateNodeEdits();
                generateResourceEdits();
                generateOtherNodeEdits();

            } else {
                generateNodeEdits();
            }
        }

        boolean start = true;
        public Component add(Component c) {
            if (start)
                cons.gridwidth = 1;
            else
                cons.gridwidth = GridBagConstraints.REMAINDER;
            grid.setConstraints(c, cons);

            c = super.add(c);

            if (start) {
                JLabel l = new JLabel(" ");
                grid.setConstraints(l, cons);
                super.add(l);
            }

            start = !start;
            return c;
        }

        public void generateInterrelationshipEdits() {
            Interrelationship ir = (Interrelationship)node;
            String s;
            final String[] names = {
                "Enables", "Disables",
                "Facilitates", "Hinders",
                "Produces", "Consumes", "Limits"
            };
            final Interrelationship[] types = {
                new EnablesInterrelationship(), new DisablesInterrelationship(),
                new FacilitatesInterrelationship(), new HindersInterrelationship(),
                new ProducesInterrelationship(),  new ConsumesInterrelationship(), new LimitsInterrelationship()
            };

            add(new JLabel("Type", JLabel.RIGHT));
            add(cb = new JComboBox());
            for (int i = 0; i < names.length; i++) {
                cb.addItem(names[i]);
                if (ir.getClass().isInstance(types[i]))
                    cb.setSelectedIndex(i);
            }
            cb.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        Interrelationship ir = (Interrelationship)node, n = null;
                        String s = ((JComboBox)e.getSource()).getSelectedItem().toString();
                        for (int i = 0; i < names.length; i++) {
                            if (s.equals(names[i])) {
                                if (!ir.getClass().isInstance(types[i])) {
                                    n = (Interrelationship)types[i].clone();
                                }
                                break;
                            }
                        }
                        if (n != null) {
                            n.replace(ir);
                            ir.copy(n);
                            selectNode(n);
                        }
                    }
                });

            TreeSet nodes = new TreeSet();
            Enumeration e = getAllNodes();
            while (e.hasMoreElements()) {
                nodes.add(((Node)e.nextElement()));
            }

            add(new JLabel("From Node", JLabel.RIGHT));
            add(cb = new JComboBox());
            Iterator it = nodes.iterator();
            if (ir.getFrom() == null) {
                cb.addItem(none);
            }
            for (int i = 0; it.hasNext(); i++) {
                Node n = (Node)it.next();
                if (n instanceof Interrelationship) { i--; continue; }
                cb.addItem(n.getLabel());
                if (n.matches(ir.getFrom()))
                    cb.setSelectedIndex(i);
            }
            if (ir.getFrom() == null) {
                cb.setSelectedIndex(0);
            }
            cb.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        String s = ((JComboBox)e.getSource()).getSelectedItem().toString();
                        if (s.equals(none)) return;
                        Node n = findNode(new Node(s));
                        n.addInterrelationship((Interrelationship)node, ((Interrelationship)node).getTo());
                    }
                });

            add(new JLabel("To Node", JLabel.RIGHT));
            add(cb = new JComboBox());
            it = nodes.iterator();
            if (ir.getTo() == null) {
                cb.addItem(none);
            }
            for (int i = 0; it.hasNext(); i++) {
                Node n = (Node)it.next();
                if (n instanceof Interrelationship) { i--; continue; }
                cb.addItem(n.getLabel());
                if (n.matches(ir.getTo()))
                    cb.setSelectedIndex(i);
            }
            if (ir.getTo() == null) {
                cb.setSelectedIndex(0);
            }
            cb.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        String s = ((JComboBox)e.getSource()).getSelectedItem().toString();
                        if (s.equals(none)) return;
                        Node n = findNode(new Node(s));
                        ((Interrelationship)node).getFrom().addInterrelationship((Interrelationship)node, n);
                    }
                });

            if (ir.hasModel()) {
                add(new JLabel("Model", JLabel.RIGHT));
                add(cb = new JComboBox());
                if (ir.getModel() == null) {
                    cb.addItem(none);
                    cb.setSelectedIndex(0);
                }
                cb.addItem("Per Time Unit");
                if (ir.getModel() == Interrelationship.PER_TIME_UNIT)
                    cb.setSelectedIndex(0);
                cb.addItem("Duration Independent");
                if (ir.getModel() == Interrelationship.DURATION_INDEPENDENT)
                    cb.setSelectedIndex(1);
                cb.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            String s = ((JComboBox)e.getSource()).getSelectedItem().toString();
                            if (s.equals(none)) return;
                            if (s.startsWith("P")) {
                                ((Interrelationship)node).setModel(Interrelationship.PER_TIME_UNIT);
                            }
                            if (s.startsWith("D")) {
                                ((Interrelationship)node).setModel(Interrelationship.DURATION_INDEPENDENT);
                            }
                        }
                    });
            }

            if (ir instanceof ConsumesInterrelationship) {
                add(new JLabel("Consumes", JLabel.RIGHT));
                s = (((ConsumesInterrelationship)ir).getConsumes() == null) ? "" : ((ConsumesInterrelationship)ir).getConsumes().output();
                add(c = new JTextField(s));
                setTextActor(c, new TextActor() {
                        public void handleText(String s) {
                            ((ConsumesInterrelationship)node).setConsumes(parseDistribution(s));
                        }
                    });

            } else if (ir instanceof ProducesInterrelationship) {
                add(new JLabel("Produces", JLabel.RIGHT));
                s = (((ProducesInterrelationship)ir).getProduces() == null) ? "" : ((ProducesInterrelationship)ir).getProduces().output();
                add(c = new JTextField(s));
                setTextActor(c, new TextActor() {
                        public void handleText(String s) {
                            ((ProducesInterrelationship)node).setProduces(parseDistribution(s));
                        }
                    });

            } else {
                add(new JLabel("Quality Power", JLabel.RIGHT));
                s = (ir.getQuality() == null) ? "" : ir.getQuality().output();
                add(c = new JTextField(s));
                setTextActor(c, new TextActor() {
                        public void handleText(String s) {
                            ((Interrelationship)node).setQuality(parseDistribution(s));
                        }
                    });

                add(new JLabel("Cost Power", JLabel.RIGHT));
                s = (ir.getCost() == null) ? "" : ir.getCost().output();
                add(c = new JTextField(s));
                setTextActor(c, new TextActor() {
                        public void handleText(String s) {
                            ((Interrelationship)node).setCost(parseDistribution(s));
                        }
                    });

                add(new JLabel("Duration Power", JLabel.RIGHT));
                s = (ir.getDuration() == null) ? "" : ir.getDuration().output();
                add(c = new JTextField(s));
                setTextActor(c, new TextActor() {
                        public void handleText(String s) {
                            ((Interrelationship)node).setDuration(parseDistribution(s));
                        }
                    });

                if (!(ir instanceof LimitsInterrelationship)) {
                    add(new JLabel("Delay", JLabel.RIGHT));
                    s = (ir.getDelay() == null) ? "" : ir.getDelay().output();
                    add(c = new JTextField(s));
                    setTextActor(c, new TextActor() {
                            public void handleText(String s) {
                                ((Interrelationship)node).setDelay(parseDistribution(s));
                            }
                        });
                }
            }

            //add(new JLabel(" "));
            //add(new JLabel(" "));
        }

        Outcome outcome;
        JTextField outq, outc, outd, outl, outdn;
        public void generateMethodEdits() {
            Method method = (Method)node;
            Enumeration e;
            String s;

            add(new JLabel("Nonlocal", JLabel.RIGHT));
            add(c = new JCheckBox("", method.isNonLocal()));
            ((JCheckBox)c).addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        ((TaskBase)node).setNonLocal(((JCheckBox)e.getSource()).isSelected());
                    }
                });

            add(new JLabel("Outcomes", JLabel.RIGHT));
            add(cb = new JComboBox());
            e = method.getOutcomes();
            if (!e.hasMoreElements()) {
                cb.addItem(none);
                cb.setSelectedIndex(0);
            }
            for (int i = 0; e.hasMoreElements(); i++) {
                Outcome o = (Outcome)e.nextElement();
                cb.addItem(o.getLabel());
                if (i == 0) outcome = o;
                if (o == last_outcome) {
                    outcome = last_outcome;
                    cb.setSelectedIndex(i);
                }
            }
            last_outcome = outcome;
            cb.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        String s = ((JComboBox)e.getSource()).getSelectedItem().toString();
                        if (s.equals(none)) return;
                        Outcome o = ((Method)node).getOutcome(s);
                        if (o == null) return;
                        last_outcome = outcome = o;

                        outl.setText((outcome.getLabel() == null) ? "" : outcome.getLabel());
                        outdn.setText((outcome.getQuality() == null) ? "" : String.valueOf(outcome.getDensity()));
                        outq.setText((outcome.getQuality() == null) ? "" : outcome.getQuality().output());
                        outc.setText((outcome.getCost() == null) ? "" : outcome.getCost().output());
                        outd.setText((outcome.getDuration() == null) ? "" : outcome.getDuration().output());
                    }
                });

            add(new JLabel("Outcome Label", JLabel.RIGHT));
            s = (outcome == null) ? "" : outcome.getLabel();
            add(c = outl = new JTextField(s));
            setTextActor(c, new TextActor() {
                    public void handleText(String s) {
                        if (outcome == null) return;
                        outcome.setLabel(parseLabel(s));
                    }
                });

            add(new JLabel("Outcome Density", JLabel.RIGHT));
            s = (outcome == null) ? "" : String.valueOf(outcome.getDensity());
            add(c = outdn = new JTextField(s));
            setTextActor(c, new TextActor() {
                    public void handleText(String s) {
                        if (outcome == null) return;
                        outcome.setDensity(parseFloat(s));
                    }
                });

            add(new JLabel("Outcome Quality", JLabel.RIGHT));
            s = ((outcome == null) || (outcome.getQuality() == null)) ? "" : outcome.getQuality().output();
            add(c = outq = new JTextField(s));
            setTextActor(c, new TextActor() {
                    public void handleText(String s) {
                        if (outcome == null) return;
                        outcome.setQuality(parseDistribution(s));
                    }
                });

            add(new JLabel("Outcome Cost", JLabel.RIGHT));
            s = ((outcome == null) || (outcome.getCost() == null)) ? "" : outcome.getCost().output();
            add(c = outc = new JTextField(s));
            setTextActor(c, new TextActor() {
                    public void handleText(String s) {
                        if (outcome == null) return;
                        outcome.setCost(parseDistribution(s));
                    }
                });

            add(new JLabel("Outcome Duration", JLabel.RIGHT));
            s = ((outcome == null) || (outcome.getDuration() == null)) ? "" : outcome.getDuration().output();
            add(c = outd = new JTextField(s));
            setTextActor(c, new TextActor() {
                    public void handleText(String s) {
                        if (outcome == null) return;
                        outcome.setDuration(parseDistribution(s));
                    }
                });

            add(b = new JButton("Delete Outcome"));
            b.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (outcome == null) return;
                        ((Method)node).removeOutcome(outcome);
                    }
                });

            add(b = new JButton("Add new Outcome"));
            b.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        String name = askString("Enter the new Outcome's label:", "Outcome_" + (((Method)node).numOutcomes() + 1));
                        if (name == null) return;
                        Outcome o = new Outcome(name,
                                                new Distribution(1, 1),
                                                new Distribution(1, 1),
                                                new Distribution(1, 1),
                                                1);
                        last_outcome = o;
                        ((Method)node).addOutcome(o);
                    }
                });

            add(new JLabel(" "));
            add(new JLabel(" "));

            add(new JLabel("Start Time", JLabel.RIGHT));
            s = (method.getStartTime() == Integer.MIN_VALUE) ? "" : String.valueOf(method.getStartTime());
            add(c = new JTextField(s));
            setTextActor(c, new TextActor() {
                    public void handleText(String s) {
                        ((Method)node).setStartTime(parseInteger(s));
                    }
                });

            add(new JLabel("Finish Time", JLabel.RIGHT));
            s = (method.getFinishTime() == Integer.MIN_VALUE) ? "" : String.valueOf(method.getFinishTime());
            add(c = new JTextField(s));
            setTextActor(c, new TextActor() {
                    public void handleText(String s) {
                        ((Method)node).setFinishTime(parseInteger(s));
                    }
                });

            add(new JLabel("Accrued Time", JLabel.RIGHT));
            s = (method.getAccruedTime() == Integer.MIN_VALUE) ? "" : String.valueOf(method.getAccruedTime());
            add(c = new JTextField(s));
            setTextActor(c, new TextActor() {
                    public void handleText(String s) {
                        ((Method)node).setAccruedTime(parseInteger(s));
                    }
                });

            add(new JLabel(" "));
            add(new JLabel(" "));
        }

        public void generateTaskBaseEdits() {
            TaskBase taskbase = (TaskBase)node;
            String s;
            Enumeration e;

            TreeSet nodes = new TreeSet();
            e = getAllNodes();
            while (e.hasMoreElements()) {
                nodes.add(((Node)e.nextElement()));
            }

            add(new JLabel("Supertasks", JLabel.RIGHT));
            add(cb = new JComboBox());
            Iterator it = nodes.iterator();
            if ((!taskbase.hasSupertasks()) ||
                (taskbase instanceof Task)) {
                cb.addItem(none);
            }
            for (int i = 0; it.hasNext(); i++) {
                Node n = (Node)it.next();
                if (!(n instanceof Task)) { i--; continue; }
                if (n == node) { i--; continue; }
                cb.addItem(n.getLabel());
                if (!taskbase.hasSupertasks()) continue;
                if (n.matches(taskbase.firstSupertask()))
                    cb.setSelectedIndex(i);
            }
            if (!taskbase.hasSupertasks()) {
                cb.setSelectedIndex(0);
            }
            cb.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        String s = ((JComboBox)e.getSource()).getSelectedItem().toString();
                        if (s.equals(none)) {
                            ((TaskBase)node).removeAllSupertasks();
                            addNode(node);
                        } else {
                            Node n = findNode(new Node(s));
                            if (n instanceof Task) {
                                ((TaskBase)node).removeAllSupertasks();
                                removeNode(node);
                                ((Task)n).addSubtask((TaskBase)node);
                            }
                        }
                    }
                });

            add(new JLabel("Earliest Start Time", JLabel.RIGHT));
            s = (taskbase.getEarliestStartTime() == Integer.MIN_VALUE) ? "" : String.valueOf(taskbase.getEarliestStartTime());
            add(c = new JTextField(s));
            setTextActor(c, new TextActor() {
                    public void handleText(String s) {
                        ((TaskBase)node).setEarliestStartTime(parseInteger(s));
                    }
                });

            add(new JLabel("Deadline", JLabel.RIGHT));
            s = (taskbase.getDeadline() == Integer.MIN_VALUE) ? "" : String.valueOf(taskbase.getDeadline());
            add(c = new JTextField(s));
            setTextActor(c, new TextActor() {
                    public void handleText(String s) {
                        ((TaskBase)node).setDeadline(parseInteger(s));
                    }
                });

            add(new JLabel("Arrival Time", JLabel.RIGHT));
            s = (taskbase.getArrivalTime() == Integer.MIN_VALUE) ? "" : String.valueOf(taskbase.getArrivalTime());
            add(c = new JTextField(s));
            setTextActor(c, new TextActor() {
                    public void handleText(String s) {
                        ((TaskBase)node).setArrivalTime(parseInteger(s));
                    }
                });
        }

        JList sl;
        public void generateTaskEdits() {
            Task task = (Task)node;

            add(new JLabel("QAF", JLabel.RIGHT));
            add(cb = new JComboBox());
            Enumeration e = QAF.getQAFs();
            if (task.getQAF() == null) {
                cb.addItem(none);
            }
            for (int i = 0; e.hasMoreElements(); i++) {
                QAF q = (QAF)e.nextElement();
                cb.addItem(q.getLabel());
                if (q.matches(task.getQAF()))
                    cb.setSelectedIndex(i);
            }
            if (task.getQAF() == null) {
                cb.setSelectedIndex(0);
            }
            cb.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        String s = ((JComboBox)e.getSource()).getSelectedItem().toString();
                        if (s.equals(none)) return;
                        QAF q = QAF.getQAF(s);
                        ((Task)node).setQAF(q);
                    }
                });

            add(new JLabel(" "));
            add(new JLabel(" "));

            JPanel panel = new JPanel();
            panel.setLayout(new VFlowLayout(VFlowLayout.TOP, true));
            panel.add(new JLabel("Subtask Ordering", JLabel.RIGHT));
            panel.add(b = new JButton("Move Up"));
            b.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        Task task = (Task)node;
                        if (sl == null) return;
                        if (!task.hasSubtasks()) return;
                        String n = (String)sl.getSelectedValue();
                        if (n == null) return;
                        TaskBase tb = (TaskBase)task.findNode(new TaskBase(n, null));
                        int i = task.getSubtaskPosition(tb);
                        if (i > 0) {
                            last_selectnode = n;
                            task.removeSubtask(tb);
                            task.insertSubtask(tb, i-1);
                        }
                    }
                });
            panel.add(b = new JButton("Move Down"));
            b.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        Task task = (Task)node;
                        if (sl == null) return;
                        if (!task.hasSubtasks()) return;
                        String n = (String)sl.getSelectedValue();
                        if (n == null) return;
                        TaskBase tb = (TaskBase)task.findNode(new TaskBase(n, null));
                        int i = task.getSubtaskPosition(tb);
                        if ((i >= 0) && (i < task.numSubtasks() - 1)) {
                            last_selectnode = n;
                            task.removeSubtask(tb);
                            task.insertSubtask(tb, i+1);
                        }
                    }
                });
            add(panel);
            Vector v = new Vector();
            e = task.getSubtasks();
            int select = 0;
            for (int i = 0; e.hasMoreElements(); i++) {
                String n = ((Node)e.nextElement()).getLabel();
                v.addElement(n);
                if (n.equals(last_selectnode))
                    select = i;
            }
            add(c = sl = new JList(v));
            sl.setSelectedIndex(select);

            add(new JLabel(" "));
            add(new JLabel(" "));

            add(new JLabel(" "));
            add(b = new JButton("Add new sub-Task"));
            b.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        String name = askString("Enter the new Task's label:", node.getLabel() + "_T" + (((Task)node).numSubtasks() + 1));
                        if (name == null) return;
                        Task t = new Task(parseLabel(name), node.getAgent(), new SumQAF());
                        ((Task)node).addSubtask(t);
                        selectNode(t);
                    }
                });

            add(new JLabel(" "));
            add(b = new JButton("Add new sub-Method"));
            b.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        String name = askString("Enter the new Method's label:", node.getLabel() + "_M" + (((Task)node).numSubtasks() + 1));
                        if (name == null) return;
                        Method m = new Method(parseLabel(name), node.getAgent());
                        m.addOutcome(new Outcome("Outcome_1",
                                                 new Distribution(1,1),
                                                 new Distribution(1,1),
                                                 new Distribution(1,1),
                                                 1));
                        ((Task)node).addSubtask(m);
                        selectNode(m);
                    }
                });
        }

        public void generateResourceEdits() {
            Resource resource = (Resource)node;
            String s;

            add(new JLabel("Model", JLabel.RIGHT));
            add(cb = new JComboBox());
            cb.addItem("Consumable");
            if (resource instanceof ConsumableResource)
                cb.setSelectedIndex(0);
            cb.addItem("Non-Consumable");
            if (resource instanceof NonConsumableResource)
                cb.setSelectedIndex(1);
            cb.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        Resource r = (Resource)node, n = null;
                        String s = ((JComboBox)e.getSource()).getSelectedItem().toString();
                        if (s.equals(none)) return;
                        if ((s.startsWith("C")) && (!(r instanceof ConsumableResource))) {
                            n = new ConsumableResource(r.getLabel(),
                                                       r.getAgent(),
                                                       r.getState(),
                                                       r.getDepletedAt(),
                                                       r.getOverloadedAt());
                        }
                        if ((s.startsWith("N")) && (!(r instanceof NonConsumableResource))) {
                            n = new NonConsumableResource(r.getLabel(),
                                                          r.getAgent(),
                                                          r.getState(),
                                                          r.getDepletedAt(),
                                                          r.getOverloadedAt());
                        }
                        if (n != null) {
                            r.copy(n);
                            addNode(n);
                            r.transferInterrelationships(n);
                            removeNode(r);
                            if (display != null)
                                display.selectNode(n);
                        }
                    }
                });

            add(new JLabel("State", JLabel.RIGHT));
            s = (resource.getState() == Double.NEGATIVE_INFINITY) ? "" : String.valueOf(resource.getState());
            add(c = new JTextField(s));
            setTextActor(c, new TextActor() {
                    public void handleText(String s) {
                        ((Resource)node).setState(parseDouble(s));
                    }
                });

            add(new JLabel("Depleted At", JLabel.RIGHT));
            s = (resource.getDepletedAt() == Double.NEGATIVE_INFINITY) ? "" : String.valueOf(resource.getDepletedAt());
            add(c = new JTextField(s));
            setTextActor(c, new TextActor() {
                    public void handleText(String s) {
                        ((Resource)node).setDepletedAt(parseDouble(s));
                    }
                });

            add(new JLabel("Overloaded At", JLabel.RIGHT));
            s = (resource.getOverloadedAt() == Double.NEGATIVE_INFINITY) ? "" : String.valueOf(resource.getOverloadedAt());
            add(c = new JTextField(s));
            setTextActor(c, new TextActor() {
                    public void handleText(String s) {
                        ((Resource)node).setOverloadedAt(parseDouble(s));
                    }
                });

            add(new JLabel(" "));
            add(new JLabel(" "));
        }

        public void generateNodeEdits() {
            add(new JLabel("Label", JLabel.RIGHT));
            add(c = new JTextField(node.getLabel()));
            setTextActor(c, new TextActor() {
                    public void handleText(String s) {
                        node.setLabel(parseLabel(s));
                    }
                });

            add(new JLabel("Agent", JLabel.RIGHT));
            add(cb = new JComboBox());
            if (node.getAgent() == null) {
                cb.addItem(none);
            }
            Enumeration e = getAgents();
            for (int i = 0; e.hasMoreElements(); i++) {
                Agent a = (Agent)e.nextElement();
                cb.addItem(a.getLabel());
                if (a.matches(node.getAgent()))
                    cb.setSelectedIndex(i);
            }
            if (node.getAgent() == null) {
                cb.setSelectedIndex(0);
            }
            cb.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (e.getSource() == null) return;
                        String s = ((JComboBox)e.getSource()).getSelectedItem().toString();
                        if (s.equals(none)) return;

                        Agent a = new Agent(s);
                        node.setAgent(a);
                    }
                });
        }

        Object attrib;
        JTextField atl, att, atd;
        public void generateOtherNodeEdits() {
            String s;

            if (!(node instanceof Interrelationship)) {
                add(new JLabel(" "));
                add(b = new JButton("Add new Interrelationship"));
                b.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            String name = askString("Enter the new Interrelationship's label:", "IR_" + node.getLabel());
                            if (name == null) return;

                            TreeSet v = new TreeSet();
                            Enumeration en = getAllNodes();
                            while (en.hasMoreElements()) {
                                Node n = (Node)en.nextElement();
                                if (n instanceof Interrelationship) continue;
                                v.add(n.getLabel());
                            }
                            String dest = (String)JOptionPane.showInputDialog(TaemsEdit.this, 
                                                                              "Choose a destination node:", "Input",
                                                                              JOptionPane.QUESTION_MESSAGE, null,
                                                                              v.toArray(), v.toArray()[0]);
                            if (dest == null) return;
                            Node d = findNode(new Node(dest));
                            if (d == null) {
                                System.err.println("Error, node " + dest + " not found");
                            }

                            Interrelationship i = new EnablesInterrelationship(parseLabel(name),
                                                                               node.getAgent(),
                                                                               new Distribution(0,1));
                            i.setCost(new Distribution(0,1));
                            i.setQuality(new Distribution(0,1));
                            i.setDuration(new Distribution(0,1));
                            node.addInterrelationship(i, d);
                            selectNode(i);
                        }
                    });
            }

            add(new JLabel(" "));
            add(new JLabel(" "));

            add(new JLabel("Attributes", JLabel.RIGHT));
            add(cb = new JComboBox());
            if (node.numAttributes() == 0) {
                cb.addItem(none);
            }
            Enumeration e = node.getAttributes();
            for (int i = 0; e.hasMoreElements(); i++) {
                Object o = e.nextElement();
                cb.addItem(o);
                if (i == 0) {
                    attrib = o;
                    cb.setSelectedIndex(0);
                }
                if (o.equals(last_attrib)) {
                    attrib = o;
                    cb.setSelectedIndex(i);
                }
            }
            last_attrib = attrib;
            if (node.numAttributes() == 0) {
                cb.setSelectedIndex(0);
            }
            cb.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        String s = ((JComboBox)e.getSource()).getSelectedItem().toString();
                        if (s.equals(none)) return;
                        last_attrib = attrib = s;
                        Object o = node.getAttribute(attrib);
                        if (o == null) return;
                        atl.setText(attrib.toString());
                        s = Converter.getPropertyType(o);
                        if (s == null) s = "Unknown";
                        att.setText(s);
                        atd.setText(Converter.unTypeProperty(o.toString(), null));
                    }
                });

            add(new JLabel("Attribute Label", JLabel.RIGHT));
            s = (attrib == null) ? "" : attrib.toString();
            add(c = atl = new JTextField(s));
            setTextActor(c, new TextActor() {
                    public void handleText(String s) {
                        if (attrib == null) return;
                        Object o = node.getAttribute(attrib);
                        if (o == null) return;
                        if (s.equals(attrib.toString())) return;
                        Object save = attrib;
                        node.setAttribute(s, o);
                        node.removeAttribute(save);
                    }
                });

            add(new JLabel("Attribute Type", JLabel.RIGHT));
            if ((attrib != null) && node.hasAttribute(attrib)) {
                s = Converter.getPropertyType(node.getAttribute(attrib));
                if (s == null) s = "Unknown";
            } else {
                s = "";
            }
            add(att = new JTextField(s));

            add(new JLabel("Attribute Data", JLabel.RIGHT));
            s = ((attrib == null) || (!node.hasAttribute(attrib))) ? "" : node.getAttribute(attrib).toString();
            add(c = atd = new JTextField(s));
            setTextActor(c, new TextActor() {
                    public void handleText(String s) {
                        if (attrib == null) return;
                        Object o = node.getAttribute(attrib);
                        String type = att.getText();
                        if (type != null) {
                            try {
                                o = Converter.reTypeProperty(s, type);
                            } catch (Exception e) {
                                o = null;
                            }
                            if (o != null) {
                                node.setAttribute(attrib, o);
                            } else {
                                System.err.println("Error parsing " + type + " attribute data");
                            }
                        }
                    }
                });

            add(b = new JButton("Delete Attribute"));
            b.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (attrib == null) return;
                        node.removeAttribute(attrib);
                    }
                });
            add(b = new JButton("Add new Attribute"));
            b.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        String name = askString("Enter the new attributes's name:", "Attribute_" + ((node.numAttributes() + 1)));
                        if (name == null) return;
                        last_attrib = name;
                        Object o = new String("data");
                        node.setAttribute(name, o);
                    }
                });

            add(new JLabel(" "));
            add(new JLabel(" "));

            add(new JLabel(" "));
            add(b = new JButton("Delete Node"));
            b.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        node.delete();
                        removeNode(node);
                        selectNode(null);
                    }
                });
        }

        Agent agent = null;
        JTextField al;
        public void generateTaemsEdits() {
            Enumeration e;
            String s;

            add(new JLabel("Label", JLabel.RIGHT));
            add(c = new JTextField(getLabel()));
            setTextActor(c, new TextActor() {
                    public void handleText(String s) {
                        setLabel(parseLabel(s));
                    }
                });

            add(new JLabel(" "));
            add(new JLabel(" "));

            add(new JLabel("Agents", JLabel.RIGHT));
            add(cb = new JComboBox());
            e = getAgents();
            if (!e.hasMoreElements()) {
                cb.addItem(none);
                cb.setSelectedIndex(0);
            }
            for (int i = 0; e.hasMoreElements(); i++) {
                Agent a = (Agent)e.nextElement();
                cb.addItem(a.getLabel());
                if (i == 0) agent = a;
            }
            cb.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        String s = ((JComboBox)e.getSource()).getSelectedItem().toString();
                        if (s.equals(none)) return;
                        Agent a = findAgent(new Agent(s));
                        if (a == null) return;
                        agent = a;

                        al.setText((agent.getLabel() == null) ? "" : agent.getLabel());
                    }
                });

            add(new JLabel("Agent Label", JLabel.RIGHT));
            s = (agent == null) ? "" : agent.getLabel();
            add(c = al = new JTextField(s));
            setTextActor(c, new TextActor() {
                    public void handleText(String s) {
                        if (agent == null) return;
                        agent.setLabel(parseLabel(s));
                    }
                });

            add(b = new JButton("Delete Agent"));
            b.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (agent == null) return;
                        removeAgent(agent);
                    }
                });
            add(b = new JButton("Add new Agent"));
            b.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        String name = askString("Enter the new agent's name", "Agent_" + (numAgents() + 1));
                        if (name == null) return;
                        Agent a = new Agent(parseLabel(name));
                        addAgent(a);
                    }
                });

            add(new JLabel(" "));
            add(new JLabel(" "));

            add(new JLabel(" "));
            add(b = new JButton("Add new Task Group"));
            b.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        String name = askString("Enter the new Task Group's label:", "Goal_" + (numTaskGroups() + 1));
                        if (name == null) return;
                        Task t = new Task(parseLabel(name), getAgent(), new SumQAF());
                        addNode(t);
                        selectNode(t);
                    }
                });

            add(new JLabel(" "));
            add(b = new JButton("Add new Resource"));
            b.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        String name = askString("Enter the new Resource's label:", "Rsrc_" + (numResources() + 1));
                        if (name == null) return;
                        Resource r = new ConsumableResource(parseLabel(name), getAgent(), 0, 0, 100);
                        addNode(r);
                        selectNode(r);
                    }
                });
        }

        public void setTextActor(JComponent c, TextActor a) {
            c.addKeyListener(new MyKeyAdapter(a));
            c.addFocusListener(new MyFocusAdapter(a));
        }

        abstract class TextActor {
            public abstract void handleText(String s);
        }
        class MyKeyAdapter extends KeyAdapter {
            TextActor actor;
            public MyKeyAdapter(TextActor a) {
                actor = a;
            }
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String s = ((JTextField)e.getSource()).getText();
                    actor.handleText(s);
                }
            }
        }
        class MyFocusAdapter extends FocusAdapter {
            TextActor actor;
            public MyFocusAdapter(TextActor a) {
                actor = a;
            }
            public void focusLost(FocusEvent e) {
                if (e.getSource() == null) return;
                String s = ((JTextField)e.getSource()).getText();
                actor.handleText(s);
            }
        }

        public String askString(String message, String def) {
            JOptionPane pane = new JOptionPane(message, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
            pane.setWantsInput(true);
            pane.setInitialValue(def);
            pane.setInitialSelectionValue(def);
            JDialog dialog = pane.createDialog(this, "Input");
            dialog.setVisible(true);
            if ((pane.getValue() != null) &&
                (pane.getValue() instanceof Integer) &&
                (((Integer)pane.getValue()).intValue() == JOptionPane.OK_OPTION))
                return (String)pane.getInputValue();
            else
                return null;
        }

        public Distribution parseDistribution(String s) {
            if (s == null) return null;
            s = s.trim();
            if (s.equals("")) return null;
            try {
                return new Distribution(s);
            } catch (Exception e) {
                System.err.println("Incorrectly formatted distribution specified");
            }
            return null;
        }
        public float parseFloat(String s) {
            if (s == null) return Float.NEGATIVE_INFINITY;
            s = s.trim();
            if (s.equals("")) return Float.NEGATIVE_INFINITY;
            try {
                return Float.parseFloat(s);
            } catch (Exception e) {
                System.err.println("Incorrectly formatted float specified");
            }
            return Float.NEGATIVE_INFINITY;
        }
        public double parseDouble(String s) {
            if (s == null) return Double.NEGATIVE_INFINITY;
            s = s.trim();
            if (s.equals("")) return Double.NEGATIVE_INFINITY;
            try {
                return Double.parseDouble(s);
            } catch (Exception e) {
                System.err.println("Incorrectly formatted double specified");
            }
            return Double.NEGATIVE_INFINITY;
        }
        public int parseInteger(String s) {
            if (s == null) return Integer.MIN_VALUE;
            s = s.trim();
            if (s.equals("")) return Integer.MIN_VALUE;
            try {
                return Integer.parseInt(s);
            } catch (Exception e) {
                System.err.println("Incorrectly formatted integer specified");
            }
            return Integer.MIN_VALUE;
        }
        public String parseLabel(String s) {
            if (s == null) {
                return null;
            }
            s = s.trim();
            if (s.equals("")) return null;
            s = s.replace(' ', '_');
            return s;
        }

        public void selectNode(Node n) {
            if (display != null) {
                display.selectNode(n);
            }
        }
    }

  /***********************************************************
   *                     Drawing Junk                        *
   ***********************************************************/
	protected static final int H_SPACE = 50;
	protected static final int V_SPACE = 50;
	protected static final int H_MARGIN = 50;
	protected static final int V_MARGIN = 50;

    TaemsCanvas display;

  /**
   * Inits the graphic stuff
   */
  JScrollPane scroll;
  protected void initGraphics() {

    setLayout(new BorderLayout());

    scroll = new JScrollPane();
    scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

    scroll.setViewportView(display = new TaemsCanvas());
    display.setVisible(true);
    add(scroll, BorderLayout.CENTER);
  }

	/**
	 * Calculates the width of the object.
	 */
	public int calculateWidth(FontMetrics fm) {
	 	int width = 0;

		Enumeration e = getNodes();
		while (e.hasMoreElements()) {
		 	Node n = (Node)e.nextElement();
		 	width += n.calculateTreeWidth(fm) + H_SPACE;
		}

		if (width > 0)
				width -= H_SPACE;

		return width;
  }

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
        int height = 0;

        Enumeration e = getNodes();
        while (e.hasMoreElements()) {
            taems.Node n = (Node)e.nextElement();
            height = Math.max(height, n.calculateTreeHeight(fm));
        }

        return height;
    }

		/**
		 * Calculates the height of the object, plus any subnodes.
		 */
		public int calculateTreeHeight(FontMetrics fm) {
				int height, sheight = 0;
				int rsrc = 0;

				Enumeration e = getNodes();
				while (e.hasMoreElements()) {
						Node n = (Node)e.nextElement();
						if (n instanceof Resource)
								rsrc = Math.max(rsrc, n.calculateTreeHeight(fm));
						else
								sheight = Math.max(sheight, n.calculateTreeHeight(fm));
				}

				height = sheight;

				height += rsrc + V_SPACE;

				return height;
		}

		/**
		 * Finds the node who's contains() function is true with the
		 * supplied point.
		 * @param p The point to check with
		 * @return A node containing the point, or null if none found
		 */
		public Node findNode(Point p) {
				Enumeration e = getAllNodes();
				
				while (e.hasMoreElements()) {
						Node n = (Node)e.nextElement();
						if (n.contains(p))
								return n;
				}
				
				return null;
		}
    
    /**
     */
    public Dimension getMinimumSize() {
        return new Dimension(25, 25);
    }

    /**
     */
    public boolean isOpaque() {
        return true;
    }

    /**
     * Taems information panel
     */
    protected class TaemsInformation extends JFrame {
        JTabbedPane tabs;
        JTextPane ttaems, summary, schedules;
        JPanel edit;

        public TaemsInformation() {
            super("Taems Information");

            // Menu bar
            JMenuBar mbar = new JMenuBar();
            JMenu menu = new JMenu("Edit") {
                    public boolean isFocusable() { return false; }
                };
            JMenuItem mitem;
            mitem = new JMenuItem("Copy");
            mitem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        //System.err.println(getFocusOwner());
                    }
                });
            menu.add(mitem);

            mitem = new JMenuItem("Paste");
            menu.add(mitem);

            mitem = new JMenuItem("Cut");
            menu.add(mitem);

            mbar.add(menu);
            setJMenuBar(mbar);

            // Tabs
            JScrollPane scroll;

            getContentPane().setLayout(new BorderLayout());
            getContentPane().add(tabs = new JTabbedPane(), BorderLayout.CENTER);

            scroll = new JScrollPane();
            scroll.getViewport().add(edit = new JPanel());
            scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            tabs.add("Edit", scroll);

            scroll = new JScrollPane();
            scroll.getViewport().add(summary = new JTextPane());
            tabs.add("Summary", scroll);
            summary.setBackground(getBackground());
            summary.setEditable(false);

            scroll = new JScrollPane();
            scroll.getViewport().add(ttaems = new JTextPane());
            scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            tabs.add("TTaems", scroll);
            ttaems.setBackground(getBackground());
            ttaems.setEditable(false);

            scroll = new JScrollPane();
            scroll.getViewport().add(schedules = new JTextPane());
            scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            tabs.add("Schedules", scroll);
            schedules.setBackground(getBackground());
            schedules.setEditable(false);

            updateInformation();

            setSize(400,450);
        }

        public void repaint() {
            updateInformation();
            super.repaint();
        }

        public void updateInformation() {
            Node selected = (display != null) ? display.selected : null;
            StringBuffer buffer;

            // Summary
            int n, tg, t, m, i, r, rt, d;
            n = tg = t = m = i = r = d = rt = 0;
            Enumeration e = getAllNodes();
            while (e.hasMoreElements()) {
                Node node = (Node)e.nextElement();
                n++;
                if (node instanceof Task) {
                    if (((Task)node).isTaskGroup())
                        tg++;
                    else
                        t++;
                } else if (node instanceof Method) {
                    d = Math.max(d, ((Method)node).calculateTreeDepth());
                    m++;
                } else if (node instanceof Interrelationship) {
                    i++;
                } else if (node instanceof Resource) {
                    r++;
                }
            }
            buffer = new StringBuffer("");
            buffer.append("  Structure: " + getLabel() + "\n");
            buffer.append("     Height: " + d + "\n");
            buffer.append("\n");
            buffer.append("      Nodes: " + n + "\n");
            buffer.append("Task Groups: " + tg + "\n");
            buffer.append("      Tasks: " + t + "\n");
            buffer.append("    Methods: " + m + "\n");
            buffer.append("       NLEs: " + i + "\n");
            buffer.append("  Resources: " + r + "\n");
            buffer.append("\n");

            e = getNodes();
            String roots = "";
            while (e.hasMoreElements()) {
                Node node = (Node)e.nextElement();
                rt++;
                roots += node.getLabel() + " ";
            }
            buffer.append(" Root Nodes: " + rt + " (includes resources)\n");
            buffer.append("Root Labels: " + roots + "\n");
            buffer.append("\n");

            buffer.append("     Agents: " + numAgents() + "\n");
            buffer.append("  Schedules: " + numSchedules() + "\n");

            summary.setText(buffer.toString());

            // TTaems
            if (selected != null) {
                ttaems.setText(selected.toString());
            } else {
                ttaems.setText(Taems.this.toString());
            }

            // Schedules
            buffer = new StringBuffer("");
            Enumeration en = getSchedules();
            while (en.hasMoreElements()) {
                buffer.append(en.nextElement().toString());
            }
            schedules.setText(buffer.toString());

            // Edit
            edit.removeAll();
            edit.add(new TaemsEdit(selected));
        }
    }

    /**
     * Used to draw the Taems object.  You shouldn't have to mess with this guy.
     */
    protected class TaemsCanvas extends JPanel implements Printable, ClipboardOwner {
        private boolean shownles = true;
        private boolean showqcds = false;
        private JFrame info;
        private File file = null;
        private float vers;
        PageFormat pageformat = new PageFormat();
        private JPopupMenu popupmenu;
        private JMenuItem copyitem, pasteitem;

        public TaemsCanvas() {
			
			// Make the popup menu
			popupmenu = generateMenu();
						
            // Watch for mouse events
            addMouseListener(new MouseListener() {
                    public void mouseClicked(MouseEvent e) { }
                    public void mouseEntered(MouseEvent e) { }
                    public void mouseExited(MouseEvent e) { }
                    public void mousePressed(MouseEvent e) {
                        handleMouseEvent(e);
                    }
                    public void mouseReleased(MouseEvent e) { }
                });
            addMouseMotionListener(new MouseMotionListener() {
                    public void mouseDragged(MouseEvent e) {
                        handleMouseEvent(e);
                    }
                    public void mouseMoved(MouseEvent e) { }
                });
        }

        public void updateMenu() {

	        if (getSelected() == null) copyitem.setEnabled(false);
	        else copyitem.setEnabled(true);

	        if (getClipboardContents() == null) pasteitem.setEnabled(false);
	        else pasteitem.setEnabled(true);
        }
        
        public JPopupMenu generateMenu() {
			JPopupMenu menu = new JPopupMenu();
            JMenuItem m;

            if ((info == null) || (!info.isVisible())) {
                m = new JMenuItem("Show Information Window");
            } else {
                m = new JMenuItem("Hide Information Window");
            }
            m.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (info == null) {
                            info = new TaemsInformation();
                        }
                        if (info.isVisible()) {
                            info.setVisible(false);
                            ((JMenuItem)(e.getSource())).setText("Show Information Window");
                        } else {
                            info.setVisible(true);
                            ((JMenuItem)(e.getSource())).setText("Hide Information Window");
                        }
                    }
                });
            menu.add(m);

            menu.addSeparator();

            copyitem = new JMenuItem("Copy Node");
            copyitem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        copyNode(getSelected());
                    }
                });
            copyitem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
            menu.add(copyitem);
            pasteitem = new JMenuItem("Paste Node");
            pasteitem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        pasteNode(popuppoint);
                    }
                });
            pasteitem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
            menu.add(pasteitem);

            menu.addSeparator();
						
            m = new JMenuItem("Revert Node Placement");
            m.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        fireNodeUpdateEvent(new NodeUpdateEvent(new Integer(0), NodeUpdateEvent.PLACEMENT));
                    }
                });
            menu.add(m);

            if (shownles) {
                m = new JMenuItem("Hide Interrelationships");
            } else {
                m = new JMenuItem("Show Interrelationships");
            }
            m.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        shownles = !shownles;
                        hideNodes(findNodes(new Interrelationship()), ! shownles);
                        if (shownles) {
                            ((JMenuItem)(e.getSource())).setText("Hide Interrelationships");
                        } else {
                            ((JMenuItem)(e.getSource())).setText("Show Interrelationships");
                        }
                    }
                });
            menu.add(m);

            if (showqcds) {
                m = new JMenuItem("Hide Distributions");
            } else {
                m = new JMenuItem("Show Distributions");
            }
            m.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        showqcds = !showqcds;
                        hideQCDs(findNodes(new Method()), showqcds);
                        if (showqcds) {
                            ((JMenuItem)(e.getSource())).setText("Hide Distributions");
                        } else {
                            ((JMenuItem)(e.getSource())).setText("Show Distributions");
                        }
                    }
                });
            menu.add(m);
						
            m = new JMenuItem("Refresh");
            m.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        fireNodeUpdateEvent(new NodeUpdateEvent(new Integer(0), NodeUpdateEvent.GRAPHIC));
                    }
                });
            menu.add(m);
						
            menu.addSeparator();

            m = new JMenuItem("Save");
            m.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        save(false);
                    }
                });
            menu.add(m);

            m = new JMenuItem("Save As...");
            m.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        save(true);
                    }
                });
            menu.add(m);

            m = new JMenuItem("Page Setup...");
            m.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        PrinterJob job = PrinterJob.getPrinterJob();
                        pageformat = job.pageDialog(pageformat);
                    }
                });
            menu.add(m);

            m = new JMenuItem("Print...");
            m.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        printToPrinter();
                    }
                });
            menu.add(m);
            
            return menu;
        }

        public synchronized void save(boolean as) {

            if (as || (file == null)) {
                String dir = System.getProperty("user.dir");
                if (dir == null) dir = ".";
                JFileChooser save = new JFileChooser(dir);
                
                save.removeChoosableFileFilter(save.getAcceptAllFileFilter());
                FileFilter v11 = new FileFilter() {
                        public boolean accept(File f) { return true; }
                        public String getDescription() { return "Version 1.1"; }
                    };
                save.addChoosableFileFilter(v11);
                FileFilter v10 = new FileFilter() {
                        public boolean accept(File f) { return true; }
                        public String getDescription() { return "Version 1.0"; }
                    };
                save.addChoosableFileFilter(v10);
                FileFilter v10a = new FileFilter() {
                        public boolean accept(File f) { return true; }
                        public String getDescription() { return "Version 1.0a"; }
                    };
                save.addChoosableFileFilter(v10a);
                save.setFileFilter(v11);

                save.setDialogTitle("Save Taems structure as...");
                int ret = save.showSaveDialog(TaemsCanvas.this);
                if (ret == JFileChooser.APPROVE_OPTION) {
                    file = save.getSelectedFile();
                    if ((!file.getPath().endsWith(".ttaems")) &&
                        (!file.getPath().endsWith(".taems"))) {
                        file = new File(file.getPath() + ".ttaems");
                    }
                    FileFilter f = save.getFileFilter();
                    vers = VCUR;
                    if (f == v11) {
                        vers = V1_1;
                    } else if (f == v10) {
                        vers = V1_0;
                    } else if (f == v10a) {
                        vers = V1_0A;
                    }
                } else {
                    return;
                }
            }

            try {
                FileWriter writer = new FileWriter(file);
                writer.write(Taems.this.toTTaems(vers));
                writer.flush();
                writer.close();
            } catch (IOException ex) {
                System.err.println(ex);
            }
        }

        /**
         * Prints it to the printer
         */
        protected Properties props = new Properties();
        public synchronized void printToPrinter() {

            PrinterJob job = PrinterJob.getPrinterJob();
            if (job.printDialog()) {
                printing = true;
                job.setPrintable(this, pageformat);
                try {
                    job.print();
                } catch (PrinterException e) {
                    System.err.println(e);
                }
                printing = false;
            }

                /*
            // Find the part frame
            Container c = this;
            while (!(c instanceof Frame)) {
                c = c.getParent();
                if (c == null) return;
            }

            // Print it
            PrintJob pjob = getToolkit().getPrintJob((Frame)c, "Print", null);
            printing = true;
            if (pjob != null) {
                Graphics pg = pjob.getGraphics();
                if (pg != null) {
                    pg.setColor(Color.white);
                    pg.setFont(getFont());
                    paintNodes(pg);
                    pg.dispose();
                }
                pjob.end();
            }
            printing = false;
                */
        }

        public int print(Graphics g, PageFormat pageFormat, int pageIndex) {
            if (pageIndex == 0) {
                Graphics2D g2 = (Graphics2D)g;
                
                // Get the page dimensions
                double pageHeight = pageFormat.getImageableHeight();
                double pageWidth = pageFormat.getImageableWidth();
                
                // Figure out the scaling for the image
                Dimension d = getPreferredSize();
                Rectangle bounds = updatePreferredSize();
                double xScale = Math.min(1, pageWidth / bounds.getWidth());
                double yScale = Math.min(1, pageHeight / bounds.getHeight());
                
                AffineTransform SAF = g2.getTransform();
                AffineTransform AF = new AffineTransform(SAF);
                
                double scale = Math.min(xScale, yScale);
                AF.translate(pageFormat.getImageableX() - bounds.getX() * scale,
                             pageFormat.getImageableY() - bounds.getY() * scale);
                AF.scale(scale, scale);

                
                g2.setBackground(Color.white);
                g2.setTransform(AF);
                paintComponent(g2);
                g2.setTransform(SAF);

                return PAGE_EXISTS;
            } else {
                return NO_SUCH_PAGE;
            }
        }

		public void lostOwnership(Clipboard clipboard, Transferable contents) {
		}
		
        /**
         * Copies a node (subtree) to memory
         */
        public void copyNode(Node n) {
            Taems clone = null;

            if (n != null) {    
    			Taems taems = new Taems();
    			taems.addNode((Node)n.clone());
    			StringSelection data = new StringSelection(taems.toString());
    			
			    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			    clipboard.setContents(data, this);
            }
        }
        public void pasteNode(Point point) {

            // Deselect node
            selectNode(null);

            // Read the clipboard
            String data = getClipboardContents();
            
            // Parse the data
            ReadTTaems reader = new ReadTTaems(log.getDefault());
            Taems taems = reader.readTTaems(new StringReader(data));
            if (taems == null) return;

            // Fix the names
            for (Enumeration e = taems.findNodes(new Node()); e.hasMoreElements(); ) {
                Node src = (Node)e.nextElement();
                if (src.isVirtual()) continue;
                if (findNode(new Node(src.getLabel())) != null) {
                    int index;
                    for (index = 1; findNode(new Node(src.getLabel() + "_" + index)) != null; index++) { }
                    src.setLabel(src.getLabel() + "_" + index);
                }
            }

            // Add it
            placeLock = true;
            addTaems(taems, false);
        }

        public String getClipboardContents() {

            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable contents = clipboard.getContents(null);
            boolean hasTransferableText = (contents != null) && contents.isDataFlavorSupported(DataFlavor.stringFlavor);
            
            if (hasTransferableText) {
                try {
                    return (String)contents.getTransferData(DataFlavor.stringFlavor);
                } catch (UnsupportedFlavorException ex) {
                    System.out.println(ex);
                } catch (IOException ex) {
                    System.out.println(ex);
                }
            }
            
            return null;
        }
		
        /**
         * Paints the display
         */
        Dimension d = new Dimension(500,500);
        public void paintComponent(Graphics g) {

            synchronized (addLock) {
                if (info != null) {
                    if (revalue) {
                        info.repaint();
                        revalue = false;
                    }
                }
                
                if (replace) {
                    placeNodes(g);
                    replace = false;
                    revalidate();
                }

                if (!printing) {
                    d = getSize();
                    g.clearRect(0, 0, d.width, d.height);
                }
                paintNodes(g);
            }
        }
				
        /**
         * Paints all the nodes
         */
        public void paintNodes(Graphics g) {
            Enumeration e;
                    
            // Erase
            //Rectangle clip = g.getClipBounds();
            //g.clearRect(clip.x, clip.y, clip.width, clip.height);
                    
            // Draw the lines
            e = getNodes();
            while (e.hasMoreElements()) {
                Node n = (Node)e.nextElement();
                n.paintLines(g);
            }
                    
            // Draw the nodes
            e = getNodes();
            while (e.hasMoreElements()) {
                Node n = (Node)e.nextElement();
                if (n.getLocation() == null) {
                    log.log("Warning, node " + n.getLabel() + " has null location", 0);
                    continue;
                }
                if (n.getLocation().x == Integer.MIN_VALUE) {
                    log.log("Warning, node " + n.getLabel() + " is unplaced", 0);
                    continue;
                }
                n.paint(g);
                /*if (n instanceof Interrelationship) { // deal with orphan IR froms here
                    if (((Interrelationship)n).getFrom() != null) {
                        if (((Interrelationship)n).getFrom().getLocation() != null) {
                            ((Interrelationship)n).getFrom().paint(g);
                        }
                    }
                    if (((Interrelationship)n).getTo() != null) {
                        if (((Interrelationship)n).getTo().getLocation() != null) {
                            ((Interrelationship)n).getTo().paint(g);
                        }
                    }
                    }*/
            }
        }

        /**
         * Places all the nodes
         */
        Dimension dim = getMinimumSize();
        boolean placeLock = false;
        public void placeNodes(Graphics g) {
            Enumeration e;
            int width, height;
            int twidth = 0, theight = 0;
            int curwidth, curheight;
            int nrsrc = 0;
            Dimension d = new Dimension(0, 0);
            dim = d;
                    
            // Init the locations
            e = getAllNodes();
            Point none = new Point(Integer.MIN_VALUE, Integer.MIN_VALUE);
            while (e.hasMoreElements()) {
                Node n = (Node)e.nextElement();
                if (!placeLock || n.getLocation() == null) {
                    n.setLocation(none);
                }
                n.updateBounds(g.getFontMetrics());
                if ((!shownles) && (n instanceof Interrelationship))
                    n.setHidden(true);
                else
                    n.setHidden(false);
            }
            placeLock = false;

            // Place the root nodes
            curwidth = H_MARGIN;
            curheight = V_MARGIN;
            e = getNodes();
            while (e.hasMoreElements()) {
                Node n = (Node)e.nextElement();
                if (n instanceof Resource) {
                    nrsrc++; // count rsrcs for later
                    continue;
                }

                width = n.calculateTreeWidth(g.getFontMetrics());

                if (n.getLocation() != none) {  // already been placed?
                    curwidth = (int)Math.max(curwidth, n.getLocation().getX() + width / 2 + H_SPACE);
                    dim.width = curwidth;
                    continue;
                }

                n.setLocation(new Point(curwidth + width / 2, curheight + n.calculateHeight(g.getFontMetrics()) / 2));
                curwidth += width + H_SPACE;
                dim.width = curwidth;

                if (n instanceof Task) { // deal with tasks here
                    Task t = (Task)n;
                    if (!t.isTaskGroup()) continue;     // specifically, only task groups
                    t.setSubLocations(g.getFontMetrics());
                }
                n.updateBounds(g.getFontMetrics());
            }


            // Place orphan IR nodes
            /*
            e = findNodes(new Interrelationship());
            while (e.hasMoreElements()) {
                Interrelationship i = (Interrelationship)e.nextElement();
                Node n = i.getFrom();
                if ((n != null) && (!(n instanceof Resource))) {
                    if (n.getLocation() != null) {
                        if (n.getLocation() == none) {
                            width = n.calculateTreeWidth(g.getFontMetrics());
                            n.setLocation(new Point(curwidth + width / 2, curheight + n.calculateHeight(g.getFontMetrics()) / 2));
                            curwidth += width + H_SPACE;
                            dim.width = curwidth;
                            if (n instanceof Task) { // deal with tasks here
                                Task t = (Task)n;
                                if (!t.isTaskGroup()) continue;     // specifically, only task groups
                                t.setSubLocations(g.getFontMetrics());
                            }
                            n.updateBounds(g.getFontMetrics());
                        }
                    }
                }
                n = i.getTo();
                if ((n != null) && (!(n instanceof Resource))) {
                    if (n.getLocation() != null) {
                        if (n.getLocation() == none) {
                            width = n.calculateTreeWidth(g.getFontMetrics());
                            n.setLocation(new Point(curwidth + width / 2, curheight + n.calculateHeight(g.getFontMetrics()) / 2));
                            curwidth += width + H_SPACE;
                            dim.width = curwidth;
                            if (n instanceof Task) { // deal with tasks here
                                Task t = (Task)n;
                                if (!t.isTaskGroup()) continue;     // specifically, only task groups
                                t.setSubLocations(g.getFontMetrics());
                            }
                            n.updateBounds(g.getFontMetrics());
                        }
                    }
                }
                }*/

            // Calc total width
            curwidth += H_MARGIN - H_SPACE;
            twidth = curwidth; // used to place the resources

            // Place resources
            curwidth = H_MARGIN;
            curwidth += (twidth - H_MARGIN * 2) / (nrsrc + 1);
            curheight = V_MARGIN + calculateTreeHeight(g.getFontMetrics());
            e = findNodes(new Resource());
            while (e.hasMoreElements()) {
                Node n = (Node)e.nextElement();
                if (!(n instanceof Resource)) continue;
                if (n.getLocation() != none) continue;
                width = n.calculateTreeWidth(g.getFontMetrics());
                n.setLocation(new Point(curwidth, curheight - n.calculateHeight(g.getFontMetrics()) / 2));
                curwidth += twidth / (nrsrc + 1);
                n.updateBounds(g.getFontMetrics());
                dim.height = curheight;
            }
                    
            // Place Interrelationships
            e = findNodes(new Interrelationship());
            while (e.hasMoreElements()) {
                Interrelationship i = (Interrelationship)e.nextElement();
                if (i.getLocation() != none) continue;
                Node f = i.getFrom();
                if (f == null) {
                    log.log("Warning, interrelationship " + i.getLabel() + " has null from link", 0);
                    continue;
                }
                if (f.getLocation() == null) {
                    log.log("Warning, ir from node " + f.getLabel() + " has null location", 0);
                    continue;
                }
                if (f.getLocation() == none) {
                    log.log("Warning, ir from node " + f.getLabel() + " has none location", 0);
                    continue;
                }
                Node t = i.getTo();
                if (t == null) {
                    log.log("Warning, interrelationship " + i.getLabel() + " has null to link", 0);
                    continue;
                }
                if (t.getLocation() == null) {
                    log.log("Warning, ir to node " + t.getLabel() + " has null location", 0);
                    continue;
                }
                if (t.getLocation() == none) {
                    log.log("Warning, ir to node " + t.getLabel() + " has none location", 0);
                    continue;
                }
                Point p = new Point((f.getLocation().x + t.getLocation().x) / 2, (f.getLocation().y + t.getLocation().y) / 2 - V_SPACE / 2);
		int search = 1;
                while (findNode(p) != null) {
                    p = new Point(p.x, p.y - (search * V_SPACE) / 2);
                    if (p.y < 0) {
                       search = -1;
                       p = new Point(p.x, p.y - (search * V_SPACE) / 2);
		    }
                }
                i.setLocation(p);
                i.updateBounds(g.getFontMetrics());
            }

            // Update the dimensions
            updatePreferredSize();
        }
            
        /**
         * Selects a node
         */
        Node selected = null;
        public void selectNode(Node n) {

            if (selected != n) {
                // Deselect them
                if (selected != null) {
                    if (!shownles) {
                        hideNodes(selected.getOutInterrelationships(), true);
                        hideNodes(selected.getInInterrelationships(), true);
                    }
                    selected.setSelected(false);
                    if ((!shownles) && (selected instanceof Interrelationship))
                        selected.setHidden(true);
                }
                
                // Pick the new selected node
                if (selected == null || selected.isVisible()) {
                    selected = n;
                } else {
                    selected = null;
                }

                // Make them selected and such
                if (selected != null) {
                    selected.setSelected(true);
                    selected.setHidden(false);
                    //if (selected.getBounds() != null)
                    //    scroll.getViewport().scrollRectToVisible(selected.getBounds());

                    if (!shownles) {
                        hideNodes(selected.getOutInterrelationships(), false);
                        hideNodes(selected.getInInterrelationships(), false);
                    }
                }

                revalue = true;
            }
        }
        public Node getSelected() { return selected; }

        /**
         * Manages the responses to mouse events in the drawing frame
         */
        Point popuppoint = null;
        public void handleMouseEvent(MouseEvent e) {

            // Selection and collapsing
            if (e.getID() == MouseEvent.MOUSE_PRESSED) {
                if (e.getClickCount() == 1) {
                    selectNode(findNode(e.getPoint()));
                } else if (e.getClickCount() == 2) {
                    Node n = findNode(e.getPoint());
                    if (n != null)
                        n.toggleCollapsed();
                }
            }

            // Show popup menu
            if (e.isPopupTrigger() && !popupmenu.isVisible()) { 
                updateMenu();
                popupmenu.show(this, e.getX(), e.getY());
                popuppoint = e.getPoint();
            }
            
            // Drag nodes
            if ((e.getID() == MouseEvent.MOUSE_DRAGGED) && !popupmenu.isVisible()) {
                if (selected != null) {
                    Point offset = new Point(e.getPoint().x - selected.getLocation().x,
                                             e.getPoint().y - selected.getLocation().y);
                    Point halfoffseto = new Point((int)Math.ceil((float)offset.x / (float)2),
                                                  (int)Math.ceil((float)offset.y / (float)2));
                    Point halfoffseti = new Point((int)Math.floor((float)offset.x / (float)2),
                                                  (int)Math.floor((float)offset.y / (float)2));
                    selected.offsetLocation(offset);
                    if (e.isControlDown()) {
                        Enumeration en, en2;
                        Node n;

                        // Offset IRs
                        en2 = selected.getOutInterrelationships();
                        while (en2.hasMoreElements()) {
                            Interrelationship i = (Interrelationship)en2.nextElement();
                            i.offsetLocation(halfoffseto);
                        }
                        en2 = selected.getInInterrelationships();
                        while (en2.hasMoreElements()) {
                            Interrelationship i = (Interrelationship)en2.nextElement();
                            i.offsetLocation(halfoffseti);
                        }

                        if (selected instanceof Task) {
                            en = ((Task)selected).getAllSubtasks();
                            while (en.hasMoreElements()) {
                                // Offset the node
                                n = (Node)en.nextElement();
                                n.offsetLocation(offset);

                                // Offset IRs
                                en2 = n.getOutInterrelationships();
                                while (en2.hasMoreElements()) {
                                    Interrelationship i = (Interrelationship)en2.nextElement();
                                    i.offsetLocation(halfoffseto);
                                }
                                en2 = n.getInInterrelationships();
                                while (en2.hasMoreElements()) {
                                    Interrelationship i = (Interrelationship)en2.nextElement();
                                    i.offsetLocation(halfoffseti);
                                }
                            }
                        }
                    }
                }
            }
        }

        /**
         */
        public Dimension getMinimumSize() {
            return new Dimension(25, 25);
        }

        /**
         * Update the preferred size
         * @return The bounding rectangle of the image
         */
        public Rectangle updatePreferredSize() {

            // Update the dimensions
            Enumeration e = getAllNodes();
            int t = Integer.MAX_VALUE;
            int l = Integer.MAX_VALUE;
            int r = Integer.MIN_VALUE;
            int b = Integer.MIN_VALUE;
            while (e.hasMoreElements()) {
                Node n = (Node)e.nextElement();
                Rectangle bounds = n.getBounds();
                if (bounds == null) continue;
                if (bounds.y < t)
                    t = bounds.y;
                if (bounds.x < l)
                    l = bounds.x;
                if (bounds.x + bounds.width > r)
                    r = bounds.x + bounds.width;
                if (bounds.y + bounds.height > b)
                    b = bounds.y + bounds.height;
            }
            dim.height = V_MARGIN * 2 + b;
            dim.width = H_MARGIN * 2 + r;

            return new Rectangle(l, t, r - l, b - t);
        }

        /**
         */
        public Dimension getPreferredSize() {
            return dim;
        }

        /**
         */
        private void hideNodes(Enumeration e, boolean h) {
            while (e.hasMoreElements()) {
                Node n = (Node)e.nextElement();
                if (! n.isSelected())
                    n.setHidden(h);
            }					 
       }

        /**
         */
        private void hideQCDs(Enumeration e, boolean h) {
            while (e.hasMoreElements()) {
                Node n = (Node)e.nextElement();
                if (!(n instanceof Method)) continue;
                ((Method)n).setShowQCDs(h);
            }					 
       }
    }
}
