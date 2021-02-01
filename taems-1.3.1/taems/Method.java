/* Copyright (C) 2005, University of Massachusetts, Multi-Agent Systems Lab
 * See LICENSE for license information
 */

/************************************************************
 * Method.java
 ************************************************************/

package taems;

/* Global Includes */
import java.util.*;
import java.io.*;
import java.awt.*;

import utilities.Distribution;
import utilities.TaemsRandom;

/**
 * Method represent actual executable actions.  They consist
 * of expected timing information and a list of possible outcomes.
 */
public class Method extends TaskBase implements Serializable, Cloneable {
  Vector outcomes = new Vector();
  protected int start_time = Integer.MIN_VALUE;
  protected int finish_time = Integer.MIN_VALUE;
  protected int accrued_time = Integer.MIN_VALUE;
  protected boolean showqcds = false;
  protected int earlieststarttime = Integer.MIN_VALUE;
  protected int lateststarttime = Integer.MIN_VALUE;
  protected int earliestfinishtime = Integer.MAX_VALUE;
  protected int latestfinishtime = Integer.MAX_VALUE;

    /**
     * placement_preference constants
     */
    public static final String EARLIER_WEAK = "earlier_weak";
    public static final String EARLIER_STRONG = "earlier_strong";
    public static final String LATER_WEAK = "later_weak";
    public static final String LATER_STRONG = "later_strong";

  /**
   * Constructor
   * @param l The method label
   * @param a The acting agent
   * @param at Arrival time
   * @param est Earliest start time
   * @param dl Deadline
   * @param s Start time
   * @param f Finish time
   * @param ac Accrued time
   */
  public Method(String l, Agent a, int at, int est, int dl, int s, int f, int ac) {
    super(l, a, at, est, dl);

    start_time = s;
    finish_time = f;
    accrued_time = ac;
  }

