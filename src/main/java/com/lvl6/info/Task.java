package com.lvl6.info;

import java.io.Serializable;
import java.util.List;

import com.lvl6.proto.InfoProto.AnimationType;

public class Task implements Serializable {

	private static final long serialVersionUID = -3002131432760456790L;
	private int id;
	private String goodName;
	private String badName;
	private int cityId;
	private int energyCost;
	private int minCoinsGained;
	private int maxCoinsGained;
	private float chanceOfEquipFloat;
	private List<Integer> potentialLootEquipIds;
	private int expGained;
	private int assetNumberWithinCity;
	private int numForCompletion;
	private String goodProcessingText;
	private String badProcessingText;
	private CoordinatePair spriteLandingCoords;
	private AnimationType animationType;

	public Task(int id, String goodName, String badName, int cityId,
			int energyCost, int minCoinsGained, int maxCoinsGained,
			float chanceOfEquipFloat, List<Integer> potentialLootEquipIds,
			int expGained, int assetNumberWithinCity, int numForCompletion,
			String goodProcessingText, String badProcessingText,
			CoordinatePair spriteLandingCoords, AnimationType animationType) {
		super();
		this.id = id;
		this.goodName = goodName;
		this.badName = badName;
		this.cityId = cityId;
		this.energyCost = energyCost;
		this.minCoinsGained = minCoinsGained;
		this.maxCoinsGained = maxCoinsGained;
		this.chanceOfEquipFloat = chanceOfEquipFloat;
		this.potentialLootEquipIds = potentialLootEquipIds;
		this.expGained = expGained;
		this.assetNumberWithinCity = assetNumberWithinCity;
		this.numForCompletion = numForCompletion;
		this.goodProcessingText = goodProcessingText;
		this.badProcessingText = badProcessingText;
		this.spriteLandingCoords = spriteLandingCoords;
		this.animationType = animationType;
	}

	public int getId() {
		return id;
	}

	public String getGoodName() {
		return goodName;
	}

	public String getBadName() {
		return badName;
	}

	public int getCityId() {
		return cityId;
	}

	public int getEnergyCost() {
		return energyCost;
	}

	public int getMinCoinsGained() {
		return minCoinsGained;
	}

	public int getMaxCoinsGained() {
		return maxCoinsGained;
	}

	public float getChanceOfEquipFloat() {
		return chanceOfEquipFloat;
	}

	public List<Integer> getPotentialLootEquipIds() {
		return potentialLootEquipIds;
	}

	public void setPotentialLootEquipIds(List<Integer> potentialLootEquipIds) {
		this.potentialLootEquipIds = potentialLootEquipIds;
	}

	public int getExpGained() {
		return expGained;
	}

	public int getAssetNumberWithinCity() {
		return assetNumberWithinCity;
	}

	public int getNumForCompletion() {
		return numForCompletion;
	}

	public String getGoodProcessingText() {
		return goodProcessingText;
	}

	public String getBadProcessingText() {
		return badProcessingText;
	}

	public CoordinatePair getSpriteLandingCoords() {
		return spriteLandingCoords;
	}

	public AnimationType getAnimationType() {
		return animationType;
	}

	@Override
	public String toString() {
		return "Task [id=" + id + ", goodName=" + goodName + ", badName="
				+ badName + ", cityId=" + cityId + ", energyCost=" + energyCost
				+ ", minCoinsGained=" + minCoinsGained + ", maxCoinsGained="
				+ maxCoinsGained + ", chanceOfEquipFloat=" + chanceOfEquipFloat
				+ ", potentialLootEquipIds=" + potentialLootEquipIds
				+ ", expGained=" + expGained + ", assetNumberWithinCity="
				+ assetNumberWithinCity + ", numForCompletion="
				+ numForCompletion + ", goodProcessingText="
				+ goodProcessingText + ", badProcessingText="
				+ badProcessingText + ", spriteLandingCoords="
				+ spriteLandingCoords + ", animationType=" + animationType
				+ "]";
	}

}
