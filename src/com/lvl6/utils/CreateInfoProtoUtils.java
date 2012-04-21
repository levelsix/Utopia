package com.lvl6.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.lvl6.info.BattleDetails;
import com.lvl6.info.City;
import com.lvl6.info.CoordinatePair;
import com.lvl6.info.Dialogue;
import com.lvl6.info.Equipment;
import com.lvl6.info.Location;
import com.lvl6.info.MarketplacePost;
import com.lvl6.info.MarketplaceTransaction;
import com.lvl6.info.NeutralCityElement;
import com.lvl6.info.Quest;
import com.lvl6.info.Referral;
import com.lvl6.info.Structure;
import com.lvl6.info.Task;
import com.lvl6.info.User;
import com.lvl6.info.UserCityExpansionData;
import com.lvl6.info.UserCritstruct;
import com.lvl6.info.UserEquip;
import com.lvl6.info.UserQuest;
import com.lvl6.info.UserStruct;
import com.lvl6.info.jobs.BuildStructJob;
import com.lvl6.info.jobs.DefeatTypeJob;
import com.lvl6.info.jobs.PossessEquipJob;
import com.lvl6.info.jobs.UpgradeStructJob;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.StartupResponseProto.AttackedNotificationProto;
import com.lvl6.proto.EventProto.StartupResponseProto.MarketplacePostPurchasedNotificationProto;
import com.lvl6.proto.EventProto.StartupResponseProto.ReferralNotificationProto;
import com.lvl6.proto.InfoProto.BuildStructJobProto;
import com.lvl6.proto.InfoProto.CoordinateProto;
import com.lvl6.proto.InfoProto.DefeatTypeJobProto;
import com.lvl6.proto.InfoProto.DialogueProto;
import com.lvl6.proto.InfoProto.DialogueProto.SpeechSegmentProto;
import com.lvl6.proto.InfoProto.DialogueProto.SpeechSegmentProto.DialogueSpeaker;
import com.lvl6.proto.InfoProto.FullCityProto;
import com.lvl6.proto.InfoProto.FullEquipProto;
import com.lvl6.proto.InfoProto.FullMarketplacePostProto;
import com.lvl6.proto.InfoProto.FullQuestProto;
import com.lvl6.proto.InfoProto.FullStructureProto;
import com.lvl6.proto.InfoProto.FullTaskProto;
import com.lvl6.proto.InfoProto.FullTaskProto.FullTaskEquipReqProto;
import com.lvl6.proto.InfoProto.FullUserCityExpansionDataProto;
import com.lvl6.proto.InfoProto.FullUserCityProto;
import com.lvl6.proto.InfoProto.FullUserCritstructProto;
import com.lvl6.proto.InfoProto.FullUserEquipProto;
import com.lvl6.proto.InfoProto.FullUserProto;
import com.lvl6.proto.InfoProto.FullUserQuestDataLargeProto;
import com.lvl6.proto.InfoProto.FullUserStructureProto;
import com.lvl6.proto.InfoProto.LocationProto;
import com.lvl6.proto.InfoProto.MinimumUserBuildStructJobProto;
import com.lvl6.proto.InfoProto.MinimumUserDefeatTypeJobProto;
import com.lvl6.proto.InfoProto.MinimumUserPossessEquipJobProto;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.InfoProto.MinimumUserQuestTaskProto;
import com.lvl6.proto.InfoProto.MinimumUserTaskProto;
import com.lvl6.proto.InfoProto.MinimumUserUpgradeStructJobProto;
import com.lvl6.proto.InfoProto.NeutralCityElementProto;
import com.lvl6.proto.InfoProto.PossessEquipJobProto;
import com.lvl6.proto.InfoProto.UpgradeStructJobProto;
import com.lvl6.proto.InfoProto.UserType;
import com.lvl6.retrieveutils.UserEquipRetrieveUtils;
import com.lvl6.retrieveutils.UserQuestsCompletedDefeatTypeJobsRetrieveUtils;
import com.lvl6.retrieveutils.UserQuestsCompletedTasksRetrieveUtils;
import com.lvl6.retrieveutils.UserQuestsDefeatTypeJobProgressRetrieveUtils;
import com.lvl6.retrieveutils.UserQuestsTaskProgressRetrieveUtils;
import com.lvl6.retrieveutils.UserStructRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.BuildStructJobRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.DefeatTypeJobRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.EquipmentRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.PossessEquipJobRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.TaskEquipReqRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.TaskRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.UpgradeStructJobRetrieveUtils;
import com.lvl6.utils.utilmethods.MiscMethods;

