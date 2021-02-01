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

public class WhileBlock extends Block {
    protected Condition condition;

    public WhileBlock(PreProcessor p) {
        super(p);
        condition = null;
    }

    public String toPTaems() {
        String answer = "#while ";
        if (condition != null)
            answer= answer+condition.toPTaems() +"\n";
	
        for(Enumeration e =getSubBlocks(); e.hasMoreElements() ; ) {
            Block b = (Block)e.nextElement();
            answer = answer + b.toPTaems();
        }
        answer = answer + "\n#endwhile \n";

        return(answer);
    }
    
    public void setCondition(Condition d) { condition = d;}

    public boolean evaluateWhile(Hashtable ht) {
        return(condition.evaluateCondition(ht));
    }

    public String toTTaems(Hashtable ht) {
        String answer = "";

        while (evaluateWhile(ht)) {
            System.err.println("Condition: " + condition.toPTaems() + " is true");
            for(Enumeration e =getSubBlocks(); e.hasMoreElements() ; ) {
                Block b = (Block)e.nextElement();
                answer = answer + "\n" + b.toTTaems(ht);
            }
        }
	//	System.err.println("While.toTTaems() = " + answer);
        return (""+answer);
    }
}
