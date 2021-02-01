package utilities;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

public class Distribution extends Vector implements Serializable {
  public Distribution() {}
  
  public Distribution(Float Value, Float float_1) {
    this();
    appendTerm(Value, float_1);
  }
  
  public Distribution(float Value, float f1) {
    this(new Float(Value), new Float(f1));
  }
  
  public Distribution(double Value, double d1) {
    this((float)Value, (float)d1);
  }
  
  public Distribution(String str) {
    str = str.trim();
    if (str.startsWith("("))
      str = str.substring(1, str.length()); 
    if (str.endsWith(")"))
      str = str.substring(0, str.length() - 1); 
    str = str.trim();
    StringTokenizer tok = new StringTokenizer(str);
    while (tok.hasMoreElements()) {
      Float v = Float.valueOf(tok.nextToken());
      Float d = Float.valueOf(tok.nextToken());
      appendTerm(v, d);
    } 
  }
  
  public float[] getArray() {
    float[] distribution = new float[size()];
    int counter = 0;
    Enumeration e = elements();
    while (e.hasMoreElements()) {
      distribution[counter + 1] = ((Float)e.nextElement()).floatValue();
      distribution[counter] = ((Float)e.nextElement()).floatValue();
      counter += 2;
    } 
    return distribution;
  }
  
  public Hashtable getHashtable() {
    Hashtable ht = new Hashtable();
    Enumeration e = elements();
    while (e.hasMoreElements())
      ht.put((Float)e.nextElement(), (Float)e.nextElement()); 
    return ht;
  }
  
  public float findValue(float v) {
    Enumeration e = elements();
    while (e.hasMoreElements()) {
      float value = ((Float)e.nextElement()).floatValue();
      float prob = ((Float)e.nextElement()).floatValue();
      if (value == v)
        return prob; 
    } 
    return 0.0F;
  }
  
  public float findClosestValue(float v) {
    Enumeration e = elements();
    float closestvalue = Float.POSITIVE_INFINITY;
    float closestprob = Float.NEGATIVE_INFINITY;
    while (e.hasMoreElements()) {
      float value = ((Float)e.nextElement()).floatValue();
      float prob = ((Float)e.nextElement()).floatValue();
      if (Math.abs(value - v) < Math.abs(closestvalue - v)) {
        closestvalue = value;
        closestprob = prob;
        continue;
      } 
      if (Math.abs(value - v) == Math.abs(closestvalue - v) && 
        prob > closestprob) {
        closestvalue = value;
        closestprob = prob;
      } 
    } 
    return closestvalue;
  }
  
  public boolean containsValue(float v, float t) {
    Enumeration e = elements();
    while (e.hasMoreElements()) {
      float value = ((Float)e.nextElement()).floatValue();
      if (value - t <= v && value + t >= v)
        return true; 
    } 
    return false;
  }
  
  public float calculateMin() {
    Enumeration e = elements();
    float min = ((Float)elementAt(0)).floatValue();
    while (e.hasMoreElements()) {
      float cur = ((Float)e.nextElement()).floatValue();
      if (min > cur)
        min = cur; 
      cur = ((Float)e.nextElement()).floatValue();
    } 
    return min;
  }
  
  public float calculateMax() {
    Enumeration e = elements();
    float max = ((Float)elementAt(0)).floatValue();
    while (e.hasMoreElements()) {
      float cur = ((Float)e.nextElement()).floatValue();
      if (max < cur)
        max = cur; 
      cur = ((Float)e.nextElement()).floatValue();
    } 
    return max;
  }
  
  public float calculateAvg() {
    Enumeration e = elements();
    float avg = 0.0F;
    while (e.hasMoreElements()) {
      float value = ((Float)e.nextElement()).floatValue();
      float prob = ((Float)e.nextElement()).floatValue();
      avg += value * prob;
    } 
    return avg;
  }
  
  public float calculateMostLikely() {
    Enumeration e = elements();
    float mvalue = 0.0F, mprob = 0.0F;
    while (e.hasMoreElements()) {
      float value = ((Float)e.nextElement()).floatValue();
      float prob = ((Float)e.nextElement()).floatValue();
      if (prob > mprob) {
        mprob = prob;
        mvalue = value;
      } 
    } 
    return mvalue;
  }
  
