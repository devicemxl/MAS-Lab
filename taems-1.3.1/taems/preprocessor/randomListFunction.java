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

public class randomListFunction extends randomFunction {
    protected Vector list;

    public randomListFunction(PreProcessor p, Vector v) {
	super(p,randomFunction.INT,new Integer(0),new Integer(v.size()));
	list = v;
    }

    public String toPTaems() {
	String answer = "#random_list(" ;
	for(Enumeration e =list.elements(); e.hasMoreElements() ; ) {
	    Object o = e.nextElement();
	    if (o instanceof Block) {
		Object o1 = ((Block)o).toPTaems();
		o = o1;
	    }
	    if (o != null)
		answer = answer + o.toString();
	}
	return(answer+")");
    }
    
   /**
     * Function used by the engine to produce the TTAEMS.
     * @param - Hashtable ht that contains all the state variable that we check in
     * the assertion
     * @return String - the TTAEMS string
     */
    public String toTTaems(Hashtable ht) {
	String answer="";
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
			Long tmplong = new Long((String)tmpString);
			r.setSeed(tmplong.longValue());
			System.err.println("Warning, I use the seed define in the PTAEMS file, (not DETERMINISTIC)");}
		    catch(NumberFormatException nfe) { 
			System.err.println("Sorry I can't convert " + tmpString + " in a seed for the random generator is it normal ?");
			System.err.println("Warning, I don't have any seed for the random engine, (not DETERMINISTIC)");}
		}
		else {
		    System.err.println("Warning, I don't have any seed for the random engine, (not DETERMINISTIC)");
		}
	    }
	}

	int key = ((Integer)nextNumber(ht)).intValue();
	if (list != null) {
	    Object o = list.elementAt(key);
	    if (o instanceof Block) {
		answer = ((Block)o).toTTaems(ht);
	    }
	    else 
		answer = (String)o;
	}
	    return " " + answer;
    }
}
