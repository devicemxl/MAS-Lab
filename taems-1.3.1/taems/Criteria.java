/* Copyright (C) 2005, University of Massachusetts, Multi-Agent Systems Lab
 * See LICENSE for license information
 */

/************************************************************
 * Criteria.java
 ************************************************************/

package taems;

/* Global imports */
import java.util.*;

/**
 * This class contains the criteria settings used by a scheduler
 * to control how it performs.  Note that some settings are 
 * interdependent (e.g. goodness cost, quality, duration) and
 * their settings must sum to some value.  In these cases, the
 * criteria object will automatically scale the other settings
 * when one it set in such a way that the total would not meet
 * expectations.
 */
public class Criteria extends Vector implements Cloneable {

	public static final int QUALITY_THRESHOLD = 0;
	public static final int COST_LIMIT = 1;
	public static final int DURATION_LIMIT = 2;
	public static final int QUALITY_CERTAINTY_THRESHOLD = 3;
	public static final int COST_CERTAINTY_THRESHOLD = 4;
	public static final int DURATION_CERTAINTY_THRESHOLD = 5;

	public static final int GOODNESS_QUALITY_SLIDER = 6;
	public static final int GOODNESS_COST_SLIDER = 7;
	public static final int GOODNESS_DURATION_SLIDER = 8;
	public static final int THRESHOLD_QUALITY_SLIDER = 9;
	public static final int THRESHOLD_COST_SLIDER = 10;
	public static final int THRESHOLD_DURATION_SLIDER = 11;
	public static final int UNCERTAINTY_QUALITY_SLIDER = 12;
	public static final int UNCERTAINTY_COST_SLIDER = 13;
	public static final int UNCERTAINTY_DURATION_SLIDER = 14;
	public static final int THRESHOLD_CERTAINTY_QUALITY_SLIDER = 15;
	public static final int THRESHOLD_CERTAINTY_COST_SLIDER = 16;
	public static final int THRESHOLD_CERTAINTY_DURATION_SLIDER = 17;
	public static final int META_GOODNESS_SLIDER = 18;
	public static final int META_THRESHOLD_SLIDER = 19;
	public static final int META_UNCERTAINTY_SLIDER = 20;
	public static final int META_UNCERTAINTY_THRESHOLD_SLIDER = 21;

	private static final int NUMCRIT = 22;

  protected String label = "unknown";
  protected int slidertotal = 1;

    /**
     * Normal constructor
     */
    public Criteria() {
	super(NUMCRIT);
	setSize(NUMCRIT);
    }

    /**
     * Depricated constructor
     * @depricated You should use the other constructor.
     */
    public Criteria(int a) {
	this();
    }

  /**
   * Accessors
   */
  public String getLabel() { return label; }
  public void setLabel(String l) { label = l; }

	/**
         * Total expected value for slider sets (typically 1 or
         * 100).  If set to zero or less, no totals are used,
         * so slider values remain independent of one another.
         */
	public void setSliderTotal(int st) { slidertotal = st; }
	public int getSliderTotal() { return slidertotal; }

    /**
     * Clones the criteria
     */
    public Object clone() {
        Criteria cloned = null;

        try {
            cloned = (Criteria)super.clone();
        } catch (Exception e) {System.out.println("Clone Error: " + e);}

        cloned.setLabel(new String(getLabel()));

        return cloned;
    }

    /**
     * Define the Accessor to this structure
     */
    /**
     * setGoodnessQuality
     * getGoodnessQuality
     */ 
    public void setGoodnessQuality(Float f) {
	setAndCheckValue(f, GOODNESS_QUALITY_SLIDER, GOODNESS_COST_SLIDER, GOODNESS_DURATION_SLIDER,-1);
    }
    public Float getGoodnessQuality() {
	return((Float)elementAt(GOODNESS_QUALITY_SLIDER));
    }

  /**
     * setGoodnessCost
     * getGoodnessCost
     */ 
    public void setGoodnessCost(Float f) {
	setAndCheckValue(f, GOODNESS_COST_SLIDER,GOODNESS_QUALITY_SLIDER,GOODNESS_DURATION_SLIDER,-1);
    }
    public Float getGoodnessCost() {
	return((Float)elementAt(GOODNESS_COST_SLIDER));
    }
    
    /**
     * setGoodnessDuration
     * getGoodnessDuration
     */ 
    public void setGoodnessDuration(Float f) {
	setAndCheckValue(f, GOODNESS_DURATION_SLIDER,GOODNESS_COST_SLIDER,GOODNESS_QUALITY_SLIDER,-1);
    }
    public Float getGoodnessDuration() {
	return((Float)elementAt(GOODNESS_DURATION_SLIDER));
    }
    
