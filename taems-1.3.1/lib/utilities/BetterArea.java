package utilities;

import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

public class BetterArea extends Area {
  static int default_depth = 6;
  
  static Hashtable CLists = new Hashtable();
  
  static BetterArea junk = new BetterArea();
  
  int depth;
  
  static {
    CLists.put(new Integer(default_depth), calculateControlList(default_depth));
  }
  
  public BetterArea() {
    this.depth = default_depth;
  }
  
  public BetterArea(Shape s) {
    super(s);
    this.depth = default_depth;
  }
  
  protected static CListElement calculateControlList(int depth) {
    depth = Math.min(depth, 16);
    int wall = 1 << depth;
    junk.getClass();
    CListElement parent = new CListElement(junk, 0, 0, wall, wall);
    parent.next = calculateControlList(parent, 1, depth);
    parent.skip = null;
    return parent;
  }
  
  protected static CListElement calculateControlList(CListElement parent, int curdepth, int maxdepth) {
    if (curdepth > maxdepth)
      return parent.skip; 
    int neww = parent.w >> 1;
    int newh = parent.h >> 1;
    if (neww == 0 || newh == 0)
      return parent.skip; 
    junk.getClass();
    CListElement e11 = new CListElement(junk, parent.x, parent.y, neww, newh);
    junk.getClass();
    CListElement e12 = new CListElement(junk, parent.x + neww, parent.y, neww, newh);
    junk.getClass();
    CListElement e21 = new CListElement(junk, parent.x, parent.y + newh, neww, newh);
    junk.getClass();
    CListElement e22 = new CListElement(junk, parent.x + neww, parent.y + newh, neww, newh);
    e11.skip = e12;
    e12.skip = e21;
    e21.skip = e22;
    e22.skip = parent.skip;
    e11.next = calculateControlList(e11, curdepth + 1, maxdepth);
    e12.next = calculateControlList(e12, curdepth + 1, maxdepth);
    e21.next = calculateControlList(e21, curdepth + 1, maxdepth);
    e22.next = calculateControlList(e22, curdepth + 1, maxdepth);
    return e11;
  }
  
  protected static CListElement getCList(int depth) {
    CListElement e = (CListElement)CLists.get(new Integer(depth));
    if (e == null)
      CLists.put(new Integer(depth), e = calculateControlList(depth)); 
    return e;
  }
  
  public static void main(String[] args) {
    Ellipse2D e = new Ellipse2D.Double(10.0D, 10.0D, 20.0D, 20.0D);
    BetterArea b = new BetterArea(e);
    System.out.println("circle of radius 10.0 - ");
    System.out.println("include_partials: true");
    b.include_partials = true;
    int i;
    for (i = 0; i <= 7; i++) {
      int temp = i;
      b.printcachestats = true;
      UTimer.time(100, new UTimer("findArea with partials", b, temp) {
            private final BetterArea val$b;
            
            private final int val$temp;
            
            public void action() {
              this.val$b.setDepth(this.val$temp);
              BetterArea.temparea = this.val$b.findArea();
              this.val$b.flushCache();
              this.val$b.printcachestats = false;
            }
          }true);
      System.out.println("  computed area with depth of " + i + ": " + temparea);
    } 
    System.out.println("include_partials: false");
    b.include_partials = false;
    for (i = 0; i <= 7; i++) {
      int temp = i;
      b.printcachestats = true;
      UTimer.time(100, new UTimer("findArea without partials", b, temp) {
            private final BetterArea val$b;
            
            private final int val$temp;
            
            public void action() {
              this.val$b.setDepth(this.val$temp);
              BetterArea.temparea = this.val$b.findArea();
              this.val$b.flushCache();
              this.val$b.printcachestats = false;
            }
          }true);
      System.out.println("computed area with depth of " + i + ": " + temparea + "\n");
    } 
    System.out.println("actual area: 314.1592653589793");
  }
  
  private static double temparea = 0.0D;
  
  public void setDepth(int i) {
    this.depth = i;
  }
  
  public int getDepth() {
    return this.depth;
  }
  
  protected boolean include_partials = true;
  
  protected boolean use_intersects = false;
  
  public double findArea() {
    if (isEmpty())
      return 0.0D; 
    CListElement clist = getCList(getDepth());
    Rectangle2D bounds = getBounds2D();
    double scalex = bounds.getWidth() / clist.w;
    double scaley = bounds.getHeight() / clist.h;
    CListElement cur = clist;
    double total = 0.0D;
    while (cur != null) {
      Rectangle2D rect = cur.getRectangle(scalex, scaley, bounds.getX(), bounds.getY());
      if (checkContains(rect)) {
        double addval = rect.getWidth() * rect.getHeight();
        total += addval;
        cur = cur.skip;
        continue;
      } 
      if (this.include_partials && cur.next == cur.skip && checkIntersects(rect)) {
        double addval = rect.getWidth() * rect.getHeight();
        total += addval;
        cur = cur.skip;
        continue;
      } 
      cur = cur.next;
    } 
    return total;
  }
  
  protected boolean use_contains_cache = true;
  
  protected boolean checkContains(Rectangle2D rect) {
    if (this.use_intersects)
      return contains(rect); 
    double x1 = rect.getX();
    double x2 = x1 + rect.getWidth();
    double y1 = rect.getY();
    double y2 = y1 + rect.getHeight();
    if (this.use_contains_cache) {
      if (!containsCacheing(x1, y1))
        return false; 
      if (!containsCacheing(x2, y1))
        return false; 
      if (!containsCacheing(x1, y2))
        return false; 
      if (!containsCacheing(x2, y2))
        return false; 
    } else {
      if (!contains(x1, y1))
        return false; 
      if (!contains(x2, y1))
        return false; 
      if (!contains(x1, y2))
        return false; 
      if (!contains(x2, y2))
        return false; 
    } 
    return true;
  }
  
