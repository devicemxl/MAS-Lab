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
public class ListBlock extends DefineBlock {
    protected Vector list;

     /**
     * Constructor
     * @param PreProcessor p - pointer back to the main class
     * @param String k - key used to referer to this block
     * @param boolean i - Evaluate immediately, or dynamically?
     * @param boolean a - Tell the define is simple or complex form
     */
    public ListBlock(PreProcessor p, String k, Vector l, boolean i,boolean a) {
        super(p, k, i,a);

        this.list = l;
    }

    public ListBlock(PreProcessor p, String k) {
        super(p,k);
    }

    public void addSubBlock(Block b) { System.err.println("Cannot addSubBlock to List"); }
    public void addString(String b) { System.err.println("Cannot addSubBlock to List"); }

    /**
     * Function used by the engine to produce the TTAEMS.
     * @param - Hashtable ht that contains all the state variable that we check in
     * the assertion
     * @return String - the TTAEMS string
     */
    public String toTTaems(Hashtable ht) { 
        if (immediate) {
            Vector v = new Vector();
            Enumeration e = list.elements();
            while (e.hasMoreElements()) {
                Object o = e.nextElement();
                if (o instanceof Block) {
                    Block b = (Block)o;
		    v.addElement(b.toTTaems(ht));
                } else {
		    v.addElement(o.toString());
                }
            }
	    
	    ListBlock d = new ListBlock(preprocessor, key, v, false,false);
            preprocessor.addListReference(key, d);
        } else {
	    preprocessor.addListReference(key, this);
	}
        return("");
    }

    public void dereference(Hashtable ht,String key) {
	for(Enumeration e =list.elements(); e.hasMoreElements() ; ) {
            Object o = e.nextElement();
            if (o instanceof Block) 
		((Block)o).dereference(ht,key);
	}
    }
    
    public String toPTaems() {
       String answer;
	if (immediate) 
	    answer = "#definenow " + key ;
	else
	    answer = "#define " + key;
	answer = answer +" = @(";
	for(Enumeration e =list.elements(); e.hasMoreElements() ; ) {
	    Object o = e.nextElement();
	    if (answer.endsWith("@(")) 
		answer = answer + o.toString();
	    else
		answer =  answer + ", " + o.toString();
	}
	answer = answer +")\n";
	return(answer);
    }

    /**
     */   
    public Object evaluate(Hashtable ht) { 
	return(list);
    }

    /**
     * Function reset called before every run
     */
    public void reset() { 
    }
}
