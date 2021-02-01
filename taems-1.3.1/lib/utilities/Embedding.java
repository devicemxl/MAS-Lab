package utilities;

import java.util.Iterator;
import java.util.Set;

public interface Embedding {
  void setGraph(Set paramSet1, Set paramSet2);
  
  Iterator getNodes();
  
  Iterator getEdges();
  
  Iterator getEdgeOrderings();
  
  Iterator getEdgeOrder(Object paramObject);
  
  Iterator getMinimumAngles();
  
  int getMinimumAngle(Object paramObject);
}
