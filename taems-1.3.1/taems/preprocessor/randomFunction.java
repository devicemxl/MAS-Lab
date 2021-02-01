/* Copyright (C) 2005, University of Massachusetts, Multi-Agent Systems Lab
 * See LICENSE for license information
 */

package taems.preprocessor;

/* Global Import */
import java.util.*;
import java.io.*;

/* Local Import */
import utilities.*;
import taems.*;

public class randomFunction extends Block {
    public static final RandomValue FLOAT = new RandomValue(0);
    public static final RandomValue INT = new RandomValue(1);
    protected static Random r;
    protected RandomValue type;
    protected Object l, h;

    public randomFunction(PreProcessor p, RandomValue r, Object l, Object h) {
	super(p);
	type = r;
	this.l = l;
	this.h = h;
    }

    public String toPTaems() {
	String answer = "#random(" ;
	for(Enumeration e =getSubBlocks(); e.hasMoreElements() ; ) {
	    Block b = (Block)e.nextElement();
	    answer = answer + b.toPTaems();
	}
	return(answer+")");
    }


    /**
     * reset the random by unsetting the random generator
     */
    public void reset() {
	r = null;
    }

   /**
     * Function used by the engine to produce the TTAEMS.
     * @param - Hashtable ht that contains all the state variable that we check in
     * the assertion
     * @return String - the TTAEMS string
     */
    public String toTTaems(Hashtable ht) {
        
	return(" " + nextNumber(ht).toString());
    }

   /**
     * Calculates the next number for the function
     * @param - Hashtable ht that contains all the state variable that we check in
     * the assertion
     * @return String - the TTAEMS string
     */
    public Object nextNumber(Hashtable ht) {
	Object answer = new Integer(0);
	String sl, sh;
	int il = 0, ih = 0;
	float fl = 0, fh = 0;

	if (l instanceof Block) {
	    //	    sl = ((Block)l).evaluate(ht);
	    sl = ((Block)l).toTTaems(ht);
	} else {
	    sl = l.toString();
	}
	if (h instanceof Block) {
	    sh = ((Block)h).toTTaems(ht);
	} else {
	    sh = h.toString();
	}
	if (type.equals(INT)) {
	    il = (int)Float.valueOf(sl.toString()).floatValue();
	    ih = (int)Float.valueOf(sh.toString()).floatValue();
	} else {
	    fl = Float.valueOf(sl.toString()).floatValue();
	    fh = Float.valueOf(sh.toString()).floatValue();
	}

	if (r == null) {
	    r = new Random();
	    if (ht.containsKey("RandomSeed")) {
		long seed = ((Long)ht.get("RandomSeed")).longValue();
		r.setSeed(seed);
	    }
	    else {
		if (preprocessor.getVarReference("RandomSeed") != null) {
		    String tmpString = (String)((DefineBlock)preprocessor.getVarReference("RandomSeed")).evaluate(ht);
		    try {
			Long tmplong = new Long(tmpString);
			r.setSeed(tmplong.longValue());
			System.err.println("Warning, I used the seed defined in the PTAEMS file");}
		    catch(NumberFormatException nfe) { 
			System.err.println("Sorry I can't convert " + tmpString + " in a seed for the random generator is it normal ?");
			System.err.println("Warning, I don't have a seed for the random engine, (not DETERMINISTIC)");}
		}
		else {
		    System.err.println("Warning, I don't have a seed for the random engine, (not DETERMINISTIC)");
		}
	    }
	}
	if (type.equals(FLOAT)) {
	    if (fh - fl != 0)
                answer= new Float(Math.abs(r.nextFloat() * 100000) % (fh - fl) + fl);
	    else
                answer= new Float(ih);
        }
	if (type.equals(INT)) {
	    if (ih - il != 0)
                answer= new Integer(Math.abs(r.nextInt()) % (ih - il) + il);
	    else
                answer= new Integer(ih);
        }
	return(answer);
    }

    public static class RandomValue implements java.io.Serializable {
	private int _value;
	private RandomValue(int value) {
	    _value = value;
	}
	
	private int asInt() { return _value;}
	public int hashCode() { return asInt(); }
	public boolean equals(Object obj) {
	    boolean ret= (obj == this);
	    if (!ret && obj !=null && obj instanceof RandomValue)
		ret = ((RandomValue)obj).asInt() == asInt();
	    return ret;
	}
    }
}
