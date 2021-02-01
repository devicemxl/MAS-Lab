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
 * Class that handles raw Textual Taems. It can interpret:<BR>
 *<UL>
 * <LI> variable substitution (prefix by '$'), 
 * <LI> functions (prefix by #)
 * </UL><P>
 */
public class TaemsBlock extends Block {
    protected String taems="";
    protected String originaltaems="";

    /**
     * Again very simple constructor.
     */
    public TaemsBlock(PreProcessor p) {
	super(p);
    }

    /**
     * Set the Textual Taems read by the parser.
     * @param String s - textual taems.
     */
    public void setTaems(String s) {
	taems = s ;
	originaltaems = s;
    }

     /**
     * Function used by the engine to produce the TTAEMS.
     * @param - Hashtable ht that contains all the state variable that we check in
     * the assertion
     * @param Hashtable ht - pass by the preprocessor (contain runtime defines).
     * @return String - the TTAEMS string
     */
    public String toTTaems(Hashtable ht) {
	String reply="";
	if ((subBlock != null) && (subBlock.size() != 0))
	    reply = reply + super.toTTaems(ht);
	if (!taems.equals("")) 
	    reply = reply + replaceDollarSign(ht);
	return(reply);
    }
    
    /**
     * In order to avoid cycle in the reference, we have to dereference
     * any pointer back to the current Define we are evaluating.
     * This function will go thru all sub-element to replace this value
     * @param Hashtable ht
     * @param String key (the current variable or list we are defining
     */
    public void dereference(Hashtable ht, String key) {
	if ((subBlock != null) && (subBlock.size() != 0))
	    super.dereference(ht,key);
	if (!taems.equals("")) 
	    dereferenceInline(ht,key);
    }
    
    /**
     * Function that looks inside the TAEMS code to
     */
    protected void dereferenceInline(Hashtable ht, String k) { 
	String s,answer="";
 	int index;
 	StringTokenizer st = new StringTokenizer(taems,"\n");
 	while (st.hasMoreTokens()) {
 	    s = st.nextToken();
 	    if ( s.indexOf('$') != -1) {
 		String s1="",key="";
 		boolean readkey=false;
 		char chars[] = s.toCharArray(); 
 		for (int i = 0; i < chars.length ; i++) {
 		    char c = chars[i];                           
 		    switch (c) {   
 		    case '$':   
 			if (readkey && !key.equals("")){
 			    if (key.equals(k)) {
				if (ht.containsKey(key))
				    s1 = s1 + ht.get(key).toString();
				else {
				    DefineBlock b = (DefineBlock)preprocessor.getVarReference(key);
				    if (b != null) 
					s1 = s1 + b.evaluate(ht);
				    else
					s1 = s1 + "$" + key;
				}
			    }
			    else
				s1 = s1 + "$" + key;
			}
 			readkey = true;
 			key="";
 			break;
 			// Whitespace
 		    case ' ':
 		    case '\t':
 		    case ')':
 			if (readkey) {
			    if (key.equals(k)) {
				if (ht.containsKey(key)) 
				    s1 = s1 + ht.get(key).toString();
				else {
				    DefineBlock b = (DefineBlock)preprocessor.getVarReference(key);
				    if (b != null) 
					s1 = s1 + b.evaluate(ht);
				    else 
					s1 = s1 + "$" + key;
				}
			    }
			    else
				s1 = s1 + "$" + key;
			    readkey=false;
 			}
 			s1 = s1 + String.valueOf(c);
 			break;
 		    default:
 			if (readkey) 
 			    key = key + String.valueOf(c);
 			else
 			    s1 = s1 + String.valueOf(c);
 		    }
 		}
 		if (readkey) {
 		    if (key.equals(k)) {
			if (ht.containsKey(key)) 
			    s1 = s1 + ht.get(key).toString();
			else {
			    DefineBlock b = (DefineBlock)preprocessor.getVarReference(key);
			    if (b != null) 
				s1 = s1 + b.evaluate(ht);
			    else
				s1 = s1 + "$" + key;
			}
		    }
		    else
			s1 = s1 + "$" + key;
		    readkey=false;
		}
		answer = answer + s1 + "\n";
	    }
 	    else {
 		answer= answer + s;
 	    }
	}
	taems = answer;
    }
    
    /**
     * Look for variable substitution to be done in the textual taems
     * and make the reference to the right Block.
     * @param Hashtable ht - pass by the preprocessor (contain runtime defines).
     * @return String - the TTAEMS string of the refered Block or the name
     * of the variable if not defined.
     */
    protected String replaceDollarSign(Hashtable ht) {
 	String s,answer="";
 	int index;
 	StringTokenizer st = new StringTokenizer(taems,"\n");
 	while (st.hasMoreTokens()) {
 	    s = st.nextToken();
 	    if ( s.indexOf('$') != -1) {
 		String s1="",key="";
 		boolean readkey=false;
 		char chars[] = s.toCharArray(); 
 		for (int i = 0; i < chars.length ; i++) {
 		    char c = chars[i];                           
 		    switch (c) {   
 		    case '$':   
 			if (readkey && !key.equals("")) {
 			    if (ht.containsKey(key))
 				s1 = s1 + ht.get(key).toString();
 			    else {
 				DefineBlock b = (DefineBlock)preprocessor.getVarReference(key);
 				if (b != null) 
 				    s1 = s1 + b.evaluate(ht);
 				else
 				    s1 = s1 + "$" + key;
 			    }
 			}
 			readkey = true;
 			key="";
 			break;
 			// Whitespace
 		    case ' ':
 		    case '\t':
 		    case ')':
 			if (readkey) {
 			    if (ht.containsKey(key)) 
 				s1 = s1 + ht.get(key).toString();
			    else {
 				DefineBlock b = (DefineBlock)preprocessor.getVarReference(key);
 				if (b != null) 
 				    s1 = s1 + b.evaluate(ht);
 				else 
 				    s1 = s1 + "$" + key;
 			    }
 			    readkey=false;
 			}
 			s1 = s1 + String.valueOf(c);
 			break;
 		    default:
 			if (readkey) 
 			    key = key + String.valueOf(c);
 			else
 			    s1 = s1 + String.valueOf(c);
 		    }
 		}
 		if (readkey) {
 		    if (ht.containsKey(key))
 			s1 = s1 + ht.get(key).toString();
 		    else {
 			DefineBlock b = (DefineBlock)preprocessor.getVarReference(key);
 			if (b != null) 
 			    s1 = s1 + b.evaluate(ht);
 			else
 			    s1 = s1 + "$" + key;
 		    }
 		    readkey=false;
 		}
 		answer = answer + s1 + "\n";
 	    }
 	    else {
 		answer= answer + s;
 	    }
 	}
 	return(answer);
     }

    //    public String evaluate(Hashtable ht) { return(toTTaems(ht)); }
    /**
     * Specialized function used for debugging to print the preprocessor
     * object. It calls on all sub blocks the function toString().
     * @return String that could be printed !
     */           
    public String toPTaems() {
	String reply="";
	if ((subBlock != null) && (subBlock.size() != 0))
	    reply = reply + super.toPTaems();
	if (!taems.equals("")) 
	    reply = reply + taems;
	return(reply);
    }

    public void reset() { 
	if (!originaltaems.equals(taems))
	    taems = originaltaems;
    }

}
