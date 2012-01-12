package com.lvl6.info;

public class Location {

  private float latitude;
  private float longitude;
  
  
  public Location(float latitude, float longitude) {
    this.latitude = latitude;
    this.longitude = longitude;
  }
  
  public float getLatitude() {
    return latitude;
  }
  public void setLatitude(float latitude) {
    this.latitude = latitude;
  }
  public float getLongitude() {
    return longitude;
  }
  public void setLongitude(float longitude) {
    this.longitude = longitude;
  }

  @Override
  public String toString() {
    return "Location [latitude=" + latitude + ", longitude=" + longitude + "]";
  }
}
