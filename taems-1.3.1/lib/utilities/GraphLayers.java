package utilities;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import utilities.cfg.ConfiguredObject;

public class GraphLayers implements ConfiguredObject {
  Hashtable layers;
  
  Vector visible_layers;
  
  static GraphLayers global_hack = null;
  
  JTabbedPane panel;
  
  public static GraphLayers getGlobal() {
    return global_hack;
  }
  
  public GraphLayers(JTabbedPane p) {
    this.panel = null;
    this.panel = p;
    this.layers = new Hashtable();
    this.visible_layers = new Vector();
    addLayer("default");
    global_hack = this;
  }
  
  public void addLayer(String name) {
    GraphLayer g = new GraphLayer(name);
    this.panel.addTab(name, g.getPanel());
    this.layers.put(name, g);
  }
  
  public void removeLayer(String name) {
    this.layers.remove(name);
    if (this.visible_layers.contains(name))
      this.visible_layers.removeElement(name); 
  }
  
  public void addToLayer(String name, GraphNode mem, JPanel panel) {
    if (!this.layers.containsKey(name))
      addLayer(name); 
    GraphLayer g = (GraphLayer)this.layers.get(name);
    if (!g.contains(mem))
      g.addToLayer(mem, panel); 
  }
  
  public void removeFromLayer(String name, GraphNode mem) {
    if (this.layers.containsKey(name)) {
      GraphLayer g = (GraphLayer)this.layers.get(name);
      if (g.contains(mem))
        g.removeFromLayer(mem); 
    } 
  }
  
  public Enumeration getLayers() {
    return this.layers.keys();
  }
  
  public Enumeration getLayers(GraphNode mem) {
    Enumeration e = this.layers.keys();
    Enumeration f = this.layers.elements();
    Vector v = new Vector();
    while (e.hasMoreElements()) {
      if (((GraphLayer)f.nextElement()).contains(mem)) {
        v.addElement(e.nextElement());
        continue;
      } 
      e.nextElement();
    } 
    return v.elements();
  }
  
  public void eliminate(GraphNode mem) {
    Enumeration e = getLayers(mem);
    while (e.hasMoreElements())
      removeFromLayer(e.nextElement(), mem); 
  }
  
  public String getVisible() {
    Enumeration e = getLayers();
    while (e.hasMoreElements()) {
      String s = e.nextElement();
      if (getLayer(s).getPanel() == this.panel.getSelectedComponent())
        return s; 
    } 
    return null;
  }
  
  public boolean isVisible(String name) {
    return true;
  }
  
  public void setVisible(String name, boolean b) {
    GraphLayer l = getLayer(name);
  }
  
  public boolean isObjectVisible(GraphNode mem) {
    String s = getVisible();
    return (isInLayer(s, mem) && isVisible(s));
  }
  
  public JPanel getPanel(String layer, GraphNode n) {
    if (layer != null && isInLayer(layer, n))
      return getLayer(layer).getPanel(n); 
    return null;
  }
  
  public JPanel getTopPanel(GraphNode n) {
    String s = getVisible();
    if (s != null && isInLayer(s, n))
      return getLayer(s).getPanel(n); 
    return null;
  }
  
  public boolean isInLayer(String layer, GraphNode mem) {
    return ((GraphLayer)this.layers.get(layer)).contains(mem);
  }
  
  public GraphLayer getLayer(String name) {
    if (!this.layers.containsKey(name))
      return null; 
    return (GraphLayer)this.layers.get(name);
  }
  
  protected void addCBForLayer(String s) {
    JCheckBox cb = new JCheckBox(s, isVisible(s));
    cb.addActionListener(new ActionListener(this) {
          private final GraphLayers this$0;
          
          public void actionPerformed(ActionEvent e) {
            JCheckBox ch = (JCheckBox)e.getSource();
            this.this$0.setVisible(ch.getText(), !this.this$0.isVisible(ch.getText()));
          }
        });
    this.panel.add(cb);
  }
  
  protected void setDisplay(JTabbedPane p) {
    this.panel = p;
  }
  
  public synchronized void updateCfg() {}
  
  public synchronized void saveCfg() {}
}
