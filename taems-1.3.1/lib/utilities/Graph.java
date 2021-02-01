package utilities;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.RepaintManager;
import utilities.cfg.CfgManager;
import utilities.cfg.ConfiguredObject;

public class Graph extends JSplitPane implements Serializable, Cloneable, ConfiguredObject {
  protected Vector nodes = new Vector();
  
  protected String label = "";
  
  protected Hashtable locations = new Hashtable();
  
  private int sequence = -1;
  
  protected GraphLayers layers;
  
  public static Graph globalhack;
  
  String default_layer;
  
  protected static final int H_SPACE = 50;
  
  protected static final int V_SPACE = 50;
  
  protected static final int H_SPACE2 = 50;
  
  protected static final int V_SPACE2 = 50;
  
  protected static final int H_MARGIN = 50;
  
  protected static final int V_MARGIN = 50;
  
  protected JTabbedPane display;
  
  protected JTextArea text;
  
  GraphNode selected;
  
  boolean reset;
  
  boolean load;
  
  protected boolean dolayersdisplayhack;
  
  public Graph() {
    this("");
  }
  
  public String getLabel() {
    return this.label;
  }
  
  public void setLabel(String l) {
    this.label = l;
  }
  
  public GraphLayers getLayers() {
    return this.layers;
  }
  
  public Graph(String l) {
    this.default_layer = "default";
    this.text = new JTextArea();
    this.selected = null;
    this.reset = false;
    this.load = true;
    this.dolayersdisplayhack = true;
    this.label = l;
    globalhack = this;
    initGraphics();
  }
  
  public void setDefaultLayer(String s) {
    if (s != null)
      this.default_layer = s; 
  }
  
  public void addNode(GraphNode n) {
    JPanel p = n.getDefaultPanel();
    if (p instanceof NodePanel)
      ((NodePanel)p).setLabel(n.getLabel()); 
    addNode(n, p, this.default_layer);
  }
  
  public void addNode(GraphNode n, JPanel p, String layer) {
    if (!this.nodes.contains(n)) {
      CfgManager manager = CfgManager.getManager(this);
      if (manager != null)
        manager.addConfiguredObject(n, manager.getObjectKey(this) + "/nodes/" + n.getLabel()); 
      this.nodes.addElement(n);
      GraphNodeFinderEnumeration.invalidateCache();
    } 
    this.layers.addToLayer(layer, n, p);
    this.display.invalidate();
  }
  
  public void addTree(GraphNode n) {
    Enumeration e = new GraphNodeFinderEnumeration(n, null);
    while (e.hasMoreElements()) {
      GraphNode m = e.nextElement();
      addNode(m);
    } 
  }
  
  public void removeNode(GraphNode n) {
    this.layers.eliminate(n);
    this.nodes.removeElement(n);
    GraphNodeFinderEnumeration.invalidateCache();
    this.display.invalidate();
  }
  
  public void exciseNode(GraphNode n) {
    removeNode(n);
    n.excise();
  }
  
  public Enumeration getNodes() {
    return this.nodes.elements();
  }
  
  public Enumeration getAllNodes() {
    return findNodes((GraphNode)null);
  }
  
  public Enumeration findNodes(GraphNode n) {
    if (n == null) {
      if (!GraphNodeFinderEnumeration.checkCache(this.sequence))
        this.sequence = GraphNodeFinderEnumeration.cacheGraph(getNodes(), (GraphNode)null); 
      return new GraphNodeFinderEnumeration(getNodes(), null, this.sequence);
    } 
    return new GraphNodeFinderEnumeration(getNodes(), n);
  }
  
  public GraphNode findNode(GraphNode n) {
    Enumeration e = findNodes(n);
    if (e.hasMoreElements())
      return e.nextElement(); 
    return null;
  }
  
  public void mergeGraph(Graph g) {
    Enumeration e = g.getNodes();
    while (e.hasMoreElements())
      mergeNode(e.nextElement()); 
  }
  
  public void unmergeGraph(Graph g) {
    Enumeration e = g.getNodes();
    while (e.hasMoreElements())
      unmergeNode(e.nextElement()); 
  }
  
  protected void mergeNode(GraphNode n) {
    this.nodes.addElement(n);
  }
  
  protected void unmergeNode(GraphNode n) {
    this.nodes.removeElement(n);
  }
  
  public Object clone() {
    Graph cloned = null;
    try {
      cloned = (Graph)super.clone();
    } catch (Exception exception) {
      System.out.println("Clone Error: " + exception);
    } 
    if (this.label != null) {
      cloned.setLabel(new String(this.label));
    } else {
      cloned.setLabel("");
    } 
    Enumeration e = getNodes();
    cloned.nodes = new Vector();
    while (e.hasMoreElements())
      cloned.addNode((GraphNode)((GraphNode)e.nextElement()).clone()); 
    return cloned;
  }
  
  public String toString() {
    return this.label;
  }
  
  protected JPanel createCanvas(String name) {
    JPanel temp = new GraphCanvas(name);
    temp.setLayout(new GraphLayout(name, this.text));
    return temp;
  }
  
  protected void dldh(boolean b) {
    this.dolayersdisplayhack = b;
  }
  
