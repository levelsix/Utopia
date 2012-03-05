package com.lvl6.info;

import com.lvl6.proto.InfoProto.NeutralCityElementProto.NeutralCityElemType;
import com.lvl6.proto.InfoProto.StructOrientation;

public class NeutralCityElement {
  private int cityId;
  private int assetId;
  private String name;
  private NeutralCityElemType type;
  private CoordinatePair coords;
  private int xLength;
  private int yLength;
  private String imgGood;
  private String imgBad;
  private StructOrientation orientation;
  public NeutralCityElement(int cityId, int assetId, String name,
      NeutralCityElemType type, CoordinatePair coords, int xLength,
      int yLength, String imgGood, String imgBad, StructOrientation orientation) {
    this.cityId = cityId;
    this.assetId = assetId;
    this.name = name;
    this.type = type;
    this.coords = coords;
    this.xLength = xLength;
    this.yLength = yLength;
    this.imgGood = imgGood;
    this.imgBad = imgBad;
    this.orientation = orientation;
  }
  public int getCityId() {
    return cityId;
  }
  public int getAssetId() {
    return assetId;
  }
  public String getName() {
    return name;
  }
  public NeutralCityElemType getType() {
    return type;
  }
  public CoordinatePair getCoords() {
    return coords;
  }
  public int getxLength() {
    return xLength;
  }
  public int getyLength() {
    return yLength;
  }
  public String getImgGood() {
    return imgGood;
  }
  public String getImgBad() {
    return imgBad;
  }
  public StructOrientation getOrientation() {
    return orientation;
  }
  @Override
  public String toString() {
    return "NeutralCityElement [cityId=" + cityId + ", assetId=" + assetId
        + ", name=" + name + ", type=" + type + ", coords=" + coords
        + ", xLength=" + xLength + ", yLength=" + yLength + ", imgGood="
        + imgGood + ", imgBad=" + imgBad + ", orientation=" + orientation + "]";
  }
}
