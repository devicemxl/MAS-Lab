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

public class additematFunction extends Block {
    protected Vector data;
    public additematFunction(PreProcessor p, Vector v) {
	super(p);
	data=v;
    }


    public String toPTaems() {
	String answer = "#additemat(" ;
	
	for(Enumeration e =data.elements(); e.hasMoreElements() ; ) {
	    Object o = e.nextElement();
	    if (o instanceof Block) {
		Object o1 = ((Block)o).toString();
		o = o1;
	    }
	    if (o != null)
		answer = answer + o.toString();
	}
	return(answer+")");
    }

    public Object evaluate(Hashtable ht) {
	try {
            Enumeration e = data.elements();
            List l = (List)e.nextElement();
            Vector v=new Vector(),v2 = new Vector();
	    //	    v2 = (Vector)l.evaluate(ht);
	    v2.addElement(l);
	    Object o1 = e.nextElement();
	    int index;
	    if (o1 instanceof Block) 
		index = (new Double(((Block)o1).toTTaems(ht))).intValue();
	    else 
		index = Integer.parseInt(o1.toString());
	    for(; e.hasMoreElements(); ) {
		Object o = e.nextElement();
		v.insertElementAt(o,0);
	    }
	    for(e = v.elements(); e.hasMoreElements();)
		v2.insertElementAt(e.nextElement(),index);
	    System.err.println("ADDDING = " + v2);
	    return(v2);
	
        } catch (Exception ex) {
            System.err.println("Whoops, error processing itemat function: " + ex);
        }
	return("");
    }
    
   /**
     * Function used by the engine to produce the TTAEMS.
     * @param - Hashtable ht that contains all the state variable that we check in
     * the assertion
     * @return String - the TTAEMS string
     */
    public String toTTaems(Hashtable ht) {
	Object o = evaluate(ht);
	String answer="";
	if (o instanceof Vector) {
	    for (Enumeration e = ((Vector)o).elements();
		 e.hasMoreElements(); ) {
		Object o1 = e.nextElement();
		if (o1 instanceof Block) 
		    answer = answer + ((Block)o1).toTTaems(ht) + " ";
		else
		    answer = answer + (String)o1 + " ";
	    }
	}
	else 
	    answer = (String)o;
	return(answer);
    }

    /**
     * In order to avoid cycle in the reference, we have to dereference
     * any pointer back to the current Define we are evaluating.
     * This function will go thru all sub-element to replace this value
     * @param Hashtable ht
     * @param String key (the current variable or list we are defining
     */
      public void dereference(Hashtable ht,String k) {
	  Enumeration e = data.elements();
	  for(; e.hasMoreElements(); ) {
	      Object o = e.nextElement();
	      if (o instanceof Block) 
		  ((Block)o).dereference(ht,k);
	  }
      }
}
