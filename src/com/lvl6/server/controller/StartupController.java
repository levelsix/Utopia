package com.lvl6.server.controller;


import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.StartupRequestEvent;
import com.lvl6.events.response.StartupResponseEvent;
import com.lvl6.info.City;
import com.lvl6.info.Quest;
import com.lvl6.info.User;
import com.lvl6.info.UserEquip;
import com.lvl6.info.UserQuest;
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
import com.lvl6.retrieveutils.UserEquipRetrieveUtils;
import com.lvl6.retrieveutils.UserQuestRetrieveUtils;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.CityRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.QuestRetrieveUtils;
import com.lvl6.server.GameServer;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.NIOUtils;
import com.lvl6.utils.utilmethods.QuestUtils;

public class StartupController extends EventController {

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

    // Don't fill in other fields if it is a major update
    StartupStatus startupStatus = StartupStatus.USER_NOT_IN_DB;
    if (updateStatus != UpdateStatus.MAJOR_UPDATE) {
      User user = UserRetrieveUtils.getUserByUDID(udid);
      if (user != null) {
        startupStatus = StartupStatus.USER_IN_DB;
        FullUserProto fup = CreateInfoProtoUtils.createFullUserProtoFromUser(user);
        resBuilder.setSender(fup);

        setCitiesAvailableToUser(resBuilder, user);
        setInProgressAndAvailableQuests(resBuilder, user);
        setUserEquips(resBuilder, user);
        //setUserStructs(resBuilder, user);

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
  }

  /*
  private void setUserStructs(Builder resBuilder, User user) {
    List<UserStruct> userStructs = UserStructRetrieveUtils.getUserStructsForUser(user.getId());
    if (userStructs != null) {
      for (UserStruct ue : userStructs) {
        resBuilder.addUserStructs(CreateInfoProtoUtils.createFullUserStructureProtoFromUserstruct(ue));
      }
    }    
  }*/

  private void setUserEquips(Builder resBuilder, User user) {
    List<UserEquip> userEquips = UserEquipRetrieveUtils.getUserEquipsForUser(user.getId());
    if (userEquips != null) {
      for (UserEquip ue : userEquips) {
        resBuilder.addUserEquips(CreateInfoProtoUtils.createFullUserEquipProtoFromUserEquip(ue));
      }
    }
  }

  private void setInProgressAndAvailableQuests(Builder resBuilder, User user) {
    List<UserQuest> inProgressAndAvailableUserQuests = UserQuestRetrieveUtils.getInProgressAndCompletedUserQuestsForUser(user.getId());
    List<Integer> inProgressQuestIds = new ArrayList<Integer>();
    List<Integer> completedQuestIds = new ArrayList<Integer>();

    Map<Integer, Quest> questIdToQuests = QuestRetrieveUtils.getQuestIdsToQuests();
    for (UserQuest uq : inProgressAndAvailableUserQuests) {
      if (uq.isRedeemed()) {
        completedQuestIds.add(uq.getQuestId());
      } else {
        inProgressQuestIds.add(uq.getQuestId());  
        resBuilder.addInProgressQuests(CreateInfoProtoUtils.createFullQuestProtoFromQuest(user.getType(), questIdToQuests.get(uq.getQuestId())));
      }
    }
    
    List<Integer> availableQuestIds = QuestUtils.getAvailableQuestsForUser(completedQuestIds, inProgressQuestIds);
    if (availableQuestIds != null) {
      for (Integer questId : availableQuestIds) {
        resBuilder.addAvailableQuests(CreateInfoProtoUtils.createFullQuestProtoFromQuest(user.getType(), questIdToQuests.get(questId)));
      }
    }
  }

  private void setCitiesAvailableToUser(Builder resBuilder, User user) {
    Map<Integer, City> cities = CityRetrieveUtils.getCityIdsToCities();
    for (Integer cityId : cities.keySet()) {
      City city = cities.get(cityId);
      if (user.getLevel() >= city.getMinLevel()) {
        resBuilder.addCitiesAvailableToUser(CreateInfoProtoUtils.createFullCityProtoFromCity(city));
      }
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
      cb.addProductPrices(IAPValues.packagePrices.get(i));
    }
    startupBuilder.setStartupConstants(cb.build());
  }



}
