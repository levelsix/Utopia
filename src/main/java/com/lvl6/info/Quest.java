package com.lvl6.info;

import java.io.Serializable;
import java.util.List;

import com.lvl6.proto.InfoProto.SpecialQuestAction;

public class Quest implements Serializable {

	private static final long serialVersionUID = -7842937332061187810L;
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
	private String goodQuestGiverImageSuffix;
	private String badQuestGiverImageSuffix;

	public Quest(int id, int cityId, String goodName, String badName,
			String goodDescription, String badDescription,
			String goodDoneResponse, String badDoneResponse,
			Dialogue goodAcceptDialogue, Dialogue badAcceptDialogue,
			int assetNumWithinCity, int coinsGained, int diamondsGained,
			int expGained, int equipIdGained,
			List<Integer> questsRequiredForThis, List<Integer> tasksRequired,
			List<Integer> upgradeStructJobsRequired,
			List<Integer> buildStructJobsRequired,
			List<Integer> defeatGoodGuysJobsRequired,
			List<Integer> defeatBadGuysJobsRequired,
			List<Integer> possessEquipJobsRequired,
			int coinRetrievalAmountRequired,
			SpecialQuestAction specialQuestActionRequired,
			String goodQuestGiverImageSuffix, String badQuestGiverImageSuffix) {
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
		this.goodQuestGiverImageSuffix = goodQuestGiverImageSuffix;
		this.badQuestGiverImageSuffix = badQuestGiverImageSuffix;
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

	public Dialogue getGoodAcceptDialogue() {
		return goodAcceptDialogue;
	}

	public Dialogue getBadAcceptDialogue() {
		return badAcceptDialogue;
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

	public int getCoinRetrievalAmountRequired() {
		return coinRetrievalAmountRequired;
	}

	public SpecialQuestAction getSpecialQuestActionRequired() {
		return specialQuestActionRequired;
	}

	public String getGoodQuestGiverImageSuffix() {
		return goodQuestGiverImageSuffix;
	}

	public String getBadQuestGiverImageSuffix() {
		return badQuestGiverImageSuffix;
	}

	public int getNumComponents(boolean isGoodSide) {
		int numComponents = 0;
		if (specialQuestActionRequired != null)
			return 1;
		if (tasksRequired != null)
			numComponents += tasksRequired.size();
		if (upgradeStructJobsRequired != null)
			numComponents += upgradeStructJobsRequired.size();
		if (buildStructJobsRequired != null)
			numComponents += buildStructJobsRequired.size();
		if (isGoodSide) {
			if (defeatBadGuysJobsRequired != null)
				numComponents += defeatBadGuysJobsRequired.size();
		} else {
			if (defeatGoodGuysJobsRequired != null)
				numComponents += defeatGoodGuysJobsRequired.size();
		}
		if (possessEquipJobsRequired != null)
			numComponents += possessEquipJobsRequired.size();
		if (coinRetrievalAmountRequired > 0)
			numComponents++;
		return numComponents;
	}

	@Override
	public String toString() {
		return "Quest [id=" + id + ", cityId=" + cityId + ", goodName="
				+ goodName + ", badName=" + badName + ", goodDescription="
				+ goodDescription + ", badDescription=" + badDescription
				+ ", goodDoneResponse=" + goodDoneResponse
				+ ", badDoneResponse=" + badDoneResponse
				+ ", goodAcceptDialogue=" + goodAcceptDialogue
				+ ", badAcceptDialogue=" + badAcceptDialogue
				+ ", assetNumWithinCity=" + assetNumWithinCity
				+ ", coinsGained=" + coinsGained + ", diamondsGained="
				+ diamondsGained + ", expGained=" + expGained
				+ ", equipIdGained=" + equipIdGained
				+ ", questsRequiredForThis=" + questsRequiredForThis
				+ ", tasksRequired=" + tasksRequired
				+ ", upgradeStructJobsRequired=" + upgradeStructJobsRequired
				+ ", buildStructJobsRequired=" + buildStructJobsRequired
				+ ", defeatGoodGuysJobsRequired=" + defeatGoodGuysJobsRequired
				+ ", defeatBadGuysJobsRequired=" + defeatBadGuysJobsRequired
				+ ", possessEquipJobsRequired=" + possessEquipJobsRequired
				+ ", coinRetrievalAmountRequired="
				+ coinRetrievalAmountRequired + ", specialQuestActionRequired="
				+ specialQuestActionRequired + "]";
	}

}
