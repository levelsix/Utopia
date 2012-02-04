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
  private int woodGained;
  private int expGained;
  private int equipIdGained;
  private List<Integer> questsRequiredForThis;
  private List<Integer> tasksRequired;
  private List<Integer> upgradeStructJobsRequired;
  private List<Integer> buildStructJobsRequired;
  private List<Integer> marketplaceJobsRequired;
  private List<Integer> defeatGoodGuysRequired;
  private List<Integer> defeatBadGuysRequired;
  private int equipIdReq;
  private int equipQuantityReq;
  
  public Quest(int id, int cityId, String goodName, String badName, String goodDescription, 
      String badDescription, String goodDoneResponse, String badDoneResponse,
      String goodInProgress, String badInProgress, int assetNumWithinCity,
      int coinsGained, int diamondsGained, int woodGained, int expGained, int equipIdGained,
      List<Integer> questsRequiredForThis, 
      List<Integer> tasksRequired, List<Integer> upgradeStructJobsRequired,
      List<Integer> buildStructJobsRequired,
      List<Integer> marketplaceJobsRequired,
      List<Integer> defeatGoodGuysRequired,
      List<Integer> defeatBadGuysRequired, int equipIdReq, int equipQuantityReq) {
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
    this.woodGained = woodGained;
    this.expGained = expGained;
    this.equipIdGained = equipIdGained;
    this.questsRequiredForThis = questsRequiredForThis;
    this.tasksRequired = tasksRequired;
    this.upgradeStructJobsRequired = upgradeStructJobsRequired;
    this.buildStructJobsRequired = buildStructJobsRequired;
    this.marketplaceJobsRequired = marketplaceJobsRequired;
    this.defeatGoodGuysRequired = defeatGoodGuysRequired;
    this.defeatBadGuysRequired = defeatBadGuysRequired;
    this.equipIdReq = equipIdReq;
    this.equipQuantityReq = equipQuantityReq;
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
  
  public int getWoodGained() {
    return woodGained;
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

  public List<Integer> getMarketplaceJobsRequired() {
    return marketplaceJobsRequired;
  }

  public List<Integer> getDefeatGoodGuysRequired() {
    return defeatGoodGuysRequired;
  }

  public List<Integer> getDefeatBadGuysRequired() {
    return defeatBadGuysRequired;
  }

  public int getEquipIdReq() {
    return equipIdReq;
  }

  public int getEquipQuantityReq() {
    return equipQuantityReq;
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
        + ", woodGained=" + woodGained + ", expGained=" + expGained
        + ", equipIdGained=" + equipIdGained + ", questsRequiredForThis="
        + questsRequiredForThis + ", tasksRequired=" + tasksRequired
        + ", upgradeStructJobsRequired=" + upgradeStructJobsRequired
        + ", buildStructJobsRequired=" + buildStructJobsRequired
        + ", marketplaceJobsRequired=" + marketplaceJobsRequired
        + ", defeatGoodGuysRequired=" + defeatGoodGuysRequired
        + ", defeatBadGuysRequired=" + defeatBadGuysRequired + ", equipIdReq="
        + equipIdReq + ", equipQuantityReq=" + equipQuantityReq + "]";
  }
  
}