  private Hashtable ccache = new Hashtable();
  
  protected void flushCache() {
    if (this.ccache.size() > 0) {
      printCacheStats();
      this.ccache = new Hashtable();
      this.containscount = 0;
      this.hitcount = 0;
      this.misscount = 0;
    } 
  }
  
  int containscount = 0;
  
  int hitcount = 0;
  
  int misscount = 0;
  
  boolean printcachestats = false;
  
  protected void printCacheStats() {
    if (this.printcachestats && (this.hitcount > 0 || this.misscount > 0))
      System.out.println("point cache hits: " + this.hitcount + ", misses: " + this.misscount); 
  }
  
  protected boolean containsCacheing(double x, double y) {
    Point p = new Point((int)x, (int)y);
    if (this.ccache.containsKey(p)) {
      this.hitcount++;
      return ((Boolean)this.ccache.get(p)).booleanValue();
    } 
    this.misscount++;
    boolean b = contains(x, y);
    this.ccache.put(p, new Boolean(b));
    return b;
  }
  
  protected boolean checkIntersects(Rectangle2D rect) {
    if (this.use_intersects)
      return intersects(rect); 
    double x1 = rect.getX();
    double x2 = x1 + rect.getWidth();
    double y1 = rect.getY();
    double y2 = y1 + rect.getHeight();
    if (this.use_contains_cache) {
      if (containsCacheing(x1, y1))
        return true; 
      if (containsCacheing(x2, y1))
        return true; 
      if (containsCacheing(x1, y2))
        return true; 
      if (containsCacheing(x2, y2))
        return true; 
    } else {
      if (contains(x1, y1))
        return true; 
      if (contains(x2, y1))
        return true; 
      if (contains(x1, y2))
        return true; 
      if (contains(x2, y2))
        return true; 
    } 
    return false;
  }
  
  public void add(Area rhs) {
    flushCache();
    super.add(rhs);
  }
  
  public void exclusiveOr(Area rhs) {
    flushCache();
    super.add(rhs);
  }
  
  public void intersect(Area rhs) {
    flushCache();
    super.intersect(rhs);
  }
  
  public void reset() {
    flushCache();
    super.reset();
  }
  
  public void subtract(Area rhs) {
    flushCache();
    super.subtract(rhs);
  }
  
  public void transform(AffineTransform t) {
    flushCache();
    super.transform(t);
  }
  
  public void dumpPath() {
    PathIterator i = getPathIterator(null);
    int winding = i.getWindingRule();
    float[] array = new float[6];
    System.err.println("dumping path:");
    System.err.println("winding " + winding);
    while (!i.isDone()) {
      int whereto = i.currentSegment(array);
      if (whereto == 4) {
        System.err.println("  SEG_CLOSE");
      } else if (whereto == 0) {
        System.err.println("  SEG_MOVETO: " + array[0] + ", " + array[1]);
      } else if (whereto == 1) {
        System.err.println("  SEG_LINETO: " + array[0] + ", " + array[1]);
      } else if (whereto == 2) {
        System.err.println("  SEG_QUADTO: " + array[0] + ", " + array[1] + ", " + array[2] + ", " + array[3]);
      } else if (whereto == 3) {
        System.err.println("  SEG_CUBICTO: " + array[0] + ", " + array[1] + ", " + array[2] + ", " + array[3] + ", " + array[4] + ", " + array[5]);
      } 
      i.next();
    } 
    System.err.println("done");
  }
  
  public Iterator getClosedSubpaths() {
    PathIterator i = getPathIterator(null);
    Vector subshapes = new Vector();
    int winding = i.getWindingRule();
    GeneralPath cur = null;
    float[] pathstart = null;
    while (!i.isDone()) {
      float[] array = new float[6];
      boolean close = false;
      int whereto = i.currentSegment(array);
      if (whereto == 4) {
        if (cur != null)
          cur.closePath(); 
        close = true;
      } else {
        if (whereto == 0) {
          pathstart = array;
          cur = new GeneralPath(winding);
          cur.moveTo(array[0], array[1]);
          i.next();
          continue;
        } 
        if (cur == null) {
          System.err.println("WARNING: path without moveto - there'llprobably be an exception soon");
          cur = new GeneralPath(winding);
        } 
        if (whereto == 1) {
          cur.lineTo(array[0], array[1]);
        } else if (whereto == 2) {
          cur.quadTo(array[0], array[1], array[2], array[3]);
        } else if (whereto == 3) {
          cur.curveTo(array[0], array[1], array[2], array[3], array[4], array[5]);
        } 
        if (pathstart == null)
          pathstart = array; 
      } 
      if (close && cur != null) {
        subshapes.add(cur);
        cur = null;
        close = false;
      } 
      i.next();
    } 
    return subshapes.iterator();
  }
  
  public boolean hasMultipleClosedSubpaths() {
    float[] pathstart = new float[6];
    float[] array = new float[6];
    PathIterator i = getPathIterator(null);
    int count = 0;
    while (!i.isDone()) {
      int whereto = i.currentSegment(array);
      if (whereto == 0) {
        i.currentSegment(pathstart);
      } else if (whereto == 4) {
        count++;
      } else {
        boolean equal = true;
        for (int c = 0; c < pathstart.length; c++) {
          if (array[c] != pathstart[c]) {
            equal = false;
            break;
          } 
        } 
        if (equal)
          count++; 
      } 
      if (count > 1)
        return true; 
      i.next();
    } 
    return false;
  }
  
  public Object clone() {
    Object o = super.clone();
    return new BetterArea((Shape)o);
  }
  
  protected class CListElement {
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
}