public class CreateInfoProtoUtils {


  public static ReferralNotificationProto createReferralNotificationProtoFromReferral(
      Referral r, User newlyReferred) {
    return ReferralNotificationProto.newBuilder().setReferred(createMinimumUserProtoFromUser(newlyReferred))
        .setRecruitTime(r.getTimeOfReferral().getTime()).setCoinsGivenToReferrer(r.getCoinsGivenToReferrer()).build();
  }

  public static MarketplacePostPurchasedNotificationProto createMarketplacePostPurchasedNotificationProtoFromMarketplaceTransaction(MarketplaceTransaction mt, User buyer) {
    FullMarketplacePostProto fmpp = createFullMarketplacePostProtoFromMarketplacePost(mt.getPost());
    return MarketplacePostPurchasedNotificationProto.newBuilder().setMarketplacePost(fmpp)
        .setBuyer(createMinimumUserProtoFromUser(buyer)).setTimeOfPurchase(mt.getTimeOfPurchase().getTime()).build();
  }

  public static AttackedNotificationProto createAttackedNotificationProtoFromBattleHistory(BattleDetails bd, User attacker) {
    AttackedNotificationProto.Builder builder = AttackedNotificationProto.newBuilder();
    builder.setAttacker(createMinimumUserProtoFromUser(attacker)).setBattleResult(bd.getResult())
    .setBattleCompleteTime(bd.getBattleCompleteTime().getTime());
    if (bd.getCoinsStolen() != ControllerConstants.NOT_SET && bd.getCoinsStolen() > 0) {
      builder.setCoinsStolen(bd.getCoinsStolen());
    }
    if (bd.getEquipStolen() != ControllerConstants.NOT_SET && bd.getEquipStolen() > 0) {
      builder.setStolenEquipId(bd.getEquipStolen());
    }
    return builder.build();
  }

  public static MinimumUserProto createMinimumUserProtoFromUser(User u) {
    return MinimumUserProto.newBuilder().setName(u.getName()).setUserId(u.getId()).setUserType(u.getType()).build();
  }

  public static FullUserCityExpansionDataProto createFullUserCityExpansionDataProtoFromUserCityExpansionData(UserCityExpansionData uced) {
    FullUserCityExpansionDataProto.Builder builder = FullUserCityExpansionDataProto.newBuilder().setUserId(uced.getUserId())
        .setFarLeftExpansions(uced.getFarLeftExpansions()).setFarRightExpansions(uced.getFarRightExpansions())
        .setIsExpanding(uced.isExpanding());
    if (uced.getLastExpandTime() != null) {
      builder.setLastExpandTime(uced.getLastExpandTime().getTime());
    }
    if (uced.getLastExpandDirection() != null) {
      builder.setLastExpandDirection(uced.getLastExpandDirection());
    }
    return builder.build();
  }

