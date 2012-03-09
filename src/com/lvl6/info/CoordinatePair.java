package com.lvl6.info;

public class CoordinatePair {
  private float x;
  private float y;
  
  public CoordinatePair(float x, float y) {
    this.x = x;
    this.y = y;
  }
  
  public float getX() {
    return x;
  }
  public float getY() {
    return y;
  }
  
  @Override
  public String toString() {
    return "CoordinatePair [x=" + x + ", y=" + y + "]";
  }
}
