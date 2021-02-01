package utilities;

import java.awt.Graphics;
import java.awt.Point;

public class Curve {
  public float Ax;
  
  public float Ay;
  
  public float Bx;
  
  public float By;
  
  public float Cx;
  
  public float Cy;
  
  public int Ndiv;
  
  public Curve(float Ax, float Ay, float Bx, float By, float Cx, float Cy, int Ndiv) {
    this.Ax = Ax;
    this.Ay = Ay;
    this.Bx = Bx;
    this.By = By;
    this.Cx = Cx;
    this.Cy = Cy;
    this.Ndiv = Ndiv;
  }
  
  public Curve(float Ax, float Ay, float Bx, float By, float Cx, float Cy) {
    this.Ax = Ax;
    this.Ay = Ay;
    this.Bx = Bx;
    this.By = By;
    this.Cx = Cx;
    this.Cy = Cy;
    this.Ndiv = (int)(Math.max(Math.abs(Ax), Math.abs(Ay)) / 2.0F);
  }
  
  public Curve() {}
  
  public void PutCurve(float Ax, float Ay, float Bx, float By, float Cx, float Cy) {
    this.Ax = Ax;
    this.Ay = Ay;
    this.Bx = Bx;
    this.By = By;
    this.Cx = Cx;
    this.Cy = Cy;
    this.Ndiv = (int)(Math.max(Math.abs(Ax), Math.abs(Ay)) / 2.0F);
  }
  
  public void draw(Graphics gra, float x, float y) {
    int Ndiv = this.Ndiv;
    if (Ndiv == 0)
      Ndiv = 1; 
    int OrigX = (int)x, OrigY = (int)y;
    for (int i = 1; i <= Ndiv; i++) {
      float t = 1.0F / Ndiv * i;
      float f = t * t * (3.0F - 2.0F * t);
      float g = t * (t - 1.0F) * (t - 1.0F);
      float h = t * t * (t - 1.0F);
      int NewX = (int)(x + this.Ax * f + this.Bx * g + this.Cx * h);
      int NewY = (int)(y + this.Ay * f + this.By * g + this.Cy * h);
      gra.drawLine(OrigX, OrigY, NewX, NewY);
      OrigX = NewX;
      OrigY = NewY;
    } 
  }
  
  public Point getPoint(float dist, float x, float y) {
    int Ndiv = this.Ndiv;
    if (Ndiv == 0)
      Ndiv = 1; 
    int distseg = Math.round((Ndiv - 1) * dist) + 1;
    int OrigX = (int)x, OrigY = (int)y;
    for (int i = 1; i <= Ndiv; i++) {
      float t = 1.0F / Ndiv * i;
      float f = t * t * (3.0F - 2.0F * t);
      float g = t * (t - 1.0F) * (t - 1.0F);
      float h = t * t * (t - 1.0F);
      int NewX = (int)(x + this.Ax * f + this.Bx * g + this.Cx * h);
      int NewY = (int)(y + this.Ay * f + this.By * g + this.Cy * h);
      if (i == distseg)
        return new Point(OrigX, OrigY); 
      OrigX = NewX;
      OrigY = NewY;
    } 
    return new Point(0, 0);
  }
}