  public float calculateDeviation() {
    float mean = calculateAvg();
    Enumeration e = elements();
    float somme = 0.0F, somme_prob = 0.0F;
    while (e.hasMoreElements()) {
      float value = ((Float)e.nextElement()).floatValue();
      float prob = ((Float)e.nextElement()).floatValue();
      somme += prob * value * value;
      somme_prob = prob + somme_prob;
    } 
    float result = (float)Math.sqrt((somme / somme_prob - mean * mean));
    return result;
  }
  
  public float calculateTotalDensity() {
    float prob = 0.0F;
    for (int i = 0; i < size(); i++) {
      i++;
      prob += ((Float)elementAt(i)).floatValue();
    } 
    return prob;
  }
  
  public Distribution applyDensity(float density) {
    Distribution d = (Distribution)clone();
    for (int i = 0; i < d.size(); i++) {
      i++;
      float prob = ((Float)d.elementAt(i)).floatValue();
      d.setElementAt((E)new Float(prob * density), i);
    } 
    return d;
  }
  
  public Distribution applyPower(float power) {
    Distribution d = (Distribution)clone();
    for (int i = 0; i < d.size(); i++) {
      float value = ((Float)d.elementAt(i)).floatValue();
      d.setElementAt((E)new Float(value * power), i);
      i++;
    } 
    return d;
  }
  
  public Distribution applyOffset(float offset) {
    Distribution d = (Distribution)clone();
    for (int i = 0; i < d.size(); i++) {
      float value = ((Float)d.elementAt(i)).floatValue();
      d.setElementAt((E)new Float(value + offset), i);
      i++;
    } 
    return d;
  }
  
  public void appendTerm(Float v, Float d) {
    addElement((E)v);
    addElement((E)d);
  }
  
  public void appendTerm(float v, float d) {
    addElement((E)new Float(v));
    addElement((E)new Float(d));
  }
  
  public void appendTerm(double v, double d) {
    addElement((E)new Float(v));
    addElement((E)new Float(d));
  }
  
  public Distribution appendDistribution(Distribution d) {
    Distribution d1 = (Distribution)clone();
    Enumeration e = d.elements();
    while (e.hasMoreElements()) {
      Float value = (Float)e.nextElement();
      Float prob = (Float)e.nextElement();
      d1.insertElementAt((E)prob, 0);
      d1.insertElementAt((E)value, 0);
    } 
    return d1;
  }
  
  public static Distribution computeJointDistribution(Vector v) {
    Distribution newd = new Distribution();
    subComputeJointDistribution(0.0F, 1.0F, v, 0, newd);
    newd.compact();
    return newd;
  }
  
  public static Distribution computeJointDistribution(Distribution d1, Distribution d2) {
    Vector v = new Vector();
    v.addElement(d1);
    v.addElement(d2);
    return computeJointDistribution(v);
  }
  
  private static void subComputeJointDistribution(float v, float p, Vector dists, int index, Distribution output) {
    if (index < dists.size()) {
      Distribution d = dists.elementAt(index);
      Enumeration e = d.elements();
      while (e.hasMoreElements()) {
        float nv = ((Float)e.nextElement()).floatValue();
        float np = ((Float)e.nextElement()).floatValue();
        subComputeJointDistribution(v + nv, p * np, dists, index + 1, output);
      } 
    } else {
      output.appendTerm(new Float(v), new Float(p));
    } 
  }
  
  public static Distribution computeDifferenceJointDistribution(Vector v) {
    Distribution newd = new Distribution();
    subComputeDifferenceJointDistribution(0.0F, 1.0F, v, 0, newd);
    newd.compact();
    return newd;
  }
  
  public static Distribution computeDifferenceJointDistribution(Distribution d1, Distribution d2) {
    Vector v = new Vector();
    v.addElement(d1);
    v.addElement(d2);
    return computeDifferenceJointDistribution(v);
  }
  
