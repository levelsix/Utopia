package com.lvl6.info.jobs;

import com.lvl6.proto.InfoProto.UserType;

public class DefeatTypeJob {
  private int id;
  private UserType enemyType;
  private int numEnemiesToDefeat;
  
  public DefeatTypeJob(int id, UserType enemyType, int numEnemiesToDefeat) {
    this.id = id;
    this.enemyType = enemyType;
    this.numEnemiesToDefeat = numEnemiesToDefeat;
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

  @Override
  public String toString() {
    return "DefeatTypeJob [id=" + id + ", enemyType=" + enemyType
        + ", numEnemiesToDefeat=" + numEnemiesToDefeat + "]";
  }
}
