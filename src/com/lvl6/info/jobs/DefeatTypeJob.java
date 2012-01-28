package com.lvl6.info.jobs;

import com.lvl6.proto.InfoProto.UserType;

public class DefeatTypeJob {
  private int id;
  private UserType enemyType;
  private int numEnemiesToDefeat;
  private int cityId;
  
  public DefeatTypeJob(int id, UserType enemyType, int numEnemiesToDefeat, int cityId) {
    this.id = id;
    this.enemyType = enemyType;
    this.numEnemiesToDefeat = numEnemiesToDefeat;
    this.cityId = cityId;
  }
  
  public int getId() {
    return id;
  }
  public UserType getEnemyType() {
    return enemyType;
  }
  public int getNumEnemiesToDefeat() {
    return numEnemiesToDefeat;
  }
  public int getCityId() {
    return cityId;
  }

  @Override
  public String toString() {
    return "DefeatTypeJob [id=" + id + ", enemyType=" + enemyType
        + ", numEnemiesToDefeat=" + numEnemiesToDefeat + ", cityId=" + cityId
        + "]";
  }


}
