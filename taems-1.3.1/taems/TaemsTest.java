/* Copyright (C) 2005, University of Massachusetts, Multi-Agent Systems Lab
 * See LICENSE for license information
 */

/***************************************************************************************
 *	TaemsTest.java
 ***************************************************************************************/

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


/**
 * This is in here for internal testing purposes, you will never need to use it
 * (except as a code reference, if you have the source).
 */
public class TaemsTest extends Panel implements  Runnable {
	
  /**
   */
  protected TaemsTest() {
  }

  /**
   */
  public synchronized void run() {

    System.err.println("Test has started.\n");

    Agent agent = new Agent("Joe");

    Taems taems = new Taems("Objective", agent);
    Task root = new Task("Root_Node", agent, new SeqLastQAF());
    Task t1 = new Task("T1", agent, new SeqMinQAF());
    Task t2 = new Task("T2", agent, new MinQAF());
    Method m1 = new Method("M1", agent);
    Method m2 = new Method("M2", agent);
    Method m3 = new Method("M3", agent);
    Method nl3 = new Method("M3", agent);
    nl3.setNonLocal(true);
    Resource r1 = new ConsumableResource("R1", null, 10, 0, 10);
		r1.setState(11);
    Resource r2 = new ConsumableResource("R2", null, 30, 0, 100);

    Interrelationship i1 = new ConsumesInterrelationship("I1", agent, new Distribution((float)1.0, (float)1.0));
    Interrelationship i2 = new EnablesInterrelationship("I2", agent, new Distribution((float)1.0, (float)1.0));
    Interrelationship i3 = new ProducesInterrelationship("I3", agent, new Distribution((float)1.0, (float)1.0));
    Interrelationship i4 = new DisablesInterrelationship("I4", agent, new Distribution((float)1.0, (float)1.0));
    Interrelationship i5 = new FacilitatesInterrelationship("I5", agent, new Distribution((float)1.0, (float)1.0), new Distribution((float)1.0, (float)1.0), new Distribution((float)1.0, (float)1.0), new Distribution((float)1.0, (float)1.0));

    taems.addNode(root);
    taems.addNode(r1);
    taems.addNode(r2);
    taems.addNode(new VirtualNode("V1", agent));
    t2.addSubtask(new VirtualTaskBase("V2", agent));
    t2.addSubtask(nl3);
    t2.addSubtask(new VirtualTaskBase("M2", agent));
    taems.addNode(t2);

    t2.setAttribute("weighting_factor", new Float(0.3));
    m3.setAttribute("weighting_factor", new Float(0.7));
    m1.setAttribute("weighting_factor", new Float(0.5));
    m2.setAttribute("weighting_factor", new Float(0.5));

    root.addSubtask(new VirtualTaskBase("M1", null));
    taems.addNode(m1);
    root.addSubtask(new VirtualTaskBase("M2", null));
    taems.addNode(m2);
    root.addSubtask(t1);
    t1.addSubtask(m3);
    m1.addInterrelationship(i1, null, new VirtualNode("R1"));
    t2.addInterrelationship(i2, null, m2);
    m1.addInterrelationship(i3, null, r1);
    m2.addInterrelationship(i4, null, new VirtualNode("T1"));		
    root.addInterrelationship(i5, null, new VirtualNode("T2"));
    taems.retargetVirtuals();

    m1.addOutcome(new Outcome("O1", new Distribution(1.0,1.0),new Distribution(1.0,1.0),new Distribution(1.0,1.0),(float)1.0));
    m2.addOutcome(new Outcome("O2", new Distribution(1.0,1.0),new Distribution(1.0,1.0),new Distribution(1.0,1.0),(float)1.0));
    m3.addOutcome(new Outcome("O3", new Distribution(1.0,1.0),new Distribution(1.0,1.0),new Distribution(1.0,1.0),(float)1.0));

    m1.setAttribute("Dummy", "Some data");
    m1.setAttribute("Dummy2", "This\r\rshould\rnot print");
    m1.setAttribute("Dummy 3", "Neither should this");
    m1.setAttribute("Dummy4", "This is fine");
    m1.setStartTime(0);
    m1.setFinishTime(10);
    m1.setCurrentQuality((float)2.0);
    m1.setScheduleNumber(1);
    m2.setStartTime(10);
    m2.setFinishTime(20);
    m2.setCurrentQuality((float)3.2);
    m3.setStartTime(20);
    m3.setFinishTime(30);
    m3.setCurrentQuality((float)4.6);
    m3.setScheduleNumber(2);
    taems.setAttribute("bubba", "bubba");

    Interrelationship i7 = new FacilitatesInterrelationship("I7", agent, new Distribution((float)1.0, (float)1.0), new Distribution((float)1.0, (float)1.0), new Distribution((float)1.0, (float)1.0), new Distribution((float)1.0, (float)1.0));
    new VirtualNode("i7v").addInterrelationship(i7, null, new VirtualNode("R1"));
    taems.addNode(i7.getFrom());
    taems.addNode(i7.getTo());
    Interrelationship i8 = new FacilitatesInterrelationship("I8", agent, new Distribution((float)1.0, (float)1.0), new Distribution((float)1.0, (float)1.0), new Distribution((float)1.0, (float)1.0), new Distribution((float)1.0, (float)1.0));
    new VirtualNode("R2").addInterrelationship(i8, null, new VirtualNode("M1_2"));
    taems.addNode(i8.getFrom());
    taems.addNode(i8.getTo());

    Method m4 = new Method("M4", agent);
    m4.setNonLocal(true);
    taems.addNode(m4);

    Vector v = new Vector();
    v.addElement(new VirtualTaskBase("MX", agent));
    v.addElement(new VirtualTaskBase("M3", agent));
    Commitment c = new Commitment("Dum Label", Commitment.DO, agent, new Agent("Bob"), v, -1, -1, -1, -1, -1);
    taems.addCommitment(c);
    v = new Vector();
    v.addElement(new VirtualTaskBase("M2", agent));
    v.addElement(new VirtualTaskBase("M4", agent));
    c = new Commitment("Dum Label 2", Commitment.DO, agent, new Agent("Bob"), v, -1, -1, -1, -1, -1);
    taems.addCommitment(c);

    taems.retargetVirtuals();

    Taems taems_2 = new Taems();
    taems_2.addAgent(new Agent("Joe2"));
    Task root_2 = new Task("Root_Node_With_A_Long_Name_Hoo_Ha", agent, new MinQAF());
    Method m1_2 = new Method("M1_2", agent);
    taems_2.addNode(root_2);
    root_2.addSubtask(m1_2);
    Interrelationship i6 = new FacilitatesInterrelationship("I6", agent, new Distribution((float)1.0, (float)1.0), new Distribution((float)1.0, (float)1.0), new Distribution((float)1.0, (float)1.0), new Distribution((float)1.0, (float)1.0));
    m1_2.addInterrelationship(i6, null, new VirtualNode("M3"));
    m1_2.setFinishTime(20);
    taems_2.addNode(i6.getTo());

    taems.addTaems(taems_2);
    taems.removeTaems(taems_2);

    m1_2.setFinishTime(Integer.MIN_VALUE);
    i6.setActive(true);
    i6.setLabel("i6_2");
    root_2.setLabel("Unioned Root");

    taems.unionTaems(taems_2);
    //taems.importTaems(taems_2);
    //taems.addTaems(taems_2);

    taems.smash();
    taems.retargetVirtuals();

    //taems = taems.removeTaems(taems_2);
    taems = taems.removeTaems(taems);
    taems.removeTaems(taems_2);
    Taems taemsclone = (Taems)taems.clone();

    // Test it
    Enumeration e = taems.getAllNodes();
    while (e.hasMoreElements()) {
        Node onode = (Node)e.nextElement();
        Node nnode = taemsclone.findNode(onode);
        if (nnode == null) {
            System.err.println(onode.getLabel() + ": Whoops, searched for node in clone returned null");
        } else if (nnode == onode) {
            System.err.println(onode.getLabel() + ": Whoops, searched for node in clone was the same as original");
        } else if (nnode != onode) {
            System.err.println(onode.getLabel() + ": good");
        }
    }

    showTaems(taemsclone);


    if (t2.matches(t2)) 
        System.err.println("t2 matches t2, which is good");
    else
        System.err.println("t2 does not match t2, which is bad");

    if (root.matches(root)) 
        System.err.println("root matches root, which is good");
    else
        System.err.println("root does not match root, which is bad");

    if (root.matches(root_2)) 
        System.err.println("root matches root_2, which is bad");
    else
        System.err.println("root not match root_2, which is good");

    /*
    taems = new Taems("Objective", agent);
    t1 = new Task("T1", agent, new SeqSumQAF());
    m1 = new Method("M1", agent);
    t1.addSubtask(m1);

    m2 = new Method("M2", agent);

    root = new Task("Root_Node", agent, new SeqMinQAF());

    taems.addNode((Task)t1.clone());
    root.addSubtask(m1);

    System.err.println(t1.toString());
    System.err.println(taems.toString());
    */

    /*
    System.err.println("--- Exact");
    e = taems.findNodes(new Node("M1", null));
    while (e.hasMoreElements()) {
      System.err.println(e.nextElement());
    }

    System.err.println("--- Star");
    e = taems.findNodes(new Node("M1*", null));
    while (e.hasMoreElements()) {
      System.err.println(e.nextElement());
    }

    System.err.println("--- Affecting");
    e = m3.getAffectingInterrelationships();
    while (e.hasMoreElements()) {
      System.err.println(e.nextElement());
    }

    /*System.err.println("--- Affected");
    e = m1.getAffectedInterrelationships();
    while (e.hasMoreElements()) {
      System.err.println(e.nextElement());
    }

    System.err.println("--- Produces");
    //Enumeration e = root.findNodes(new Task("T", null, new MinQAF()));
    e = root.findNodes(new ProducesInterrelationship());
    while (e.hasMoreElements()) {
      System.err.println(e.nextElement());
    }
    */
  }

  private static final int INITIAL_WIDTH = 500;
  private static final int INITIAL_HEIGHT = 400;
  public void showTaems(Taems t) {
    JFrame frame;

    frame = new JFrame("Taems"); 

    frame.setSize(INITIAL_WIDTH, INITIAL_HEIGHT);
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    frame.setLocation(screenSize.width/2 - INITIAL_WIDTH/2,
		      screenSize.height/2 - INITIAL_HEIGHT/2);
    frame.getContentPane().add(t, BorderLayout.CENTER);
    frame.setVisible(true);
  }

  /**
   * Main function. This function read the configuration file, all generator 
   * grammars and the resources scripting file.
   */
  public static void main(String args[]) {
    
    TaemsTest test = new TaemsTest();
    test.run();
  }
}