  private static void subComputeDifferenceJointDistribution(float v, float p, Vector dists, int index, Distribution output) {
    if (index < dists.size()) {
      Distribution d = dists.elementAt(index);
      Enumeration e = d.elements();
      while (e.hasMoreElements()) {
        float v1, p1, nv = ((Float)e.nextElement()).floatValue();
        float np = ((Float)e.nextElement()).floatValue();
        if (v == 0.0D) {
          v1 = nv;
          p1 = np;
        } else {
          v1 = v - nv;
          p1 = p * np;
        } 
        subComputeDifferenceJointDistribution(v1, p1, dists, index + 1, output);
      } 
    } else {
      output.appendTerm(new Float(v), new Float(p));
    } 
  }
  
  public static Distribution computeMaxJointDistribution(Vector v) {
    Distribution newd = new Distribution();
    subComputeMaxJointDistribution(0.0F, 1.0F, v, 0, newd);
    newd.compact();
    return newd;
  }
  
  public static Distribution computeMaxJointDistribution(Distribution d1, Distribution d2) {
    Vector v = new Vector();
    v.addElement(d1);
    v.addElement(d2);
    return computeMaxJointDistribution(v);
  }
  
  private static void subComputeMaxJointDistribution(float v, float p, Vector dists, int index, Distribution output) {
    if (index < dists.size()) {
      Distribution d = dists.elementAt(index);
      Enumeration e = d.elements();
      while (e.hasMoreElements()) {
        float fv, nv = ((Float)e.nextElement()).floatValue();
        float np = ((Float)e.nextElement()).floatValue();
        if (v > nv) {
          fv = v;
        } else {
          fv = nv;
        } 
        subComputeMaxJointDistribution(fv, p * np, dists, index + 1, output);
      } 
    } else {
      output.appendTerm(new Float(v), new Float(p));
      output.compact();
    } 
  }
  
  public static Distribution computeMinJointDistribution(Vector v) {
    Distribution newd = new Distribution();
    subComputeMinJointDistribution(2.14748365E9F, 1.0F, v, 0, newd);
    newd.compact();
    return newd;
  }
  
  public static Distribution computeMinJointDistribution(Distribution d1, Distribution d2) {
    Vector v = new Vector();
    v.addElement(d1);
    v.addElement(d2);
    return computeMinJointDistribution(v);
  }
  
  private static void subComputeMinJointDistribution(float v, float p, Vector dists, int index, Distribution output) {
    if (index < dists.size()) {
      Distribution d = dists.elementAt(index);
      Enumeration e = d.elements();
      while (e.hasMoreElements()) {
        float fv, nv = ((Float)e.nextElement()).floatValue();
        float np = ((Float)e.nextElement()).floatValue();
        if (v < nv) {
          fv = v;
        } else {
          fv = nv;
        } 
        subComputeMinJointDistribution(fv, p * np, dists, index + 1, output);
      } 
    } else {
      output.appendTerm(new Float(v), new Float(p));
      output.compact();
    } 
  }
  
  public void compact() {
    for (int i = 0; i < size(); i += 2) {
      Float value = (Float)elementAt(i);
      Float prob = (Float)elementAt(i + 1);
      for (int j = size() - 2; j > i; j -= 2) {
        Float v = (Float)elementAt(j);
        if (value.equals(v)) {
          Float p = (Float)elementAt(j + 1);
          prob = new Float(prob.floatValue() + p.floatValue());
          removeElementAt(j + 1);
          removeElementAt(j);
        } 
      } 
      setElementAt((E)prob, i + 1);
    } 
  }
  
  public void normalize() {
    Enumeration e = elements();
    float totalprob = 0.0F;
    while (e.hasMoreElements()) {
      e.nextElement();
      totalprob += ((Float)e.nextElement()).floatValue();
    } 
    for (int i = 1; i < size(); i += 2) {
      Float prob = (Float)elementAt(i);
      setElementAt((E)new Float(prob.floatValue() / totalprob), i);
    } 
  }
  
