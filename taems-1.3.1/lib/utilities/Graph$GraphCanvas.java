package utilities;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.util.Enumeration;
import javax.swing.JPanel;
import javax.swing.RepaintManager;

public class GraphCanvas extends JPanel {
  private transient Image offscreen;
  
  Dimension dim;
  
  public GraphNode selectedinternal;
  
  private boolean thorough_placement;
  
  String canvas_name;
  
  private final Graph this$0;
  
  public GraphCanvas(Graph this$0, String n) {
    this.this$0 = this$0;
    this.dim = getMinimumSize();
    this.thorough_placement = false;
    this.canvas_name = null;
    this.canvas_name = n;
  }
  
  public GraphCanvas(Graph this$0) {
    this(this$0, null);
  }
  
  public boolean isOptimizedDrawingEnabled() {
    return false;
  }
  
  public String toString() {
    return new String("GraphCanvas '" + this.canvas_name + "'");
  }
  
  public void paint(Graphics g) {
    RepaintManager.currentManager(this).markCompletelyDirty(this);
    super.paint(g);
    RepaintManager.currentManager(this).markCompletelyClean(this);
  }
  
  public void paintEdge(Graphics g, GraphEdge edge) {
    if (!this.this$0.layers.isObjectVisible(edge))
      return; 
    int np = 0;
    float[] px = new float[5];
    float[] py = new float[5];
    g.setColor(Color.black);
    NodePanel frompanel = null, topanel = null, edgepanel = null;
    GraphNode from = edge.getFrom();
    GraphNode to = edge.getTo();
    JPanel t;
    if ((t = this.this$0.layers.getTopPanel(from)) != null && t instanceof NodePanel)
      frompanel = (NodePanel)t; 
    if ((t = this.this$0.layers.getTopPanel(to)) != null && t instanceof NodePanel)
      topanel = (NodePanel)t; 
    if ((t = this.this$0.layers.getTopPanel(edge)) != null && t instanceof NodePanel)
      edgepanel = (NodePanel)t; 
    if (frompanel != null && this.this$0.layers.isObjectVisible(from))
      if (topanel != null && this.this$0.layers.isObjectVisible(to)) {
        Point tmp = frompanel.getLocationForLine(to);
        px[np] = tmp.x;
        py[np] = tmp.y;
        np++;
        if (!tmp.equals(frompanel.getLocationForLine())) {
          g.setColor(Color.black);
          g.fillRoundRect(tmp.x - 4, tmp.y - 4, 8, 8, 8, 8);
        } 
      }  
    px[np] = (edgepanel.getLocationForLine()).x;
    py[np] = (edgepanel.getLocationForLine()).y;
    np++;
    g.setColor(Color.black);
    if (topanel != null && this.this$0.layers.isObjectVisible(to))
      if (frompanel != null && this.this$0.layers.isObjectVisible(from)) {
        Point tmp = topanel.getLocationForLine(from);
        px[np] = tmp.x;
        py[np] = tmp.y;
        np++;
        if (!tmp.equals(topanel.getLocationForLine())) {
          g.setColor(Color.black);
          g.fillRoundRect(tmp.x - 2, tmp.y - 2, 4, 4, 4, 4);
        } 
      }  
    if (np <= 1)
      return; 
    Spline spline = new Spline(px, py, np);
    spline.Generate();
    spline.draw(g);
    if (edge.getDrawArrows()) {
      Point p1 = spline.getPoint(0.15F);
      Point p2 = spline.getPoint(0.25F);
      Arrow.drawArrowhead(g, p1.x, p1.y, p2.x, p2.y);
      p1 = spline.getPoint(0.75F);
      p2 = spline.getPoint(0.85F);
      Arrow.drawArrowhead(g, p1.x, p1.y, p2.x, p2.y);
    } 
  }
  
  public void paintLines(Graphics g) {
    g.setColor(Color.black);
    Enumeration e = this.this$0.getAllNodes();
    while (e.hasMoreElements()) {
      GraphNode n = e.nextElement();
      NodePanel npanel = null;
      JPanel t;
      if ((t = this.this$0.layers.getTopPanel(n)) != null && t instanceof NodePanel)
        npanel = (NodePanel)t; 
      if (npanel == null || !this.this$0.layers.isObjectVisible(n))
        continue; 
      if (n instanceof GraphEdge) {
        paintEdge(g, (GraphEdge)n);
        continue;
      } 
      Enumeration f = n.getUndirEdges();
      while (f.hasMoreElements()) {
        GraphNode node = f.nextElement();
        NodePanel nodepanel = null;
        if ((t = this.this$0.layers.getTopPanel(node)) != null && t instanceof NodePanel)
          nodepanel = (NodePanel)t; 
        if (nodepanel == null || node instanceof GraphEdge || !this.this$0.layers.isObjectVisible(node))
          continue; 
        Point tmp = npanel.getLocationForLine(node);
        g.drawLine((nodepanel.getLocationForLine()).x, (nodepanel.getLocationForLine()).y, tmp.x, tmp.y);
        if (!tmp.equals(npanel.getLocationForLine())) {
          g.setColor(Color.black);
          g.fillRoundRect(tmp.x - 2, tmp.y - 2, 4, 4, 4, 4);
        } 
      } 
    } 
  }
  
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    Enumeration e = this.this$0.getAllNodes();
    while (e.hasMoreElements()) {
      GraphNode n = e.nextElement();
      JPanel p = this.this$0.layers.getTopPanel(n);
      if (p instanceof NodePanel)
        ((NodePanel)p).paintBackImage(g); 
    } 
    paintLines(g);
  }
}
