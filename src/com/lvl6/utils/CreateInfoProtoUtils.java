package com.lvl6.utils;

import java.util.List;
import java.util.Map;

import com.lvl6.info.City;
import com.lvl6.info.CoordinatePair;
import com.lvl6.info.Equipment;
import com.lvl6.info.Location;
import com.lvl6.info.MarketplacePost;
import com.lvl6.info.Quest;
import com.lvl6.info.Structure;
import com.lvl6.info.Task;
import com.lvl6.info.User;
import com.lvl6.info.UserCritstruct;
import com.lvl6.info.UserEquip;
import com.lvl6.info.UserStruct;
import com.lvl6.info.jobs.BuildStructJob;
import com.lvl6.info.jobs.DefeatTypeJob;
import com.lvl6.info.jobs.PossessEquipJob;
import com.lvl6.info.jobs.UpgradeStructJob;
import com.lvl6.proto.InfoProto.BuildStructJobProto;
import com.lvl6.proto.InfoProto.CoordinateProto;
import com.lvl6.proto.InfoProto.DefeatTypeJobProto;
import com.lvl6.proto.InfoProto.FullCityProto;
import com.lvl6.proto.InfoProto.FullEquipProto;
import com.lvl6.proto.InfoProto.FullMarketplacePostProto;
import com.lvl6.proto.InfoProto.FullQuestProto;
import com.lvl6.proto.InfoProto.FullStructureProto;
import com.lvl6.proto.InfoProto.FullTaskProto;
import com.lvl6.proto.InfoProto.FullTaskProto.FullTaskEquipReqProto;
import com.lvl6.proto.InfoProto.FullUserCritstructProto;
import com.lvl6.proto.InfoProto.FullUserEquipProto;
import com.lvl6.proto.InfoProto.FullUserProto;
import com.lvl6.proto.InfoProto.FullUserStructureProto;
import com.lvl6.proto.InfoProto.LocationProto;
import com.lvl6.proto.InfoProto.MarketplacePostType;
import com.lvl6.proto.InfoProto.PossessEquipJobProto;
import com.lvl6.proto.InfoProto.UpgradeStructJobProto;
import com.lvl6.proto.InfoProto.UserType;
import com.lvl6.retrieveutils.rarechange.EquipmentRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.TaskEquipReqRetrieveUtils;
import com.lvl6.utils.utilmethods.MiscMethods;

public class CreateInfoProtoUtils {

  public static FullQuestProto createFullQuestProtoFromQuest(UserType userType, Quest quest) {
    boolean goodSide = MiscMethods.checkIfGoodSide(userType);
    
    String name = null;
    String description = null;
    String doneResponse = null;
    String inProgress = null;
    List<Integer> defeatTypeReqs = null;

    if (goodSide) {
      name = quest.getGoodName();
      description = quest.getGoodDescription();
      doneResponse = quest.getGoodDoneResponse();
      inProgress = quest.getGoodInProgress();
      defeatTypeReqs = quest.getDefeatBadGuysJobsRequired();
    } else {
      name = quest.getBadName();
      description = quest.getBadDescription();
      doneResponse = quest.getBadDoneResponse();
      inProgress = quest.getBadInProgress();
      defeatTypeReqs = quest.getDefeatGoodGuysJobsRequired();
    }

    return FullQuestProto.newBuilder().setQuestId(quest.getId()).setCityId(quest.getCityId()).setName(name)
        .setDescription(description).setDoneResponse(doneResponse).setInProgress(inProgress).setAssetNumWithinCity(quest.getAssetNumWithinCity())
        .setCoinsGained(quest.getCoinsGained()).setDiamondsGained(quest.getDiamondsGained()).setWoodGained(quest.getWoodGained())
        .setExpGained(quest.getExpGained()).setEquipIdGained(quest.getEquipIdGained()).addAllQuestsRequiredForThis(quest.getQuestsRequiredForThis())
        .addAllTaskReqs(quest.getTasksRequired()).addAllUpgradeStructJobsReqs(quest.getUpgradeStructJobsRequired())
        .addAllBuildStructJobsReqs(quest.getBuildStructJobsRequired())
        .addAllDefeatTypeReqs(defeatTypeReqs).addAllPossessEquipJobReqs(quest.getPossessEquipJobsRequired()).build();
  }
  