  public static FullQuestProto createFullQuestProtoFromQuest(UserType userType, Quest quest) {
    boolean goodSide = MiscMethods.checkIfGoodSide(userType);

    String name = null;
    String description = null;
    String doneResponse = null;
    String inProgress = null;
    List<Integer> defeatTypeReqs = null;
    Dialogue acceptDialogue = null;

    if (goodSide) {
      name = quest.getGoodName();
      description = quest.getGoodDescription();
      doneResponse = quest.getGoodDoneResponse();
      inProgress = quest.getGoodInProgress();
      defeatTypeReqs = quest.getDefeatBadGuysJobsRequired();
      acceptDialogue = quest.getGoodAcceptDialogue();
    } else {
      name = quest.getBadName();
      description = quest.getBadDescription();
      doneResponse = quest.getBadDoneResponse();
      inProgress = quest.getBadInProgress();
      defeatTypeReqs = quest.getDefeatGoodGuysJobsRequired();
      acceptDialogue = quest.getBadAcceptDialogue();
    }

    FullQuestProto.Builder builder = FullQuestProto.newBuilder().setQuestId(quest.getId()).setCityId(quest.getCityId()).setName(name)
        .setDescription(description).setDoneResponse(doneResponse).setInProgress(inProgress).setAssetNumWithinCity(quest.getAssetNumWithinCity())
        .setCoinsGained(quest.getCoinsGained()).setDiamondsGained(quest.getDiamondsGained())
        .setExpGained(quest.getExpGained()).setEquipIdGained(quest.getEquipIdGained()).addAllQuestsRequiredForThis(quest.getQuestsRequiredForThis())
        .addAllTaskReqs(quest.getTasksRequired()).addAllUpgradeStructJobsReqs(quest.getUpgradeStructJobsRequired())
        .addAllBuildStructJobsReqs(quest.getBuildStructJobsRequired())
        .addAllDefeatTypeReqs(defeatTypeReqs).addAllPossessEquipJobReqs(quest.getPossessEquipJobsRequired())
        .setNumComponentsForGood(quest.getNumComponents(true)).setNumComponentsForBad(quest.getNumComponents(false));
    if (acceptDialogue != null) {
      builder.setAcceptDialogue(createDialogueProtoFromDialogue(acceptDialogue));
    }
    return builder.build();
  }
  
  private static DialogueProto createDialogueProtoFromDialogue(Dialogue d) {
    if (d == null) return null;

    DialogueProto.Builder dp = DialogueProto.newBuilder();
    
    List<String> speakerTexts = d.getSpeakerTexts();
    int i = 0;
    for (DialogueSpeaker speaker : d.getSpeakers()) {
      dp.addSpeechSegment(SpeechSegmentProto.newBuilder().setSpeaker(speaker).
          setSpeakerText(speakerTexts.get(i)).build());
      i++;
    }
    return dp.build();
  }

  public static FullMarketplacePostProto createFullMarketplacePostProtoFromMarketplacePost(MarketplacePost mp) {
    FullMarketplacePostProto.Builder builder = FullMarketplacePostProto.newBuilder().setMarketplacePostId(mp.getId())
        .setPosterId(mp.getPosterId()).setPostType(mp.getPostType())
        .setTimeOfPost(mp.getTimeOfPost().getTime()).setPostedEquip(createFullEquipProtoFromEquip(
            EquipmentRetrieveUtils.getEquipmentIdsToEquipment().get(mp.getPostedEquipId())));
    if (mp.getDiamondCost() != ControllerConstants.NOT_SET) {
      builder.setDiamondCost(mp.getDiamondCost());
    }
    if (mp.getCoinCost() != ControllerConstants.NOT_SET) {
      builder.setCoinCost(mp.getCoinCost());
    }
    return builder.build();
  }

  public static FullUserStructureProto createFullUserStructureProtoFromUserstruct(UserStruct userStruct) {
    FullUserStructureProto.Builder builder = FullUserStructureProto.newBuilder().setUserStructId(userStruct.getId())
        .setUserId(userStruct.getUserId()).setStructId(userStruct.getStructId()).setLevel(userStruct.getLevel())
        .setIsComplete(userStruct.isComplete())
        .setCoordinates(createCoordinateProtoFromCoordinatePair(userStruct.getCoordinates()))
        .setOrientation(userStruct.getOrientation());
    if (userStruct.getPurchaseTime() != null) {
      builder.setPurchaseTime(userStruct.getPurchaseTime().getTime());
    }
    if (userStruct.getLastRetrieved() != null) {
      builder.setLastRetrieved(userStruct.getLastRetrieved().getTime());
    }
    if (userStruct.getLastUpgradeTime() != null) {
      builder.setLastUpgradeTime(userStruct.getLastUpgradeTime().getTime());
    }
    return builder.build();
  }

