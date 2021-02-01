/* Copyright (C) 2005, University of Massachusetts, Multi-Agent Systems Lab
 * See LICENSE for license information
 */

package taems;

import java.awt.*;
import java.awt.event.*;
 
public class ScheduleElementDisplay implements java.awt.event.MouseListener {

  protected ScheduleElement scheduleElement;
  //Display variable
  protected Point localization;
  
  public ScheduleElementDisplay(ScheduleElement l) {
    scheduleElement = l;
    //    addMouseListener(this);
  }

  public void mouseEntered(MouseEvent e) { }
  public void mouseClicked(MouseEvent e) {
    //handleMouseEvent(e);
  }
  public void mouseExited(MouseEvent e) { }
  public void mousePressed(MouseEvent e) {
    //  handleMouseEvent(e);
  }
  public void mouseReleased(MouseEvent e) { }

  public String getPaintLabel() {
    return(getPaintLabel(2048));
  }

  public String getPaintLabel(int max) {
    String label = scheduleElement.getLabel();
    return label;
  }

  

  /**
   * Display methods
   */
  public void paint(Graphics g) {
    FontMetrics fm = g.getFontMetrics();
    int x = (int)localization.x;
    int y = (int)localization.y;
    Dimension d = getSize(g);
    int w = d.width;
    int h = d.height;
    
    // Draw it
    g.fillRect(x - w/2, y - h / 2, w, h);
    g.setColor(Color.black);
    g.drawRect(x - w/2, y - h / 2, w-1, h-1);
    try {
      int numMaxOfCharToDisplay = (d.width + getPaintLabel().length()) / 
	(fm.stringWidth(getPaintLabel()) + 10);

      g.drawString(getPaintLabel(numMaxOfCharToDisplay), 
		   x - (w-10)/2, (y - (h-4)/2) + fm.getAscent());
    }
    catch (java.lang.NullPointerException e)
      {
	e.printStackTrace();
	System.err.println("God damn it, again an error: This is NOT OUR FAULT ");
      }

    //    rect = new Rectangle(x - w/2, y - h/2, w, h); // cache the location
  }
  
  /**
   * Get time increment. How many pixels is one time unit.
   */
  public int getTimeIncrement() {
    return(scheduleElement.getSchedule().getTimeIncrement());
  }
  
  public void setLocation(Point i) {
    localization = i;
  }
  
  public Dimension getSize(Graphics g) {
    Dimension d = new Dimension();
    try {
      FontMetrics fm = g.getFontMetrics();
      if (scheduleElement.getDuration() != -1) 
	d.width = getTimeIncrement() * scheduleElement.getDuration();
      else
	d.width = 0;
      d.height = fm.getHeight() + 4;
      return d;
    }

    catch (java.lang.ArrayIndexOutOfBoundsException e)
      {
	e.printStackTrace();
	System.err.println("God damn it, again an error: This is NOT OUR FAULT ");
	return(new Dimension(60,30));
      }
  }
}
