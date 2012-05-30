package com.lvl6.info;

import java.util.List;

import com.lvl6.proto.InfoProto.SpecialQuestAction;

public class Quest {
  
  private int id;
  private int cityId;
  private String goodName;
  private String badName;
  private String goodDescription;
  private String badDescription;
  private String goodDoneResponse;
  private String badDoneResponse;
  private Dialogue goodAcceptDialogue;
  private Dialogue badAcceptDialogue;
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
  private int coinRetrievalAmountRequired;
  private SpecialQuestAction specialQuestActionRequired;

  public Quest(int id, int cityId, String goodName, String badName,
      String goodDescription, String badDescription, String goodDoneResponse,
      String badDoneResponse, Dialogue goodAcceptDialogue,
      Dialogue badAcceptDialogue, int assetNumWithinCity, int coinsGained,
      int diamondsGained, int expGained, int equipIdGained,
      List<Integer> questsRequiredForThis, List<Integer> tasksRequired,
      List<Integer> upgradeStructJobsRequired,
      List<Integer> buildStructJobsRequired,
      List<Integer> defeatGoodGuysJobsRequired,
      List<Integer> defeatBadGuysJobsRequired,
      List<Integer> possessEquipJobsRequired, int coinRetrievalAmountRequired,
      SpecialQuestAction specialQuestActionRequired) {
    super();
    this.id = id;
    this.cityId = cityId;
    this.goodName = goodName;
    this.badName = badName;
    this.goodDescription = goodDescription;
    this.badDescription = badDescription;
    this.goodDoneResponse = goodDoneResponse;
    this.badDoneResponse = badDoneResponse;
    this.goodAcceptDialogue = goodAcceptDialogue;
    this.badAcceptDialogue = badAcceptDialogue;
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
    this.coinRetrievalAmountRequired = coinRetrievalAmountRequired;
    this.specialQuestActionRequired = specialQuestActionRequired;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getCityId() {
    return cityId;
  }

  public void setCityId(int cityId) {
    this.cityId = cityId;
  }

  public String getGoodName() {
    return goodName;
  }

  public void setGoodName(String goodName) {
    this.goodName = goodName;
  }

  public String getBadName() {
    return badName;
  }

  public void setBadName(String badName) {
    this.badName = badName;
  }

  public String getGoodDescription() {
    return goodDescription;
  }

  public void setGoodDescription(String goodDescription) {
    this.goodDescription = goodDescription;
  }

  public String getBadDescription() {
    return badDescription;
  }

  public void setBadDescription(String badDescription) {
    this.badDescription = badDescription;
  }

  public String getGoodDoneResponse() {
    return goodDoneResponse;
  }

  public void setGoodDoneResponse(String goodDoneResponse) {
    this.goodDoneResponse = goodDoneResponse;
  }

  public String getBadDoneResponse() {
    return badDoneResponse;
  }

  public void setBadDoneResponse(String badDoneResponse) {
    this.badDoneResponse = badDoneResponse;
  }

  public Dialogue getGoodAcceptDialogue() {
    return goodAcceptDialogue;
  }

  public void setGoodAcceptDialogue(Dialogue goodAcceptDialogue) {
    this.goodAcceptDialogue = goodAcceptDialogue;
  }

  public Dialogue getBadAcceptDialogue() {
    return badAcceptDialogue;
  }

  public void setBadAcceptDialogue(Dialogue badAcceptDialogue) {
    this.badAcceptDialogue = badAcceptDialogue;
  }

  public int getAssetNumWithinCity() {
    return assetNumWithinCity;
  }

  public void setAssetNumWithinCity(int assetNumWithinCity) {
    this.assetNumWithinCity = assetNumWithinCity;
  }

  public int getCoinsGained() {
    return coinsGained;
  }

  public void setCoinsGained(int coinsGained) {
    this.coinsGained = coinsGained;
  }

  public int getDiamondsGained() {
    return diamondsGained;
  }

  public void setDiamondsGained(int diamondsGained) {
    this.diamondsGained = diamondsGained;
  }

  public int getExpGained() {
    return expGained;
  }

  public void setExpGained(int expGained) {
    this.expGained = expGained;
  }

  public int getEquipIdGained() {
    return equipIdGained;
  }

  public void setEquipIdGained(int equipIdGained) {
    this.equipIdGained = equipIdGained;
  }

  public List<Integer> getQuestsRequiredForThis() {
    return questsRequiredForThis;
  }

  public void setQuestsRequiredForThis(List<Integer> questsRequiredForThis) {
    this.questsRequiredForThis = questsRequiredForThis;
  }

  public List<Integer> getTasksRequired() {
    return tasksRequired;
  }

  public void setTasksRequired(List<Integer> tasksRequired) {
    this.tasksRequired = tasksRequired;
  }

  public List<Integer> getUpgradeStructJobsRequired() {
    return upgradeStructJobsRequired;
  }

  public void setUpgradeStructJobsRequired(List<Integer> upgradeStructJobsRequired) {
    this.upgradeStructJobsRequired = upgradeStructJobsRequired;
  }

  public List<Integer> getBuildStructJobsRequired() {
    return buildStructJobsRequired;
  }

  public void setBuildStructJobsRequired(List<Integer> buildStructJobsRequired) {
    this.buildStructJobsRequired = buildStructJobsRequired;
  }

  public List<Integer> getDefeatGoodGuysJobsRequired() {
    return defeatGoodGuysJobsRequired;
  }

  public void setDefeatGoodGuysJobsRequired(
      List<Integer> defeatGoodGuysJobsRequired) {
    this.defeatGoodGuysJobsRequired = defeatGoodGuysJobsRequired;
  }

  public List<Integer> getDefeatBadGuysJobsRequired() {
    return defeatBadGuysJobsRequired;
  }

  public void setDefeatBadGuysJobsRequired(List<Integer> defeatBadGuysJobsRequired) {
    this.defeatBadGuysJobsRequired = defeatBadGuysJobsRequired;
  }

  public List<Integer> getPossessEquipJobsRequired() {
    return possessEquipJobsRequired;
  }

  public void setPossessEquipJobsRequired(List<Integer> possessEquipJobsRequired) {
    this.possessEquipJobsRequired = possessEquipJobsRequired;
  }

  public int getCoinRetrievalAmountRequired() {
    return coinRetrievalAmountRequired;
  }

  public void setCoinRetrievalAmountRequired(int coinRetrievalAmountRequired) {
    this.coinRetrievalAmountRequired = coinRetrievalAmountRequired;
  }

  public SpecialQuestAction getSpecialQuestActionRequired() {
    return specialQuestActionRequired;
  }

  public void setSpecialQuestActionRequired(
      SpecialQuestAction specialQuestActionRequired) {
    this.specialQuestActionRequired = specialQuestActionRequired;
  }

  public int getNumComponents(boolean isGoodSide) {
    int numComponents = 0;
    if (tasksRequired != null) numComponents += tasksRequired.size();
    if (upgradeStructJobsRequired != null) numComponents += upgradeStructJobsRequired.size();
    if (buildStructJobsRequired != null) numComponents += buildStructJobsRequired.size();
    if (isGoodSide) {
      if (defeatBadGuysJobsRequired != null) numComponents += defeatBadGuysJobsRequired.size();
    } else {
      if (defeatGoodGuysJobsRequired != null) numComponents += defeatGoodGuysJobsRequired.size();
    }
    if (possessEquipJobsRequired != null) numComponents += possessEquipJobsRequired.size();
    if (coinRetrievalAmountRequired > 0) numComponents++;
    return numComponents;
  }

  @Override
  public String toString() {
    return "Quest [id=" + id + ", cityId=" + cityId + ", goodName=" + goodName
        + ", badName=" + badName + ", goodDescription=" + goodDescription
        + ", badDescription=" + badDescription + ", goodDoneResponse="
        + goodDoneResponse + ", badDoneResponse=" + badDoneResponse
        + ", goodAcceptDialogue=" + goodAcceptDialogue + ", badAcceptDialogue="
        + badAcceptDialogue + ", assetNumWithinCity=" + assetNumWithinCity
        + ", coinsGained=" + coinsGained + ", diamondsGained=" + diamondsGained
        + ", expGained=" + expGained + ", equipIdGained=" + equipIdGained
        + ", questsRequiredForThis=" + questsRequiredForThis
        + ", tasksRequired=" + tasksRequired + ", upgradeStructJobsRequired="
        + upgradeStructJobsRequired + ", buildStructJobsRequired="
        + buildStructJobsRequired + ", defeatGoodGuysJobsRequired="
        + defeatGoodGuysJobsRequired + ", defeatBadGuysJobsRequired="
        + defeatBadGuysJobsRequired + ", possessEquipJobsRequired="
        + possessEquipJobsRequired + ", coinRetrievalAmountRequired="
        + coinRetrievalAmountRequired + ", specialQuestActionRequired="
        + specialQuestActionRequired + "]";
  }
  
}