  public void cluster(int pointsNumber) {
    compact();
    Distribution original = (Distribution)clone();
    if (pointsNumber >= size() / 2)
      return; 
    removeAllElements();
    if (pointsNumber == 1) {
      appendTerm(new Float(original.calculateMostLikely()), new Float(1.0F));
    } else if (pointsNumber == 2) {
      float minVal = original.calculateMin();
      float minDis = original.findValue(original.calculateMin());
      float avgVal = original.calculateMostLikely();
      float avgDis = original.findValue(original.calculateMostLikely());
      float maxVal = original.calculateMax();
      float maxDis = original.findValue(original.calculateMax());
      if (minDis > maxDis) {
        appendTerm(new Float(minVal), new Float(minDis));
        appendTerm(new Float(avgVal), new Float(1.0F - minDis));
      } else {
        appendTerm(new Float(avgVal), new Float(1.0F - maxDis));
        appendTerm(new Float(maxVal), new Float(maxDis));
      } 
      compact();
    } else {
      float minValue = original.calculateMin();
      float maxValue = original.calculateMax();
      float mostLikelyValue = original.calculateMostLikely();
      appendTerm(new Float(minValue), new Float(0.0F));
      appendTerm(new Float(maxValue), new Float(0.0F));
      appendTerm(new Float(mostLikelyValue), new Float(0.0F));
      original.sortByDistribution();
      int k = 3;
      int i;
      for (i = original.size() - 2; i > 0 && k < pointsNumber; i -= 2) {
        float val = ((Float)original.elementAt(i)).floatValue();
        if (val != minValue && val != maxValue && val != mostLikelyValue) {
          appendTerm(new Float(val), new Float(0.0F));
          k++;
        } 
      } 
      compact();
      sortByValue();
      int j;
      for (j = 0; j < original.size(); j += 2) {
        float delta2 = Float.MAX_VALUE;
        boolean found_right_bucket = false;
        float movedVal = ((Float)original.elementAt(j)).floatValue();
        float movedDis = ((Float)original.elementAt(j + 1)).floatValue();
        float prevBucketDis = 0.0F;
        float bucketVal = 0.0F;
        float bucketDis = 0.0F;
        int m;
        for (m = 0; !found_right_bucket && m < size(); m += 2) {
          if (m > 0)
            prevBucketDis = ((Float)elementAt(m - 1)).floatValue(); 
          bucketVal = ((Float)elementAt(m)).floatValue();
          bucketDis = ((Float)elementAt(m + 1)).floatValue();
          float delta1 = bucketVal - movedVal;
          if (delta1 < 0.0F)
            delta1 *= -1.0F; 
          if (delta1 == delta2) {
            setElementAt((E)new Float(prevBucketDis + movedDis / 2.0F), m - 1);
            setElementAt((E)new Float(bucketDis + movedDis / 2.0F), m + 1);
            found_right_bucket = true;
          } else if (delta1 > delta2) {
            setElementAt((E)new Float(prevBucketDis + movedDis), m - 1);
            found_right_bucket = true;
          } 
          delta2 = delta1;
        } 
        if (!found_right_bucket) {
          bucketDis = ((Float)elementAt(size() - 1)).floatValue();
          setElementAt((E)new Float(bucketDis + movedDis), size() - 1);
        } 
      } 
    } 
  }
  
  public void sort() {
    sortByValue();
  }
  
  public void sortByValue() {
    Enumeration e = elements();
    Float frequency = new Float(0.0F);
    int i;
    for (i = 0; i < size() - 2; i += 2) {
      int enumr = i;
      Float value = (Float)elementAt(enumr);
      int j;
      for (j = i; j < size(); j += 2) {
        if (((Float)elementAt(j)).floatValue() < value.floatValue()) {
          enumr = j;
          value = (Float)elementAt(enumr);
          frequency = (Float)elementAt(enumr + 1);
        } 
      } 
      if (i != enumr) {
        setElementAt(elementAt(i), enumr);
        setElementAt(elementAt(i + 1), enumr + 1);
        setElementAt((E)value, i);
        setElementAt((E)frequency, i + 1);
      } 
    } 
  }
  
