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

public class GreaterCondition extends Condition {

    public GreaterCondition(PreProcessor p) {
	super(p);
    }

    public String toPTaems() {
	String answer = "";
	String value0;
	if (subBlock.elementAt(0) instanceof Block)
	    value0 = ((Block)subBlock.elementAt(0)).toPTaems();
	else {
	    value0 = (String)subBlock.elementAt(0);
	}
	String value1;
	if (subBlock.elementAt(1) instanceof Block)
	    value1 = ((Block)subBlock.elementAt(1)).toPTaems();
	else {
	    value1 = (String)subBlock.elementAt(1);
	}
	answer = "(" + value0 + " > " + value1 +")";
	return(answer);
    }
    

    public boolean evaluateCondition(Hashtable ht) { 
	boolean answer;
	String value0;
	if (subBlock.elementAt(0) instanceof Block)
	    value0 = ((Block)subBlock.elementAt(0)).toTTaems(ht);
	else {
	    value0 = (String)subBlock.elementAt(0);
	}
	String value1;
	if (subBlock.elementAt(1) instanceof Block)
	    value1 = ((Block)subBlock.elementAt(1)).toTTaems(ht);
	else {
	    value1 = (String)subBlock.elementAt(1);
	}
	int lex;
	boolean isANumber=true;
	Float tmpFloat0=new Float(0), tmpFloat1=new Float(0);
	// Roger is happy about thoses variables.
	try {
	     tmpFloat0 = new Float(value0);
	 }
	catch(NumberFormatException nfe) { isANumber = false; }
	 try {
	    tmpFloat1 = new Float(value1);
	 }
	catch(NumberFormatException nfe) { isANumber = false; }
	
	if (isANumber) {
	    if (tmpFloat0.floatValue() > tmpFloat1.floatValue()) 
		answer = true;
	    else
		answer=false;
	}
	else { 
	    if ((lex = value0.compareTo(value1)) > 0) 
		answer = true;
	    else
		answer=false;
	}
	log.log("Condition : " + this.toPTaems() + " is " + answer,2);
	return answer;
    }
}

