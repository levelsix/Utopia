package com.lvl6.info;

import java.util.List;

public class Quest {
  
  private int id;
  private int cityId;
  private String goodName;
  private String badName;
  private String goodDescription;
  private String badDescription;
  private String goodDoneResponse;
  private String badDoneResponse;
  private String goodInProgress;
  private String badInProgress;
  private int assetNumWithinCity;
  private int coinsGained;
  private int diamondsGained;
  private int expGained;
  private int equipIdGained;
  private List<Integer> questsRequiredForThis;
  private List<Integer> tasksRequired;
  private List<Integer> upgradeStructJobsRequired;
  private List<Integer> buildStructJobsRequired;
  private List<Integer> defeatGoodGuysJobsRequired;
  private List<Integer> defeatBadGuysJobsRequired;
  private List<Integer> possessEquipJobsRequired;
  public Quest(int id, int cityId, String goodName, String badName,
      String goodDescription, String badDescription, String goodDoneResponse,
      String badDoneResponse, String goodInProgress, String badInProgress,
      int assetNumWithinCity, int coinsGained, int diamondsGained,
      int expGained, int equipIdGained,
      List<Integer> questsRequiredForThis, List<Integer> tasksRequired,
      List<Integer> upgradeStructJobsRequired,
      List<Integer> buildStructJobsRequired,
      List<Integer> defeatGoodGuysJobsRequired,
      List<Integer> defeatBadGuysJobsRequired,
      List<Integer> possessEquipJobsRequired) {
    this.id = id;
    this.cityId = cityId;
    this.goodName = goodName;
    this.badName = badName;
    this.goodDescription = goodDescription;
    this.badDescription = badDescription;
    this.goodDoneResponse = goodDoneResponse;
    this.badDoneResponse = badDoneResponse;
    this.goodInProgress = goodInProgress;
    this.badInProgress = badInProgress;
    this.assetNumWithinCity = assetNumWithinCity;
    this.coinsGained = coinsGained;
    this.diamondsGained = diamondsGained;
    this.expGained = expGained;
    this.equipIdGained = equipIdGained;
    this.questsRequiredForThis = questsRequiredForThis;
    this.tasksRequired = tasksRequired;
    this.upgradeStructJobsRequired = upgradeStructJobsRequired;
    this.buildStructJobsRequired = buildStructJobsRequired;
    this.defeatGoodGuysJobsRequired = defeatGoodGuysJobsRequired;
    this.defeatBadGuysJobsRequired = defeatBadGuysJobsRequired;
    this.possessEquipJobsRequired = possessEquipJobsRequired;
  }
  public int getId() {
    return id;
  }
  public int getCityId() {
    return cityId;
  }
  public String getGoodName() {
    return goodName;
  }
  public String getBadName() {
    return badName;
  }
  public String getGoodDescription() {
    return goodDescription;
  }
  public String getBadDescription() {
    return badDescription;
  }
  public String getGoodDoneResponse() {
    return goodDoneResponse;
  }
  public String getBadDoneResponse() {
    return badDoneResponse;
  }
  public String getGoodInProgress() {
    return goodInProgress;
  }
  public String getBadInProgress() {
    return badInProgress;
  }
  public int getAssetNumWithinCity() {
    return assetNumWithinCity;
  }
  public int getCoinsGained() {
    return coinsGained;
  }
  public int getDiamondsGained() {
    return diamondsGained;
  }
  public int getExpGained() {
    return expGained;
  }
  public int getEquipIdGained() {
    return equipIdGained;
  }
  public List<Integer> getQuestsRequiredForThis() {
    return questsRequiredForThis;
  }
  public List<Integer> getTasksRequired() {
    return tasksRequired;
  }
  public List<Integer> getUpgradeStructJobsRequired() {
    return upgradeStructJobsRequired;
  }
  public List<Integer> getBuildStructJobsRequired() {
    return buildStructJobsRequired;
  }
  public List<Integer> getDefeatGoodGuysJobsRequired() {
    return defeatGoodGuysJobsRequired;
  }
  public List<Integer> getDefeatBadGuysJobsRequired() {
    return defeatBadGuysJobsRequired;
  }
  public List<Integer> getPossessEquipJobsRequired() {
    return possessEquipJobsRequired;
  }
  @Override
  public String toString() {
    return "Quest [id=" + id + ", cityId=" + cityId + ", goodName=" + goodName
        + ", badName=" + badName + ", goodDescription=" + goodDescription
        + ", badDescription=" + badDescription + ", goodDoneResponse="
        + goodDoneResponse + ", badDoneResponse=" + badDoneResponse
        + ", goodInProgress=" + goodInProgress + ", badInProgress="
        + badInProgress + ", assetNumWithinCity=" + assetNumWithinCity
        + ", coinsGained=" + coinsGained + ", diamondsGained=" + diamondsGained
        + ", expGained=" + expGained
        + ", equipIdGained=" + equipIdGained + ", questsRequiredForThis="
        + questsRequiredForThis + ", tasksRequired=" + tasksRequired
        + ", upgradeStructJobsRequired=" + upgradeStructJobsRequired
        + ", buildStructJobsRequired=" + buildStructJobsRequired
        + ", defeatGoodGuysJobsRequired=" + defeatGoodGuysJobsRequired
        + ", defeatBadGuysJobsRequired=" + defeatBadGuysJobsRequired
        + ", possessEquipJobsRequired=" + possessEquipJobsRequired + "]";
  }

  
}
