/* Copyright (C) 2005, University of Massachusetts, Multi-Agent Systems Lab
 * See LICENSE for license information
 */

/************************************************************
 * Resource.java
 ************************************************************/

package taems;

/* Global Includes */
import java.util.*;
import java.io.*;
import java.awt.*;

/**
 * Resource descriptor
 * <P>
 * You shouldn't have to instantiate one of these directly,
 * except when using them to find other matching nodes.
 */
public class Resource extends Node implements Serializable, Cloneable {
  protected double state = Double.NEGATIVE_INFINITY;
  protected double depleted_at = Double.NEGATIVE_INFINITY;
  protected double overloaded_at = Double.NEGATIVE_INFINITY;
  protected Agent agent = null;
  private Polygon poly;

  /**
   * Constructor
   * @param l The node's label
   * @param a The agent who manage the resource
   * @param s The current state of the resource
   * @param d The resource depletion level
   * @param o The resource overload level
   */
  public Resource(String l, Agent a, double s, double d, double o) {
    super(l);

    setAgent(a);
    setDepletedAt(d);
    setOverloadedAt(o);
    setState(s);
  }

  /**
   * Mostly Blank Constructor
   * @param l The node's label
   * @param a The agent who manage the resource
   */
  public Resource(String l, Agent a) {
    this (l, a, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
  }

  /**
   * Blank Constructor
   */
  public Resource() {
    this (null, null);
  }

  /**
   * Accessors
   */
  public double getState() { return state; }
  public void setState(double s) {
      if (getState() == s) return;

      state = s;
      updateStatus();

      fireNodeUpdateEvent(new NodeUpdateEvent(this, NodeUpdateEvent.VALUE));
  }
  public double getDepletedAt() { return depleted_at; }
  public void setDepletedAt(double d) {
      if (getDepletedAt() == d) return;

      depleted_at = d;
      updateStatus();

      fireNodeUpdateEvent(new NodeUpdateEvent(this, NodeUpdateEvent.VALUE));
  }
  public boolean isDepleted() {
      return (getState() < getDepletedAt());
  }
  public double getOverloadedAt() {
      return overloaded_at;
  }
  public void setOverloadedAt(double o) {
      if (getOverloadedAt() == o) return;

      overloaded_at = o;
      updateStatus();

      fireNodeUpdateEvent(new NodeUpdateEvent(this, NodeUpdateEvent.VALUE));
  }
  public boolean isOverloaded() {
      return (getState() > getOverloadedAt());
  }

    /**
     * Updates the status indicator
     */
    protected void updateStatus() {

      if (isDepleted()) {
          if (getState() == Double.NEGATIVE_INFINITY) {
              setStatus(DISABLED);
          } else {
              setStatus(UNDERLOADED);
          }
      } else if (isOverloaded()) {
          setStatus(OVERLOADED);
      } else {
          setStatus(NORMAL);
      }
    }

  /**
   * Determines if the given state value is within the resource's bounds.
   */
  public boolean isNormalState(double s) {

      if (s > getDepletedAt()) {
          if (s < getOverloadedAt()) {
              return true;
          } else if (Double.isInfinite(getOverloadedAt())) {
              return true;
          }
      }

      return false;
  }

  /**
   * Returns the ttaems version of the node
   * @param v The version number output style to use
   */
  public String toTTaems(float v) {
    StringBuffer sb = new StringBuffer("");
    Agent saveagent = getAgent();

    if (v < Taems.V1_1) {
        setAgent(null);
    }
    sb.append(super.toTTaems(v));
    if (v < Taems.V1_1) {
        setAgent(saveagent);
    }
    sb.append("   (state " + getState() + ")\n");
    sb.append("   (depleted_at " + getDepletedAt() + ")\n");
    sb.append(";  ** Status is: " + getStatus() + "\n");
    if (v >= Taems.V1_1) {
	/** Overloaded at should be in TAEMS V1.0 but DTC doesn't
            handle that very well, so I move it here for now 
	    remove it when DTC works with it = RV =  **/
	//      sb.append("   (overloaded_at " + getOverloadedAt() + ")\n");
	sb.append("   (overloaded_at " + getOverloadedAt() + ")\n");
      //if (agent != null) 
       //   sb.append("   (agent " + getAgent() + ")\n");
    }    
    return sb.toString();
  }

  /**
   * Determines if an object matches this one.
   * <BR>
   * This matches against:
   * <UL>
   * <LI> State
   * <LI> Depleted At
   * <LI> Overloaded At
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

        if (n instanceof Resource) {
            if (!matches(((Resource)n).getState(), getState())) return false;
            if (!matches(((Resource)n).getDepletedAt(), getDepletedAt())) return false;
            if (!matches(((Resource)n).getOverloadedAt(), getOverloadedAt())) return false;
        }

        return true;
    }

  /**
   * Copies the node's fields into the provided node.
   * @param n The node to copy stuff into
   */
    public void copy(Node n) {

        if (n instanceof Resource) {
            Resource r = (Resource)n;
            r.setState(getState());
            r.setDepletedAt(getDepletedAt());
            r.setOverloadedAt(getOverloadedAt());
        }

        super.copy(n);
    }

  /**
   * Clone me
   */
  public Object clone() {
    Resource cloned = null;

    try {
      cloned = (Resource)super.clone();
    } catch (Exception e) {
      System.out.println("Clone Error: " + e);
    }

    cloned.setState(getState());
    cloned.setDepletedAt(getDepletedAt());
    cloned.setOverloadedAt(getOverloadedAt());

    return cloned;
  }

  /***********************************************************
   *                     Drawing Junk                        *
   ***********************************************************/

  public Rectangle getBounds() { 
      if (poly != null)
          return poly.getBounds();
      else
          return null;
  }

  /**
   */
  public boolean contains(Point p) {

    if (isHidden()) return false;

    if (poly != null)
      return poly.contains(p);
    else
      return false;
  }

    /**
     * Updates the object's bounds
     */
    public void updateBounds(FontMetrics fm) {
        int x = (int)loc.x;
        int y = (int)loc.y;
        int w = calculateWidth(fm);
        int h = calculateHeight(fm);
        
        // Calc the triangle
        poly = new Polygon();
        poly.addPoint(loc.x - w/2, loc.y - h/2);
        poly.addPoint(loc.x + w/2, loc.y - h/2);
        poly.addPoint(loc.x, loc.y + h/2);
    }
    
  /**
   */
  public void paint(Graphics g) {
      g.setFont(normalFont);
      FontMetrics fm = g.getFontMetrics();
      int x = (int)loc.x;
      int y = (int)loc.y;
      int w = calculateWidth(fm);
      int h = calculateHeight(fm);
      updateBounds(fm);
      
      if (! isHidden()) {
          // Draw it
          g.setColor(getBackground());
          g.fillPolygon(poly);

          double s = getState();
          double o = getOverloadedAt();
          double d = getDepletedAt();
          if ((s != Double.NEGATIVE_INFINITY) && (o != Double.NEGATIVE_INFINITY)) {
              if (d == Double.NEGATIVE_INFINITY) d = 0;
              Rectangle bounds = poly.getBounds();
              int l = Math.min((int)Math.round(bounds.height * ((s - d) / (o - d))), bounds.height);
              l = Math.max(l, 0);

              double tx1 = bounds.x;
              double ty1 = bounds.y;
              double tx2 = bounds.x + bounds.width / 2;
              double ty2 = bounds.y + bounds.height;

              double lx1 = bounds.x;
              double ly1 = bounds.y + bounds.height - l;
              double lx2 = bounds.x + bounds.width;
              double ly2 = bounds.y + bounds.height - l;

              double tb = (ty2 - ty1) / (tx2 - tx1);
              double lb = (ly2 - ly1) / (lx2 - lx1);

              double ta = ty1 - tb * tx1;
              double la = ly1 - lb * lx1;

              int x1 = (int)Math.round(- (ta - la) / (tb - lb));
              int x2 = (int)Math.round(tx2 + (tx2 - x1));
              int yi = (int)Math.round(ta + tb * x1);

              if (isSelected())
                  g.setColor(getBackground());
              else
                  g.setColor(Color.pink);

              Polygon p = new Polygon();
              p.addPoint(x1, yi);
              p.addPoint(x2, yi);
              p.addPoint((int)tx2, (int)ty2);
              g.fillPolygon(p);
          }

          g.setColor(Color.black);
          g.drawPolygon(poly);
          String str = getLabel();
          while (fm.stringWidth(str) > (w-10)) {
              str = str.substring(0,str.length()-1);
          }
          try {
              g.drawString(str, x - fm.stringWidth(str)/2, (y - (h-4)/2) + fm.getAscent());

              g.setFont(smallFont);
              fm = g.getFontMetrics();

              if ((s != Double.NEGATIVE_INFINITY) && (o != Double.NEGATIVE_INFINITY)) {
                  String num = String.valueOf(getDepletedAt() + " / " + getState() + " / " + getOverloadedAt());
                  if (Taems.printing)
                    g.setColor(Color.black);
                  else
                    g.setColor(Color.gray);
                  g.drawString(num, x - fm.stringWidth(num)/2, loc.y + h/2 + V_MARGIN + fm.getAscent());
              }

          } catch (java.lang.NullPointerException e) {
              e.printStackTrace();
              System.err.println("Error drawing label");
          }
      }

		g.setFont(normalFont);

      
    // Draw Interrelationships
    Enumeration e = getOutInterrelationships();
    while (e.hasMoreElements()) {
        Node n = (Node)e.nextElement();
        n.paint(g);
    }
  }

  /**
   * Calculates the width of the object.
   */
  public int calculateWidth(FontMetrics fm) {

      return 70;
  }

  /**
   * Calculates the height of the object.
   */
  public int calculateHeight(FontMetrics fm) {

      return 60;
  }
}


