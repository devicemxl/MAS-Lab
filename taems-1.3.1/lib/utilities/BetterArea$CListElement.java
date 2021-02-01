package utilities;

import java.awt.geom.Rectangle2D;

public class CListElement {
  public int x;
  
  public int y;
  
  public int w;
  
  public int h;
  
  public CListElement next;
  
  public CListElement skip;
  
  private final BetterArea this$0;
  
  public CListElement(BetterArea this$0, int tx, int ty, int tw, int th) {
    this.this$0 = this$0;
    this.next = null;
    this.skip = null;
    this.x = tx;
    this.y = ty;
    this.w = tw;
    this.h = th;
  }
  
  public Rectangle2D getRectangle(double scalex, double scaley, double x1, double y1) {
    double tx = this.x * scalex + x1;
    double ty = this.y * scaley + y1;
    double tw = this.w * scalex;
    double th = this.h * scaley;
    return new Rectangle2D.Double(tx, ty, tw, th);
  }
  
  public Rectangle2D getRectangle(double scalex, double scaley) {
    return getRectangle(scalex, scaley, 0.0D, 0.0D);
  }
  
  public String toStringWithoutLinks() {
    return new String("x " + this.x + ", y " + this.y + ", w " + this.w + ", h " + this.h);
  }
  
  public String toString() {
    String s = toStringWithoutLinks();
    s = new String(s + ", next [");
    if (this.next == null) {
      s = new String(s + "null], skip [");
    } else {
      s = new String(s + this.next.toStringWithoutLinks() + "], skip [");
    } 
    if (this.skip == null) {
      s = new String(s + "null]");
    } else {
      s = new String(s + this.skip.toStringWithoutLinks() + "]");
    } 
    return s;
  }
}
