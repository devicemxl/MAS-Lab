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

public class Condition extends Block {

    public Condition(PreProcessor p) {
	super(p);
    }

    
    public String toTTaems(Hashtable ht) { return("" + evaluate(ht)); }

    public boolean evaluateCondition(Hashtable ht) {
	boolean answer = true;
	for(Enumeration e =getSubBlocks(); e.hasMoreElements() ; ) {
	    Condition b = (Condition)e.nextElement();
	    answer = (answer && b.evaluateCondition(ht));
	}
	return answer;
    }
}
