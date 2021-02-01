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

/**
 * DefineBlock is equivalent to affecting anything to variable name. 
 */
public class List extends Variable {

    public List(PreProcessor p, String k) {
        super(p,k);
    }

    /**
     * Specialized function used for debugging to print the preprocessor
     * object.
     * @return String that could be printed !
     */         
    public String toPTaems() {
        String answer = "@" + key;
        return(answer);
    }

    /**
     * isDefined(), Accessor to know if this list is defined
     * or not.
     */
    public boolean isDefined(Hashtable ht) {
	if (ht.containsKey(key)) 
	    return(true);
	else
	    if (preprocessor.getListReference(key) != null) 
		return(true);
	return(false);
    }

    public String toTTaems(Hashtable ht) { 
	Object o = evaluate(ht);
	String answer="";
	if (o instanceof String) { return((String)o); }
	if (o instanceof Vector) {
	    for (Enumeration e = ((Vector)o).elements(); 
		 e.hasMoreElements(); ) {
		Object o1 = e.nextElement();
		String add;
		if (o1 instanceof Block) 
		    add = ((Block)o1).toTTaems(ht);
		else 
		    add = (String)o1;
		if (answer.equals("")) 
		    answer = add;
		else
		    answer = answer + " " + add;
	    }
	    return(answer);
	}
	return("; PROBLEM IN CONVERSION OF LIST ");
    }
	
     /**
      * Evaluate function
      */   
    public Object evaluate(Hashtable ht) { 
	Object answer=new Vector();
       if (local) { return(cache); }
       if (ht.containsKey(key)) {
            answer = ht.get(key);
        } else {
            if (preprocessor.getListReference(key) != null) 
                answer = preprocessor.getListReference(key).evaluate(ht);
            else {
                log.log("No matching for @" + key,2);
                answer = "@"+key;
            }
        }
        return(answer);
    }

    /**
     * In order to avoid cycle in the reference, we have to dereference
     * any pointer back to the current Define we are evaluating.
     * This function will go thru all sub-element to replace this value
     * @param Hashtable ht
     * @param String key (the current variable or list we are defining
     */
    public void dereference(Hashtable ht,String k) {
	if (k.equals(key)) {
	    local = false;
	    cache = evaluate(ht);
	    local = true;
	}
    }

}
