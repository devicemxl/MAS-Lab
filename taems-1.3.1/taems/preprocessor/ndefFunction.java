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

public class ndefFunction extends Block {
    protected Vector data;
    public ndefFunction(PreProcessor p, Vector v) {
	super(p);
	data=v;
    }
    public String toPTaems() {
	String answer = "#ndef(" ;
	
	for(Enumeration e =data.elements(); e.hasMoreElements() ; ) {
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
	boolean answer=true;
	for(Enumeration e = data.elements(); e.hasMoreElements() ; ) {
	    Object o = e.nextElement();
	    if (o instanceof Block) {
		if (o instanceof Variable) {
		    if (!((Variable)o).isDefined(ht))
			o = null;
		}
		if (o instanceof List) {
		    if (!((List)o).isDefined(ht))
			o = null;
		}
	    }
	    if (o == null) {
		answer &= true;
	    } else {
		answer &= false;
	    }
	}
	return(""+answer);
    }
}
