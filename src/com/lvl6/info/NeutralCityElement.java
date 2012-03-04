package com.lvl6.info;

import com.lvl6.proto.InfoProto.NeutralCityElementProto.NeutralCityElemType;
import com.lvl6.proto.InfoProto.StructOrientation;

public class NeutralCityElement {
  private int cityId;
  private int assetId;
  private NeutralCityElemType type;
  private CoordinatePair coords;
  private int xLength;
  private int yLength;
  private String imgId;
  private StructOrientation orientation;
  public NeutralCityElement(int cityId, int assetId, NeutralCityElemType type,
      CoordinatePair coords, int xLength, int yLength, String imgId,
      StructOrientation orientation) {
    this.cityId = cityId;
    this.assetId = assetId;
    this.type = type;
    this.coords = coords;
    this.xLength = xLength;
    this.yLength = yLength;
    this.imgId = imgId;
    this.orientation = orientation;
  }
  public int getCityId() {
    return cityId;
  }
  public int getAssetId() {
    return assetId;
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
  public String getImgId() {
    return imgId;
  }
  public StructOrientation getOrientation() {
    return orientation;
  }
  @Override
  public String toString() {
    return "NeutralCityElement [cityId=" + cityId + ", assetId=" + assetId
        + ", type=" + type + ", coords=" + coords + ", xLength=" + xLength
        + ", yLength=" + yLength + ", imgId=" + imgId + ", orientation="
        + orientation + "]";
  }
}
