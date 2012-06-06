package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.RetrieveCurrencyFromNormStructureRequestEvent;
import com.lvl6.events.response.RetrieveCurrencyFromNormStructureResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.Quest;
import com.lvl6.info.Structure;
import com.lvl6.info.User;
import com.lvl6.info.UserQuest;
import com.lvl6.info.UserStruct;
import com.lvl6.proto.EventProto.RetrieveCurrencyFromNormStructureRequestProto;
import com.lvl6.proto.EventProto.RetrieveCurrencyFromNormStructureResponseProto;
import com.lvl6.proto.EventProto.RetrieveCurrencyFromNormStructureResponseProto.Builder;
import com.lvl6.proto.EventProto.RetrieveCurrencyFromNormStructureResponseProto.RetrieveCurrencyFromNormStructureStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserQuestRetrieveUtils;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.retrieveutils.UserStructRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.QuestRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.StructureRetrieveUtils;
import com.lvl6.server.GameServer;
import com.lvl6.utils.utilmethods.MiscMethods;
import com.lvl6.utils.utilmethods.QuestUtils;
import com.lvl6.utils.utilmethods.UpdateUtils;

 @Component public class RetrieveCurrencyFromNormStructureController extends EventController{

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  public RetrieveCurrencyFromNormStructureController() {
    numAllocatedThreads = 14;
  }
  
  @Override
  public RequestEvent createRequestEvent() {
    return new RetrieveCurrencyFromNormStructureRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_RETRIEVE_CURRENCY_FROM_NORM_STRUCTURE_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    RetrieveCurrencyFromNormStructureRequestProto reqProto = ((RetrieveCurrencyFromNormStructureRequestEvent)event).getRetrieveCurrencyFromNormStructureRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int userStructId = reqProto.getUserStructId();
    Timestamp timeOfRetrieval = new Timestamp(reqProto.getTimeOfRetrieval());

    RetrieveCurrencyFromNormStructureResponseProto.Builder resBuilder = RetrieveCurrencyFromNormStructureResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    UserStruct userStruct = UserStructRetrieveUtils.getSpecificUserStruct(userStructId);
    Structure struct = null;
    if (userStruct != null) {
      struct = StructureRetrieveUtils.getStructForStructId(userStruct.getStructId());
    }
    server.lockPlayer(senderProto.getUserId());

    try {
      User user = UserRetrieveUtils.getUserById(senderProto.getUserId());
      
      int coinGain = MiscMethods.calculateIncomeGainedFromUserStruct(struct.getIncome(), userStruct.getLevel());
      
      boolean legitRetrieval = checkLegitRetrieval(resBuilder, user, userStruct, struct, timeOfRetrieval, coinGain);
      
      if (legitRetrieval) {
        if (!user.updateRelativeCoinsCoinsretrievedfromstructs(coinGain)) {
          log.error("problem with updating user stats after retrieving " + coinGain + " silver");
          legitRetrieval = false;
        }
        if (!UpdateUtils.updateUserStructLastretrieved(userStructId, timeOfRetrieval)) {
          log.error("problem with updating user struct last retrieved for userStructId " + userStructId + " to " + timeOfRetrieval);
          legitRetrieval = false;
        }
      }

      RetrieveCurrencyFromNormStructureResponseEvent resEvent = new RetrieveCurrencyFromNormStructureResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setRetrieveCurrencyFromNormStructureResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);
      
      if (legitRetrieval) {
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEvent(user);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);
        
        updateAndCheckUserQuests(server, coinGain, senderProto);        
      }

    } catch (Exception e) {
      log.error("exception in RetrieveCurrencyFromNormStructureController processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());      
    }
  }

  private void updateAndCheckUserQuests(GameServer server, int coinGain, MinimumUserProto senderProto) {
    List<UserQuest> inProgressUserQuests = UserQuestRetrieveUtils.getIncompleteUserQuestsForUser(senderProto.getUserId());
    if (inProgressUserQuests != null) {
      List<Integer> relevantQuests = new ArrayList<Integer>();
      for (UserQuest userQuest : inProgressUserQuests) {
        if (!userQuest.isComplete()) {
          Quest quest = QuestRetrieveUtils.getQuestForQuestId(userQuest.getQuestId());
          if (quest != null) {
            if (quest.getCoinRetrievalAmountRequired() > 0) {
              userQuest.setCoinsRetrievedForReq(userQuest.getCoinsRetrievedForReq() + coinGain);
              QuestUtils.checkQuestCompleteAndMaybeSendIfJustCompleted(server, quest, userQuest, senderProto, true, null);
              relevantQuests.add(quest.getId());
            }
          } else {
            log.error("quest for userQuest does not exist. user quest's quest is " + userQuest.getQuestId());
          }
        }
      }
      if (relevantQuests.size() > 0 && !UpdateUtils.updateUserQuestsCoinsretrievedforreq(senderProto.getUserId(), relevantQuests, coinGain)) {
        log.error("problem with incrementing coins retrieved by " + coinGain + " in user quest info for these quests:" + relevantQuests);
      }
    }
  }

  private boolean checkLegitRetrieval(Builder resBuilder, User user, UserStruct userStruct, Structure struct, Timestamp timeOfRetrieval,
      int coinGain) {
    // TODO Auto-generated method stub
    if (user == null || userStruct == null || timeOfRetrieval == null || userStruct.getLastRetrieved() == null) {
      resBuilder.setStatus(RetrieveCurrencyFromNormStructureStatus.OTHER_FAIL);
      log.error("parameter passed in is null. user=" + user + ", userStruct= " + userStruct
          + ", timeOfRetrieval=" + timeOfRetrieval + ", userStruct's last retrieved time=" 
          + userStruct.getLastRetrieved());
      return false;
    }
    if (user.getId() != userStruct.getUserId() || !userStruct.isComplete()) {
      resBuilder.setStatus(RetrieveCurrencyFromNormStructureStatus.OTHER_FAIL);
      log.error("struct owner is not user, or struct is not complete yet. userStruct=" + userStruct);
      return false;
    }
    if (!MiscMethods.checkClientTimeAroundApproximateNow(timeOfRetrieval)) {
      resBuilder.setStatus(RetrieveCurrencyFromNormStructureStatus.CLIENT_TOO_APART_FROM_SERVER_TIME);
      log.error("client time too apart of server time. client time=" + timeOfRetrieval + ", servertime~="
          + new Date());
      return false;
    }
    if ((timeOfRetrieval.getTime() - userStruct.getLastRetrieved().getTime())  < 60000*struct.getMinutesToGain()) {
      resBuilder.setStatus(RetrieveCurrencyFromNormStructureStatus.NOT_LONG_ENOUGH);
      log.error("struct not ready for retrieval yet. time of retrieval=" + timeOfRetrieval
          + ", userStruct=" + userStruct + ", takes this many minutes to gain:" + struct.getMinutesToGain()); 
      return false;
    }
    if (coinGain <= 0) {
      resBuilder.setStatus(RetrieveCurrencyFromNormStructureStatus.OTHER_FAIL);
      log.error("coinGain <= 0. coinGain is " + coinGain);
      return false;
    }
    resBuilder.setStatus(RetrieveCurrencyFromNormStructureStatus.SUCCESS);
    return true;
  }

}
