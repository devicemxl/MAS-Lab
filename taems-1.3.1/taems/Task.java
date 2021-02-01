/* Copyright (C) 2005, University of Massachusetts, Multi-Agent Systems Lab
 * See LICENSE for license information
 */

/************************************************************
 * Task.java
 ************************************************************/

package taems;

/* Global Includes */
import java.util.*;
import java.io.*;
import java.awt.*;

import utilities.EnumerationEnumeration;

/**
 * Task node.  These form the internal structure of the Taems
 * graph, organizing other tasks and methods into a coherant
 * form.  Tasks which form the root of a tree (i.e. have no
 * supertasks) are known as task groups and will typically
 * represent some sort of high level goal the agent is attempting
 * to achieve.
 */
public class Task extends TaskBase implements Serializable, Cloneable  {
  protected QAF qaf = null;
  protected Vector subtasks = new Vector();

  /**
   * Constructor
   * @param l The node's label
   * @param a The agent the node belongs to
   * @param q The task's QAF
   * @param at Arrival time
   * @param est Earliest start time
   * @param dl Deadline
   */
  public Task(String l, Agent a, QAF q, int at, int est, int dl) {
    super(l, a, at, est, dl);

    qaf = q;
  }

  /**
   * Simple constructor
   * @param l The node's label
   * @param a The agent the node belongs to
   * @param q The task's QAF
   */
  public Task(String l, Agent a, QAF q) {
    this(l, a, q, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
  }

  /**
   * Blank Constructor
   */
  public Task() {
    this (null, null, null, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
  }

  /**
   * Accessors
   */
  public QAF getQAF() { return qaf; }
  public void setQAF(QAF q) {
      if (getQAF() != null)
          if (getQAF().matches(q)) return;
      qaf = q;
      fireNodeUpdateEvent(new NodeUpdateEvent(this, NodeUpdateEvent.VALUE));
  }
  public int getStartTime() {
    int s = Integer.MAX_VALUE;

    Enumeration e = getSubtasks();
    while (e.hasMoreElements()) {
      TaskBase n = (TaskBase)e.nextElement();
      int ns = ((TaskBase)n).getStartTime();
      if (ns == Integer.MIN_VALUE) continue;
      if (ns < s) s = ns;
    }

    if (s == Integer.MAX_VALUE)
      s = Integer.MIN_VALUE;

    return s;
  }
  public int getFinishTime() { 
    int s = Integer.MIN_VALUE;

    Enumeration e = getSubtasks();
    while (e.hasMoreElements()) {
      TaskBase n = (TaskBase)e.nextElement();
      int ns = ((TaskBase)n).getFinishTime();
      if (ns > s) s = ns;
    }

    return s;
  }

  public float getMaximumQuality() {
    if (hasAttribute("max_quality"))
        return ((Float)getAttribute("max_quality")).floatValue();
    else {
        float maximumQuality;
        Enumeration e = getSubtasks();
        maximumQuality = getQAF().calculateMaximumQuality(e);
        setMaximumQuality(maximumQuality);
        return(maximumQuality);
    }
  }
  
  /**
   * Updates the local quality, by using the tasks QAF.
   */
  public void updateQuality() {
    float q = 0;

    if (getQAF() != null) {
      Enumeration e = getSubtasks();
      q = getQAF().calculateQuality(e);
    }

    setCurrentQuality(q);
  }

  /**
   * Updates the local duration, by summing the duration of its
   * subtasks.
   */
  public void updateDuration() {
    float d = 0;

    Enumeration e = getSubtasks();
    while (e.hasMoreElements()) {
      TaskBase t = (TaskBase)e.nextElement();
      d += t.getCurrentDuration();
    }

    setCurrentDuration(d);
  }

  /**
   * Updates the local cost, by summing the costs of its
   * subtasks.
   */
  public void updateCost() {
    float c = 0;

    Enumeration e = getSubtasks();
    while (e.hasMoreElements()) {
      TaskBase t = (TaskBase)e.nextElement();
      c += t.getCurrentCost();
    }

    setCurrentCost(c);
  }

  /**
   * Updates QDC, using the methods above
   */
  public void updateState() {

    updateQuality();
    updateDuration();
    updateCost();
  }

  /**
   * Detaches the node from the structure it is in, be careful with
   * this.  Note you may have to also call Taems.removeNode to remove
   * it from the enclosing Taems structure.
   * <BR>
   * Afterwards, the node will have VirtualTaskBases representing the 
   * subtasks removed from it.
   */
  public Node excise() {
    Node n = super.excise();

    Enumeration e = new utilities.SafeEnumeration(getSubtasks());
    while (e.hasMoreElements()) {
      TaskBase t = (TaskBase)e.nextElement();
      replaceSubtask(t, new VirtualTaskBase(t.getLabel(), t.getAgent()));
    }

    return n;
  }

  /**
   * Destroys this node, and any other nodes that are dependent on it.
   * Note you may have to also call Taems.removeNode to remove
   * it from the enclosing Taems structure.
   */
  public void delete() {

      Enumeration e = new utilities.SafeEnumeration(getSubtasks());
      while (hasSubtasks()) {
          TaskBase t = (TaskBase)subtasks.firstElement();
          removeSubtask(t);
          if (!t.hasSupertasks())
              t.delete();
      }

      super.delete();
  }

  /**
   * Adds a subtask to this node (at the end of the list)
   * @param st The task/method to add
   */
  public void addSubtask(TaskBase st) {

    subtasks.addElement(st);
    st.addSupertask(this);
    st.addNodeUpdateEventListener(this);

    fireNodeUpdateEvent(new NodeUpdateEvent(this, NodeUpdateEvent.VALUE));
    fireNodeUpdateEvent(new NodeUpdateEvent(this, NodeUpdateEvent.PLACEMENT));

    updateState();
  }

  /**
   * Inserts a subtask in this node's child list
   * @param st The task/method to add
   * @param i The index to add to
   */
  public void insertSubtask(TaskBase st, int i) {

    subtasks.insertElementAt(st, i);
    st.addSupertask(this);
    st.addNodeUpdateEventListener(this);

    fireNodeUpdateEvent(new NodeUpdateEvent(this, NodeUpdateEvent.VALUE));
    fireNodeUpdateEvent(new NodeUpdateEvent(this, NodeUpdateEvent.PLACEMENT));

    updateState();
  }

  /**
   * Sets a subtask in this node's child list, replacing the
   * one that was there.
   * @param st The task/method to add
   * @param i The index to add to
   */
  public void setSubtask(TaskBase st, int i) {

    TaskBase ost = getSubtask(i);
    if (ost != null) {
      ost.removeSupertask(this);
      ost.removeNodeUpdateEventListener(this);
    }

    subtasks.setElementAt(st, i);
    st.addSupertask(this);
    st.addNodeUpdateEventListener(this);

    fireNodeUpdateEvent(new NodeUpdateEvent(this, NodeUpdateEvent.PLACEMENT));

    updateState();
  }

  /**
   * Replaces a TaskBase in the tasks subtask list with another.
   * If the original is not found in the subtask list, no
   * substitution is made.
   * @param ot The task/method to replace
   * @param nt The new task/method to replace it with
   */
  public void replaceSubtask(TaskBase ot, TaskBase nt) {
    int index = getSubtaskPosition(ot);

    if (index > -1) {
      setSubtask(nt, index);
    }
  }

  /**
   * Removes a subtask from this node
   * @param st The task/method to remove
   */
  public void removeSubtask(TaskBase st) {

    st.removeSupertask(this);
    subtasks.removeElement(st);
    st.removeNodeUpdateEventListener(this);

    fireNodeUpdateEvent(new NodeUpdateEvent(this, NodeUpdateEvent.PLACEMENT));

    updateState();
  }

  /**
   * Gets a specific subtask
   * @param i The index to get
   * @return The task at the given index, or null if none found
   */
  public TaskBase getSubtask(int i) {

    try {
      return (TaskBase)subtasks.elementAt(i);
    } catch (ArrayIndexOutOfBoundsException e) {
      return null;
    }
  }

  /**
   * Gets a subtask's position
   * @param n The subtask to query
   * @return The index, or -1 if not found
   */
  public int getSubtaskPosition(Node n) {

    return subtasks.indexOf(n);
  }

  /**
   * Returns a list of the node's subtasks.
   * @return An enumeration of the subtasks
   */
  public Enumeration getSubtasks() {
    return subtasks.elements();
  }

  /**
   * Returns a list of all the node's subtasks, including the subtasks
   * of task children.  Essentially, this should return a list of all
   * the unique nodes (Methods and Tasks) which fall below this one.
   * @return An enumeration of subtasks
   */
  public Enumeration getAllSubtasks() {
      Vector v = new Vector();

      v.addElement(subtasks.elements());

      Enumeration e = subtasks.elements();
      while (e.hasMoreElements()) {
          TaskBase t = (TaskBase)e.nextElement();
          if (t instanceof Task) {
              v.addElement(((Task)t).getAllSubtasks());
          }
      }

    return new EnumerationEnumeration(v, true);
  }

  /**
   * Determines the number of subtasks
   * @param The number
   */
  public int numSubtasks() {
    return subtasks.size();
  }
  
  /**
   * Determines if the task is a group (that is, it a root
   * node of the graph)
   */
  public boolean isTaskGroup() { return (!hasSupertasks()); }

  /**
   * Determines if the task is barren (that is, it has no children)
   */
  public boolean hasSubtasks() { return (subtasks.size() > 0); }

  /**
   * Returns the ttaems version of the node
   * @param v The version number output style to use
   */
  public String toTTaems(float v) {
    StringBuffer sb = new StringBuffer("");
    Enumeration e;

    if ((v == Taems.V1) && (!hasSubtasks()))
      return "";

    if (isTaskGroup())
      sb.append("(spec_task_group\n");
    else
      sb.append("(spec_task\n");
    sb.append(super.toTTaems(v));

    if (hasSubtasks()) {
      StringBuffer comment = new StringBuffer("");
      sb.append("   (subtasks ");
      e = getSubtasks();
      while(e.hasMoreElements()) {
	Node n = (Node)e.nextElement();
	if ((v == Taems.V1) && ((TaskBase)n).isNonLocal())
	  continue;
	if ((v == Taems.V1) && (n instanceof Task) && (!((Task)n).hasSubtasks()))
	  continue;
	sb.append(n.getLabel());
	if (n.isVirtual())
	  comment.append(";  ** Note: node " + n.getLabel() + " is virtual\n");  
	if (e.hasMoreElements()) sb.append(" ");
      }
      sb.append(")\n");
      sb.append(comment.toString());
    }

    if (getQAF() != null)
      sb.append("   (qaf " + getQAF().getLabel() + ")\n");
    sb.append(")\n");

    return sb.toString();
  }

  /**
   * Determines if an object matches this one.
   * <BR>
   * This matches against:
   * <UL>
   * <LI> QAFs
   * <LI> Subtasks (empty list is wild, order maintained) - This essentially
   * means that you can match the entire (sub)tree if you include
   * subtasks in your match task.
   * </UL>
   * Check the matches function for the parent classes of this
   * class for more details.
   * @see TaskBase#matches
   * @see Node#matches
   */
    public boolean matches(Node n) {

        if (n.getClass().isInstance(this)) {
            if (! super.matches(n))
                return false;
        } else
            return false;

        if (n instanceof Task) {
            if ((((Task)n).getQAF()) != null) {
                if (getQAF() == null) return false;
                if (! getQAF().getClass().isInstance(((Task)n).getQAF())) return false;
            }

            if (((Task)n).hasSubtasks()) {
                if (((Task)n).numSubtasks() != numSubtasks()) return false;

                Enumeration e1 = getSubtasks();
                Enumeration e2 = ((Task)n).getSubtasks();
                while (e1.hasMoreElements()) {
                    TaskBase tb1 = (TaskBase)e1.nextElement();
                    TaskBase tb2 = (TaskBase)e2.nextElement();
                    
                    if (! tb1.matches(tb2)) return false;
                }
            }
        }

        return true;
    }

  /**
   * Clone
   */
  public Object clone() {
    Task cloned = null;

    try {
      cloned = (Task)super.clone();
    } catch (Exception e) {System.out.println("Clone Error: " + e);}

    if (qaf != null) cloned.setQAF((QAF)qaf.clone());
    else cloned.setQAF(qaf);
    cloned.subtasks = new Vector();
    if (hasSubtasks()) {
      Enumeration e = getSubtasks();
      while (e.hasMoreElements()) {
	TaskBase t = (TaskBase)e.nextElement();
        // Make sure we are the "owner" of this task, to prevent
        // over-cloning.  If we aren't, stick a virtual node in
        // and pray someone retargets us later.
        if (t.firstSupertask() == this)
	  cloned.addSubtask((TaskBase)t.clone());
        else
	  cloned.addSubtask(new VirtualTaskBase(t.getLabel(), t.getAgent()));
      }
    }

    // Look for IRs to reattach
    Enumeration e = cloned.findNodes(new Interrelationship(null, null, null, null, null, null));
    while(e.hasMoreElements()) {
      Interrelationship i = (Interrelationship)e.nextElement();
      if (i.getTo().isVirtual()) {
          Enumeration ne = cloned.findNodes(new Node(i.getTo().getLabel(), i.getTo().getAgent()));
          if (ne.hasMoreElements()) {
              Node temp = (Node)ne.nextElement();
              if (temp.isVirtual()) continue;
              i.setTo(temp);
          }
          if (ne.hasMoreElements()) {
              // Warn if multiple matches
              Node temp = (Node)ne.nextElement();
              if (temp.isVirtual()) continue;
              System.err.println("Warning: Duplicate match(es) found for IR (" + i.getLabel() + ") target " + temp.getLabel());
          }
      }
    }

    return cloned;
  }


  /**
   * Copies the node's fields into the provided node.
   * Does not do anything with endpoints.
   * @param n The node to copy stuff into
   */
    public void copy(Node n) {

        if (n instanceof Task) {
            Task t = (Task)n;

            if (qaf != null) t.setQAF((QAF)qaf.clone());
            else t.setQAF(qaf);
        }

        super.copy(n);
    }

  /***********************************************************
   *                     Drawing Junk                        *
   ***********************************************************/
	protected static final int H_SPACE = Taems.H_SPACE;
	protected static final int V_SPACE = Taems.V_SPACE;

    /**
     * Sets the location of the subtasks.
     */
    public void setSubLocations(FontMetrics fm) {

        // Place children
        int curx = getLocation().x - calculateSubTreeWidth(fm) / 2;
        Enumeration e = getSubtasks();
        while (e.hasMoreElements()) {
            TaskBase n = (TaskBase)e.nextElement();
            if (n.firstSupertask() == this) {
                int width = n.calculateTreeWidth(fm);
                n.setLocation(new Point(curx + width / 2, getLocation().y + V_SPACE));
                if (n instanceof Task)
                    ((Task)n).setSubLocations(fm);
                curx += width + H_SPACE;
            }
        }
    }
		
    /**
     * Paints any connection lines that need to be painted
     * <P>
     * This also paints the lines of any subtasks.
     * @param g Where to draw them
     */
    public void paintLines(Graphics g) {
				
        if (isVisible() && ! isCollapsed()) {
            // Draw child lines
            Enumeration e = getSubtasks();
            while (e.hasMoreElements()) {
                Node n = (Node)e.nextElement();
                if (isSelected())
                    g.setColor(Color.red);
                else if (n.isSelected())
                    g.setColor(Color.blue);
                else
                    g.setColor(Color.black);

                if (getLocation() == null) {
                    System.err.println("getLocation() return null for task " + getLabel());

                } else {
                    if (n.getLocation() == null) {
                        System.err.println("getLocation() return null for task child " + n.getLabel());
                    } else {
                        g.drawLine(getLocation().x, getLocation().y, n.getLocation().x, n.getLocation().y);
                    }
                }

                n.paintLines(g);
            }
        }

        super.paintLines(g);
    }

    /**
     * Paints the node and any children it is responsible for
     * @param g Where to draw it
     */
    public void paint(Graphics g) {
        Enumeration e;
        g.setFont(normalFont);
        updateBounds(g.getFontMetrics());
				
        if (isVisible()) {
            // Draw bounding rect
            g.setColor(getBackground());
            g.fillRoundRect(rect.x, rect.y, rect.width, rect.height, rect.height, rect.height);
            g.setColor(Color.black);
            if (isCollapsed()) {
                ((Graphics2D)g).setStroke(collapsedstroke);
            }
            g.drawRoundRect(rect.x, rect.y, rect.width, rect.height, rect.height, rect.height);
            if (isCollapsed()) {
                ((Graphics2D)g).setStroke(normalstroke);
            }

            // Draw label
            try {
                g.setColor(getForeground());
                g.drawString(getLabel(), rect.x + H_MARGIN, rect.y + rect.height - (V_MARGIN + g.getFontMetrics().getDescent()));

                g.setFont(smallFont);
                FontMetrics fm = g.getFontMetrics();

                if (getQAF() != null) {
                    if (Taems.printing)
                      g.setColor(Color.black);
                    else
                      g.setColor(Color.gray);
                    g.drawString(getQAF().getLabel(), getLocation().x - fm.stringWidth(qaf.getLabel())/2, rect.y + rect.height + V_MARGIN + fm.getAscent());
                }
						
                if (getCurrentQuality() != 0) {
                    String num = String.valueOf(getCurrentQuality());
                    g.setColor(Color.red);
                    g.drawString(num, rect.x - (fm.stringWidth(num) + H_MARGIN), rect.y + rect.height - (V_MARGIN + fm.getDescent()));
                }
						
            } catch (java.lang.NullPointerException ex) {
                System.err.println("Error drawing label");
                ex.printStackTrace();
            }
            g.setFont(normalFont);
        }

        if (!isCollapsed()) {
            // Draw children
            e = getSubtasks();
            while (e.hasMoreElements()) {
                TaskBase n = (TaskBase)e.nextElement();
                if (n.firstVisibleSupertask() == this) {
                    n.paint(g);
                }
            }
        }

        // Draw Interrelationships
        e = getOutInterrelationships();
        while (e.hasMoreElements()) {
            Node n = (Node)e.nextElement();
            n.paint(g);
        }
    }

    /**
     * Calculates the width of the object, plus any subnodes.
     * @param fm The FontMetrics to use in calculation
     * @return The width
     */
    public int calculateTreeWidth(FontMetrics fm) {
        int width = super.calculateTreeWidth(fm);
        int subwidth = calculateSubTreeWidth(fm);

        /*if (getQAF() != null) {
          width = Math.max(width, fm.stringWidth(getQAF().getLabel()));
          }*/

        return Math.max(width, subwidth);
    }

		/**
		 * Calculates the width of the subtree
		 * @param fm The FontMetrics to use in calculation
		 * @return The width
		 */
		public int calculateSubTreeWidth(FontMetrics fm) {
				int width = -H_SPACE;

				Enumeration e = getSubtasks();
				while (e.hasMoreElements()) {
						TaskBase n = (TaskBase)e.nextElement();
						if (n.firstSupertask() == this)
								width += n.calculateTreeWidth(fm) + H_SPACE;
				}

				return width;
    }

		/**
		 * Calculates the height of the object, plus any subnodes.
		 */
		public int calculateTreeHeight(FontMetrics fm) {
				int height = super.calculateTreeHeight(fm);
				int sheight = 0;

				if (hasSubtasks()) {
						height += V_SPACE;
						Enumeration e = getSubtasks();
						while (e.hasMoreElements()) {
								TaskBase n = (TaskBase)e.nextElement();
								if (n.firstSupertask() == this) {
										sheight = Math.max(sheight, n.calculateTreeHeight(fm) - n.calculateHeight(fm) / 2);
								}
						}
				}

				return height + sheight;
		}
}
