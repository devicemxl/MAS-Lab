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
public class Variable extends Block {
    protected String key;
    protected Object cache;
    protected boolean local = false;
     /**
     * Constructor
     * @param PreProcessor p - pointer back to the main class
     * @param String k - key used to referer to this block
     */
    public Variable(PreProcessor p, String k) {
	super(p);
	this.key = k;
    }

    /**
     * Specialized function used for debugging to print the preprocessor
     * object. It calls on all sub blocks the function toString().
     * @return String that could be printed !
     */         
    public String toPTaems() {
	String answer = "$" + key;
	return(answer);
    }

    /**
     * Accessor to get the key
     */
    public String getKey() { return key; }

    /**
     * isDefined(), Accessor to know if this varaible is defined
     * or not.
     */
    public boolean isDefined(Hashtable ht) {
	if (ht.containsKey(key)) 
	    return(true);
	else
	    if (preprocessor.getVarReference(key) != null) 
		return(true);
	return(false);
    }

    /**
     * Function used by the engine to produce the TTAEMS.
     * @param - Hashtable ht that contains all the state variable that we check in
     * the assertion
     * @return String - the TTAEMS string
     */
    public String toTTaems(Hashtable ht) { 
	String answer = "";
	if (local) { return((String)cache); }
	
	if (ht.containsKey(key)) {
	    answer = ht.get(key).toString();
	} else {
	    if (preprocessor.getVarReference(key) != null) 
		answer = (String)((DefineBlock)preprocessor.getVarReference(key)).evaluate(ht);
	    else {
		log.log("No matching for $" + key,2);
		answer = "$"+key;
	    }
	}
	log.log("Variable.toTTaems(" + key + ") = ["+answer+"]",4);
	return(answer);
    }

    /**
     * Specialize function, when you evaluate a DefineBlock it will
     * set the value in the PreProcessor Hashtable to point to this
     * object. Of course if later on another DefineBlock with the same
     * key could override the reference.
     */   
    public Object evaluate(Hashtable ht) { 
	return(toTTaems(ht));
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
	    cache = evaluate(ht);
	    local = true;
	}
    }

    /**
     * Function reset called before every run
     */
    public void reset() { 
	local=false;
	cache=null;
    }
}