  public void addLayer(String s) {
    this.layers.addLayer(s);
  }
  
  public void addToLayer(String layer, GraphNode n, JPanel p) {
    this.layers.addToLayer(layer, n, p);
  }
  
  protected void initGraphics() {
    this.display = new JTabbedPane();
    this.layers = new GraphLayers(this.display);
    setOrientation(1);
    setLabel(getLabel());
    JScrollPane sp;
    setRightComponent(sp = new JScrollPane());
    sp.setMinimumSize(new Dimension(0, 0));
    sp.setViewportView(this.display);
    JPanel p = new JPanel();
    sp = new JScrollPane();
    sp.setMinimumSize(new Dimension(0, 0));
    sp.setViewportView(this.text);
    sp.setPreferredSize(new Dimension(200, sp.getHeight()));
    p.add(sp);
    if (this.dolayersdisplayhack) {
      p.setLayout(new GridLayout(3, 1));
      try {
        String http = "http://mas.cs.umass.edu/images/masl.gif";
        if (System.getProperty("MASL") != null)
          http = System.getProperty("MASL"); 
        JPanel r = new JPanel();
        JPanel plugh = new JPanel();
        JImageComponent s = new JImageComponent(new URL(http));
        s.setMinimumSize(new Dimension(0, 0));
        plugh.setLayout(new GridLayout(1, 1));
        plugh.add(s);
        r.add(plugh);
        r.setPreferredSize(s.getPreferredSize());
        p.add(r);
      } catch (MalformedURLException e) {
        System.err.println("Error finding image: " + e);
      } 
      JPanel q = new JPanel();
      p.add(q);
      this.layers.setDisplay(this.display);
      setLeftComponent(p);
    } else {
      setLeftComponent(sp);
    } 
  }
  
  public void addMenuItem(JMenuItem item) {
    Enumeration e = getLayers().getLayers();
    while (e.hasMoreElements()) {
      String s = e.nextElement();
      GraphLayer g = getLayers().getLayer(s);
      JPanel p = g.getPanel();
      if (p.getLayout() instanceof GraphLayout)
        ((GraphLayout)p.getLayout()).addMenuItem(item); 
    } 
  }
  
  public void updateCfg() {}
  
  public void saveCfg() {}
  
  public class GraphCanvas extends JPanel {
    private transient Image offscreen;
    
    Dimension dim;
    
    public GraphNode selectedinternal;
    
    private boolean thorough_placement;
    
    String canvas_name;
    
    private final Graph this$0;
    
    public GraphCanvas(Graph this$0, String n) {
      Graph.this = Graph.this;
      this.dim = getMinimumSize();
      this.thorough_placement = false;
      this.canvas_name = null;
      this.canvas_name = n;
    }
    
    public GraphCanvas() {
      this(null);
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
      if (!Graph.this.layers.isObjectVisible(edge))
        return; 
      int np = 0;
      float[] px = new float[5];
      float[] py = new float[5];
      g.setColor(Color.black);
      NodePanel frompanel = null, topanel = null, edgepanel = null;
      GraphNode from = edge.getFrom();
      GraphNode to = edge.getTo();
      JPanel t;
      if ((t = Graph.this.layers.getTopPanel(from)) != null && t instanceof NodePanel)
        frompanel = (NodePanel)t; 
      if ((t = Graph.this.layers.getTopPanel(to)) != null && t instanceof NodePanel)
        topanel = (NodePanel)t; 
      if ((t = Graph.this.layers.getTopPanel(edge)) != null && t instanceof NodePanel)
        edgepanel = (NodePanel)t; 
      if (frompanel != null && Graph.this.layers.isObjectVisible(from))
        if (topanel != null && Graph.this.layers.isObjectVisible(to)) {
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
      if (topanel != null && Graph.this.layers.isObjectVisible(to))
        if (frompanel != null && Graph.this.layers.isObjectVisible(from)) {
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
      Enumeration e = Graph.this.getAllNodes();
      while (e.hasMoreElements()) {
        GraphNode n = e.nextElement();
        NodePanel npanel = null;
        JPanel t;
        if ((t = Graph.this.layers.getTopPanel(n)) != null && t instanceof NodePanel)
          npanel = (NodePanel)t; 
        if (npanel == null || !Graph.this.layers.isObjectVisible(n))
          continue; 
        if (n instanceof GraphEdge) {
          paintEdge(g, (GraphEdge)n);
          continue;
        } 
        Enumeration f = n.getUndirEdges();
        while (f.hasMoreElements()) {
          GraphNode node = f.nextElement();
          NodePanel nodepanel = null;
          if ((t = Graph.this.layers.getTopPanel(node)) != null && t instanceof NodePanel)
            nodepanel = (NodePanel)t; 
          if (nodepanel == null || node instanceof GraphEdge || !Graph.this.layers.isObjectVisible(node))
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
      Enumeration e = Graph.this.getAllNodes();
      while (e.hasMoreElements()) {
        GraphNode n = e.nextElement();
        JPanel p = Graph.this.layers.getTopPanel(n);
        if (p instanceof NodePanel)
          ((NodePanel)p).paintBackImage(g); 
      } 
      paintLines(g);
    }
  }
}
