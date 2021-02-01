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
public class DefineBlock extends Block {
    protected String key;
    protected boolean immediate = false;
    protected boolean affect=false;

    /**
     * Constructor
     * @param PreProcessor p - pointer back to the main class
     * @param String k - key used to referer to this block
     * @param boolean i - Evaluate immediately, or dynamically?
     * @param boolean a - Tell the define is simple or complex form
     */
    public DefineBlock(PreProcessor p, String k, boolean i,boolean a) {
	super(p);
	this.key = k;
	this.immediate = i;
	affect=a;
    }

    /**
     * Constructor
     * @param PreProcessor p - pointer back to the main class
     * @param String k - key used to referer to this block
     */
    public DefineBlock(PreProcessor p, String k) {
	this(p, k, false,false);
    }

    /**
     * Specialized function used for printing itself in the original
     * format. It calls on all sub blocks the function toPTaems().
     * @return String that could be printed !
     */ 
    public String toPTaems() {
	String answer;
	if (immediate) 
	    answer = "#definenow " + key ;
	else
	    answer = "#define " + key;
	if (affect) 
	    answer = answer +" = ";
	else
	    answer = answer +" ";
	for(Enumeration e =getSubBlocks(); e.hasMoreElements() ; ) {
	    Block b = (Block)e.nextElement();
	    answer = answer + b.toPTaems();
	}
	if (affect) 
	    answer = answer +"\n";
	else
	    answer = answer + "\n#enddefine \n";
	return(answer);
    }

    /**
     * Function used by the engine to produce the TTAEMS.
     * @param - Hashtable ht that contains all the state variable that we check in
     * the assertion
     * @return String - the TTAEMS string
     */
    public String toTTaems(Hashtable ht) { 
	for(Enumeration e =getSubBlocks(); e.hasMoreElements() ; ) {
	    Block b = (Block)e.nextElement();
	    b.dereference(ht,key);
	}
        Object o = evaluate(ht);

	if (o instanceof Vector) {
	    if (immediate) {
		Vector v =new Vector();
		for (Enumeration e1 = ((Vector)o).elements() ; 
		     e1.hasMoreElements(); ) {
		    Object o1 = e1.nextElement();
		    if (o1 instanceof Block) 
			v.addElement(((Block)o1).evaluate(ht));
		    else
			v.addElement(o1);
		}
		ListBlock d = new ListBlock(preprocessor, key, v,false,false);
		preprocessor.addListReference(key, d);
	    }
	    else {
		ListBlock d = new ListBlock(preprocessor, key, (Vector)o,false,false);
		preprocessor.addListReference(key, d);
	    }
	}
	else {
	    if (immediate) {
		DefineBlock d = new DefineBlock(preprocessor, key, false,false);
		TaemsBlock tb = new TaemsBlock(preprocessor);
		tb.setTaems((String)o);
		d.addSubBlock(tb);
		preprocessor.addVarReference(key, d);
	    } else {
		preprocessor.addVarReference(key, this);
	    }

        }
        return("");
    }

     /**
     * Specialize function, when you evaluate a DefineBlock it will
     * set the value in the PreProcessor Hashtable to point to this
     * object. Of course if later on another DefineBlock with the same
     * key could override the reference.
     */   
    public Object evaluate(Hashtable ht) { 
	Object answer=null;
	boolean string=false;
	for(Enumeration e =getSubBlocks(); e.hasMoreElements() ; ) {
	    Block b = (Block)e.nextElement();
	    Object o;
	    if (b instanceof DefineBlock) 
		o = b.toTTaems(ht);
	    else
		o = b.evaluate(ht);
	    if (answer == null) {
		if (o instanceof Vector) {
		    string = false;
		    answer=new Vector();
		}
	        else {
		    string = true;
		    answer = new String("");
		}
	    }
	    if (string) 
		if (o instanceof String) 
		    answer = answer + (String)o;
	        else 
		    System.err.println("Error this is not a String ");
	    else 
		if (o instanceof Vector) 
		    for (Enumeration enumr = ((Vector)o).elements(); 
			 enumr.hasMoreElements(); ) 
			((Vector)answer).addElement(enumr.nextElement());
	    
		else 
		    ((Vector)answer).addElement(o);
	}
	return(answer);
    }

    /**
     * Function reset called before every run
     */
    public void reset() { 
    }
}
