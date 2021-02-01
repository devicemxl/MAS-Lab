package utilities;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;

public class EdgeNodePanel extends NodePanel {
  public EdgeNodePanel(GraphEdge n, String l) {
    super(n, l);
  }
  
  protected EdgeNodePanel() {
    this((GraphEdge)null, (String)null);
  }
  
  public void paint(Graphics g) {
    FontMetrics fm = g.getFontMetrics();
    Rectangle rect = getBounds();
    rect = new Rectangle(0, 0, rect.width, rect.height);
    if ((getLabel() == null || getLabel().equals("")) && isVisible()) {
      int x = rect.x + rect.width / 2 - 4;
      int y = rect.y + rect.height / 2 - 4;
      g.setColor(getForeground());
      g.drawRoundRect(x, y, 8, 8, 8, 8);
      g.setColor(getForeground());
      g.fillRoundRect(x, y, 8, 8, 8, 8);
    } else {
      super.paint(g);
    } 
  }
  
  public int getRectRoundness() {
    return 8;
  }
}
