package utilities;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.swing.JPanel;

public class ContainerGraphNode extends GraphNode implements Serializable, Cloneable {
  protected Hashtable contained = new Hashtable();
  
  boolean physical = false;
  
  public ContainerGraphNode(String l) {
    super(l);
  }
  
  public ContainerGraphNode() {
    this("");
  }
  
  public Enumeration getContained() {
    return this.contained.keys();
  }
  
  public Enumeration getPanels() {
    return this.contained.elements();
  }
  
  public void addContained(JPanel panel, GraphNode n, JPanel p) {
    if (p == null)
      p = n.getDefaultPanel(); 
    if (p instanceof NodePanel)
      ((NodePanel)p).setLabel(n.getLabel()); 
    this.contained.put(n, p);
    panel.add(p, n);
    panel.revalidate();
  }
  
  public void addContained(JPanel panel, GraphNode n) {
    addContained(panel, n, null);
  }
  
  public void removeContained(JPanel panel, GraphNode n) {
    panel.remove((JPanel)this.contained.get(n));
    this.contained.remove(n);
  }
  
  public boolean matches(GraphNode n) {
    if (n == null)
      return true; 
    if (n instanceof ContainerGraphNode)
      return matches(n); 
    return false;
  }
  
  public Object clone() {
    ContainerGraphNode cloned = null;
    try {
      cloned = (ContainerGraphNode)super.clone();
    } catch (Exception e) {
      System.out.println("Clone Error: " + e);
    } 
    return cloned;
  }
  
  public JPanel getDefaultPanel() {
    GraphLayout temp = new GraphLayout(0, 1);
    temp.setSpacing(50, 25, 50, 25, NodePanel.H_MARGIN, NodePanel.V_MARGIN);
    temp.setSave(false);
    temp.setRecursivePlacement(false);
    JPanel panel = new ContainerNodePanel(this, getLabel());
    panel.setLayout(temp);
    return panel;
  }
}