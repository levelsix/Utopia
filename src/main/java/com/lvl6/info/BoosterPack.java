package com.lvl6.info;

import java.io.Serializable;

public class BoosterPack implements Serializable {
  private static final long serialVersionUID = -5134245989644009715L;
  private int id;
  private int coinCost;
  private int diamondCost;
  private String name;
  private String image;
  private String description;
  private int numEquips;
  
  public BoosterPack(int id, int coinCost, int diamondCost,
      String name, String image, String description, int numEquips) {
    super();
    this.id = id;
    this.coinCost = coinCost;
    this.diamondCost = diamondCost;
    this.name = name;
    this.image = image;
    this.description = description;
    this.numEquips = numEquips;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getCoinCost() {
    return coinCost;
  }

  public void setCoinCost(int coinCost) {
    this.coinCost = coinCost;
  }

  public int getDiamondCost() {
    return diamondCost;
  }

  public void setDiamondCost(int diamondCost) {
    this.diamondCost = diamondCost;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getImage() {
    return image;
  }

  public void setImage(String image) {
    this.image = image;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public int getNumEquips() {
    return numEquips;
  }

  public void setNumEquips(int numEquips) {
    this.numEquips = numEquips;
  }

  @Override
  public String toString() {
    return "BoosterPack [id=" + id + ", coinCost=" + coinCost
        + ", diamondCost=" + diamondCost + ", name=" + name + ", image="
        + image + ", description=" + description + ", numEquips=" + numEquips
        + "]";
  }
  
}
