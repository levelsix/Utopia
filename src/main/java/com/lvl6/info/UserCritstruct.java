package com.lvl6.info;

import com.lvl6.proto.InfoProto.CritStructType;
import com.lvl6.proto.InfoProto.StructOrientation;

public class UserCritstruct {
  private CritStructType type;
  private CoordinatePair coords;
  private StructOrientation orientation;
  public UserCritstruct(CritStructType type, CoordinatePair coords,
      StructOrientation orientation) {
    this.type = type;
    this.coords = coords;
    this.orientation = orientation;
  }
  public CritStructType getType() {
    return type;
  }
  public CoordinatePair getCoords() {
    return coords;
  }
  public StructOrientation getOrientation() {
    return orientation;
  }
  @Override
  public String toString() {
    return "UserCritstruct [type=" + type + ", coords=" + coords
        + ", orientation=" + orientation + "]";
  }
}