    /**
     * setThresholdQuality
     * getThresholdQuality
     */
    public void setThresholdQuality(Float f) {
	setAndCheckValue(f, THRESHOLD_QUALITY_SLIDER,THRESHOLD_COST_SLIDER,THRESHOLD_DURATION_SLIDER,-1);
    }
    public Float getThresholdQuality (){
	return((Float)elementAt(THRESHOLD_QUALITY_SLIDER));
    }
    
    /**
     * setThresholdCost
     * getThresholdCost
     */
    public void setThresholdCost(Float f) {
	setAndCheckValue(f, THRESHOLD_COST_SLIDER,THRESHOLD_QUALITY_SLIDER,THRESHOLD_DURATION_SLIDER,-1);
    }
    public Float getThresholdCost (){
	return((Float)elementAt(THRESHOLD_COST_SLIDER));
    }
    
    /**
     * setThresholdDuration
     * getThresholdDuration
     */
    public void setThresholdDuration(Float f) {
	setAndCheckValue(f, THRESHOLD_DURATION_SLIDER, THRESHOLD_COST_SLIDER, THRESHOLD_QUALITY_SLIDER,-1);
    }
    public Float getThresholdDuration (){
	return((Float)elementAt(THRESHOLD_DURATION_SLIDER));
    }
    
    /**
     * setQualityThreshold
     * getQualityThreshold
     */
    public void setQualityThreshold(Float f) {
	setElementAt(f, QUALITY_THRESHOLD);
    }
    public Float getQualityThreshold() {
	return((Float)elementAt(QUALITY_THRESHOLD));
    }
    
    /**
     * setCostThreshold
     * getCostThreshold
     */
    public void setCostThreshold(Float f) {
	setElementAt(f, COST_LIMIT);
    }
    public Float getCostThreshold() {
	return((Float)elementAt(COST_LIMIT));
    }
    
    /**
     * setDurationThreshold
     * getDurationThreshold
     */
    public void setDurationThreshold(Float f) {
	setElementAt(f, DURATION_LIMIT);
    }
    public Float getDurationThreshold() {
	return((Float)elementAt(DURATION_LIMIT));
    }

    /**
     * setUncertaintyQuality
     * getUncertaintyQuality
     */
    public void setUncertaintyQuality(Float f) {
	setAndCheckValue(f, UNCERTAINTY_QUALITY_SLIDER, UNCERTAINTY_COST_SLIDER, UNCERTAINTY_DURATION_SLIDER,-1);
    }
    public Float getUncertaintyQuality() {
	return((Float)elementAt(UNCERTAINTY_QUALITY_SLIDER));
    }
    
    /**
     * setUncertaintyCost
     * getUncertaintyCost
     */
    public void setUncertaintyCost(Float f) {
	setAndCheckValue(f, UNCERTAINTY_COST_SLIDER, UNCERTAINTY_QUALITY_SLIDER, UNCERTAINTY_DURATION_SLIDER,-1);
    }
    public Float getUncertaintyCost() {
	return((Float)elementAt(UNCERTAINTY_COST_SLIDER));
    }
    
    /**
     * setUncertaintyDuration
     * getUncertaintyDuration
     */
    public void setUncertaintyDuration(Float f) {
	setAndCheckValue(f, UNCERTAINTY_DURATION_SLIDER, UNCERTAINTY_QUALITY_SLIDER, UNCERTAINTY_COST_SLIDER,-1);
    }
    public Float getUncertaintyDuration() {
	return((Float)elementAt(UNCERTAINTY_DURATION_SLIDER));
    }
    
    /**
     * setThresholdCertaintyQuality
     * getThresholdCertaintyQuality
     */
    public void setThresholdCertaintyQuality(Float f) {
	setElementAt(f, THRESHOLD_CERTAINTY_QUALITY_SLIDER);
    }
    public Float getThresholdCertaintyQuality() {
	return((Float)elementAt(THRESHOLD_CERTAINTY_QUALITY_SLIDER));
    }
    
     /**
     * setThresholdCertaintyCost
     * getThresholdCertaintyCost
     */
    public void setThresholdCertaintyCost(Float f) {
	setElementAt(f, THRESHOLD_CERTAINTY_COST_SLIDER);
    }
    public Float getThresholdCertaintyCost() {
	return((Float)elementAt(THRESHOLD_CERTAINTY_COST_SLIDER));
    }
    
