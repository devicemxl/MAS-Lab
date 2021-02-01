package utilities;

import java.io.Serializable;
import javax.swing.JPanel;

public class GraphEdge extends GraphNode implements Serializable, Cloneable {
  protected GraphNode from = null;
  
  protected GraphNode to = null;
  
  protected boolean drawarrows = true;
  
  public GraphEdge(String l, GraphNode nodeFrom, GraphNode nodeTo) {
    super(l);
    setDrawArrows(true);
    setFrom(nodeFrom);
    setTo(nodeTo);
  }
  
  public GraphEdge(GraphNode nodeFrom, GraphNode nodeTo) {
    this((String)null, nodeFrom, nodeTo);
  }
  
  public GraphEdge(String l) {
    this(l, (GraphNode)null, (GraphNode)null);
  }
  
  public GraphEdge() {
    this((String)null, (GraphNode)null, (GraphNode)null);
  }
  
  public GraphNode getFrom() {
    return this.from;
  }
  
  public GraphNode getTo() {
    return this.to;
  }
  
  public void setFrom(GraphNode n) {
    if (this.from != null) {
      this.from.removeOutEdge(this);
      removeEdge(this.from);
    } 
    this.from = n;
    if (this.from != null) {
      n.addOutEdge(this);
      addEdge(this.from);
    } 
  }
  
  public void setDrawArrows(boolean b) {
    this.drawarrows = b;
  }
  
  public boolean getDrawArrows() {
    return this.drawarrows;
  }
  
  public void setTo(GraphNode n) {
    if (this.to != null) {
      this.to.removeInEdge(this);
      removeEdge(this.to);
    } 
    this.to = n;
    if (this.to != null) {
      this.to.addInEdge(this);
      addEdge(this.to);
    } 
  }
  
  protected void setEndpoints(GraphNode f, GraphNode t) {
    if (f instanceof GraphEdge || t instanceof GraphEdge) {
      System.err.println("Error: Cannot attach a graph edge to another graph edge");
      return;
    } 
    setFrom(f);
    setTo(t);
  }
  
  public void excise() {
    super.excise();
    setEndpoints((GraphNode)null, (GraphNode)null);
  }
  
  public boolean matches(Object o) {
    if (o == null)
      return true; 
    if (o.getClass().isInstance(this))
      return equals(o); 
    return false;
  }
  
  public Object clone() {
    GraphEdge cloned = new GraphEdge(getLabel(), getTo(), getFrom());
    return cloned;
  }
  
  public JPanel getDefaultPanel() {
    return new EdgeNodePanel(this, getLabel());
  }
}