  public static FullUserProto createFullUserProtoFromUser(User u) {
    FullUserProto.Builder builder = FullUserProto.newBuilder().setUserId(u.getId()).setName(u.getName())
        .setLevel(u.getLevel()).setUserType(u.getType()).setAttack(u.getAttack())
        .setDefense(u.getDefense()).setStamina(u.getStamina())
        .setEnergy(u.getEnergy())
        .setSkillPoints(u.getSkillPoints()).setHealthMax(u.getHealthMax())
        .setEnergyMax(u.getEnergyMax()).setStaminaMax(u.getStaminaMax()).setDiamonds(u.getDiamonds())
        .setCoins(u.getCoins()).setMarketplaceDiamondsEarnings(u.getMarketplaceDiamondsEarnings())
        .setMarketplaceCoinsEarnings(u.getMarketplaceCoinsEarnings())
        .setVaultBalance(u.getVaultBalance()).setExperience(u.getExperience())
        .setTasksCompleted(u.getTasksCompleted()).setBattlesWon(u.getBattlesWon())
        .setBattlesLost(u.getBattlesLost()).setFlees(u.getFlees())
        .setReferralCode(u.getReferralCode()).setNumReferrals(u.getNumReferrals())
        .setUdid(u.getUdid())
        .setUserLocation(createLocationProtoFromLocation(u.getUserLocation()))
        .setNumPostsInMarketplace(u.getNumPostsInMarketplace()).setNumMarketplaceSalesUnredeemed(u.getNumMarketplaceSalesUnredeemed())
        .setLastLoginTime(u.getLastLogin().getTime()).setIsFake(u.isFake())
        .setCreateTime(u.getCreateTime().getTime());
    if (u.getWeaponEquipped() != ControllerConstants.NOT_SET) {
      builder.setWeaponEquipped(u.getWeaponEquipped());
    }
    if (u.getArmorEquipped() != ControllerConstants.NOT_SET) {
      builder.setArmorEquipped(u.getArmorEquipped());
    }
    if (u.getAmuletEquipped() != ControllerConstants.NOT_SET) {
      builder.setAmuletEquipped(u.getAmuletEquipped());
    }
    if (u.getLastEnergyRefillTime() != null) {
      builder.setLastEnergyRefillTime(u.getLastEnergyRefillTime().getTime());
    }
    if (u.getLastStaminaRefillTime() != null) {
      builder.setLastStaminaRefillTime(u.getLastStaminaRefillTime().getTime());
    }
    if (u.getLastLogout() != null) {
      builder.setLastLogoutTime(u.getLastLogout().getTime());
    }
    if (u.getLastShortLicensePurchaseTime() != null) {
      builder.setLastShortLicensePurchaseTime(u.getLastShortLicensePurchaseTime().getTime());
    }
    if (u.getLastLongLicensePurchaseTime() != null) {
      builder.setLastLongLicensePurchaseTime(u.getLastLongLicensePurchaseTime().getTime());
    }
    return builder.build();
  }

  public static FullEquipProto createFullEquipProtoFromEquip(Equipment equip) {
    FullEquipProto.Builder builder =  FullEquipProto.newBuilder().setEquipId(equip.getId()).setName(equip.getName())
        .setEquipType(equip.getType()).setDescription(equip.getDescription()).setAttackBoost(equip.getAttackBoost()).setDefenseBoost(equip.getDefenseBoost())
        .setMinLevel(equip.getMinLevel()).setChanceOfLoss(equip.getChanceOfLoss()).setClassType(equip.getClassType())
        .setRarity(equip.getRarity()).setIsBuyableInArmory(equip.isBuyableInArmory());
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
        .setQuantity(ue.getQuantity()).build();
  }

  public static FullUserCritstructProto createFullUserCritstructProtoFromUserCritstruct(UserCritstruct uc) {
    return FullUserCritstructProto.newBuilder().setType(uc.getType())
        .setCoords(createCoordinateProtoFromCoordinatePair(uc.getCoords()))
        .setOrientation(uc.getOrientation()).build();
  }

