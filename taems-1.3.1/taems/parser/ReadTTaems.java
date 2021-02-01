/* Copyright (C) 2005, University of Massachusetts, Multi-Agent Systems Lab
 * See LICENSE for license information
 */

package taems.parser;


/* Global Inclues */
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
 */
public class ReadTTaems  {
    private static TTaemsGrammar parser = null;
    private static Integer lock = new Integer(1);
    private Log log;

    /**
     */
    public ReadTTaems(Log l) {
        log = l;
    }


    public Taems readTTaems (java.io.InputStream stream) {
        if (stream == null) { return null; }

        synchronized (lock) {
            try {
                if (parser == null)
                    parser = new TTaemsGrammar(stream);
                else
                    parser.ReInit(stream);
            } catch (Exception e) { 
                e.printStackTrace();
                System.err.println(e);}
            
            return runParser(parser);
        }
    }

    public Taems readTTaems (String filename) {
        FileInputStream fis = null;
        Taems task;
        log.log("Reading textual taems file "+ filename);
    
        try {
            fis = new FileInputStream(filename);
        } catch (Exception e) {      
            log.log("Error finding textual taems input file "+ filename);
        } 
    
        log.log("File found and opened");

        synchronized (lock) {    
            if (fis == null) { return null; }
            try {
                if (parser == null)
                    parser = new TTaemsGrammar(fis);
                else
                    parser.ReInit(fis);
            } catch (Exception e) { 
                e.printStackTrace();
                System.err.println(e);}
    
            task = runParser(parser);
        }

        try {
            fis.close();
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
            System.err.println(ioe);
        }
        return(task);
    }


    public Taems readTTaems (Reader r) {
    
        if (r == null)
            return null;
    
        synchronized (lock) {
            try {
                if (parser == null)
                    parser = new TTaemsGrammar(r);
                else
                    parser.ReInit(r);
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println(e);}

            return runParser(parser);
        }
    }

    private Taems runParser(TTaemsGrammar p) {
        Taems task_structure = null;
        Log log = Log.getDefault();
	
        try {
            log.log("Parser opened");
	
            if (parser == null) {
                log.log("Parser opening failed");
                return null;
            }
            task_structure = p.Input(log);
	
            log.log("parsed successfully");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e);}

        return task_structure;
    }
}

