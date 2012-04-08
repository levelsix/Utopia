package com.lvl6.server.controller;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.StartupRequestEvent;
import com.lvl6.events.response.StartupResponseEvent;
import com.lvl6.info.BattleDetails;
import com.lvl6.info.City;
import com.lvl6.info.Equipment;
import com.lvl6.info.MarketplaceTransaction;
import com.lvl6.info.NeutralCityElement;
import com.lvl6.info.Quest;
import com.lvl6.info.Referral;
import com.lvl6.info.Structure;
import com.lvl6.info.Task;
import com.lvl6.info.User;
import com.lvl6.info.UserEquip;
import com.lvl6.info.UserQuest;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.properties.Globals;
import com.lvl6.proto.EventProto.StartupRequestProto;
import com.lvl6.proto.EventProto.StartupResponseProto;
import com.lvl6.proto.EventProto.StartupResponseProto.Builder;
import com.lvl6.proto.EventProto.StartupResponseProto.StartupStatus;
import com.lvl6.proto.EventProto.StartupResponseProto.TutorialConstants;
import com.lvl6.proto.EventProto.StartupResponseProto.TutorialConstants.FullTutorialQuestProto;
import com.lvl6.proto.EventProto.StartupResponseProto.UpdateStatus;
import com.lvl6.proto.InfoProto.FullStructureProto;
import com.lvl6.proto.InfoProto.FullTaskProto;
import com.lvl6.proto.InfoProto.FullUserProto;
import com.lvl6.proto.InfoProto.UserType;
import com.lvl6.proto.InfoProto.FullEquipProto.Rarity;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.BattleDetailsRetrieveUtils;
import com.lvl6.retrieveutils.MarketplaceTransactionRetrieveUtils;
import com.lvl6.retrieveutils.ReferralsRetrieveUtils;
import com.lvl6.retrieveutils.UserCityRetrieveUtils;
import com.lvl6.retrieveutils.UserEquipRetrieveUtils;
import com.lvl6.retrieveutils.UserQuestRetrieveUtils;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.retrieveutils.UserTaskRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.CityRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.EquipmentRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.LevelsRequiredExperienceRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.NeutralCityElementsRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.QuestRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.StructureRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.TaskRetrieveUtils;
import com.lvl6.server.GameServer;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.NIOUtils;
import com.lvl6.utils.utilmethods.MiscMethods;
import com.lvl6.utils.utilmethods.QuestUtils;

public class StartupController extends EventController {

  public StartupController() {
    numAllocatedThreads = 3;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new StartupRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_STARTUP_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) {
    StartupRequestProto reqProto = ((StartupRequestEvent)event).getStartupRequestProto();
    UpdateStatus updateStatus;
    String udid = reqProto.getUdid();

    StartupResponseProto.Builder resBuilder = StartupResponseProto.newBuilder();

    // Check version number
    if ((int)reqProto.getVersionNum() < (int)GameServer.clientVersionNumber) {
      updateStatus = UpdateStatus.MAJOR_UPDATE;
    } else if (reqProto.getVersionNum() < GameServer.clientVersionNumber) {
      updateStatus = UpdateStatus.MINOR_UPDATE;
    } else {
      updateStatus = UpdateStatus.NO_UPDATE;
    }

    resBuilder.setUpdateStatus(updateStatus);
    resBuilder.setAppStoreURL(Globals.appStoreURL);

    User user = null;

    // Don't fill in other fields if it is a major update
    StartupStatus startupStatus = StartupStatus.USER_NOT_IN_DB;

    if (updateStatus != UpdateStatus.MAJOR_UPDATE) {
      user = UserRetrieveUtils.getUserByUDID(udid);
      if (user != null) {
        server.lockPlayer(user.getId());
        try {
          startupStatus = StartupStatus.USER_IN_DB;
          syncUserDevicetokenAndBadges(reqProto, user);
          setCitiesAndUserCityInfos(resBuilder, user);
          setInProgressAndAvailableQuests(resBuilder, user);
          setUserEquipsAndEquips(resBuilder, user);
          FullUserProto fup = CreateInfoProtoUtils.createFullUserProtoFromUser(user);
          resBuilder.setSender(fup);
          resBuilder.setExperienceRequiredForNextLevel(
              LevelsRequiredExperienceRetrieveUtils.getRequiredExperienceForLevel(user.getLevel() + 1));
          resBuilder.setExperienceRequiredForCurrentLevel(
              LevelsRequiredExperienceRetrieveUtils.getRequiredExperienceForLevel(user.getLevel()));
          setNotifications(resBuilder, user);
        } catch (Exception e) {
          log.error("exception in StartupController processEvent", e);
        } finally {
          server.unlockPlayer(user.getId()); 
        }
      }
    }
    resBuilder.setStartupStatus(startupStatus);
    setConstants(resBuilder);

    if (startupStatus == StartupStatus.USER_NOT_IN_DB) {
      setTutorialConstants(resBuilder);
    }

    StartupResponseProto resProto = resBuilder.build();
    StartupResponseEvent resEvent = new StartupResponseEvent(udid);
    resEvent.setTag(event.getTag());
    resEvent.setStartupResponseProto(resProto);

    log.info("Writing event: " + resEvent);
    // Write event directly since EventWriter cannot handle without userId.
    ByteBuffer writeBuffer = ByteBuffer.allocateDirect(Globals.MAX_EVENT_SIZE);
    NIOUtils.prepBuffer(resEvent, writeBuffer);

    SocketChannel sc = server.removePreDbPlayer(udid);
    if (sc != null)
      NIOUtils.channelWrite(sc, writeBuffer);

    if (user != null) {
      if (!user.updateLastloginLastlogout(new Timestamp(new Date().getTime()), null)) {
        log.error("problem with updating user's last login time");
      }
    }    
  }