  /**
   * Simple constructor
   */
  public Method(String l, Agent a) {
    this(l, a, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
  }

  /**
   * Blank Constructor
   */
  public Method() {
    this (null, null);
  }

  /**
   * Accessors
   */
  public boolean getShowQCDs() { return showqcds; }
  public void setShowQCDs(boolean s) {
      showqcds = s; 
      fireNodeUpdateEvent(new NodeUpdateEvent(this, NodeUpdateEvent.GRAPHIC));
  }
  public int getStartTime() { return start_time; }
  public void setStartTime(int s) { start_time = s; super.setStartTime(s); }
  public int getFinishTime() { return finish_time; }
  public void setFinishTime(int f) { finish_time = f; super.setFinishTime(f); }
  
  public int getActualEarliestStartTime() { 
	  return earlieststarttime; }
  public void setActualEarliestStartTime(int e) { 
      earlieststarttime = e; 
      earliestfinishtime = e + (int)getGlobalOutcome().getDuration().calculateMin();
  }

    public void setActualEarliestFinishTime(int e) {
	earliestfinishtime = e;
	earlieststarttime = e - (int)getGlobalOutcome().getDuration().calculateMin();
    }
    
    public int getActualEarliestFinishTime() { return earliestfinishtime; }
    public int getActualLatestStartTime() { return lateststarttime; }
    public int getActualLatestFinishTime() { 
	  return latestfinishtime; }
    
    public void setActualLatestFinishTime(int time) {
	latestfinishtime = time;
	lateststarttime = time - (int)getGlobalOutcome().getDuration().calculateMax();
    }

    
    public void setActualLatestStartTime(int time) { 
	lateststarttime = time;
	latestfinishtime = time + (int)getGlobalOutcome().getDuration().calculateMax();
    }
    
    public void resetActualTiming() {
	lateststarttime = Integer.MIN_VALUE;
	latestfinishtime = Integer.MAX_VALUE;
	earlieststarttime = Integer.MIN_VALUE;
	earliestfinishtime = Integer.MAX_VALUE;
    }

  public int getAccruedTime() { return accrued_time; }
  public void setAccruedTime(int a) { accrued_time = a; }
  public float getMaximumQuality() {
    float maximumQuality;
    if (hasAttribute("max_quality"))
      return ((Float)getAttribute("max_quality")).floatValue();
    else {
      Outcome c = getGlobalOutcome();
      Distribution dQ = c.quality;
      maximumQuality = dQ.calculateMax();
      setMaximumQuality(maximumQuality);
    }
    return(maximumQuality);
  }

  /**
   * Attribute based accessors
   */
  public int getScheduleNumber() {
      if (hasAttribute("sched_num")) {
          Object n = getAttribute("sched_num");
          if (n instanceof Integer)
              return ((Integer)n).intValue();
          else
              return Integer.parseInt(n.toString());
      }
      return -1;
  }
  public void setScheduleNumber(int s) {
    setAttribute("sched_num", new Integer(s));
  }
  public boolean hasScheduleNumber() { return (getScheduleNumber() != -1); }
    public void setPlacementPreference(String s) { setAttribute("placement_preference", s); }
    public String getPlacementPreference() { return (String)getAttribute("placement_preference"); }
    public void setWeightingFactor(float f) { setAttribute("weighting_factor", new Float(f)); }
    public float getWeightingFactor() { return ((Float)getAttribute("weighting_factor")).floatValue(); }
    public void setTimeToScheduleFor(float f) { setAttribute("time_to_schedule_for", new Float(f)); }
    public float getTimeToScheduleFor() { return ((Float)getAttribute("time_to_schedule_for")).floatValue(); }

  /**
   * Adds an outcome to this method.
   */
  public void addOutcome(Outcome o) {
    outcomes.addElement(o);
    fireNodeUpdateEvent(new NodeUpdateEvent(this, NodeUpdateEvent.VALUE));
  }

  /**
   * Removes an outcome from this node.
   */
  public void removeOutcome(Outcome o) {
    outcomes.removeElement(o);
    fireNodeUpdateEvent(new NodeUpdateEvent(this, NodeUpdateEvent.VALUE));
  }

  /**
   * Removes all the outcomes
   */
  public void removeAllOutcomes() {
    outcomes.removeAllElements();
  }

  /**
   * Returns a list of the outcomes.
   */
  public Enumeration getOutcomes() {
    return outcomes.elements();
  }

  /**
   * Returns the number outcomes.
   */
  public int numOutcomes() {
    return outcomes.size();
  }

  /**
   * Choose an Outcome depending of the TaemsRandom generator.
   * This function apply also all the NLEs that affected this
   * method outcomes and returns it.
   * @param complex, flag to use or not the quality of the
   *   originator to compute the effects of the NLEs
   * @param tr, is the Taems Random generator.
   * @return the choosen Outcome with all NLE applyed.
   */
  public Outcome chooseOutcome(boolean complex, TaemsRandom tr) {
    Enumeration e;
    float[] dist;
    int count=0;
    int i=0;
    Outcome o;

    e = getOutcomes();
    if (numOutcomes() != 0) 
      if (numOutcomes() != 1) {
	dist = new float [numOutcomes()*2];
	while (e.hasMoreElements() ) {
	  dist[count] = ((Outcome)e.nextElement()).getDensity();
	  dist[count+1] = i;
	  count = count +2;
	  i = i+1;
	}
      
	/* Set distribution */
	tr.setDistribution(dist);
	/* Get a value */
	float choice = tr.nextValue();
	tr.unsetDistribution();
	/* Get the outcome and return it */
	o= (Outcome)(outcomes.elementAt((int) choice));
      }
      else
	{
	  o = (Outcome)(outcomes.elementAt(0));
	}
    
    else  
      return null; 
    return(applyNLEsOnOutcome(o, complex, tr));
  }

  /**
   * Apply all the affecting NLE on this outcome.
   */ 
  protected Outcome applyNLEsOnOutcome(Outcome o, boolean complex, 
				       TaemsRandom tr) {
    Enumeration e ;
    Outcome outcome = (Outcome)o.clone();
    e=new SortedEnumerationByTimeStamp(getAffectingInterrelationships());
    
    while (e.hasMoreElements()) {
      Interrelationship NLE = (Interrelationship)e.nextElement();
      outcome = NLE.applyNLEonOutcome(outcome, complex, tr);
    }
    
    return outcome;
  }

  /**
   * Looks for an outcome with the given label
   * @param l The outcome name to look for
   * @return The outcome, or null if not found
   */
  public Outcome getOutcome(String s) {
    Enumeration e = getOutcomes();

    while (e.hasMoreElements()) {
      Outcome o = (Outcome)e.nextElement();
      if (o.getLabel().equalsIgnoreCase(s))
	return o;
    }

    return null;
  }

  /**
   * Compact all the outcomes of one method in one
   * global one.
   * @return The global outcome, or null if no outcomes are found
   */
    public Outcome getGlobalOutcome() {
        float[] results = new float[6];
        Distribution 
            dQ = new Distribution(), 
            dC = new Distribution(),
            dD = new Distribution();
    
        if (numOutcomes() > 0) { 
            Enumeration e = outcomes.elements();
            while (e.hasMoreElements() ) {
                Outcome out = ((Outcome)e.nextElement());
                dD = dD.appendDistribution(out.getDuration().applyDensity(out.getDensity()));
                dQ = dQ.appendDistribution(out.getQuality().applyDensity(out.getDensity()));
                dC = dC.appendDistribution(out.getCost().applyDensity(out.getDensity()));
            }
        } else {
            return null;
        }

        Outcome o = (Outcome)(outcomes.elementAt(0));
        o = (Outcome)o.clone();
        o.setDuration(dD);
        o.setCost(dC);
        o.setQuality(dQ);
        o.setDensity(1);
        return(o);
    }

  /**
   * Returns the outcome with the highest density.
   * @return The most likely outcome, or null if not found
   */
  public Outcome getMostLikelyOutcome() {
    Enumeration e = getOutcomes();
    Outcome o = null;

    while (e.hasMoreElements()) {
      Outcome to = (Outcome)e.nextElement();
      if ((o == null) || (to.getDensity() > o.getDensity())) 
	o = to;
    }

    return o;
  }
  
  /**
   * Update the NLE thru all the Taems tree. 
   */
  public void updateNLEs(int time) {
    Enumeration e = getAffectedInterrelationships();
    while (e.hasMoreElements()) {
      ((Interrelationship)e.nextElement()).update(time);
    }
  }
  

  /**
   * Returns the ttaems version of the node
   * @param v The version number output style to use
   */
  public String toTTaems(float v) {
    StringBuffer sb = new StringBuffer("");
    
    sb.append("(spec_method\n");
    sb.append(super.toTTaems(v));
    if (earlieststarttime != Integer.MIN_VALUE)
      sb.append(";        (est "+earlieststarttime+")\n");
    if (lateststarttime != Integer.MIN_VALUE)
      sb.append(";        (lst "+lateststarttime+")\n");
    if (earliestfinishtime != Integer.MAX_VALUE)
	sb.append(";        (eft "+earliestfinishtime+")\n");
    if (latestfinishtime != Integer.MAX_VALUE)
	sb.append(";        (lft "+latestfinishtime+")\n");
    if (numOutcomes() > 0) {
      sb.append("   (outcomes\n");
      Enumeration e = getOutcomes();
      while(e.hasMoreElements()) {
	Outcome o = (Outcome)e.nextElement();
	sb.append(o.toTTaems(v));
      }
      sb.append("   )\n");
    } else {
      System.err.println("Warning: Method has no outcomes");
    }

    if (start_time != Integer.MIN_VALUE)
      sb.append("   (start_time " + start_time + ")\n");
    if (finish_time != Integer.MIN_VALUE)
      sb.append("   (finish_time " + finish_time + ")\n");
    if (accrued_time != Integer.MIN_VALUE)
      sb.append("   (accrued_time " + accrued_time + ")\n");

    sb.append(")\n");

    return sb.toString();
  }

  /**
   * Determines if an object matches this one.
   * <BR>
   * This matches against:
   * <UL>
   * <LI> Start time
   * <LI> Finish time
   * <LI> Accrued time
   * <LI> Nonlocal-ness
   * <LI> Outcomes (empty outcomes vector is wildcard) - searches
   * for a 1:1 match for each outcome in the match method.
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

    if (n instanceof Method) {
        if (!matches(((Method)n).getStartTime(), getStartTime())) return false;
        if (!matches(((Method)n).getFinishTime(), getFinishTime())) return false;
        if (!matches(((Method)n).getAccruedTime(), getAccruedTime())) return false;
        if (!matches(((Method)n).nonlocal, nonlocal)) return false;

        if (((Method)n).numOutcomes() > 1) {
            if (((Method)n).numOutcomes() != numOutcomes()) return false;
            Enumeration me = ((Method)n).getOutcomes();
            while (me.hasMoreElements()) {
                Outcome mo = (Outcome)me.nextElement();
                boolean found = false;
                Enumeration oe = getOutcomes();
                while (oe.hasMoreElements()) {
                    Outcome oo = (Outcome)oe.nextElement();
                    if (oo.matches(mo)) {
                        found = true;
                        break;
                    }
                }
                if (!found) return false;
            }
        }
    }

    return true;
  }

  /**
   * Clone.
   */
  public Object clone() {
    Method cloned = null;

    try {
      cloned = (Method)super.clone();
    } catch (Exception e) {System.out.println("Clone Error: " + e);}

    cloned.setStartTime(getStartTime());
    cloned.setFinishTime(getFinishTime());
    cloned.setAccruedTime(getAccruedTime());

    cloned.outcomes = new Vector();
    Enumeration e = getOutcomes();
    while (e.hasMoreElements()) {
      Outcome o = (Outcome)e.nextElement();
      cloned.addOutcome((Outcome)o.clone());
    }

    return cloned;
  }

  /**
   * Copies the node's fields into the provided node.
   * Does not do anything with endpoints.
   * @param n The node to copy stuff into
   */
    public void copy(Node n) {

        if (n instanceof Method) {
            Method m = (Method)n;
            
            m.setStartTime(getStartTime());
            m.setFinishTime(getFinishTime());
            m.setAccruedTime(getAccruedTime());

            m.outcomes = new Vector();
            Enumeration e = getOutcomes();
            while (e.hasMoreElements()) {
                Outcome o = (Outcome)e.nextElement();
                m.addOutcome((Outcome)o.clone());
            }
        }

        super.copy(n);
    }
  
  /***********************************************************
   *                     Drawing Junk                        *
   ***********************************************************/

    Rectangle bigbounds = new Rectangle(0,0,0,0);
    public Rectangle getBounds() {
        return bigbounds;
    }

    /**
     */
    public void paint(Graphics g) {

        super.paint(g);
				
        if (isVisible()) {
            // Draw label
            g.setFont(smallFont);
            FontMetrics fm = g.getFontMetrics();
            try {
                bigbounds.setBounds(rect);

                if ((status == COMPLETED) || (getCurrentQuality() != 0)) {
                    String num = String.valueOf(getCurrentQuality());
                    g.setColor(Color.red);
                    g.drawString(num, rect.x - (fm.stringWidth(num) + H_MARGIN), rect.y + rect.height - (V_MARGIN + fm.getDescent()));

                }

                if (getShowQCDs()) {
                    g.setColor(Color.black);
                    if (numOutcomes() > 0) {
                        StringBuffer sb = new StringBuffer("");
                        Enumeration e = getOutcomes();
                        while(e.hasMoreElements()) {
                            Outcome o = (Outcome)e.nextElement();
                            sb.append(o.getLabel() + " (" + (o.getDensity() * 100) + "%)\n");
                            sb.append("Q: " + o.getQuality() + "\n");
                            sb.append("C: " + o.getCost() + "\n");
                            sb.append("D: " + o.getDuration() + "\n");
                            sb.append("\n");
                        }

                        char str[] = sb.toString().toCharArray();
                        int y = rect.y + rect.height + V_MARGIN + fm.getAscent();
                        int h = -1;
                        for (int i = 0; i < str.length; ) {
                            int next;
                            for (next = i + 1; (next < str.length) && (str[next] != '\n'); next++) { }
                            String piece = new String(str, i, next - i);
                            if (piece.startsWith("\n")) piece = piece.substring(1, piece.length());
                            if (h < 0) h = fm.stringWidth(piece) / 2;
                            g.drawString(piece, getLocation().x - h, y);
                            y += V_MARGIN + fm.getDescent() + fm.getAscent();
                            i = next;
                        }
                        bigbounds.height = y - bigbounds.y;
                        bigbounds.x = Math.min(bigbounds.x, getLocation().x - h);
                        bigbounds.width = Math.max(bigbounds.width, h * 2);
                    }
                }

                if (hasScheduleNumber()) {
                    String num = String.valueOf(getScheduleNumber());
                    g.setColor(Color.gray);
                    if (getShowQCDs()) {
                        g.drawString(num, rect.x + rect.width + H_MARGIN, rect.y + rect.height - (V_MARGIN + fm.getDescent()));
                        bigbounds.width += H_MARGIN + fm.stringWidth(num);
                    } else {
                        g.drawString(num, getLocation().x - fm.stringWidth(num)/2, rect.y + rect.height + V_MARGIN + fm.getAscent());
                        bigbounds.height += V_MARGIN + fm.getAscent();
                    }
                }
	
            } catch (java.lang.NullPointerException ex) {
                System.err.println("Error drawing label");
                ex.printStackTrace();
            }
        }
        g.setFont(normalFont);
    }

  /**
   * Get the background color
   */
  protected Color getBackground() {

      if (hasScheduleNumber() && (getStatus() == NORMAL) && (!isSelected()))
          return Color.cyan;
      else return super.getBackground();
  }
}