  public void sortByDistribution() {
    Enumeration e = elements();
    Float value = new Float(0.0F);
    int i;
    for (i = 0; i < size() - 2; i += 2) {
      int enumr = i;
      Float frequency = (Float)elementAt(enumr + 1);
      int j;
      for (j = i; j < size(); j += 2) {
        if (((Float)elementAt(j + 1)).floatValue() < frequency.floatValue()) {
          enumr = j;
          value = (Float)elementAt(enumr);
          frequency = (Float)elementAt(enumr + 1);
        } 
      } 
      if (i != enumr) {
        setElementAt(elementAt(i), enumr);
        setElementAt(elementAt(i + 1), enumr + 1);
        setElementAt((E)value, i);
        setElementAt((E)frequency, i + 1);
      } 
    } 
  }
  
  public boolean equals(Distribution d) {
    if (d == null)
      return false; 
    if (size() != d.size())
      return false; 
    Enumeration e = elements();
    while (e.hasMoreElements()) {
      Float value = (Float)e.nextElement();
      Float prob = (Float)e.nextElement();
      boolean found = false;
      for (int i = 0; i < d.size(); i += 2) {
        Float dvalue = (Float)d.elementAt(i);
        Float dprob = (Float)d.elementAt(i + 1);
        if (value.equals(dvalue) && prob.equals(dprob)) {
          found = true;
          break;
        } 
      } 
      if (!found)
        return false; 
    } 
    return true;
  }
  
  public double compare(Distribution b) {
    return compare(this, b);
  }
  
  private double compare(Distribution a, Distribution b) {
    double accumval = 0.0D, accsum = 0.0D;
    Hashtable accumhash0 = new Hashtable();
    Hashtable accumhash1 = new Hashtable();
    Hashtable hash0 = a.getHashtable();
    Hashtable hash1 = b.getHashtable();
    Distribution listOfAllValues = a.appendDistribution(b);
    listOfAllValues.compact();
    listOfAllValues.sortByValue();
    int i;
    for (i = 0; i < listOfAllValues.size(); i += 2) {
      if (hash0.containsKey(listOfAllValues.elementAt(i))) {
        double tmpval = ((Float)hash0.get(listOfAllValues.elementAt(i))).doubleValue();
        accsum += tmpval;
      } 
      accumhash0.put(listOfAllValues.elementAt(i), new Double(accsum));
    } 
    accsum = 0.0D;
    for (i = 0; i < listOfAllValues.size(); i += 2) {
      if (hash1.containsKey(listOfAllValues.elementAt(i))) {
        double tmpval = ((Float)hash1.get(listOfAllValues.elementAt(i))).doubleValue();
        accsum += tmpval;
      } 
      accumhash1.put(listOfAllValues.elementAt(i), new Double(accsum));
    } 
    Enumeration e = accumhash0.keys();
    while (e.hasMoreElements()) {
      Float tmpkey = e.nextElement();
      Double tmpval = (Double)accumhash0.get(tmpkey);
    } 
    e = accumhash1.keys();
    while (e.hasMoreElements()) {
      Float tmpkey = e.nextElement();
      Double tmpval = (Double)accumhash1.get(tmpkey);
    } 
    double finalsum = 0.0D;
    for (int j = 0; j < listOfAllValues.size() - 2; j += 2) {
      double intersum = Math.abs(((Double)accumhash0.get(listOfAllValues.elementAt(j))).doubleValue() - ((Double)accumhash1.get(listOfAllValues.elementAt(j))).doubleValue());
      double intersum2 = (((Float)listOfAllValues.elementAt(j + 2)).doubleValue() - ((Float)listOfAllValues.elementAt(j)).doubleValue()) * intersum;
      finalsum += intersum2;
    } 
    double lastdiv = ((Float)listOfAllValues.elementAt(listOfAllValues.size() - 2)).doubleValue() - ((Float)listOfAllValues.firstElement()).doubleValue();
    double distance = finalsum / lastdiv;
    return distance;
  }
  
  public String output() {
    StringBuffer sb = new StringBuffer("");
    Enumeration e = elements();
    while (e.hasMoreElements()) {
      float n2, n1 = ((Float)e.nextElement()).floatValue();
      if (e.hasMoreElements()) {
        n2 = ((Float)e.nextElement()).floatValue();
      } else {
        return "Error";
      } 
      sb.append(n1);
      sb.append(" ");
      sb.append(n2);
      if (e.hasMoreElements())
        sb.append(" "); 
    } 
    return sb.toString();
  }
}
