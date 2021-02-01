/* Copyright (C) 2005, University of Massachusetts, Multi-Agent Systems Lab
 * See LICENSE for license information
 */

package taems.parser;


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

/**
 * This is in here for internal testing purposes, you will never need to use it
 * (except as a code reference, if you have the source).
 */
public class ParserTest  {
	
  private ReadTTaems reader = null;

  /**
   */
  public ParserTest(Log log) {
    reader = new ReadTTaems(log);
  }


    public static void main(String argv[]) {
        int i;
        String input_script = null;
        String output_file = null;
        String log_file = null;
        String file_name = null;
        boolean display=false;
        boolean silent=false;
        boolean nooutput=false;
        TTaemsGrammar gen;
        Taems task=null;

        for (i=0; i<argv.length; i++) {
            if (argv[i].equals("-if"))
                input_script = argv[++i];
            else if (argv[i].equals("-of"))
                output_file = argv[++i];
            else if (argv[i].equals("-lf"))
                log_file = argv[++i];

            else if (argv[i].equals("-no"))
                nooutput = true;
            else if (argv[i].equals("-display"))
                display=true;
            else if (argv[i].equals("-silent"))
                silent=true;
            else if (argv[i].equals("-verbose"))
                silent=false;

            else if (!argv[i].startsWith("-")) {
                input_script = argv[i];
                //display=true;
                //silent=true;
                //nooutput = true;
		break;
            }
        }
    
        Log genlog = null;
        try {
            if (log_file == null) {
                if (silent) {
			try {
				genlog=new Log(new FileOutputStream("/dev/null"),0);
			} catch (Exception ex) {
				System.err.println("Error using silent mode, reverting to verbose");
				ex.printStackTrace();
				silent = false;
			}
                }
		if (!silent) {
                    genlog = new Log(System.err,5);
		}
            } else {
                genlog = new Log(new FileOutputStream(log_file),5);
	    }
            Log.setDefault(genlog);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println(ex);
        }
    
        ParserTest pt = new ParserTest(genlog);
    
        if (input_script == null) {
            showTaems(new Taems());

        } else {
            genlog.log("Reading textual taems file");
            task = pt.reader.readTTaems(input_script);
            if (task == null) {
		    System.err.println("Error reading structure from " + input_script);
            }
            task.retargetAttributes();
            genlog.log("Textual taems file read!");
            FileOutputStream f2;
        
            if (!nooutput) {
                try {
                    if (output_file == null) {
                        f2 = new FileOutputStream("ttaems_read-1.0");
                        genlog.log("Textual taems file re-printed in ttaems_read-1.0");
                    } else {
                        f2 = new FileOutputStream(output_file+"-1.0");
                        genlog.log("Textual taems file re-printed in " + 
                                   output_file+ "-1.0");
                    }
                    if (f2 != null) {
                        if (task != null)
                            f2.write(task.toTTaems(Taems.V1_0).getBytes());
                        else
                            System.out.println("Read_in is null :(");
                    }
                    f2.close();
                } catch (Exception exc) {
                    exc.printStackTrace();
                    System.err.println(exc);
                }
        
                try {
                    if (output_file == null) {
                        f2 = new FileOutputStream("ttaems_read-1.0A");
                        genlog.log("Textual taems file re-printed in ttaems_read-1.0A");
                    } else {
                        f2 = new FileOutputStream(output_file+"-1.0A");
                        genlog.log("Textual taems file re-printed in " + 
                                   output_file+ "-1.0A");
                    }
                    if (f2 != null) {
                        if (task != null)
                            f2.write(task.toTTaems(Taems.V1_0A).getBytes());
                        else
                            System.out.println("Read_in is null :(");
                    }
                    f2.close();
                } catch (Exception exc) {
                    exc.printStackTrace();
                    System.err.println(exc);
                }

                try {
                    if (output_file == null) {
                        f2 = new FileOutputStream("ttaems_read-1.1");
                        genlog.log("Textual taems file re-printed in ttaems_read-1.1");
                    } else {
                        f2 = new FileOutputStream(output_file+"-1.1");
                        genlog.log("Textual taems file re-printed in " + 
                                   output_file+ "-1.1");
                    }
                    if (f2 != null) {
                        if (task != null)
                            f2.write(task.toTTaems(Taems.V1_1).getBytes());
                        else
                            System.out.println("Read_in is null :(");
                    }
                    f2.close();
                } catch (Exception exc) {
                    exc.printStackTrace();
                    System.err.println(exc);
                }
            }

            if (display)
                showTaems(task);
            else
                System.exit(0);
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
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.getContentPane().add(t, BorderLayout.CENTER);
            frame.setVisible(true);
        }
    }




