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

public class AndCondition extends Condition {

    public AndCondition(PreProcessor p) {
	super(p);
    }

    public String toPTaems() {
	String answer = "";
	for(Enumeration e =getSubBlocks(); e.hasMoreElements() ; ) {
	    if (answer.equals("")) 
		answer = ((Condition)e.nextElement()).toPTaems() ;
	    Condition b = (Condition)e.nextElement();
	    answer = "(" + answer + " && " + b.toPTaems() + ")";
	}
	return(answer);
    }
    

    public boolean evaluateCondition(Hashtable ht) { 
	boolean answer = true;
	for(Enumeration e =getSubBlocks(); e.hasMoreElements() ; ) {
	    Condition b = (Condition)e.nextElement();
	    answer = (answer && b.evaluateCondition(ht));
	}
	return answer;
    }
}

