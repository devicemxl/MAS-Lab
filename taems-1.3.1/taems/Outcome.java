/* Copyright (C) 2005, University of Massachusetts, Multi-Agent Systems Lab
 * See LICENSE for license information
 */

/************************************************************
 * Outcome.java
 ************************************************************/

package taems;

/* Global Includes */
import java.util.*;
import java.io.*;
import java.awt.*;

import utilities.Distribution;

/**
 * Outcome class
 */
public class Outcome implements Serializable, Cloneable {
  protected String label;
  protected Distribution quality;
  protected Distribution duration;
  protected Distribution cost;
  protected float density = (float)1.0;

  /**
   * Default constructor
   */
  public Outcome(String l, Distribution q, Distribution d, Distribution c, float dens) {
    label = l;
    quality = q;
    duration = d;
    cost = c;
    density = dens; 
  }

  /**
   * Default constructor
   */
  public Outcome(String l) {
    this(l, null, null, null, (float)1.0);
  }

  /**
   * Accessors
   */
  public String getLabel() { return label; }
  public void setLabel(String l) { label = l; }
  public Distribution getDuration() { return duration; }
  public void setDuration(Distribution d) { duration = d; }
  public Distribution getQuality() { return quality; }
  public void setQuality(Distribution q) { quality = q; }
  public Distribution getCost() { return cost; }
  public void setCost(Distribution c) { cost = c; }
  public float getDensity() { return density; }
  public void setDensity(float d) { density = d; }

  /**
   * Returns the ttaems version of the node
   * @param v The version number output style to use
   */
  public String toTTaems(float v) {
    StringBuffer sb = new StringBuffer("      (" + label + "\n");

      sb.append("         (density " + density + ")\n");
    if (quality != null)
      sb.append("         (quality_distribution " + quality.output() + ")\n");
    if (duration != null)
      sb.append("         (duration_distribution " + duration.output() + ")\n");
    if (cost != null)
      sb.append("         (cost_distribution " + cost.output() + ")\n");
    sb.append("      )\n");
    
    return sb.toString();
  }

  /**
   * Stringify
   */
  public String toString() {
    return toTTaems(Taems.VCUR);
  }

  /**
   * Determines if an object matches this one.
   * <BR>
   * This matches against:
   * <UL>
   * <LI> Label
   * <LI> Duration
   * <LI> Quality
   * <LI> Cost
   * <LI> Density
   * </UL>
   * @see Node#matches
   */
  public boolean matches(Outcome o) {

      if (o != null) {
          if (!Node.matches(o.getLabel(), getLabel())) return false;
          if (!Node.matches(o.getDuration(), getDuration())) return false;
          if (!Node.matches(o.getQuality(), getQuality())) return false;
          if (!Node.matches(o.getCost(), getCost())) return false;
          if (!Node.matches(o.getDensity(), getDensity())) return false;
      }

      return true;
  }

  /**
   * Clone
   */
  public Object clone() {
    Outcome cloned = null;

    try {
      cloned = (Outcome)super.clone();
    } catch (Exception e) {
      System.out.println("Clone Error: " + e);
    }

    if (getLabel() != null)
      cloned.setLabel(new String(getLabel()));
    else
      cloned.setLabel(null);

    if (duration != null)
      cloned.setDuration((Distribution)duration.clone());
    else
      cloned.setDuration(null);

    if (quality != null)
      cloned.setQuality((Distribution)quality.clone());
    else
      cloned.setQuality(null);

    if (cost != null)
      cloned.setCost((Distribution)cost.clone());
    else
      cloned.setCost(null);

    if (density != Float.NEGATIVE_INFINITY)
      cloned.setDensity(density);
    else
      cloned.setDensity(density);

    return cloned;
  }

}

    
