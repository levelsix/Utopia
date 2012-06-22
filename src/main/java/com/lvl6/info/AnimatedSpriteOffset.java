package com.lvl6.info;

public class AnimatedSpriteOffset {
  private String imgName;
  private CoordinatePair offSet;
  
  public AnimatedSpriteOffset(String imgName, CoordinatePair offSet) {
    this.imgName = imgName;
    this.offSet = offSet;
  }

  public String getImgName() {
    return imgName;
  }

  public CoordinatePair getOffSet() {
    return offSet;
  }
}