  private void syncUserDevicetokenAndBadges(StartupRequestProto reqProto, User user) {
    String newDeviceToken = reqProto.getDeviceToken();
    if (user.getNumBadges() != 0 || user.getDeviceToken() != newDeviceToken) {
      if (newDeviceToken != null && newDeviceToken.length() > 0) { 
        //        ApnsServiceBuilder builder = APNS.newService().withCert(APNSProperties.PATH_TO_CERT, APNSProperties.CERT_PASSWORD);
        //        if (Globals.IS_SANDBOX) {
        //          builder.withSandboxDestination();
        //        }
        //        ApnsService service = builder.build();
        //        service.push(newDeviceToken, APNS.newPayload().badge(0).build());
        //        service.stop();
      }
      if (!user.updateResetNumbadgesSetdevicetoken(newDeviceToken)) {
        log.error("problem in resetting num badges and syncing device token");
      }
    }
  }

  private void setNotifications(Builder resBuilder, User user) {
    if (user.getLastLogout() != null) {
      List <Integer> userIds = new ArrayList<Integer>();

      Timestamp lastLogout = new Timestamp(user.getLastLogout().getTime());

      List<MarketplaceTransaction> marketplaceTransactions = 
          MarketplaceTransactionRetrieveUtils.getAllMarketplaceTransactionsAfterLastlogoutForDefender(lastLogout, user.getId());
      if (marketplaceTransactions != null && marketplaceTransactions.size() > 0) {
        for (MarketplaceTransaction mt : marketplaceTransactions) {
          userIds.add(mt.getBuyerId());
        }
      }

      List<BattleDetails> battleDetails = BattleDetailsRetrieveUtils.getAllBattleDetailsAfterLastlogoutForDefender(lastLogout, user.getId());
      if (battleDetails != null && battleDetails.size() > 0) {
        for (BattleDetails bd : battleDetails) {
          userIds.add(bd.getAttackerId());
        }        
      }

      List<Referral> referrals = ReferralsRetrieveUtils.getAllReferralsAfterLastlogoutForReferrer(lastLogout, user.getId());
      if (referrals != null && referrals.size() > 0) {
        for (Referral r : referrals) {
          userIds.add(r.getNewlyReferredId());
        }
      }

      Map<Integer, User> usersByIds = null;
      if (userIds.size() > 0) {
        usersByIds = UserRetrieveUtils.getUsersByIds(userIds);
      }

      if (marketplaceTransactions != null && marketplaceTransactions.size() > 0) {
        for (MarketplaceTransaction mt : marketplaceTransactions) {
          resBuilder.addMarketplacePurchaseNotifications(CreateInfoProtoUtils.createMarketplacePostPurchasedNotificationProtoFromMarketplaceTransaction(mt, usersByIds.get(mt.getBuyerId())));
        }
      }
      if (battleDetails != null && battleDetails.size() > 0) {
        for (BattleDetails bd : battleDetails) {
          resBuilder.addAttackNotifications(CreateInfoProtoUtils.createAttackedNotificationProtoFromBattleHistory(bd, usersByIds.get(bd.getAttackerId())));
        }        
      } 
      if (referrals != null && referrals.size() > 0) {
        for (Referral r : referrals) {
          resBuilder.addReferralNotifications(CreateInfoProtoUtils.createReferralNotificationProtoFromReferral(r, usersByIds.get(r.getNewlyReferredId())));
        }
      }
    }
  }

