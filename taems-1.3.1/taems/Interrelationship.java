/* Copyright (C) 2005, University of Massachusetts, Multi-Agent Systems Lab
 * See LICENSE for license information
 */

/************************************************************
 * Interrelationship.java
 ************************************************************/

package taems;

/* Global Includes */
import java.util.*;
import java.io.*;
import java.awt.*;

import utilities.*;

/**
 * Interrelationships are used to represent the effects which
 * one node in a task structure will have on another.  For instance,
 * if you have two methods, where one cannot be successfully completed
 * before the other is successfully completed (for instance, you typically
 * can't drive a car before you get in), then you would model that by
 * having an EnablesInterrelationship arising from the precursor 
 * method and terminating at the sucessor.
 * <P>
 * You shouldn't have to instantiate one of these directly,
 * except when using them to find other matching nodes.  Instead,
 * you will work with classes like EnablesInterrelationship or
 * ProducesInterrelationship which represent real non local effects
 * in the task structure.
 */
public class Interrelationship extends Node implements Serializable, Cloneable {
  protected Node from = null;
  protected Outcome from_outcome = null;
  protected Node to = null;
  protected String model = null;
  protected Distribution quality;
  protected Distribution duration;
  protected Distribution cost;
  protected Distribution delay;

  protected int timestamp = 0;
  public boolean active = false;

  /* Resource Model Constants */
  public static final String PER_TIME_UNIT = "per_time_unit";
  public static final String DURATION_INDEPENDENT = "duration_independent";

  /**
   * Constructor
   */
  public Interrelationship(String l, Agent a, Distribution q, Distribution du, Distribution c, Distribution de) {
    super(l, a);

    quality = q;
    duration = du;
    cost = c;
    delay = de;

    setActive(false);
  }

  /**
   * Constructor
   */
  public Interrelationship(String l, Agent a) {
    super(l, a);

    setActive(false);
  }

  /**
   * Blank Constructor
   */
  public Interrelationship() {
    this (null, null, null, null, null, null);
  }

  /**
   * Accessors
   */
  public int getTimeStamp() { return timestamp; }
  public void setTimeStamp(int t) { timestamp = t; }
  public Distribution getQuality() { return quality; }
  public void setQuality(Distribution q) {
      if (getQuality() != null)
          if (getQuality().equals(q)) return;
      quality = q;
      fireNodeUpdateEvent(new NodeUpdateEvent(this, NodeUpdateEvent.VALUE));
  }
  public Distribution getDuration() { return duration; }
  public void setDuration(Distribution d) {
      if (getDuration() != null)
          if (getDuration().equals(d)) return;
      duration = d;
      fireNodeUpdateEvent(new NodeUpdateEvent(this, NodeUpdateEvent.VALUE));
  }
  public Distribution getCost() { return cost; }
  public void setCost(Distribution c) {
      if (getCost() != null)
          if (getCost().equals(c)) return;
      cost = c;
      fireNodeUpdateEvent(new NodeUpdateEvent(this, NodeUpdateEvent.VALUE));
  }
  public Distribution getDelay() { return delay; }
  public void setDelay(Distribution d) {
      if (getDelay() != null)
          if (getDelay().equals(d)) return;
      delay = d;
      fireNodeUpdateEvent(new NodeUpdateEvent(this, NodeUpdateEvent.VALUE));
  }
  public void setModel(String m) {
      if (getModel() != null)
          if (getModel().equals(m)) return;
      model = m;
      fireNodeUpdateEvent(new NodeUpdateEvent(this, NodeUpdateEvent.VALUE));
  }
  public String getModel() { return model; }
  public boolean hasModel() { return false; }

  /**
   * Attribute based accessors
   */
  public boolean getActive() {
    if (hasAttribute("active")) {
      if (!(getAttribute("active") instanceof Boolean))
         setActive(Boolean.valueOf((String)getAttribute("active")).booleanValue());
      return ((Boolean)getAttribute("active")).booleanValue();
    } else
      return false;
  }
  public void setActive(boolean a) { 
	setAttribute("active", new Boolean(a));
	if (a) {
		setStatus(ACTIVE);
	} else {
		setStatus(NORMAL);
	}
  }
  public boolean isActive() { return getActive(); }

