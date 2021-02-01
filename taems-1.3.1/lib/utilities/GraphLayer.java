package utilities;

import java.awt.Component;
import java.awt.Point;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.swing.JPanel;
import utilities.cfg.CfgManager;
import utilities.cfg.ConfiguredObject;

public class GraphLayer implements ConfiguredObject {
  protected Hashtable members;
  
  protected Hashtable components;
  
  protected Hashtable locations;
  
  protected String name;
  
  boolean visible = false;
  
  boolean willsave = true;
  
  String savefile;
  
  JPanel panel;
  
  public GraphLayer(String n, JPanel p) {
    this.name = n;
    this.locations = new Hashtable();
    this.members = new Hashtable();
    this.components = new Hashtable();
    this.savefile = new String("resourcenodes-" + this.name + ".cfg");
    if (p == null) {
      this.panel = Graph.globalhack.createCanvas(this.savefile);
    } else {
      this.panel = p;
    } 
  }
  
  public GraphLayer(String n) {
    this(n, null);
  }
  
  public GraphLayer() {
    this("default");
  }
  
  public String getName() {
    return this.name;
  }
  
  public void setName(String n) {
    this.name = n;
  }
  
  public JPanel getPanel() {
    return this.panel;
  }
  
  public boolean isVisible() {
    return true;
  }
  
  public boolean willSave() {
    return this.willsave;
  }
  
  public void setWillSave(boolean b) {
    this.willsave = b;
  }
  
  public GraphNode getNode(Component c) {
    if (this.components.containsKey(c))
      return (GraphNode)this.components.get(c); 
    return null;
  }
  
  protected String getSaveName(Component c) {
    String s = null;
    GraphNode n;
    if ((n = getNode(c)) != null) {
      if (n instanceof GraphNode) {
        s = n.getLabel();
        if ((s == null || s == "") && n instanceof GraphEdge)
          s = "Edge: " + ((GraphEdge)n).getFrom().getLabel() + " to " + ((GraphEdge)n).getTo().getLabel(); 
      } 
    } else {
      s = String.valueOf(c.hashCode());
    } 
    return s;
  }
  
  public void storeNodePlacement(Component c) {
    String s = getSaveName(c);
    if (s != null && s != "")
      this.locations.put(s, c.getLocation()); 
  }
  
  public Point restoreNodePlacement(Component c) {
    String s = getSaveName(c);
    Point loading = null;
    if (s != null && this.locations.containsKey(s))
      loading = (Point)this.locations.get(s); 
    if (loading != null)
      loading = new Point(loading.x, loading.y); 
    return loading;
  }
  
  public boolean hasStoredPlace(Component c) {
    return (getSaveName(c) != null && this.locations.containsKey(getSaveName(c)));
  }
  
  public void addToLayer(GraphNode n, Component c) {
    if (n != null && !this.members.containsKey(n)) {
      if (c == null)
        c = n.getDefaultPanel(); 
      this.members.put(n, c);
      this.components.put(c, n);
      this.panel.add(c, n);
      CfgManager manager = CfgManager.getManager(n);
      if (manager != null && c instanceof ConfiguredObject)
        manager.addConfiguredObject((ConfiguredObject)c, manager.getObjectKey(n) + "/layers/" + getName()); 
    } 
  }
  
  public synchronized void removeFromLayer(GraphNode n) {
    if (n != null && this.members.containsKey(n)) {
      this.panel.remove((JPanel)this.members.get(n));
      this.components.remove(this.members.get(n));
      this.members.remove(n);
    } 
  }
  
  public boolean contains(Object n) {
    return this.members.containsKey(n);
  }
  
  public boolean isPlaced(GraphNode n) {
    if (contains(n))
      return this.locations.containsKey(getSaveName((Component)this.members.get(n))); 
    return false;
  }
  
  public Enumeration elements() {
    return this.members.keys();
  }
  
  public Enumeration panels() {
    return this.members.elements();
  }
  
  public JPanel getPanel(GraphNode n) {
    return (JPanel)this.members.get(n);
  }
  
  public int hashCode() {
    return this.name.hashCode();
  }
  
  public synchronized void updateCfg() {}
  
  public synchronized void saveCfg() {}
}