  public static FullMarketplacePostProto createFullMarketplacePostProtoFromMarketplacePost(MarketplacePost mp) {
    FullMarketplacePostProto.Builder builder = FullMarketplacePostProto.newBuilder().setMarketplacePostId(mp.getId())
        .setPosterId(mp.getPosterId()).setPostType(mp.getPostType())
        .setTimeOfPost(mp.getTimeOfPost().getTime());

    if (mp.getPostType() == MarketplacePostType.COIN_POST) {
      builder.setPostedCoins(mp.getPostedCoins());
    }
    if (mp.getPostType() == MarketplacePostType.DIAMOND_POST) {
      builder.setPostedDiamonds(mp.getPostedDiamonds());
    }
    if (mp.getPostType() == MarketplacePostType.EQUIP_POST) {
      builder.setPostedEquip(CreateInfoProtoUtils.createFullEquipProtoFromEquip(
          EquipmentRetrieveUtils.getEquipmentIdsToEquipment().get(mp.getPostedEquipId())));
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

  public static FullUserStructureProto createFullUserStructureProtoFromUserstruct(UserStruct userStruct) {
    FullUserStructureProto.Builder builder = FullUserStructureProto.newBuilder().setUserStructId(userStruct.getId())
        .setUserId(userStruct.getUserId()).setStructId(userStruct.getStructId()).setLevel(userStruct.getLevel())
        .setIsComplete(userStruct.isComplete())
        .setCoordinates(CreateInfoProtoUtils.createCoordinateProtoFromCoordinatePair(userStruct.getCoordinates()))
        .setOrientation(userStruct.getOrientation());
    if (userStruct.getPurchaseTime() != null) {
      builder.setPurchaseTime(userStruct.getPurchaseTime().getTime());
    }
    if (userStruct.getLastRetrieved() != null) {
      builder.setLastRetrieved(userStruct.getLastRetrieved().getTime());
    }
    if (userStruct.getLastUpgradeTime() != null) {
      builder.setLastRetrieved(userStruct.getLastUpgradeTime().getTime());
    }
    return builder.build();
  }

  public static FullUserProto createFullUserProtoFromUser(User u) {
    FullUserProto ftp = FullUserProto.newBuilder().setUserId(u.getId()).setName(u.getName())
        .setLevel(u.getLevel()).setUserType(u.getType()).setAttack(u.getAttack())
        .setDefense(u.getDefense()).setStamina(u.getStamina()).setEnergy(u.getEnergy())
        .setSkillPoints(u.getSkillPoints()).setHealthMax(u.getHealthMax())
        .setEnergyMax(u.getEnergyMax()).setStaminaMax(u.getStaminaMax()).setDiamonds(u.getDiamonds())
        .setCoins(u.getCoins()).setWood(u.getWood()).setMarketplaceDiamondsEarnings(u.getMarketplaceDiamondsEarnings())
        .setMarketplaceCoinsEarnings(u.getMarketplaceCoinsEarnings()).setMarketplaceWoodEarnings(u.getMarketplaceWoodEarnings())
        .setVaultBalance(u.getVaultBalance()).setExperience(u.getEnergyMax())
        .setTasksCompleted(u.getTasksCompleted()).setBattlesWon(u.getBattlesWon())
        .setBattlesLost(u.getBattlesLost()).setHourlyCoins(u.getHourlyCoins())
        .setArmyCode(u.getArmyCode()).setNumReferrals(u.getNumReferrals())
        .setUdid(u.getUdid())
        .setUserLocation(CreateInfoProtoUtils.createLocationProtoFromLocation(u.getUserLocation()))
        .setNumPostsInMarketplace(u.getNumPostsInMarketplace()).setNumMarketplaceSalesUnredeemed(u.getNumMarketplaceSalesUnredeemed()).build();
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
  
  public static FullUserEquipProto createFullUserEquipProtoFromUserEquip(UserEquip ue) {
    return FullUserEquipProto.newBuilder().setUserId(ue.getUserId()).setEquipId(ue.getEquipId())
        .setQuantity(ue.getQuantity()).setIsStolen(ue.isStolen()).build();
  }
  
  public static FullUserCritstructProto createFullUserCritstructProtoFromUserCritstruct(UserCritstruct uc) {
    return FullUserCritstructProto.newBuilder().setType(uc.getType())
        .setCoords(CreateInfoProtoUtils.createCoordinateProtoFromCoordinatePair(uc.getCoords()))
        .setOrientation(uc.getOrientation()).build();
  }
    
  public static FullTaskProto createFullTaskProtoFromTask(UserType userType, Task task) {

    boolean goodSide = MiscMethods.checkIfGoodSide(userType);
    
    String name = null;

    if (goodSide) {
      name = task.getGoodName();
    } else {
      name = task.getBadName();
    }

    FullTaskProto.Builder builder = FullTaskProto.newBuilder().setTaskId(task.getId()).setName(name).setCityId(task.getCityId()).setEnergyCost(task.getEnergyCost()).setMinCoinsGained(task.getMinCoinsGained())
        .setMaxCoinsGained(task.getMaxCoinsGained()).setChanceOfEquipLoot(task.getChanceOfEquipFloat())
        .setExpGained(task.getExpGained()).setAssetNumWithinCity(task.getAssetNumberWithinCity()).
        setNumRequiredForCompletion(task.getNumForCompletion()).addAllPotentialLootEquipIds(task.getPotentialLootEquipIds());

    Map<Integer, Integer> equipIdsToQuantity = TaskEquipReqRetrieveUtils.getEquipmentIdsToQuantityForTaskId(task.getId());
    if (equipIdsToQuantity != null && equipIdsToQuantity.size() > 0) {
      for (Integer equipId : equipIdsToQuantity.keySet()) {
        FullTaskEquipReqProto fterp = FullTaskEquipReqProto.newBuilder().setTaskId(task.getId()).setEquipId(equipId).setQuantity(equipIdsToQuantity.get(equipId)).build();
        builder.addEquipReqs(fterp);
      }
    }
    return builder.build();
  }

  public static LocationProto createLocationProtoFromLocation(Location location) {
    LocationProto lp = LocationProto.newBuilder().setLatitude(location.getLatitude()).setLongitude(location.getLongitude()).build();
    return lp;
  }
  
  public static CoordinateProto createCoordinateProtoFromCoordinatePair(CoordinatePair cp) {
    return CoordinateProto.newBuilder().setX(cp.getX()).setY(cp.getY()).build();
  }

  public static FullStructureProto createFullStructureProtoFromStructure(Structure s) {
    return FullStructureProto.newBuilder().setStructId(s.getId()).setName(s.getName()).setIncome(s.getIncome())
        .setMinutesToGain(s.getMinutesToGain()).setMinutesToBuild(s.getMinutesToBuild())
        .setMinutesToUpgradeBase(s.getMinutesToUpgradeBase()).setCoinPrice(s.getCoinPrice())
        .setDiamondPrice(s.getDiamondPrice()).setWoodPrice(s.getWoodPrice()).setMinLevel(s.getMinLevel())
        .setXLength(s.getxLength()).setYLength(s.getyLength()).setUpgradeCoinCostBase(s.getUpgradeCoinCostBase())
        .setUpgradeDiamondCostBase(s.getDiamondPrice()).setUpgradeWoodCostBase(s.getUpgradeWoodCostBase())
        .setInstaBuildDiamondCostBase(s.getInstaBuildDiamondCostBase())
        .setInstaRetrieveDiamondCostBase(s.getInstaRetrieveDiamondCostBase())
        .setInstaUpgradeDiamondCostBase(s.getInstaUpgradeDiamondCostBase()).build();
  }

  public static FullCityProto createFullCityProtoFromCity(City c) {
    return FullCityProto.newBuilder().setCityId(c.getId()).setName(c.getName()).setMinLevel(c.getMinLevel())
        .setExpGainedBaseOnRankup(c.getExpGainedBaseOnRankup()).setCoinsGainedBaseOnRankup(c.getCoinsGainedBaseOnRankup())
        .build();
  }

  public static BuildStructJobProto createFullBuildStructJobProtoFromBuildStructJob(
      BuildStructJob j) {
    return BuildStructJobProto.newBuilder().setBuildStructJobId(j.getId()).setStructId(j.getStructId()).setQuantityRequired(j.getQuantity()).build();
  }

  public static DefeatTypeJobProto createFullDefeatTypeJobProtoFromDefeatTypeJob(
      DefeatTypeJob j) {
    return DefeatTypeJobProto.newBuilder().setDefeatTypeJobId(j.getId()).setTypeOfEnemy(j.getEnemyType())
        .setNumEnemiesToDefeat(j.getNumEnemiesToDefeat()).setCityId(j.getCityId()).build();
  }

  public static UpgradeStructJobProto createFullUpgradeStructJobProtoFromUpgradeStructJob(
      UpgradeStructJob j) {
    return UpgradeStructJobProto.newBuilder().setUpgradeStructJobId(j.getId()).setStructId(j.getStructId()).setLevelReq(j.getLevelReq()).build();
  }

  public static PossessEquipJobProto createFullPossessEquipJobProtoFromPossessEquipJob(
      PossessEquipJob j) {
    return PossessEquipJobProto.newBuilder().setPossessEquipJobId(j.getId()).setEquipId(j.getEquipId()).setQuantityReq(j.getQuantity()).build();
  }


}