  /**
   * Attachers
   */
  public Node getFrom() { return from; }
  public Outcome getFromOutcome() { return from_outcome; }
  protected void setFromOutcome(Outcome o) { from_outcome = o; }
  protected void setFrom(Node n, Outcome o) {
    
    if (from != null) {
      from.removeOutInterrelationship(this);
    }

    from = n;
    if (from != null) {
      from.addOutInterrelationship(this);
    }
    setFromOutcome(o);
    fireNodeUpdateEvent(new NodeUpdateEvent(this, NodeUpdateEvent.PLACEMENT));
  }
  protected void setFrom(Node n) {
    setFrom(n, getFromOutcome());
  }
  public Node getTo() { return to; }
  protected void setTo(Node n) {
    
    if (to != null) {
      to.removeInInterrelationship(this);
    }

    to = n;
    if (to != null) {
      to.addInInterrelationship(this);
    }
    fireNodeUpdateEvent(new NodeUpdateEvent(this, NodeUpdateEvent.PLACEMENT));
  }
  protected void setEndpoints(Node f, Outcome fo, Node t) {

    if ((f instanceof Interrelationship) || (t instanceof Interrelationship)) {
      System.err.println("Error: Cannot attach an IR to another IR");
      return;
    }

    setFrom(f, fo);
    setTo(t);
  }

    /**
     * Replaces another interrelationship with this one. Upon completion,
     * the replacing interrelationship (i.e. <TT>x</TT>, in <TT>x.replace(y)</TT>)
     * will have the enpoints of the replaced interrelationship.  The
     * replaced interrelationship (i.e. <TT>y</TT>, in <TT>x.replace(y)</TT>)
     * will then have virtual nodes identifying its original enpoints as
     * its endpoints.
     * @param i The interrelationship to replace
     */
    public void replace(Interrelationship i) {

        Node of = i.getFrom();
        VirtualNode vf = null;
        if (of != null)
            vf = new VirtualNode(of.getLabel(), of.getAgent());
        Outcome oo = i.getFromOutcome();
        Outcome vo = null;
        if (oo != null)
            vo = (Outcome)oo.clone();
        Node ot = i.getTo();
        VirtualNode vt = null;
        if (ot != null)
            vt = new VirtualNode(ot.getLabel(), ot.getAgent());
        
        i.setEndpoints(vf, vo, vt);
        setEndpoints(of, oo, ot);
    }

  /**
   * Detaches the node from the structure it is in, be careful with
   * this.  Note you may have to also call Taems.removeNode to remove
   * it from the enclosing Taems structure.
   * <BR>
   * Afterwards, The IR will have new VirtualNodes indicating 
   * where the from and to nodes it was taken from.
   */
  public Node excise() {
    Node n = super.excise();

    VirtualNode f = null;
    if (getFrom() != null)
        f = new VirtualNode(getFrom().getLabel(), getFrom().getAgent());
    Outcome o = null;
    if (getFromOutcome() != null)
        o = (Outcome)getFromOutcome().clone();
    VirtualNode t = null;
    if (getTo() != null)
        t = new VirtualNode(getTo().getLabel(), getTo().getAgent());

    setEndpoints(f, o, t);

    return n;
  }

  /**
   * Destroys this node, and any other nodes that are dependent on it.
   * Note you may have to also call Taems.removeNode to remove
   * it from the enclosing Taems structure.
   */
  public void delete() {
      setEndpoints(null, null, null);
      super.delete();
  }

  /**
   * Determines if an object matches this one.
   * <BR>
   * This matches against:
   * <UL>
   * <LI> Quality
   * <LI> Duration
   * <LI> Cost
   * <LI> Delay
   * </UL>
   * Check the matches function for the parent classes of this
   * class for more details.
   * @see Node#matches
   */
    public boolean matches(Node n) {

        if (n.getClass().isInstance(this)) {
            if (! super.matches(n))
                return false;
        } else
            return false;

        if (n instanceof Interrelationship) {
            if (!matches(((Interrelationship)n).getQuality(), getQuality())) return false;
            if (!matches(((Interrelationship)n).getDuration(), getDuration())) return false;
            if (!matches(((Interrelationship)n).getCost(), getCost())) return false;
            if (!matches(((Interrelationship)n).getDelay(), getDelay())) return false;
        }

        return true;
    }