  private void setUserEquipsAndEquips(Builder resBuilder, User user) {
    List<UserEquip> userEquips = UserEquipRetrieveUtils.getUserEquipsForUser(user.getId());
    if (userEquips != null) {
      Map<Integer, Equipment> equipIdsToEquipment = EquipmentRetrieveUtils.getEquipmentIdsToEquipment();
      for (UserEquip ue : userEquips) {
        resBuilder.addUserEquips(CreateInfoProtoUtils.createFullUserEquipProtoFromUserEquip(ue));
        resBuilder.addEquips(CreateInfoProtoUtils.createFullEquipProtoFromEquip(equipIdsToEquipment.get(ue.getEquipId())));
      }
    }
  }

  private void setInProgressAndAvailableQuests(Builder resBuilder, User user) {
    List<UserQuest> inProgressAndRedeemedUserQuests = UserQuestRetrieveUtils.getInProgressAndRedeemedUserQuestsForUser(user.getId());
    List<Integer> inProgressQuestIds = new ArrayList<Integer>();
    List<Integer> redeemedQuestIds = new ArrayList<Integer>();

    Map<Integer, Quest> questIdToQuests = QuestRetrieveUtils.getQuestIdsToQuests();
    for (UserQuest uq : inProgressAndRedeemedUserQuests) {
      if (uq.isRedeemed()) {
        redeemedQuestIds.add(uq.getQuestId());
      } else {
        inProgressQuestIds.add(uq.getQuestId());  
        resBuilder.addInProgressQuests(CreateInfoProtoUtils.createFullQuestProtoFromQuest(user.getType(), questIdToQuests.get(uq.getQuestId())));
      }
    }

    List<Integer> availableQuestIds = QuestUtils.getAvailableQuestsForUser(redeemedQuestIds, inProgressQuestIds);
    if (availableQuestIds != null) {
      for (Integer questId : availableQuestIds) {
        resBuilder.addAvailableQuests(CreateInfoProtoUtils.createFullQuestProtoFromQuest(user.getType(), questIdToQuests.get(questId)));
      }
    }
  }

  private void setCitiesAndUserCityInfos(Builder resBuilder, User user) {
    Map<Integer, Integer> cityIdsToUserCityRanks = UserCityRetrieveUtils.getCityIdToUserCityRank(user.getId());
    Map<Integer, Integer> taskIdToNumTimesActedInRank = UserTaskRetrieveUtils.getTaskIdToNumTimesActedInRankForUser(user.getId());


    Map<Integer, City> cities = CityRetrieveUtils.getCityIdsToCities();
    for (Integer cityId : cities.keySet()) {
      City city = cities.get(cityId);
      resBuilder.addAllCities(CreateInfoProtoUtils.createFullCityProtoFromCity(city));
      if (user.getLevel() >= city.getMinLevel() && cityIdsToUserCityRanks.containsKey(city.getId())) {
        int numTasksComplete = getNumTasksCompleteForUserCity(user, city, taskIdToNumTimesActedInRank);

        resBuilder.addUserCityInfos(CreateInfoProtoUtils.createFullUserCityProto(user.getId(), city.getId(), 
            cityIdsToUserCityRanks.get(city.getId()), numTasksComplete));
      }
    }
  }

  private int getNumTasksCompleteForUserCity(User user, City city, Map<Integer, Integer> taskIdToNumTimesActedInRank) {
    List<Task> tasks = TaskRetrieveUtils.getAllTasksForCityId(city.getId());
    int numCompletedTasks = 0;
    if (tasks != null) {
      for (Task t : tasks) {
        if (taskIdToNumTimesActedInRank.containsKey(t.getId()) && taskIdToNumTimesActedInRank.get(t.getId()) >= t.getNumForCompletion()) {
          numCompletedTasks++;
        }
      }
    }
    return numCompletedTasks;
  }

  private void setConstants(Builder startupBuilder) {
    startupBuilder.setStartupConstants(MiscMethods.createStartupConstantsProto());
  }