     /**
     * setThresholdCertaintyDuration
     * getThresholdCertaintyDuration
     */
    public void setThresholdCertaintyDuration(Float f) {
	setElementAt(f, THRESHOLD_CERTAINTY_DURATION_SLIDER);
    }
    public Float getThresholdCertaintyDuration() {
	return((Float)elementAt(THRESHOLD_CERTAINTY_DURATION_SLIDER));
    }
    
    /**
     * setQualityCertaintyThreshold
     * getQualityCertaintyThreshold
     */
    public void setQualityCertaintyThreshold(Float f) {
	setElementAt(f, QUALITY_CERTAINTY_THRESHOLD);
    }
    public Float getQualityCertaintyThreshold() {
	return((Float)elementAt(QUALITY_CERTAINTY_THRESHOLD));
    }
    
    /**
     * setCostCertaintyThreshold
     * getCostCertaintyThreshold
     */
    public void setCostCertaintyThreshold(Float f) {
	setElementAt(f, COST_CERTAINTY_THRESHOLD);
    }
    public Float getCostCertaintyThreshold() {
	return((Float)elementAt(COST_CERTAINTY_THRESHOLD));
    }
    
    /**
     * setDurationCertaintyThreshold
     * getDurationCertaintyThreshold
     */
    public void setDurationCertaintyThreshold(Float f) {
	setElementAt(f, DURATION_CERTAINTY_THRESHOLD);
    }
    public Float getDurationCertaintyThreshold() {
	return((Float)elementAt(DURATION_CERTAINTY_THRESHOLD));
    }
    
    /**
     * setMetaGoodness
     * getMetaGoodness
     */
    public void setMetaGoodness(Float f) {
	setAndCheckValue(f, META_GOODNESS_SLIDER,META_THRESHOLD_SLIDER,META_UNCERTAINTY_SLIDER,META_UNCERTAINTY_THRESHOLD_SLIDER);
    }
    public Float getMetaGoodness() {
	return((Float)elementAt(META_GOODNESS_SLIDER));
    }

    /**
     * setMetaThreshold
     * getMetaThreshold
     */
    public void setMetaThreshold(Float f) {
	setAndCheckValue(f, META_THRESHOLD_SLIDER,META_GOODNESS_SLIDER,META_UNCERTAINTY_SLIDER,META_UNCERTAINTY_THRESHOLD_SLIDER);
    }
    public Float getMetaThreshold() {
	return((Float)elementAt(META_THRESHOLD_SLIDER));
    }

    /**
     * setMetaUncertainty
     * getMetaUncertainty
     */
    public void setMetaUncertainty(Float f) {
	setAndCheckValue(f, META_UNCERTAINTY_SLIDER, META_GOODNESS_SLIDER,META_THRESHOLD_SLIDER,META_UNCERTAINTY_THRESHOLD_SLIDER);
    }
    public Float getMetaUncertainty() {
	return((Float)elementAt(META_UNCERTAINTY_SLIDER));
    }

    /**
     * setMetaUncertaintyThreshold
     * getMetaUncertaintyThreshold
     */
    public void setMetaUncertaintyThreshold(Float f) {
	setAndCheckValue(f, META_UNCERTAINTY_THRESHOLD_SLIDER, META_GOODNESS_SLIDER,META_THRESHOLD_SLIDER,META_UNCERTAINTY_SLIDER);
    }
    public Float getMetaUncertaintyThreshold() {
	return((Float)elementAt(META_UNCERTAINTY_THRESHOLD_SLIDER));
    }

