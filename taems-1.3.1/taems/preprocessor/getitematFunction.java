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

public class getitematFunction extends Block {
    protected Vector data;
    public getitematFunction(PreProcessor p, Vector v) {
	super(p);
	data=v;
    }


    public String toPTaems() {
	String answer = "#getitemat(" ;
	
	for(Enumeration e =data.elements(); e.hasMoreElements() ; ) {
	    Object o = e.nextElement();
	    if (o instanceof Block) {
		Object o1 = ((Block)o).toPTaems();
		o = o1;
	    }
	    if (o != null)
		if (answer.equals("#getitemat("))
		    answer = answer + o.toString();
		else
		    answer = answer + ", " + o.toString();
	}
	return(answer +")");
    }

    public Object evaluate(Hashtable ht) {
	try {
            Enumeration e = data.elements();
            List l = (List)e.nextElement();
            Vector v=(Vector)l.evaluate(ht);
	    Object o = e.nextElement();
	    int index;
	    if (o instanceof Block) 
		index = (new Double(((Block)o).toTTaems(ht))).intValue();
	    else 
		index = Integer.parseInt(o.toString());
	    if (v != null)
		return(v.elementAt(index));
	    
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
	  //	  List l = (List)e.nextElement();
	  //	  Vector v = (Vector)l.evaluate(ht);
	  for(; e.hasMoreElements(); ) {
	      Object o = e.nextElement();
	      if (o instanceof Block) 
		  ((Block)o).dereference(ht,k);
	  }
      }
}
