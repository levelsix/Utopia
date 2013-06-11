package com.lvl6.info;

import java.io.Serializable;

public class City implements Serializable {

	private static final long serialVersionUID = 6586158330829582696L;
	private int id;
	private String name;
	private int minLevel;
	private int expGainedBaseOnRankup;
	private int coinsGainedBaseOnRankup;
	private String mapImgName;
	private CoordinatePair center;
	private int boosterPackId;

	public City(int id, String name, int minLevel, int expGainedBaseOnRankup,
			int coinsGainedBaseOnRankup, String mapImgName,
			CoordinatePair center, int boosterPackId) {
		super();
		this.id = id;
		this.name = name;
		this.minLevel = minLevel;
		this.expGainedBaseOnRankup = expGainedBaseOnRankup;
		this.coinsGainedBaseOnRankup = coinsGainedBaseOnRankup;
		this.mapImgName = mapImgName;
		this.center = center;
		this.boosterPackId = boosterPackId;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getMinLevel() {
		return minLevel;
	}

	public void setMinLevel(int minLevel) {
		this.minLevel = minLevel;
	}

	public int getExpGainedBaseOnRankup() {
		return expGainedBaseOnRankup;
	}

	public void setExpGainedBaseOnRankup(int expGainedBaseOnRankup) {
		this.expGainedBaseOnRankup = expGainedBaseOnRankup;
	}

	public int getCoinsGainedBaseOnRankup() {
		return coinsGainedBaseOnRankup;
	}

	public void setCoinsGainedBaseOnRankup(int coinsGainedBaseOnRankup) {
		this.coinsGainedBaseOnRankup = coinsGainedBaseOnRankup;
	}

	public String getMapImgName() {
		return mapImgName;
	}

	public void setMapImgName(String mapImgName) {
		this.mapImgName = mapImgName;
	}

	public CoordinatePair getCenter() {
		return center;
	}

	public void setCenter(CoordinatePair center) {
		this.center = center;
	}

	public int getBoosterPackId() {
    return boosterPackId;
  }

  public void setBoosterPackId(int boosterPackId) {
    this.boosterPackId = boosterPackId;
  }

  @Override
  public String toString() {
    return "City [id=" + id + ", name=" + name + ", minLevel=" + minLevel
        + ", expGainedBaseOnRankup=" + expGainedBaseOnRankup
        + ", coinsGainedBaseOnRankup=" + coinsGainedBaseOnRankup
        + ", mapImgName=" + mapImgName + ", center=" + center
        + ", boosterPackId=" + boosterPackId + "]";
  }

}