  /**
   * Copies the node's fields into the provided node.
   * Does not do anything with endpoints.
   * @param n The node to copy stuff into
   */
    public void copy(Node n) {

        if (n instanceof Interrelationship) {
            Interrelationship i = (Interrelationship)n;

            if (quality != null) i.setQuality((Distribution)quality.clone());
            else i.setQuality(null);
            if (duration != null) i.setDuration((Distribution)duration.clone());
            else i.setDuration(null);
            if (cost != null) i.setCost((Distribution)cost.clone());
            else i.setCost(null);
            if (delay != null) i.setDelay((Distribution)delay.clone());
            else i.setDelay(null);
            if (model != null) i.setModel(getModel());
            else i.setModel(null);

            i.setActive(getActive());
        }

        super.copy(n);
    }
    
  /**
   * Clone, note this does not re-link the IR in the new graph
   * it might be part of, you must do that elsewhere.  In place of
   * to and from, VirtualNodes are used to aid in re-linking.
   */
  public Object clone() {
    Interrelationship cloned = null;

    try {
      cloned = (Interrelationship)super.clone();
    } catch (Exception e) {System.out.println("Clone Error: " + e);}

    if (quality != null) cloned.setQuality((Distribution)quality.clone());
    else cloned.setQuality(null);
    if (duration != null) cloned.setDuration((Distribution)duration.clone());
    else cloned.setDuration(null);
    if (cost != null) cloned.setCost((Distribution)cost.clone());
    else cloned.setCost(null);
    if (delay != null) cloned.setDelay((Distribution)delay.clone());
    else cloned.setDelay(null);
    if (model != null) cloned.setModel(getModel());
    else cloned.setModel(null);

    VirtualNode vf = null;
    if (getFrom() != null) vf = new VirtualNode(getFrom().getLabel(), getFrom().getAgent());
    VirtualNode vt = null;
    if (getTo() != null) vt = new VirtualNode(getTo().getLabel(), getTo().getAgent());
    cloned.setEndpoints(vf, null, vt);

    return cloned;
  }

  /**
   * Returns the ttaems version of the node
   * @param v The version number output style to use
   */
  public String toTTaems(float v) {
    StringBuffer sb = new StringBuffer("");

    sb.append(super.toTTaems(v));
    if (getFrom() != null) {
        sb.append("   (from " + getFrom().getLabel() + ")\n");  
        if (getFrom().isVirtual())
            sb.append(";   ** Note: from node " + getFrom().getLabel() + " is virtual\n");  
    }
    if (getFromOutcome() != null)
        sb.append("   (from_outcome " + getFromOutcome().getLabel() + ")\n");   
    if (getTo() != null) {
        sb.append("   (to " + getTo().getLabel() + ")\n");
        if (getTo().isVirtual())
            sb.append(";   ** Note: to node " + getTo().getLabel() + " is virtual\n");  
    }
    if (hasModel() && (getModel() != null) && (v >= Taems.V1_1)) 
        /** Resource Model  should be in TAEMS V1.0 but DTC doesn't
            handle that very well, so I move it here for now 
            remove it when DTC works with it = RV =  **/
        sb.append("   (model " + getModel() + ")\n");
    
    return sb.toString();
  }


  /**
   * Update the state of the NLE depending of the source quality.
   * @param time is the current time (useful for the timestamp).
   */
  public void update(int time) {
    setTimeStamp(time);
  }


  /**
   * This is the function you have to specify in the different subclasses
   * in order to compute the effects of this NLE on the outcome.
   * @param o original Outcome 
   * @param complex describe if you take in account the quality of 
   *        the originator.
   * @param tr is the TaemsRandom generator
   * @ return an outcome #see Outcome.
   */
  public Outcome applyNLEonOutcome(Outcome o, boolean complex, TaemsRandom tr)
  {
    return o;
  }

  /**
   * Check if this NLE needs to be updated
   */
  public boolean needToBeUpdated() {
    TaskBase task = (TaskBase)getFrom();
     if ((task.getCurrentQuality() != 0) && (getActive() != true))
       return true;
     else if ((getActive() == true) && (task.getCurrentQuality() == 0)) 
       return true;
     return false;
  }

  /***********************************************************
   *                     Drawing Junk                        *
   ***********************************************************/
  private transient Point     floc, tloc;