  public static FullTaskProto createFullTaskProtoFromTask(UserType userType, Task task) {

    boolean goodSide = MiscMethods.checkIfGoodSide(userType);

    String name = null;
    String processingText = null;

    if (goodSide) {
      name = task.getGoodName();
      processingText = task.getGoodProcessingText();
    } else {
      name = task.getBadName();
      processingText = task.getBadProcessingText();
    }

    FullTaskProto.Builder builder = FullTaskProto.newBuilder().setTaskId(task.getId()).setName(name).setCityId(task.getCityId()).setEnergyCost(task.getEnergyCost()).setMinCoinsGained(task.getMinCoinsGained())
        .setMaxCoinsGained(task.getMaxCoinsGained()).setChanceOfEquipLoot(task.getChanceOfEquipFloat())
        .setExpGained(task.getExpGained()).setAssetNumWithinCity(task.getAssetNumberWithinCity()).
        setNumRequiredForCompletion(task.getNumForCompletion()).addAllPotentialLootEquipIds(task.getPotentialLootEquipIds())
        .setProcessingText(processingText);

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
        .setDiamondPrice(s.getDiamondPrice()).setMinLevel(s.getMinLevel())
        .setXLength(s.getxLength()).setYLength(s.getyLength())
        .setInstaBuildDiamondCost(s.getInstaBuildDiamondCost())
        .setInstaRetrieveDiamondCostBase(s.getInstaRetrieveDiamondCostBase())
        .setInstaUpgradeDiamondCostBase(s.getInstaUpgradeDiamondCostBase())
        .setImgVerticalPixelOffset(s.getImgVerticalPixelOffset()).build();
  }

