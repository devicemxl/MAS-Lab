package utilities;

import java.awt.Label;

public class Clock {
  private static Clock clock = new Clock();
  
  private static int TICKS;
  
  private static Label timeLabel;
  
  private Log logger;
  
  private Clock() {
    timeLabel = new Label("Time: 0");
  }
  
  public static Clock getClock() {
    return clock;
  }
  
  public static void reset() {
    Log.getDefault().log("(Clock) Resetting the clock.", 1);
    setTime(0);
  }
  
  public static synchronized void tick() {
    Log.getDefault().log("(Clock) Tick", 1);
    setTime(TICKS + 1);
  }
  
  public static void setTime(int t) {
    TICKS = t;
    timeLabel.setText("Time: " + TICKS);
  }
  
  public static int getTime() {
    return TICKS;
  }
  
  public Label getLabel() {
    return timeLabel;
  }
}
