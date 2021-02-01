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

public class IfBlock extends Block {
    protected Condition condition;
    protected Vector iftrue;
    protected Vector elseiftrue;
    protected Vector elsetrue;

    public IfBlock(PreProcessor p) {
	super(p);
	condition = null;
	iftrue = null;
	elseiftrue = null;
	elsetrue = null;
    }

    
    public void addElseIf(Block b) {
	if (elseiftrue == null) 
	    elseiftrue = new Vector();
	elseiftrue.addElement(b);
    }


    public void addIf(Block b) {
        if (iftrue == null) 
            iftrue = new Vector();
        iftrue.addElement(b);
    }
    public void addIfs(Vector v) {
        Enumeration e = v.elements();
        while (e.hasMoreElements()) { addIf((Block)e.nextElement()); }
    }


    public void addElse(Block b) {
	if (elsetrue == null) 
	    elsetrue = new Vector();
	elsetrue.addElement(b);
    }
    public void addElses(Vector v) {
        Enumeration e = v.elements();
        while (e.hasMoreElements()) { addElse((Block)e.nextElement()); }
    }

    public String toPTaems() {
	String answer = "#if ";
	if (condition != null)
	    answer= answer+condition.toPTaems() +"\n";
	
	if (iftrue != null) {
	    for(Enumeration e =iftrue.elements(); e.hasMoreElements() ; ) {
		Block b = (Block)e.nextElement();
		answer = answer + b.toPTaems() +"\n";
	    }
	}
	if (elseiftrue != null) {
	    answer = answer + "\n";
	    for(Enumeration e =elseiftrue.elements(); e.hasMoreElements() ; ) {
		Block b = (Block)e.nextElement();
		answer = answer + "#else " + b.toPTaems() + "\n";
	    }
	}
	if (elsetrue != null) {
	    answer = answer + "\n#else\n";
	    for(Enumeration e =elsetrue.elements(); e.hasMoreElements() ; ) {
		Block b = (Block)e.nextElement();
		answer = answer + b.toPTaems();
	    }
	}
	answer = answer + "\n#endif \n";
	return(answer);
    }
    
    public void setCondition(Condition d) { condition = d;}

    public boolean evaluateIf(Hashtable ht) {
	return(condition.evaluateCondition(ht));
    }

    public String toTTaems(Hashtable ht) {
	String answer = "";
	if (evaluateIf(ht)) {
	    log.log("Condition: " + condition.toPTaems() + " is true",3);
	    for(Enumeration e =iftrue.elements(); e.hasMoreElements() ; ) {
		Block b = (Block)e.nextElement();
		answer = answer + "\n" + b.toTTaems(ht);
	    }
	}
	else {
	    log.log("Condition: " + condition.toPTaems() + " is false",3);
	    if (elseiftrue != null) {
		for(Enumeration e =elseiftrue.elements(); 
		    e.hasMoreElements();){
		    IfBlock b = (IfBlock)e.nextElement();
		    if (b.evaluateIf(ht))
			return(b.toTTaems(ht));
		}
	    }
	    if (elsetrue != null) {
		for(Enumeration e =elsetrue.elements(); 
		    e.hasMoreElements() ; ) {
		    Block b = (Block)e.nextElement();
		    answer = answer + "\n" + b.toTTaems(ht);
		}
	    }
	}
	return (""+answer);
    }
}