  public static FullCityProto createFullCityProtoFromCity(City c) {
    FullCityProto.Builder builder = FullCityProto.newBuilder().setCityId(c.getId()).setName(c.getName()).setMinLevel(c.getMinLevel())
        .setExpGainedBaseOnRankup(c.getExpGainedBaseOnRankup()).setCoinsGainedBaseOnRankup(c.getCoinsGainedBaseOnRankup())
        .setMapImgName(c.getMapImgName()).setAviaryCoords(createCoordinateProtoFromCoordinatePair(c.getAviaryCoords()))
        .setSpriteAviaryLandingCoords(createCoordinateProtoFromCoordinatePair(c.getSpriteAviaryLandingCoords()))
        .setAviaryOrientation(c.getAviaryOrientation());
    List<Task> tasks = TaskRetrieveUtils.getAllTasksForCityId(c.getId());
    if (tasks != null) {
      for (Task t : tasks) {
        builder.addTaskIds(t.getId());
      }
    }
    return builder.build();
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

  public static List<FullUserQuestDataLargeProto> createFullUserQuestDataLarges(List<UserQuest> userQuests, Map<Integer, Quest> questIdsToQuests, UserType userType) {
    List<FullUserQuestDataLargeProto> fullUserQuestDataLargeProtos = new ArrayList<FullUserQuestDataLargeProto>();

    Map<Integer, List<Integer>> questIdToUserTasksCompletedForQuestForUser = null;
    Map<Integer, Map<Integer, Integer>> questIdToTaskIdsToNumTimesActedInQuest = null;

    Map<Integer, List<Integer>> questIdToUserDefeatTypeJobsCompletedForQuestForUser = null;
    Map<Integer, Map<Integer, Integer>> questIdToDefeatTypeJobIdsToNumDefeated = null;

    Map<Integer, List<UserStruct>> structIdsToUserStructs = null;

    Map<Integer, UserEquip> equipIdsToUserEquips = null;
    boolean goodSide = MiscMethods.checkIfGoodSide(userType);
    
    for (UserQuest userQuest : userQuests) {
      Quest quest = questIdsToQuests.get(userQuest.getQuestId());
      FullUserQuestDataLargeProto.Builder builder = FullUserQuestDataLargeProto.newBuilder();

      if (quest != null) {
        int numComponentsComplete = 0;
        builder.setUserId(userQuest.getUserId());
        builder.setQuestId(quest.getId());
        builder.setIsRedeemed(userQuest.isRedeemed());
        boolean isComplete = true;
        if (!userQuest.isRedeemed()) {
          List<Integer> tasksRequired = quest.getTasksRequired(); 
          if (tasksRequired != null && tasksRequired.size() > 0) {
            
            if (questIdToUserTasksCompletedForQuestForUser == null) {
              questIdToUserTasksCompletedForQuestForUser = UserQuestsCompletedTasksRetrieveUtils.getQuestIdToUserTasksCompletedForQuestForUser(userQuest.getUserId());
            }
            List<Integer> userTasksCompletedForQuest = questIdToUserTasksCompletedForQuestForUser.get(userQuest.getQuestId());
            
            for (Integer requiredTaskId : tasksRequired) {
              boolean taskCompletedForQuest = false;
              Integer numTimesActed = null;
              if (userQuest.isTasksComplete() || (userTasksCompletedForQuest != null && userTasksCompletedForQuest.contains(requiredTaskId))) {
                taskCompletedForQuest = true;
                numComponentsComplete++;
              } else {
                isComplete = false;
                if (questIdToTaskIdsToNumTimesActedInQuest == null) {
                  questIdToTaskIdsToNumTimesActedInQuest = UserQuestsTaskProgressRetrieveUtils.getQuestIdToTaskIdsToNumTimesActedInQuest(userQuest.getUserId());
                }
                Map<Integer, Integer> taskIdsToNumTimesActedForUserQuest = questIdToTaskIdsToNumTimesActedInQuest.get(userQuest.getQuestId());
                if (taskIdsToNumTimesActedForUserQuest != null) {
                  numTimesActed = taskIdsToNumTimesActedForUserQuest.get(requiredTaskId);
                  if (numTimesActed == null) {
                    numTimesActed = 0;
                  }
                } else {
                  numTimesActed = 0;
                }
              }
              builder.addRequiredTasksProgress(createMinimumUserQuestTaskProto(userQuest, userType, requiredTaskId, taskCompletedForQuest, numTimesActed));
            }
          }

          List<Integer> defeatTypeJobsRequired; 
          if (goodSide) {
            defeatTypeJobsRequired = quest.getDefeatBadGuysJobsRequired();
          } else {
            defeatTypeJobsRequired = quest.getDefeatGoodGuysJobsRequired();
          }
          if (defeatTypeJobsRequired != null && defeatTypeJobsRequired.size() > 0) {
            if (questIdToUserDefeatTypeJobsCompletedForQuestForUser == null) {
              questIdToUserDefeatTypeJobsCompletedForQuestForUser = UserQuestsCompletedDefeatTypeJobsRetrieveUtils.getQuestIdToUserDefeatTypeJobsCompletedForQuestForUser(userQuest.getUserId());
            }
            List<Integer> userDefeatTypeJobsCompletedForQuest = questIdToUserDefeatTypeJobsCompletedForQuestForUser.get(userQuest.getQuestId());
            for (Integer requiredDefeatTypeJobId : defeatTypeJobsRequired) {
              boolean defeatJobCompletedForQuest = false;
              Integer numTimesUserDidJob = null;
              if (userQuest.isDefeatTypeJobsComplete() || (userDefeatTypeJobsCompletedForQuest != null && userDefeatTypeJobsCompletedForQuest.contains(requiredDefeatTypeJobId))) {
                defeatJobCompletedForQuest = true;
                numComponentsComplete++;
              } else {
                isComplete = false;
                if (questIdToDefeatTypeJobIdsToNumDefeated == null) {
                  questIdToDefeatTypeJobIdsToNumDefeated = UserQuestsDefeatTypeJobProgressRetrieveUtils.getQuestIdToDefeatTypeJobIdsToNumDefeated(userQuest.getUserId());
                }
                Map<Integer, Integer> defeatTypeJobIdsToNumDefeatedForUserQuest = questIdToDefeatTypeJobIdsToNumDefeated.get(userQuest.getQuestId());
                if (defeatTypeJobIdsToNumDefeatedForUserQuest != null) {
                  numTimesUserDidJob = defeatTypeJobIdsToNumDefeatedForUserQuest.get(requiredDefeatTypeJobId);
                  if (numTimesUserDidJob == null) {
                    numTimesUserDidJob = 0;
                  }
                } else {
                  numTimesUserDidJob = 0;
                }
              }
              builder.addRequiredDefeatTypeJobProgress(createMinimumUserDefeatTypeJobProto(userQuest, userType, requiredDefeatTypeJobId, defeatJobCompletedForQuest, numTimesUserDidJob));
            }
          }
          if (quest.getBuildStructJobsRequired() != null && quest.getBuildStructJobsRequired().size() > 0) {
            if (structIdsToUserStructs == null) {
              structIdsToUserStructs = UserStructRetrieveUtils.getStructIdsToUserStructsForUser(userQuest.getUserId());              
            }
            for (Integer buildStructJobId : quest.getBuildStructJobsRequired()) {
              BuildStructJob buildStructJob = BuildStructJobRetrieveUtils.getBuildStructJobForBuildStructJobId(buildStructJobId);
              List<UserStruct> userStructs = structIdsToUserStructs.get(buildStructJob.getStructId());
              int quantityBuilt = 0;
              if (userStructs != null) {
                for (UserStruct us : userStructs) {
                  if (us.getLastRetrieved() != null) {
                    quantityBuilt++;
                  }
                }
              }
              if (quantityBuilt < buildStructJob.getQuantity()) {
                isComplete = false;
              } else {
                numComponentsComplete++;
              }
              builder.addRequiredBuildStructJobProgress(createMinimumUserBuildStructJobProto(userQuest, buildStructJob, quantityBuilt));
            }
          }
          if (quest.getUpgradeStructJobsRequired() != null && quest.getUpgradeStructJobsRequired().size() > 0) {
            if (structIdsToUserStructs == null) {
              structIdsToUserStructs = UserStructRetrieveUtils.getStructIdsToUserStructsForUser(userQuest.getUserId());              
            }
            for (Integer upgradeStructJobId : quest.getUpgradeStructJobsRequired()) {
              UpgradeStructJob upgradeStructJob = UpgradeStructJobRetrieveUtils.getUpgradeStructJobForUpgradeStructJobId(upgradeStructJobId);
              List<UserStruct> userStructs = structIdsToUserStructs.get(upgradeStructJob.getStructId());
              int currentLevel = 0;
              if (userStructs != null) {
                for (UserStruct us : userStructs) {
                  if (us.getLevel() > currentLevel) {
                    currentLevel = us.getLevel();
                  }
                }
              }
              if (currentLevel < upgradeStructJob.getLevelReq()) {
                isComplete = false;
              } else {
                numComponentsComplete++;
              }
              builder.addRequiredUpgradeStructJobProgress(createMinimumUserUpgradeStructJobProto(userQuest, upgradeStructJob, currentLevel));
            }
          }
          if (quest.getPossessEquipJobsRequired() != null && quest.getPossessEquipJobsRequired().size() > 0) {
            if (equipIdsToUserEquips == null) {
              equipIdsToUserEquips = UserEquipRetrieveUtils.getEquipIdsToUserEquipsForUser(userQuest.getUserId());
            }
            for (Integer possessEquipJobId : quest.getPossessEquipJobsRequired()) {
              PossessEquipJob possessEquipJob = PossessEquipJobRetrieveUtils.getPossessEquipJobForPossessEquipJobId(possessEquipJobId);
              UserEquip userEquip = equipIdsToUserEquips.get(possessEquipJob.getEquipId());
              int quantityOwned = (userEquip != null) ? userEquip.getQuantity() : 0;
              if (quantityOwned < possessEquipJob.getQuantity()) {
                isComplete = false;
              } else {
                numComponentsComplete++;
              }
              builder.addRequiredPossessEquipJobProgress(createMinimumUserPossessEquipJobProto(userQuest, possessEquipJob, quantityOwned));
            }
          }
        } else {
          numComponentsComplete = quest.getNumComponents(goodSide);
        }
        fullUserQuestDataLargeProtos.add(builder.setIsComplete(isComplete).setNumComponentsComplete(numComponentsComplete).build());
      }      
    }
    return fullUserQuestDataLargeProtos;
  }

  public static MinimumUserTaskProto createMinimumUserTaskProto(UserType userType, Integer userId, int taskId, Integer numTimesUserActed) {
    return MinimumUserTaskProto.newBuilder().setUserId(userId).setTaskId(taskId).setNumTimesActed(numTimesUserActed).build();
  }
  
  public static NeutralCityElementProto createNeutralCityElementProtoFromNeutralCityElement(NeutralCityElement nce, UserType type) {
    NeutralCityElementProto.Builder builder = NeutralCityElementProto.newBuilder().setCityId(nce.getCityId()).setAssetId(nce.getAssetId())
        .setType(nce.getType())
        .setCoords(createCoordinateProtoFromCoordinatePair(nce.getCoords()));
    boolean goodSide = MiscMethods.checkIfGoodSide(type);
    if (goodSide) {
      builder.setName(nce.getGoodName());
      builder.setImgId(nce.getImgGood());
    } else {
      builder.setName(nce.getBadName());
      builder.setImgId(nce.getImgBad());      
    }
    if (nce.getOrientation() != null) {
      builder.setOrientation(nce.getOrientation());
    }
    if (nce.getxLength() > 0) {
      builder.setXLength(nce.getxLength());
    }
    if (nce.getyLength() > 0) {
      builder.setYLength(nce.getyLength());
    }
    return builder.build();
  }
  
  public static FullUserCityProto createFullUserCityProto(int userId, int cityId, int currentRank, int numTasksCurrentlyCompleteInRank) {
    return FullUserCityProto.newBuilder().setUserId(userId).setCityId(cityId).setCurrentRank(currentRank).setNumTasksCurrentlyCompleteInRank(numTasksCurrentlyCompleteInRank)
        .build();
  }

  private static MinimumUserPossessEquipJobProto createMinimumUserPossessEquipJobProto(UserQuest userQuest, PossessEquipJob possessEquipJob, int quantityOwned) {
    return MinimumUserPossessEquipJobProto.newBuilder().setUserId(userQuest.getUserId()).setQuestId(userQuest.getQuestId()).setPossessEquipJobId(possessEquipJob.getId()).setNumEquipUserHas(quantityOwned).build();
  }

  private static MinimumUserUpgradeStructJobProto createMinimumUserUpgradeStructJobProto(UserQuest userQuest, UpgradeStructJob upgradeStructJob, int currentLevel) {
    return MinimumUserUpgradeStructJobProto.newBuilder().setUserId(userQuest.getUserId()).setQuestId(userQuest.getQuestId()).setUpgradeStructJobId(upgradeStructJob.getId()).setCurrentLevel(currentLevel).build();
  }

  private static MinimumUserBuildStructJobProto createMinimumUserBuildStructJobProto(UserQuest userQuest, BuildStructJob buildStructJob, int quantityOwned) {
    return MinimumUserBuildStructJobProto.newBuilder().setUserId(userQuest.getUserId()).setQuestId(userQuest.getQuestId()).setBuildStructJobId(buildStructJob.getId()).setNumOfStructUserHas(quantityOwned).build();
  }

  private static MinimumUserDefeatTypeJobProto createMinimumUserDefeatTypeJobProto(UserQuest userQuest, UserType userType, Integer requiredDefeatTypeJobId, boolean defeatJobCompletedForQuest, 
      Integer numTimesUserDidJob) {
    DefeatTypeJob dtj = DefeatTypeJobRetrieveUtils.getDefeatTypeJobForDefeatTypeJobId(requiredDefeatTypeJobId);
    int numDefeated = (defeatJobCompletedForQuest) ? dtj.getNumEnemiesToDefeat() : numTimesUserDidJob;
    return MinimumUserDefeatTypeJobProto.newBuilder().setUserId(userQuest.getUserId()).setQuestId(userQuest.getQuestId()).setDefeatTypeJobId(requiredDefeatTypeJobId).setNumDefeated(numDefeated).build();
  }

  private static MinimumUserQuestTaskProto createMinimumUserQuestTaskProto(UserQuest userQuest, UserType userType, Integer requiredTaskId, boolean taskCompletedForQuest, Integer numTimesUserActed) {
    Task task = TaskRetrieveUtils.getTaskForTaskId(requiredTaskId);
    int numTimesCompleted = (taskCompletedForQuest) ? task.getNumForCompletion() : numTimesUserActed;
    return MinimumUserQuestTaskProto.newBuilder().setUserId(userQuest.getUserId()).setTaskId(requiredTaskId).setNumTimesActed(numTimesCompleted).setQuestId(userQuest.getQuestId()).build();
  }

}
