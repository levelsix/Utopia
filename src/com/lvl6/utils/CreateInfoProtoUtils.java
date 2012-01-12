package com.lvl6.utils;

import com.lvl6.info.Equipment;
import com.lvl6.info.Location;
import com.lvl6.info.Task;
import com.lvl6.info.User;
import com.lvl6.proto.InfoProto.FullEquipProto;
import com.lvl6.proto.InfoProto.FullTaskProto;
import com.lvl6.proto.InfoProto.FullUserProto;
import com.lvl6.proto.InfoProto.LocationProto;
import com.lvl6.proto.InfoProto.UserType;

public class CreateInfoProtoUtils {

  public static FullUserProto createFullUserProtoFromUser(User user) {
    FullUserProto ftp = FullUserProto.newBuilder().setUserId(user.getId()).setName(user.getName())
        .setLevel(user.getLevel()).setUserType(user.getType()).setAttack(user.getAttack())
        .setDefense(user.getDefense()).setStamina(user.getStamina()).setEnergy(user.getEnergy())
        .setHealth(user.getHealth()).setSkillPoints(user.getSkillPoints()).setHealthMax(user.getHealthMax())
        .setEnergyMax(user.getEnergyMax()).setStaminaMax(user.getStaminaMax()).setDiamonds(user.getDiamonds())
        .setCoins(user.getCoins()).setVaultBalance(user.getVaultBalance()).setExperience(user.getEnergyMax())
        .setTasksCompleted(user.getTasksCompleted()).setBattlesWon(user.getBattlesWon())
        .setBattlesLost(user.getBattlesLost()).setHourlyCoins(user.getHourlyCoins())
        .setArmyCode(user.getArmyCode()).setNumReferrals(user.getNumReferrals())
        .setUdid(user.getUdid())
        .setUserLocation(CreateInfoProtoUtils.createLocationProtoFromLocation(user.getUserLocation())).build();
    return ftp;
  }

  public static FullEquipProto createFullEquipProtoFromEquip(Equipment equip) {
    FullEquipProto.Builder builder =  FullEquipProto.newBuilder().setEquipId(equip.getId()).setName(equip.getName())
        .setEquipType(equip.getType()).setAttackBoost(equip.getAttackBoost()).setDefenseBoost(equip.getDefenseBoost())
        .setMinLevel(equip.getMinLevel()).setChanceOfLoss(equip.getChanceOfLoss()).setClassType(equip.getClassType());
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

    FullTaskProto ftp = FullTaskProto.newBuilder().setId(task.getId()).setName(name).setCityId(task.getCityId()).setEnergyCost(task.getEnergyCost()).setMinCoinsGained(task.getMinCoinsGained())
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
