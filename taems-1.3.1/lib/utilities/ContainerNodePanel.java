package utilities;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashMap;

public class ContainerNodePanel extends NodePanel {
  protected Dimension labelsize = null;
  
  HashMap linklocations;
  
  public ContainerNodePanel(ContainerGraphNode n, String l) {
    super(n, l);
    this.linklocations = new HashMap();
    setLayout(new GraphLayout());
    setOpaque(false);
  }
  
  protected ContainerNodePanel() {
    this((ContainerGraphNode)null, new String(""));
  }
  
  public Dimension getLabelSize() {
    return this.labelsize;
  }
  
  public void repaint() {
    if (getParent() != null) {
      getParent().repaint();
    } else {
      super.repaint();
    } 
  }
  
  public boolean isOptimizedDrawingEnabled() {
    return false;
  }
  
  public void paintComponent(Graphics g) {
    Rectangle rect = new Rectangle(getBounds());
    FontMetrics fm = g.getFontMetrics();
    g.setColor(Color.black);
    g.drawRect(0, 0, rect.width - 1, rect.height - 1);
    try {
      this.labelsize = new Dimension(fm.stringWidth(getLabel()) + H_MARGIN * 2, fm.getHeight());
      if (isSelected()) {
        g.setColor(Color.yellow);
      } else {
        g.setColor(Color.white);
      } 
      g.fillRoundRect(H_MARGIN, rect.height - this.labelsize.height, this.labelsize.width, this.labelsize.height - V_MARGIN, 5, 5);
      g.setColor(getForeground());
      g.drawRoundRect(H_MARGIN, rect.height - this.labelsize.height, this.labelsize.width, this.labelsize.height - V_MARGIN, 5, 5);
      g.drawString(getLabel(), H_MARGIN * 2, rect.height - V_MARGIN * 2);
    } catch (NullPointerException ex) {
      System.err.println("Error drawing label");
      ex.printStackTrace();
    } 
  }
  
  public Point getLinkLocation(String name) {
    return (Point)this.linklocations.get(name);
  }
  
  public void setLinkLocation(String name, Point loc) {
    this.linklocations.put(name, loc);
  }
  
  public Point getLocationForLine() {
    Dimension d = getLabelSize();
    if (d == null)
      d = getSize(); 
    int x = getX() + d.width / 2;
    int y = getY() + getHeight() - d.height / 2;
    return new Point(x, y);
  }
  
  public Point getLocationForLine(GraphNode n) {
    Point tmp = (Point)this.linklocations.get(n);
    if (tmp != null)
      return tmp; 
    return getLocationForLine();
  }
  
  public boolean intersects(Rectangle r) {
    Rectangle r2;
    if ((r2 = getBounds()) != null)
      return r.intersects(r2); 
    return false;
  }
  
  public Dimension getPreferredSize() {
    if (getShape() != null || getBackImage() != null)
      return super.getPreferredSize(); 
    Dimension d = getLayout().preferredLayoutSize(getParent());
    FontMetrics fm = getFontMetrics(getFont());
    int height = d.height + fm.getHeight() + V_MARGIN * 2;
    int width = Math.max(d.width, fm.stringWidth(getLabel()) + H_MARGIN * 4);
    return new Dimension(width, height);
  }
}
