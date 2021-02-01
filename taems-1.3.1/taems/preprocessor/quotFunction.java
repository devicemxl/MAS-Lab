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

public class quotFunction extends Block {
    protected Vector data;
    public quotFunction(PreProcessor p, Vector v) {
	super(p);
	data=v;
    }
    public String toPTaems() {
	String answer = "#quot(" ;
	
	for(Enumeration e =data.elements(); e.hasMoreElements() ; ) {
	    Object o = e.nextElement();
	    if (o instanceof Block) {
		Object o1 = ((Block)o).toPTaems();
		o = o1;
	    }
	    if (o != null)
		if (answer.equals("#quot("))
		    answer = answer + o.toString();
		else
		    answer = answer + ", " + o.toString();
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
	float answer=Float.MIN_VALUE;
	Float tmp=new Float(0);
	for(Enumeration e = data.elements(); e.hasMoreElements() ; ) {
	    Object o = e.nextElement();
	    if (o instanceof Block) {
		String o1 = ((Block)o).toTTaems(ht);
		o = o1;
	    }
	    try {
		tmp = new Float((String)o);
	    }
	    catch(NumberFormatException nfe) { 
		System.err.println("Sorry I can't convert " + o + " in a number is it normal ?");
		o = null;
	    }
	    if (o != null)
		if (answer == Float.MIN_VALUE) 
		    answer = tmp.floatValue();
		else
		    answer = answer / tmp.floatValue();
	}
	if (answer == Float.MIN_VALUE) 
	    answer = 0;
	
	return(" "+answer);
    }

}
