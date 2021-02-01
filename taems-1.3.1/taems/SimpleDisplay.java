/* Copyright (C) 2005, University of Massachusetts, Multi-Agent Systems Lab
 * See LICENSE for license information
 */

package taems;


/* Global Inclues */
import javax.swing.*;
import java.applet.*;
import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;

/* Utilities */
import utilities.*;

/* Generator imports */
import taems.*;
import taems.parser.*;

/**
 */
public class SimpleDisplay  {
    private static Log genlog = Log.getDefault();
    private static ReadTTaems reader = new ReadTTaems(genlog);

    /**
     */
    public SimpleDisplay() {
    }

    /**
     * Main
     */
    public static void main(String argv[]) {
        Taems task = new Taems("Combined Output");
        String file_name = null;
        boolean silent = false;
        TTaemsGrammar gen;

        for (int i = 0; i < argv.length; i++) {
            if (argv[i].startsWith("-")) {
                if (argv[i].equals("-silent"))
                    genlog.setLevel(-1);
                if (argv[i].equals("-s"))
                    genlog.setLevel(-1);
            }
        }
    
        showTaems(task);

        for (int i = 0; i < argv.length; i++) {
            if (argv[i].startsWith("-"))
                continue;

            String input_script = argv[i];

            genlog.log("Reading textual taems file " + input_script);
            Taems temp = reader.readTTaems(input_script);
            genlog.log("Textual taems file read!");

            genlog.log("Combining " + temp.getLabel());
            task.addTaems(temp);
        }
    }

    private static final int INITIAL_WIDTH = 500;
    private static final int INITIAL_HEIGHT = 400;
    public static void showTaems(Taems t) {
        JFrame frame;

        frame = new JFrame("Taems"); 
        
        frame.setSize(INITIAL_WIDTH, INITIAL_HEIGHT);
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(screenSize.width/2 - INITIAL_WIDTH/2,
                          screenSize.height/2 - INITIAL_HEIGHT/2);

        frame.getContentPane().add(t, BorderLayout.CENTER);
        frame.setVisible(true);
    }
}




