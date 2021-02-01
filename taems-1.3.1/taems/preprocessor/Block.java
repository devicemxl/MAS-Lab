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
 * Basic class to create a block. This class handles and defines most 
 * of the functionnality  needed by the PreProcessor Engine.
 */

public class Block {
    protected Vector subBlock;
    protected PreProcessor preprocessor;
    protected Log log = Log.getDefault(); 
    /**
     * Basic Constructor, nothing to worry about
     */ 
    public Block() {}
	

    /**
     * Simple constructor with a pointer to the PreProcessor object.
     * @param PreProcessor p - pointer back to the preprocessor -
     */
    public Block(PreProcessor p) { 
	subBlock = new Vector();
	preprocessor = p;
    }

    /**
     * Original function used to print any block. This function prints
     * the name of the class and print all the sub block attached to.
     * This function could be overrided in the more specialized class.
     *<P>
     * Mainly used for debugging purpose.
     */
    public String toString() {
	return(toPTaems());
    }

    /**
     * Specialized function used for printing itself in the original
     * format. It calls on all sub blocks the function toPTaems().
     * @return String that could be printed !
     */ 
    public String toPTaems() {
	String answer = "";
	for(Enumeration e =getSubBlocks(); e.hasMoreElements() ; ) {
	    Block b = (Block)e.nextElement();
	    answer = answer + b.toPTaems();
	}
	return(answer);
    }

    /**
     * Again another set of accessors. This time it will access to the 
     * list of sub blocks.
     */
    public Enumeration getSubBlocks() { return subBlock.elements(); }
    public int getNumberOfSubBlock() { return subBlock.size(); }
    public Block getSubBlockAt(int position) { return((Block)subBlock.elementAt(position)); }
    public void addSubBlock(Block b) { subBlock.addElement(b); }
    public void addSubBlocks(Vector v) {
        Enumeration e = v.elements();
        while (e.hasMoreElements()) { addSubBlock((Block)e.nextElement()); }
    }
    public void addString(String b) { subBlock.addElement(b); }

    /**
     * Function used by the engine to produce the TTAEMS.
     * @param - Hashtable ht that contains all the state variable that we check in
     * the assertion
     * @param Hashtable ht - argument of the preprocessor.
     * @return String - the TTAEMS string
     */
    public String toTTaems(Hashtable ht) {
	String answer = "";
	for(Enumeration e =getSubBlocks(); e.hasMoreElements() ; ) {
	    Block b = (Block)e.nextElement();
	    answer = answer + b.toTTaems(ht);
	}
	return(answer);
    }

    /**
     * Function reset called before every run to remove cached data !!!
     * This function call reset() on every sub blocks.
     */
    public void reset() { 
	if (subBlock != null) {
	    for(Enumeration e = getSubBlocks() ; e.hasMoreElements() ; ) {
		Block b = (Block)e.nextElement();
		b.reset();
	    }
	}
    }

    /**
     * Another function made to evaluate PreProcessor specific function,
     * like concat, trim, sum etc...<P>
     * If it's a Block that are pure Taems text it will defined that as 
     * the same as toTTaems(). Otherwise you have to specialize it.
     */
    public Object evaluate(Hashtable ht) { return(toTTaems(ht)); }

     /**
     * In order to avoid cycle in the reference, we have to dereference
     * any pointer back to the current Define we are evaluating.
     * This function will go thru all sub-element to replace this value
     * @param Hashtable ht
     * @param String key (the current variable or list we are defining
     */
    public void dereference(Hashtable ht, String key) {
	if (subBlock != null) {
	    for(Enumeration e = getSubBlocks() ; e.hasMoreElements() ; ) {
		Block b = (Block)e.nextElement();
		b.dereference(ht,key);
	    }
	}
    }	
}
