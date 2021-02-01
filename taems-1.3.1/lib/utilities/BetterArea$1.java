package utilities;

class null extends UTimer {
  private final BetterArea val$b;
  
  private final int val$temp;
  
  null(String x0, BetterArea val$b, int val$temp) {
    super(x0);
    this.val$b = val$b;
    this.val$temp = val$temp;
  }
  
  public void action() {
    this.val$b.setDepth(this.val$temp);
    BetterArea.access$002(this.val$b.findArea());
    this.val$b.flushCache();
    this.val$b.printcachestats = false;
  }
}
