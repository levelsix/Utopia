package com.lvl6.info;

public class CoordinatePair {
  private int x;
  private int y;
  
  public CoordinatePair(int x, int y) {
    this.x = x;
    this.y = y;
  }
  
  public int getX() {
    return x;
  }
  public void setX(int x) {
    this.x = x;
  }
  public int getY() {
    return y;
  }
  public void setY(int y) {
    this.y = y;
  }
  
  @Override
  public String toString() {
    return "CoordinatePair [x=" + x + ", y=" + y + "]";
  }
}
