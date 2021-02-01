package utilities;

import java.awt.Graphics;
import java.awt.Point;

public class Arrow {
  private static double AANG = 0.39269908169872414D;
  
  private static int ALEN = 10;
  
  public static void drawArrow(Graphics g, int x1, int y1, int x2, int y2, boolean start, boolean end, double place) {
    double side = 1.0D;
    double theta = Math.atan((y2 - y1) / (x2 - x1));
    double len = Math.sqrt(Math.pow((x2 - x1), 2.0D) + Math.pow((y2 - y1), 2.0D)) * place;
    if (x2 < x1)
      side = -1.0D; 
    if (end) {
      Point tip = new Point((int)(x1 + side * len * Math.cos(theta)), (int)(y1 + side * len * Math.sin(theta)));
      Point side1 = new Point((int)(tip.x - side * ALEN * Math.cos(theta + AANG)), (int)(tip.y - side * ALEN * Math.sin(theta + AANG)));
      Point side2 = new Point((int)(tip.x - side * ALEN * Math.cos(theta - AANG)), (int)(tip.y - side * ALEN * Math.sin(theta - AANG)));
      g.drawLine(x1, y1, x2, y2);
      g.fillPolygon(new int[] { tip.x, side1.x, side2.x }, new int[] { tip.y, side1.y, side2.y }, 3);
    } 
    if (start) {
      side *= -1.0D;
      Point tip = new Point((int)(x2 + side * len * Math.cos(theta)), (int)(y2 + side * len * Math.sin(theta)));
      Point side1 = new Point((int)(tip.x - side * ALEN * Math.cos(theta + AANG)), (int)(tip.y - side * ALEN * Math.sin(theta + AANG)));
      Point side2 = new Point((int)(tip.x - side * ALEN * Math.cos(theta - AANG)), (int)(tip.y - side * ALEN * Math.sin(theta - AANG)));
      g.drawLine(x1, y1, x2, y2);
      g.fillPolygon(new int[] { tip.x, side1.x, side2.x }, new int[] { tip.y, side1.y, side2.y }, 3);
    } 
  }
  
  public static void drawArrowhead(Graphics g, int x1, int y1, int x2, int y2) {
    double side = 1.0D;
    double theta = Math.atan((y2 - y1) / (x2 - x1));
    double len = Math.sqrt(Math.pow((x2 - x1), 2.0D) + Math.pow((y2 - y1), 2.0D));
    if (x2 < x1)
      side = -1.0D; 
    Point tip = new Point((int)(x1 + side * len * Math.cos(theta)), (int)(y1 + side * len * Math.sin(theta)));
    Point side1 = new Point((int)(tip.x - side * ALEN * Math.cos(theta + AANG)), (int)(tip.y - side * ALEN * Math.sin(theta + AANG)));
    Point side2 = new Point((int)(tip.x - side * ALEN * Math.cos(theta - AANG)), (int)(tip.y - side * ALEN * Math.sin(theta - AANG)));
    g.fillPolygon(new int[] { tip.x, side1.x, side2.x }, new int[] { tip.y, side1.y, side2.y }, 3);
  }
  
  public static void drawArrow(Graphics g, int x1, int y1, int x2, int y2, double place) {
    drawArrow(g, x1, y1, x2, y2, false, true, place);
  }
  
  public static void drawArrow(Graphics g, int x1, int y1, int x2, int y2) {
    drawArrow(g, x1, y1, x2, y2, false, true, 1.0D);
  }
}
