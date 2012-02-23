package com.lvl6.server.controller;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.StartupRequestEvent;
import com.lvl6.events.response.StartupResponseEvent;
import com.lvl6.info.BattleDetails;
import com.lvl6.info.City;
import com.lvl6.info.Equipment;
import com.lvl6.info.MarketplaceTransaction;
import com.lvl6.info.Quest;
import com.lvl6.info.Structure;
import com.lvl6.info.User;
import com.lvl6.info.UserEquip;
import com.lvl6.info.UserQuest;
import com.lvl6.info.UserStruct;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.properties.Globals;
import com.lvl6.properties.IAPValues;
import com.lvl6.proto.EventProto.StartupRequestProto;
import com.lvl6.proto.EventProto.StartupResponseProto;
import com.lvl6.proto.EventProto.StartupResponseProto.Builder;
import com.lvl6.proto.EventProto.StartupResponseProto.StartupConstants;
import com.lvl6.proto.EventProto.StartupResponseProto.StartupStatus;
import com.lvl6.proto.EventProto.StartupResponseProto.UpdateStatus;
import com.lvl6.proto.InfoProto.FullUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.BattleDetailsRetrieveUtils;
import com.lvl6.retrieveutils.MarketplaceTransactionRetrieveUtils;
import com.lvl6.retrieveutils.UserEquipRetrieveUtils;
import com.lvl6.retrieveutils.UserQuestRetrieveUtils;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.retrieveutils.UserStructRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.EquipmentRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.LevelsRequiredExperienceRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.QuestRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.StructureRetrieveUtils;
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

    User user = null;
    Timestamp clientTime = null;
    // Don't fill in other fields if it is a major update
    StartupStatus startupStatus = StartupStatus.USER_NOT_IN_DB;
    if (updateStatus != UpdateStatus.MAJOR_UPDATE) {
      user = UserRetrieveUtils.getUserByUDID(udid);
      if (user != null) {
        clientTime = new Timestamp(reqProto.getClientTime());
        server.lockPlayer(user.getId());
        try {
          startupStatus = StartupStatus.USER_IN_DB;
          setCitiesAvailableToUser(resBuilder, user);
          setInProgressAndAvailableQuests(resBuilder, user);
          setUserEquipsAndEquips(resBuilder, user);
          setUserStructsAndStructs(resBuilder, user);
          FullUserProto fup = CreateInfoProtoUtils.createFullUserProtoFromUser(user);
          resBuilder.setSender(fup);
          resBuilder.setExperienceRequiredForNextLevel(
              LevelsRequiredExperienceRetrieveUtils.getRequiredExperienceForLevel(user.getLevel() + 1));
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

    StartupResponseProto resProto = resBuilder.build();
    StartupResponseEvent resEvent = new StartupResponseEvent(udid);
    resEvent.setStartupResponseProto(resProto);

    log.info("Writing event: " + resEvent);
    // Write event directly since EventWriter cannot handle without userId.
    ByteBuffer writeBuffer = ByteBuffer.allocateDirect(Globals.MAX_EVENT_SIZE);
    NIOUtils.prepBuffer(resEvent, writeBuffer);

    SocketChannel sc = server.removePreDbPlayer(udid);
    NIOUtils.channelWrite(sc, writeBuffer);

    if (user != null && clientTime != null) {
      if (!user.updateLastloginLastlogout(clientTime, null)) {
        log.error("problem with updating user's last login time");
      }
    }    
  }

  private void setNotifications(Builder resBuilder, User user) {
    if (user.getLastLogout() != null) {
      List <Integer> userIds = new ArrayList<Integer>();

      List<MarketplaceTransaction> marketplaceTransactions = 
          MarketplaceTransactionRetrieveUtils.getAllMarketplaceTransactionsAfterLastlogoutForDefender(new Timestamp(user.getLastLogout().getTime()), user.getId());
      if (marketplaceTransactions != null && marketplaceTransactions.size() > 0) {
        for (MarketplaceTransaction mt : marketplaceTransactions) {
          userIds.add(mt.getBuyerId());
        }
      }

      List<BattleDetails> battleDetails = BattleDetailsRetrieveUtils.getAllBattleDetailsAfterLastlogoutForDefender(new Timestamp(user.getLastLogout().getTime()), user.getId());
      if (battleDetails != null && battleDetails.size() > 0) {
        for (BattleDetails bd : battleDetails) {
          userIds.add(bd.getAttackerId());
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
    }
  }

  private void setUserStructsAndStructs(Builder resBuilder, User user) {
    List<UserStruct> userStructs = UserStructRetrieveUtils.getUserStructsForUser(user.getId());
    if (userStructs != null) {
      Map<Integer, Structure> structIdsToStructures = StructureRetrieveUtils.getStructIdsToStructs();
      for (UserStruct us : userStructs) {
        resBuilder.addUserStructures(CreateInfoProtoUtils.createFullUserStructureProtoFromUserstruct(us));
        resBuilder.addStructs(CreateInfoProtoUtils.createFullStructureProtoFromStructure(structIdsToStructures.get(us.getStructId())));
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

  private void setCitiesAvailableToUser(Builder resBuilder, User user) {
    List<City> availCities = MiscMethods.getCitiesAvailableForUserLevel(user.getLevel());
    for (City city : availCities) {
      resBuilder.addCitiesAvailableToUser(CreateInfoProtoUtils.createFullCityProtoFromCity(city));
    }
  }

  private void setConstants(Builder startupBuilder) {
    StartupConstants.Builder cb = StartupConstants.newBuilder()
        .setDiamondCostForEnergyRefill(ControllerConstants.REFILL_STAT_WITH_DIAMONDS__DIAMOND_COST_FOR_ENERGY_REFILL)
        .setDiamondCostForStaminaRefill(ControllerConstants.REFILL_STAT_WITH_DIAMONDS__DIAMOND_COST_FOR_STAMINA_REFILL)
        .setMaxItemUsePerBattle(ControllerConstants.BATTLE__MAX_ITEMS_USED);
    for (int i = 0; i < IAPValues.packageNames.size(); i++) {
      cb.addProductIds(IAPValues.packageNames.get(i));
      cb.addProductDiamondsGiven(IAPValues.packageGivenDiamonds.get(i));
    }
    startupBuilder.setStartupConstants(cb.build());
  }



}
