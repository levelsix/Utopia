package com.lvl6.utils;

import com.lvl6.info.Equipment;
import com.lvl6.info.Location;
import com.lvl6.info.MarketplacePost;
import com.lvl6.info.Task;
import com.lvl6.info.User;
import com.lvl6.info.UserStruct;
import com.lvl6.proto.InfoProto.CoordinateProto;
import com.lvl6.proto.InfoProto.FullEquipProto;
import com.lvl6.proto.InfoProto.FullMarketplacePostProto;
import com.lvl6.proto.InfoProto.FullTaskProto;
import com.lvl6.proto.InfoProto.FullUserProto;
import com.lvl6.proto.InfoProto.FullUserStructureProto;
import com.lvl6.proto.InfoProto.LocationProto;
import com.lvl6.proto.InfoProto.MarketplacePostType;
import com.lvl6.proto.InfoProto.UserType;

public class CreateInfoProtoUtils {

  public static FullMarketplacePostProto createFullMarketplacePostProtoFromMarketplacePost(MarketplacePost mp) {
    FullMarketplacePostProto.Builder builder = FullMarketplacePostProto.newBuilder().setMarketplacePostId(mp.getId())
        .setPosterId(mp.getPosterId()).setPostType(mp.getPostType())
        .setTimeOfPost(mp.getTimeOfPost().getTime());

    if (mp.getPostType() == MarketplacePostType.COIN_POST) {
      builder.setPostedCoins(mp.getPostedCoins());
    }
    if (mp.getPostType() == MarketplacePostType.DIAMOND_POST) {
      builder.setDiamondCost(mp.getPostedDiamonds());
    }
    if (mp.getPostType() == MarketplacePostType.EQUIP_POST) {
      builder.setPostedEquipId(mp.getPostedEquipId());
    }
    if (mp.getPostType() == MarketplacePostType.WOOD_POST) {
      builder.setPostedWood(mp.getPostedWood());
    }
    if (mp.getDiamondCost() != MarketplacePost.NOT_SET) {
      builder.setDiamondCost(mp.getDiamondCost());
    }
    if (mp.getCoinCost() != MarketplacePost.NOT_SET) {
      builder.setCoinCost(mp.getCoinCost());
    }
    if (mp.getWoodCost() != MarketplacePost.NOT_SET) {
      builder.setWoodCost(mp.getWoodCost());
    }
    return builder.build();
  }

  public static FullUserStructureProto createFullUserStructureProto(UserStruct userStruct) {
    FullUserStructureProto.Builder builder = FullUserStructureProto.newBuilder().setId(userStruct.getId())
        .setUserId(userStruct.getId()).setStructId(userStruct.getStructId()).setLevel(userStruct.getLevel())
        .setIsComplete(userStruct.isComplete()).setCoordinates(CoordinateProto.newBuilder().setX(userStruct.getCoordinates().getX())
            .setY(userStruct.getCoordinates().getY()));
    if (userStruct.getPurchaseTime() != null) {
      builder.setPurchaseTime(userStruct.getPurchaseTime().getTime());
    }
    if (userStruct.getLastRetrieved() != null) {
      builder.setLastRetrieved(userStruct.getLastRetrieved().getTime());
    }
    return builder.build();
  }

  public static FullUserProto createFullUserProtoFromUser(User user) {
    FullUserProto ftp = FullUserProto.newBuilder().setUserId(user.getId()).setName(user.getName())
        .setLevel(user.getLevel()).setUserType(user.getType()).setAttack(user.getAttack())
        .setDefense(user.getDefense()).setStamina(user.getStamina()).setEnergy(user.getEnergy())
        .setSkillPoints(user.getSkillPoints()).setHealthMax(user.getHealthMax())
        .setEnergyMax(user.getEnergyMax()).setStaminaMax(user.getStaminaMax()).setDiamonds(user.getDiamonds())
        .setCoins(user.getCoins()).setWood(user.getWood()).setMarketplaceDiamondsEarnings(user.getMarketplaceDiamondsEarnings())
        .setMarketplaceCoinsEarnings(user.getMarketplaceCoinsEarnings()).setMarketplaceWoodEarnings(user.getMarketplaceWoodEarnings())
        .setVaultBalance(user.getVaultBalance()).setExperience(user.getEnergyMax())
        .setTasksCompleted(user.getTasksCompleted()).setBattlesWon(user.getBattlesWon())
        .setBattlesLost(user.getBattlesLost()).setHourlyCoins(user.getHourlyCoins())
        .setArmyCode(user.getArmyCode()).setNumReferrals(user.getNumReferrals())
        .setUdid(user.getUdid())
        .setUserLocation(CreateInfoProtoUtils.createLocationProtoFromLocation(user.getUserLocation()))
        .setNumPostsInMarketplace(user.getNumPostsInMarketplace()).build();
    return ftp;
  }

  public static FullEquipProto createFullEquipProtoFromEquip(Equipment equip) {
    FullEquipProto.Builder builder =  FullEquipProto.newBuilder().setEquipId(equip.getId()).setName(equip.getName())
        .setEquipType(equip.getType()).setAttackBoost(equip.getAttackBoost()).setDefenseBoost(equip.getDefenseBoost())
        .setMinLevel(equip.getMinLevel()).setChanceOfLoss(equip.getChanceOfLoss()).setClassType(equip.getClassType())
        .setRarity(equip.getRarity());
    if (equip.getCoinPrice() != Equipment.NOT_SET) {
      builder.setCoinPrice(equip.getCoinPrice());
    }
    if (equip.getDiamondPrice() != Equipment.NOT_SET) {
      builder.setDiamondPrice(equip.getDiamondPrice());
    }
    return builder.build();
  }

  public static FullTaskProto createFullTaskProtoFromTask(UserType userType, Task task) {
    boolean goodSide;
    if (userType == UserType.GOOD_ARCHER || userType == UserType.GOOD_MAGE || 
        userType == UserType.GOOD_WARRIOR) goodSide = true;
    else goodSide = false;

    String name = null;

    if (goodSide) {
      name = task.getGoodName();
    } else {
      name = task.getBadName();
    }

    FullTaskProto ftp = FullTaskProto.newBuilder().setTaskId(task.getId()).setName(name).setCityId(task.getCityId()).setEnergyCost(task.getEnergyCost()).setMinCoinsGained(task.getMinCoinsGained())
        .setMaxCoinsGained(task.getMaxCoinsGained()).setChanceOfEquipLoot(task.getChanceOfEquipFloat())
        .setExpGained(task.getExpGained()).setAssetNumWithinCity(task.getAssetNumberWithinCity()).
        setNumRequiredForCompletion(task.getNumForCompletion()).addAllPotentialLootEquipIds(task.getPotentialLootEquipIds()).build();

    return ftp;
  }

  public static LocationProto createLocationProtoFromLocation(Location location) {
    LocationProto lp = LocationProto.newBuilder().setLatitude(location.getLatitude()).setLongitude(location.getLongitude()).build();
    return lp;
  }


}
