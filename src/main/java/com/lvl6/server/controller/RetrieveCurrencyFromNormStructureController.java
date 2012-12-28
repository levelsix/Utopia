package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
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
import com.lvl6.misc.MiscMethods;
import com.lvl6.proto.EventProto.RetrieveCurrencyFromNormStructureRequestProto;
import com.lvl6.proto.EventProto.RetrieveCurrencyFromNormStructureRequestProto.StructRetrieval;
import com.lvl6.proto.EventProto.RetrieveCurrencyFromNormStructureResponseProto;
import com.lvl6.proto.EventProto.RetrieveCurrencyFromNormStructureResponseProto.Builder;
import com.lvl6.proto.EventProto.RetrieveCurrencyFromNormStructureResponseProto.RetrieveCurrencyFromNormStructureStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.rarechange.QuestRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.StructureRetrieveUtils;
import com.lvl6.server.GameServer;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.QuestUtils;
import com.lvl6.utils.utilmethods.UpdateUtils;

  @Component @DependsOn("gameServer") public class RetrieveCurrencyFromNormStructureController extends EventController{

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

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
    List<StructRetrieval> structRetrievals = reqProto.getStructRetrievalsList();
    
    Map<Integer, Timestamp> structIdsToTimesOfRetrieval =  new HashMap<Integer, Timestamp>();
    //create map from ids to times and check for duplicates
    boolean uniqueStructs = getIdsAndTimes(structRetrievals, structIdsToTimesOfRetrieval); 
    
    RetrieveCurrencyFromNormStructureResponseProto.Builder resBuilder = RetrieveCurrencyFromNormStructureResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId());
    
    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());
      List<Integer> structIds = new ArrayList<Integer>(structIdsToTimesOfRetrieval.keySet());
      
      Map<Integer, UserStruct> structIdsToUserStructs = getStructIdsToUserStructs(structIds);
      Map<Integer, Structure> structIdsToStructures = getStructIdsToStructs(structIds);
      
      int coinGain = calculateMoneyGainedFromStructs(structIds, structIdsToUserStructs, structIdsToStructures);
      boolean legitRetrieval = checkLegitRetrieval(resBuilder, user, structIds, structIdsToUserStructs,
          structIdsToStructures, structIdsToTimesOfRetrieval, uniqueStructs, coinGain);
      
      if (legitRetrieval) {
//        if (!user.updateRelativeCoinsCoinsretrievedfromstructs(coinGain)) {
//          log.error("problem with updating user stats after retrieving " + coinGain + " silver");
//          legitRetrieval = false;
//        }
//        if (!UpdateUtils.get().updateUserStructLastretrieved(userStructId, timeOfRetrieval)) {
//          log.error("problem with updating user struct last retrieved for userStructId " + userStructId + " to " + timeOfRetrieval);
//          legitRetrieval = false;
//        }
      }

      RetrieveCurrencyFromNormStructureResponseEvent resEvent = new RetrieveCurrencyFromNormStructureResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setRetrieveCurrencyFromNormStructureResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);
      
      if (legitRetrieval) {
//        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEventAndUpdateLeaderboard(user);
//        resEventUpdate.setTag(event.getTag());
//        server.writeEvent(resEventUpdate);
//        
//        updateAndCheckUserQuests(server, coinGain, senderProto);        
      }
    } catch (Exception e) {
      log.error("exception in RetrieveCurrencyFromNormStructureController processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());      
    }
  }

  //returns true if all struct ids are unique (there must be at least one), false otherwise
  private boolean getIdsAndTimes(List<StructRetrieval> srList, Map<Integer, Timestamp> structIdsToTimesOfRetrieval) {
    if (srList.isEmpty()) {
      return false;
    }
    for(StructRetrieval sr : srList) {
      int key = sr.getUserStructId();
      Timestamp value = new Timestamp(sr.getTimeOfRetrieval());
      
      if(structIdsToTimesOfRetrieval.containsKey(key)) {
        return false; //duplicate
      } else {
        structIdsToTimesOfRetrieval.put(key, value);
      }
    }
    return true;
  }
  
  private Map<Integer, UserStruct> getStructIdsToUserStructs(List<Integer> structIds) {
    Map<Integer, UserStruct> returnValue = new HashMap<Integer, UserStruct>();
    
    List<UserStruct> userStructList = RetrieveUtils.userStructRetrieveUtils()
        .getUserStructs(structIds);
    for(UserStruct us : userStructList) {
      if(null != us) {
        returnValue.put(us.getStructId(), us);
      } else {
        //TODO: MAYBE MAKE A TO STRING METHOD FOR THE LIST AND MAP...
        log.error("could not retrieve one of the structs. structIds to retrieve="
            + structIds + ". structs retrieved=" + userStructList + ". Continuing with processing.");
      }
    }
    return returnValue;
  }
  
  private Map<Integer, Structure> getStructIdsToStructs(List<Integer> structIds) {
    Map<Integer, Structure> returnValue = new HashMap<Integer, Structure>();
    Map<Integer, Structure> structIdsToStructs = StructureRetrieveUtils.getStructIdsToStructs();
    
    for(Integer i : structIds) {
      Structure s = structIdsToStructs.get(i);
      if(null != s) {
        returnValue.put(i, s);
      } else {
        log.error("structure with id " + i + " does not exist");
      }
    }
    
    return returnValue;
  }
  
  private int calculateMoneyGainedFromStructs(List<Integer> structIds,
      Map<Integer, UserStruct> structIdsToUserStructs, Map<Integer, Structure> structIdsToStructures) {
    int totalCoinsGained = 0;
    
    for(Integer i : structIds) {
      UserStruct userStructure = structIdsToUserStructs.get(i);
      Structure struct = structIdsToStructures.get(i);
      
      totalCoinsGained += MiscMethods.calculateIncomeGainedFromUserStruct(
          struct.getIncome(), userStructure.getLevel());
    }
    
    return totalCoinsGained;
  }
  
  private void updateAndCheckUserQuests(GameServer server, int coinGain, MinimumUserProto senderProto) {
    List<UserQuest> inProgressUserQuests = RetrieveUtils.userQuestRetrieveUtils().getIncompleteUserQuestsForUser(senderProto.getUserId());
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
      if (relevantQuests.size() > 0 && !UpdateUtils.get().updateUserQuestsCoinsretrievedforreq(senderProto.getUserId(), relevantQuests, coinGain)) {
        log.error("problem with incrementing coins retrieved by " + coinGain + " in user quest info for these quests:" + relevantQuests);
      }
    }
  }

  private boolean checkLegitRetrieval(Builder resBuilder, User user, List<Integer> structIds, 
      Map<Integer, UserStruct> structIdsToUserStructs, Map<Integer, Structure> structIdsToStructures,
      Map<Integer, Timestamp> structIdsToTimesOfRetrieval, boolean uniqueStructs, int coinGain) {
//    if (user == null || structIds.isEmpty()) { //|| timeOfRetrieval == null || userStruct.getLastRetrieved() == null) {
//      resBuilder.setStatus(RetrieveCurrencyFromNormStructureStatus.OTHER_FAIL);
//      log.error("user is null or empty structs from which to collect money. user=" + user);
//      return false;
//    }
//    if (user.getId() != userStruct.getUserId() || !userStruct.isComplete()) {
//      resBuilder.setStatus(RetrieveCurrencyFromNormStructureStatus.OTHER_FAIL);
//      log.error("struct owner is not user, or struct is not complete yet. userStruct=" + userStruct);
//      return false;
//    }
//    if (!MiscMethods.checkClientTimeAroundApproximateNow(timeOfRetrieval)) {
//      resBuilder.setStatus(RetrieveCurrencyFromNormStructureStatus.CLIENT_TOO_APART_FROM_SERVER_TIME);
//      log.error("client time too apart of server time. client time=" + timeOfRetrieval + ", servertime~="
//          + new Date());
//      return false;
//    }
//    if ((timeOfRetrieval.getTime() - userStruct.getLastRetrieved().getTime())  < 60000*struct.getMinutesToGain()) {
//      resBuilder.setStatus(RetrieveCurrencyFromNormStructureStatus.NOT_LONG_ENOUGH);
//      log.error("struct not ready for retrieval yet. time of retrieval=" + timeOfRetrieval
//          + ", userStruct=" + userStruct + ", takes this many minutes to gain:" + struct.getMinutesToGain()); 
//      return false;
//    }
//    if (coinGain <= 0) {
//      resBuilder.setStatus(RetrieveCurrencyFromNormStructureStatus.OTHER_FAIL);
//      log.error("coinGain <= 0. coinGain is " + coinGain);
//      return false;
//    }
//    resBuilder.setStatus(RetrieveCurrencyFromNormStructureStatus.SUCCESS);
//    return true;
    //TODO: DELETE THESE TWO LINES AFTER IMPLEMENTING LOGIC FOR THIS FUNCTION
    resBuilder.setStatus(RetrieveCurrencyFromNormStructureStatus.OTHER_FAIL);
    return false;
  }

}