  private void setTutorialConstants(Builder resBuilder) {
    Map<Integer, Equipment> equipmentIdsToEquipment = EquipmentRetrieveUtils.getEquipmentIdsToEquipment();

    UserType aGoodType = UserType.GOOD_ARCHER;
    UserType aBadType = UserType.BAD_ARCHER;

    Task task = TaskRetrieveUtils.getTaskForTaskId(ControllerConstants.TUTORIAL__FIRST_TASK_ID);
    FullTaskProto ftpGood = CreateInfoProtoUtils.createFullTaskProtoFromTask(aGoodType, task);
    FullTaskProto ftpBad = CreateInfoProtoUtils.createFullTaskProtoFromTask(aBadType, task);

    FullTutorialQuestProto tqbp = FullTutorialQuestProto.newBuilder()
        .setGoodName(ControllerConstants.TUTORIAL__FAKE_QUEST_GOOD_NAME)
        .setBadName(ControllerConstants.TUTORIAL__FAKE_QUEST_BAD_NAME)
        .setGoodDescription(ControllerConstants.TUTORIAL__FAKE_QUEST_GOOD_DESCRIPTION)
        .setBadDescription(ControllerConstants.TUTORIAL__FAKE_QUEST_BAD_DESCRIPTION)
        .setGoodDoneResponse(ControllerConstants.TUTORIAL__FAKE_QUEST_GOOD_DONE_RESPONSE)
        .setBadDoneResponse(ControllerConstants.TUTORIAL__FAKE_QUEST_BAD_DONE_RESPONSE)
        .setGoodInProgress(ControllerConstants.TUTORIAL__FAKE_QUEST_GOOD_IN_PROGRESS)
        .setBadInProgress(ControllerConstants.TUTORIAL__FAKE_QUEST_BAD_IN_PROGRESS)
        .setAssetNumWithinCity(ControllerConstants.TUTORIAL__FAKE_QUEST_ASSET_NUM_WITHIN_CITY)
        .setCoinsGained(ControllerConstants.TUTORIAL__FAKE_QUEST_COINS_GAINED)
        .setExpGained(ControllerConstants.TUTORIAL__FAKE_QUEST_EXP_GAINED)
        .setFirstTaskGood(ftpGood).setFirstTaskBad(ftpBad)
        .setFirstTaskCompleteCoinGain(MiscMethods.calculateCoinsGainedFromTutorialTask(task))
        .setFirstDefeatTypeJobBattleCoinGain(ControllerConstants.TUTORIAL__FIRST_DEFEAT_TYPE_JOB_BATTLE_COIN_GAIN)
        .setFirstDefeatTypeJobBattleExpGain(ControllerConstants.TUTORIAL__FIRST_DEFEAT_TYPE_JOB_BATTLE_EXP_GAIN)
        .setFirstDefeatTypeJobBattleLootAmulet
        (CreateInfoProtoUtils.createFullEquipProtoFromEquip(equipmentIdsToEquipment.get(ControllerConstants.TUTORIAL__FIRST_DEFEAT_TYPE_JOB_BATTLE_AMULET_LOOT_EQUIP_ID)))
        .build();

    TutorialConstants.Builder builder = TutorialConstants.newBuilder()
        .setInitEnergy(ControllerConstants.TUTORIAL__INIT_ENERGY).setInitStamina(ControllerConstants.TUTORIAL__INIT_STAMINA)
        .setInitHealth(ControllerConstants.TUTORIAL__INIT_HEALTH).setStructToBuild(ControllerConstants.TUTORIAL__FIRST_STRUCT_TO_BUILD)
        .setDiamondCostToInstabuildFirstStruct(ControllerConstants.TUTORIAL__DIAMOND_COST_TO_INSTABUILD_FIRST_STRUCT)
        .setArcherInitAttack(ControllerConstants.TUTORIAL__ARCHER_INIT_ATTACK).setArcherInitDefense(ControllerConstants.TUTORIAL__ARCHER_INIT_DEFENSE)
        .setArcherInitWeapon(CreateInfoProtoUtils.createFullEquipProtoFromEquip(equipmentIdsToEquipment.get(ControllerConstants.TUTORIAL__ARCHER_INIT_WEAPON_ID)))
        .setArcherInitArmor(CreateInfoProtoUtils.createFullEquipProtoFromEquip(equipmentIdsToEquipment.get(ControllerConstants.TUTORIAL__ARCHER_INIT_ARMOR_ID)))
        .setMageInitAttack(ControllerConstants.TUTORIAL__MAGE_INIT_ATTACK).setMageInitDefense(ControllerConstants.TUTORIAL__MAGE_INIT_DEFENSE)
        .setMageInitWeapon(CreateInfoProtoUtils.createFullEquipProtoFromEquip(equipmentIdsToEquipment.get(ControllerConstants.TUTORIAL__MAGE_INIT_WEAPON_ID)))
        .setMageInitArmor(CreateInfoProtoUtils.createFullEquipProtoFromEquip(equipmentIdsToEquipment.get(ControllerConstants.TUTORIAL__MAGE_INIT_ARMOR_ID)))
        .setWarriorInitAttack(ControllerConstants.TUTORIAL__WARRIOR_INIT_ATTACK).setWarriorInitDefense(ControllerConstants.TUTORIAL__WARRIOR_INIT_DEFENSE)
        .setWarriorInitWeapon(CreateInfoProtoUtils.createFullEquipProtoFromEquip(equipmentIdsToEquipment.get(ControllerConstants.TUTORIAL__WARRIOR_INIT_WEAPON_ID)))
        .setWarriorInitArmor(CreateInfoProtoUtils.createFullEquipProtoFromEquip(equipmentIdsToEquipment.get(ControllerConstants.TUTORIAL__WARRIOR_INIT_ARMOR_ID)))
        .setTutorialQuest(tqbp).setMinNameLength(ControllerConstants.USER_CREATE__MIN_NAME_LENGTH)
        .setTutorialQuest(tqbp).setMaxNameLength(ControllerConstants.USER_CREATE__MAX_NAME_LENGTH)
        .setDiamondRewardForBeingReferred(ControllerConstants.USER_CREATE__DIAMOND_REWARD_FOR_BEING_REFERRED)
        .setInitDiamonds(ControllerConstants.TUTORIAL__INIT_DIAMONDS)
        .setInitCoins(ControllerConstants.TUTORIAL__INIT_COINS)
        .setExpRequiredForLevelTwo(LevelsRequiredExperienceRetrieveUtils.getLevelsToRequiredExperienceForLevels().get(2))
        .setExpRequiredForLevelThree(LevelsRequiredExperienceRetrieveUtils.getLevelsToRequiredExperienceForLevels().get(3));

    List<NeutralCityElement> neutralCityElements = NeutralCityElementsRetrieveUtils.getNeutralCityElementsForCity(ControllerConstants.TUTORIAL__FIRST_NEUTRAL_CITY_ID);
    if (neutralCityElements != null) {
      for (NeutralCityElement nce : neutralCityElements) {
        builder.addFirstCityElementsForGood(CreateInfoProtoUtils.createNeutralCityElementProtoFromNeutralCityElement(nce, aGoodType));
        builder.addFirstCityElementsForBad(CreateInfoProtoUtils.createNeutralCityElementProtoFromNeutralCityElement(nce, aBadType));
      }
    }

    Map<Integer, Structure> structIdsToStructs = StructureRetrieveUtils.getStructIdsToStructs();
    for (Structure struct : structIdsToStructs.values()) {
      if (struct != null) {
        FullStructureProto fsp = CreateInfoProtoUtils.createFullStructureProtoFromStructure(struct);
        builder.addCarpenterStructs(fsp);
        if (struct.getMinLevel() == 2) {
          builder.addNewlyAvailableStructsAfterLevelup(CreateInfoProtoUtils.createFullStructureProtoFromStructure(struct));
        } 
      }
    }

    List<City> availCities = MiscMethods.getCitiesAvailableForUserLevel(2);
    for (City city : availCities) {
      if (city.getMinLevel() == 2) {
        builder.addCitiesNewlyAvailableToUserAfterLevelup(CreateInfoProtoUtils.createFullCityProtoFromCity(city));
      }
    }

    Map<Integer, Equipment> equipIdToEquips = EquipmentRetrieveUtils.getEquipmentIdsToEquipment();
    if (equipIdToEquips != null) {
      for (Equipment e : equipIdToEquips.values()) {
        if (e != null && e.getMinLevel() == 2 && (e.getRarity() == Rarity.EPIC || e.getRarity() == Rarity.LEGENDARY)) {
          builder.addNewlyEquippableEpicsAndLegendariesForAllClassesAfterLevelup(CreateInfoProtoUtils.createFullEquipProtoFromEquip(e));
        }
      }
    }
    resBuilder.setTutorialConstants(builder.build());
  }




}