    /**
     * Calculates the height of the object, plus any subnodes.
     */
    public int calculateTreeHeight(FontMetrics fm) {
        return calculateHeight(fm);
    }

  /**
   */
  public void updateRect(Graphics g) {
    int x = (int)loc.x;
    int y = (int)loc.y;
    Dimension d = getSize(g);
    int w = d.width;
    int h = d.height;

    rect = new Rectangle(x - w/2, y - h/2, w, h);
  }


  /**
   * Returns the size of the node
   */
  public Dimension getSize(Graphics g) {
      g.setFont(mediumFont);
      Dimension d = super.getSize(g);
      g.setFont(normalFont);
      return d;
  }

    public boolean isVisible() {
        if (!super.isVisible())
            return false;
        if (getTo() != null && !getTo().isVisible())
            return false;
        if (getFrom() != null && !getFrom().isVisible())
            return false;
        return true;
    }

    /**
     * Paints any connection lines that need to be painted
     * @param g Where to draw them
     */
    private boolean lock = false;
    public void paintLines(Graphics g) {

        if (lock) return;
        lock = true;

        if (isVisible()) {
            int np = 0;
            float []px = new float[5];
            float []py = new float[5];

            g.setColor(Color.black);

            if (getFrom() != null) {
                if (getFrom().getLocation() == null) {
                    System.err.println("Warning, paintLines for ir " + getLabel() + " from node " + getFrom().getLabel() + " ran into null location");
                    return;
                }
                if (getFrom().getLocation().x == Integer.MIN_VALUE) {
                    System.err.println("Warning, paintLines for ir " + getLabel() + " from node " + getFrom().getLabel() + " ran into none location");
                    return;
                }

                px[np] = (float)getFrom().getLocation().x;
                py[np] = (float)getFrom().getLocation().y;
                np++;
                if (getTo().isSelected())
                    g.setColor(Color.blue);
                if (getFrom().isSelected())
                    g.setColor(Color.red);
                if (isSelected())
                    g.setColor(Color.blue);
            }

            if (getLocation() == null) {
                System.err.println("Warning, paintLines for ir " + getLabel() + " ran into null location");
                return;
            }

            px[np] = getLocation().x;
            py[np] = getLocation().y;
            np++;

            if (getTo() != null) {
                if (getTo().getLocation() == null) {
                    System.err.println("Warning, paintLines for ir " + getLabel() + " to node " + getTo().getLabel() + " ran into null location");
                    return;
                }
                if (getTo().getLocation().x == Integer.MIN_VALUE) {
                    System.err.println("Warning, paintLines for ir " + getLabel() + " to node " + getTo().getLabel() + " ran into none location");
                    return;
                }

                px[np] = (float)getTo().getLocation().x;
                py[np] = (float)getTo().getLocation().y;
                np++;
            }

            Spline spline = new Spline(px, py, np);
            spline.Generate();
            spline.draw(g);

            Point p1 = spline.getPoint((float)0.15);
            Point p2 = spline.getPoint((float)0.25);
            Arrow.drawArrowhead(g, p1.x, p1.y, p2.x, p2.y);
            p1 = spline.getPoint((float)0.75);
            p2 = spline.getPoint((float)0.85);
            Arrow.drawArrowhead(g, p1.x, p1.y, p2.x, p2.y);
        }

        lock = false;
    }

    /**
     * Paints the node
     */
    public void paint(Graphics g) {
        FontMetrics fm = g.getFontMetrics();
        updateRect(g);
            
        if (lock) return;
        lock = true;

        if (isVisible()) {
            // Paint it
            g.setColor(getBackground());
            g.fillRect(rect.x, rect.y, rect.width, rect.height);
            g.setColor(getForeground());
            g.drawRect(rect.x, rect.y, rect.width-1, rect.height-1);

            try {
                g.setFont(mediumFont);
                g.drawString(getLabel(), rect.x + 5, rect.y + 2 + fm.getAscent());
                g.setFont(normalFont);
            } catch (java.lang.NullPointerException e) {
                e.printStackTrace();
                System.err.println("Freaky string error.");
            }
        }

        lock = false;
    }

    /**
     * Get the foreground color
     */
    protected Color getForeground() {
				
        if (isActive() || Taems.printing) 
            return Color.black;
        else
            return Color.gray;
    }
}