    /**
     * Handles the setting of one value when it is part of a group
     * of three or four.  The initial slot will take the given value.
     * If the sum of the group's settings exceeds one, the remaining
     * two or three of the group will each be reduced (equally) untill
     * the sum equals 1.
     * <P>
     * If any of the set have not been assigned values yet, no reductions
     * will be performed.
     * @param value The value to set the slot to 
     * @param start The slot number to change
     * @param dependance1 The slot number of the first dependance
     * @param dependance2 The slot number of the second dependance
     * @param dependance1 The slot number of the third dependance (use
     *   -1 if no third dependance exists)
     */
    protected void setAndCheckValue(Float value, int start, int dependance1, 
				    int dependance2, int dependance3) {
	float valueDependance1, valueDependance2, valueDependance3;
	int numOfDependance = 2;

        if (getSliderTotal() <= 0) {
	   setElementAt(value, start);
	   return;
	}

	if (value.floatValue() > getSliderTotal())
	   value = new Float(getSliderTotal());
	setElementAt(value, start);

        if (elementAt(dependance1) == null)
           setElementAt(new Float(0), dependance1);
        if (elementAt(dependance2) == null)
           setElementAt(new Float(0), dependance2);
	if (dependance3 != -1) {
           if (elementAt(dependance3) == null)
              setElementAt(new Float(0), dependance3);
	}

	valueDependance1 =  ((Float)elementAt(dependance1)).floatValue();
	valueDependance2 =  ((Float)elementAt(dependance2)).floatValue();
	if (dependance3 != -1) {
	   valueDependance3 =  ((Float)elementAt(dependance3)).floatValue();
	   numOfDependance++;
	} else {
	   valueDependance3 = 0;
        }

	float total = value.floatValue() + valueDependance1 + valueDependance2 + valueDependance3;
	if (total > getSliderTotal()) {
	    if (dependance3 == -1) {
               float delta = (getSliderTotal() - value.floatValue()) / (valueDependance1 + valueDependance2);
	       setElementAt(new Float(valueDependance1 * delta), dependance1);
	       setElementAt(new Float(valueDependance2 * delta), dependance2);
	    } else {
               float delta = (getSliderTotal() - value.floatValue()) / (valueDependance1 + valueDependance2 + valueDependance3);
	       setElementAt(new Float(valueDependance1 * delta), dependance1);
	       setElementAt(new Float(valueDependance2 * delta), dependance2);
	       setElementAt(new Float(valueDependance3 * delta), dependance3);
            }
	}
    }

    /**
     * Returns the ttaems version of the criteria
     * @param v The version number output style to use
     */
    public String toTTaems(float v) {
	StringBuffer sb = new StringBuffer("");
	sb.append("(spec_evaluation_criteria\n");
	sb.append("   (label " + getLabel() + ")\n");
	sb.append("   (goodness_quality_slider "+elementAt(GOODNESS_QUALITY_SLIDER)+")\n");
	sb.append("   (goodness_cost_slider "+elementAt(GOODNESS_COST_SLIDER)+")\n");
	sb.append("   (goodness_duration_slider "+elementAt(GOODNESS_DURATION_SLIDER)+")\n");
	sb.append("   (threshold_quality_slider "+elementAt(THRESHOLD_QUALITY_SLIDER)+")\n");
	sb.append("   (threshold_cost_slider "+elementAt(THRESHOLD_COST_SLIDER)+")\n");
	sb.append("   (threshold_duration_slider "+elementAt(THRESHOLD_DURATION_SLIDER)+")\n");
	sb.append("   (quality_threshold "+elementAt(QUALITY_THRESHOLD)+")\n");
	sb.append("   (cost_limit "+elementAt(COST_LIMIT)+")\n");
	sb.append("   (duration_limit "+elementAt(DURATION_LIMIT)+")\n");
	sb.append("   (uncertainty_quality_slider "+elementAt(UNCERTAINTY_QUALITY_SLIDER)+")\n");
	sb.append("   (uncertainty_cost_slider "+elementAt(UNCERTAINTY_COST_SLIDER)+")\n");
	sb.append("   (uncertainty_duration_slider "+elementAt(UNCERTAINTY_DURATION_SLIDER)+")\n");
	sb.append("   (threshold_certainty_quality_slider "+elementAt(THRESHOLD_CERTAINTY_QUALITY_SLIDER)+")\n");
	sb.append("   (threshold_certainty_cost_slider "+elementAt(THRESHOLD_CERTAINTY_COST_SLIDER)+")\n");
	sb.append("   (threshold_certainty_duration_slider "+elementAt(THRESHOLD_CERTAINTY_DURATION_SLIDER)+")\n");
	sb.append("   (quality_certainty_threshold "+elementAt(QUALITY_CERTAINTY_THRESHOLD)+")\n");
	sb.append("   (cost_certainty_threshold "+elementAt(COST_CERTAINTY_THRESHOLD)+")\n");
	sb.append("   (duration_certainty_threshold "+elementAt(DURATION_CERTAINTY_THRESHOLD)+")\n");
	sb.append("   (meta_goodness_slider "+elementAt(META_GOODNESS_SLIDER)+")\n");
	sb.append("   (meta_threshold_slider "+elementAt(META_THRESHOLD_SLIDER)+")\n");
	sb.append("   (meta_uncertainty_slider "+elementAt(META_UNCERTAINTY_SLIDER)+")\n");
	sb.append("   (meta_uncertainty_threshold_slider "+elementAt(META_UNCERTAINTY_THRESHOLD_SLIDER)+")\n");
	sb.append(")\n");
	return sb.toString();
    }
}
