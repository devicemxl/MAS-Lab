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
 * Main class to run the PreProcessor, this will generate the String
 * that could be parse by the TTaems Parser and generate a Taems object.
 * <P>
 * Normally, people don't have to use that, they have to used the
 * PreProcessorTaemsReader defined in agent.mass.PreProcessorTaemsReader.
 */ 
public class PreProcessor {
    
    protected Hashtable varreference;
    protected Hashtable listreference;
    protected Log log = Log.getDefault(); 
    protected Vector blocks;

    /** 
     * Constructor is pretty simple, no arguments is always good !
     */ 
    public PreProcessor () {
        varreference = new Hashtable();
        listreference = new Hashtable();
        blocks = new Vector();		
    }
    
    /**
     * @depricated
     */ 
    public void addReference(String name, Block b) {
        addVarReference(name, b);
    }

    /**
     * @depricated
     */
    public Block getReference(String name) {
        return getVarReference(name);
    }
    
    /**
     * When you are creating a define, it will store itself so it can
     * be retrieved later on. 
     * @param String name is the reference 
     * @param Block b is the object you want to reference to.
     */ 
    public void addVarReference(String name, Block b) {
        varreference.put(name,b);
    }
    
    /**
     * Get the block referenced by name.
     * @param String name
     * @return null or the Block that this name reference to.
     */
    public Block getVarReference(String name) {
        return((Block)varreference.get(name));
    }

    /**
     * When you are creating a define, it will store itself so it can
     * be retrieved later on. 
     * @param String name is the reference 
     * @param Block b is the object you want to reference to.
     */ 
    public void addListReference(String name, ListBlock b) {
        listreference.put(name,b);
    }
    
    /**
     * Get the block referenced by name.
     * @param String name
     * @return null or the Block that this name reference to.
     */
    public ListBlock getListReference(String name) {
        return((ListBlock)listreference.get(name));
    }

    /**
     * Add the top level block to get an handle to them.
     * @param Block b - any Block object -
     */
    public void addBlock(Block b) { 
	    blocks.addElement(b);
    }
    
    /**
     * Adds a list of top level blocks
     * @param Vector v A list of blocks
     */
    public void addBlocks(Vector v) {
        Enumeration e = v.elements();        
        while (e.hasMoreElements()) {
            addBlock((Block)e.nextElement());
        }
    }

    /**
     * Function used by the engine to produce the TTAEMS.
     * @param - Hashtable ht that contains all the state variable that we check in
     * the assertion
     * @return String - the TTAEMS string
     */
    public String toTTaems(Hashtable ht) {
	String answer = "";
	for(Enumeration e = getBlocks() ; e.hasMoreElements() ; ) {
	    Block b = (Block)e.nextElement();
	    answer = answer + "\n" + b.toTTaems(ht);
	}
	return(answer);
    }

    /**
     * Reset all the cache data in the define.
     * As a side effect, it will called this function on every sub 
     * block attached to the object.
     */
  
    public void reset() {
	
        for(Enumeration e = getBlocks() ; e.hasMoreElements() ; ) {
            Block b = (Block)e.nextElement();
            b.reset();
        }
        varreference.clear();
        listreference.clear();
    }

    /**
     * returns an Enumeration of all the sub block 
     * define in the preprocessor.
     * @return an Enumeration.
     */
    public Enumeration getBlocks() { return(blocks.elements()); }
    
    /**
     * Gives the number of sub block actually defined in the PreProcessor
     * object.
     * @return int 
     */
    public int getNumberOfBlock() { return(blocks.size()); }

    /**
     * Generic function used for debugging to print the preprocessor
     * object. It calls on all sub blocks the function toString().
     * @return String that could be printed !
     */
    public String toString() {
	return(toPTaems());
    }

    public String toPTaems() { 
	String answer = "";
	for(Enumeration e = getBlocks() ; e.hasMoreElements() ; ) {
	    Block b = (Block)e.nextElement();
	    answer = answer + "\n" + b.toPTaems();
	}
	return(answer);
    }
}
