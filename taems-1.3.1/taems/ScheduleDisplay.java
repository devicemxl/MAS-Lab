/* Copyright (C) 2005, University of Massachusetts, Multi-Agent Systems Lab
 * See LICENSE for license information
 */

package taems;

import java.awt.event.*;
import java.awt.*;
import java.util.*;

public class ScheduleDisplay extends java.awt.Panel implements java.awt.event.MouseListener {

  protected Schedule schedule;
  //Display variable
  protected Point localisation;
  protected Vector scheduleElements;
  protected int timeIncrement=1;

  public ScheduleDisplay(Schedule s) {
    schedule = s;
    scheduleElements = new Vector();
    addMouseListener(this);
    Enumeration e = s.getScheduleElements().elements();
    while (e.hasMoreElements()) {
      ScheduleElement se = (ScheduleElement)e.nextElement();
      scheduleElements.addElement(se.getDisplay());
    }
  }

  public void mouseClicked(MouseEvent e) {
    //handleMouseEvent(e);
  }
  public void mouseEntered(MouseEvent e) { }

  public void mouseExited(MouseEvent e) { }
  public void mousePressed(MouseEvent e) {
    //  handleMouseEvent(e);
  }
  public void mouseReleased(MouseEvent e) { }

  public int getTimeIncrement() { return timeIncrement; }
  public void setTimeIncrement(int time) { 
    timeIncrement = time;
    update(null,true);
  }

  public void update() { update(null, false); }
  public void update(Graphics g, boolean force) {
    if (force) {
      Enumeration e = scheduleElements.elements();
      int x = localisation.x;
      int y = localisation.y;
      while (e.hasMoreElements()) {
	ScheduleElementDisplay se = (ScheduleElementDisplay)e.nextElement();
	se.setLocation(new Point(x,y));
	x = x + se.getSize(g).width ;
      }
    }
  }
  
  public void setLocation(Point d) {
    localisation = d;
    update(null,true);
  }
      
  public void paint(Graphics g) {
    update(g, false);
    Enumeration e = scheduleElements.elements();
    while (e.hasMoreElements()) {
      ScheduleElementDisplay se = (ScheduleElementDisplay)e.nextElement();
      se.paint(g);
    }
  }
  
  public void setSize(Dimension d) {
    int incT = d.width / schedule.getTotalDuration();
    setTimeIncrement(incT);
  }


}
