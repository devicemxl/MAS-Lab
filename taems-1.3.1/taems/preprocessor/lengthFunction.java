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

public class lengthFunction extends Block {
    protected Vector data;
    public lengthFunction(PreProcessor p, Vector v) {
	super(p);
	data=v;
    }
    public String toPTaems() {
	String answer = "#length(" ;
	
	for(Enumeration e =data.elements(); e.hasMoreElements() ; ) {
	    Object o = e.nextElement();
	    if (o instanceof Block) {
		Object o1 = ((Block)o).toPTaems();
		o = o1;
	    }
	    if (o != null)
		answer = answer + o.toString();
	}
	return(answer + ")");
    }
    
   /**
     * Function used by the engine to produce the TTAEMS.
     * @param - Hashtable ht that contains all the state variable that we check in
     * the assertion
     * @return String - the TTAEMS string
     */
    public String toTTaems(Hashtable ht) {
        int total = 0;

        Enumeration e = data.elements();
        while (e.hasMoreElements()) {
            Block b = (Block)e.nextElement();
            if ((b.evaluate(ht) != null) && (b instanceof List) && (b.evaluate(ht) instanceof Vector)) {
                total += ((Vector)b.evaluate(ht)).size();
            }
        }
        return("" + total);
    }
}
