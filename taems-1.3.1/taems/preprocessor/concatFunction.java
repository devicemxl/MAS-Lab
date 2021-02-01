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

public class concatFunction extends Block {
    protected Vector data;
    public concatFunction(PreProcessor p, Vector v) {
	super(p);
	data=v;
    }
    public String toString() {
	String answer = "#concat(" ;
	
	for(Enumeration e =data.elements(); e.hasMoreElements() ; ) {
	    Object o = e.nextElement();
	    if (o instanceof Block) {
		Object o1 = ((Block)o).toString();
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
	StringBuffer answer = new StringBuffer("");
	for(Enumeration e = data.elements(); e.hasMoreElements() ; ) {
	    Object o = e.nextElement();
	    if (o instanceof Block) {
		String o1 = ((Block)o).toTTaems(ht);
		o = o1;
	    }
	    
            answer.append(o.toString());
	}
	return(" "+answer.toString());
    }
}
