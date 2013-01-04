package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
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
    
    Map<Integer, Timestamp> userStructIdsToTimesOfRetrieval =  new HashMap<Integer, Timestamp>();
    List<Integer> duplicates = new ArrayList<Integer>();
    //create map from ids to times and check for duplicates
    getIdsAndTimes(structRetrievals, userStructIdsToTimesOfRetrieval, duplicates); 
    
    RetrieveCurrencyFromNormStructureResponseProto.Builder resBuilder = RetrieveCurrencyFromNormStructureResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId());
    
    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());
      List<Integer> userStructIds = new ArrayList<Integer>(userStructIdsToTimesOfRetrieval.keySet());
      
      Map<Integer, UserStruct> userStructIdsToUserStructs = getUserStructIdsToUserStructs(userStructIds);
      Map<Integer, Structure> userStructIdsToStructures = getUserStructIdsToStructs(userStructIdsToUserStructs.values());
      
      int coinGain = calculateMoneyGainedFromStructs(userStructIds, userStructIdsToUserStructs, userStructIdsToStructures);
      boolean legitRetrieval = checkLegitRetrieval(resBuilder, user, userStructIds, userStructIdsToUserStructs,
          userStructIdsToStructures, userStructIdsToTimesOfRetrieval, duplicates, coinGain);
      
      if (legitRetrieval) {
        if (!user.updateRelativeCoinsCoinsretrievedfromstructs(coinGain)) {
          log.error("problem with updating user stats after retrieving " + coinGain + " silver");
          legitRetrieval = false;
        }
        if (!UpdateUtils.get().updateUserStructsLastretrieved(userStructIdsToTimesOfRetrieval, userStructIdsToUserStructs)) {
          log.error("problem with updating user structs last retrieved for userStructIds " + shallowMapToString(userStructIdsToTimesOfRetrieval));
          legitRetrieval = false;
        }
      }

      RetrieveCurrencyFromNormStructureResponseEvent resEvent = new RetrieveCurrencyFromNormStructureResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setRetrieveCurrencyFromNormStructureResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);
      
      if (legitRetrieval) {
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEventAndUpdateLeaderboard(user);
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

  //separate the duplicate ids from the unique ones
  private void getIdsAndTimes(List<StructRetrieval> srList, Map<Integer, Timestamp> structIdsToTimesOfRetrieval,
      List<Integer> duplicates) {
    if (srList.isEmpty()) {
      log.error("RetrieveCurrencyFromNormStruct request did not send any user struct ids.");
      return;
    }
    for(StructRetrieval sr : srList) {
      int key = sr.getUserStructId();
      Timestamp value = new Timestamp(sr.getTimeOfRetrieval());
      
      if(structIdsToTimesOfRetrieval.containsKey(key)) {
        duplicates.add(key);
      } else {
        structIdsToTimesOfRetrieval.put(key, value);
      }
    }
  }
  
  private Map<Integer, UserStruct> getUserStructIdsToUserStructs(List<Integer> userStructIds) {
    Map<Integer, UserStruct> returnValue = new HashMap<Integer, UserStruct>();
    if(null == userStructIds || userStructIds.isEmpty()) {
      log.error("no user struct ids!");
    }
    
    List<UserStruct> userStructList = RetrieveUtils.userStructRetrieveUtils()
        .getUserStructs(userStructIds);
    for(UserStruct us : userStructList) {
      if(null != us) {
        returnValue.put(us.getId(), us);
      } else {
        log.error("could not retrieve one of the user structs. userStructIds to retrieve="
            + shallowListToString(userStructIds) + ". user structs retrieved=" 
            + shallowListToString(userStructList) + ". Continuing with processing.");
        return new HashMap<Integer, UserStruct>();
      }
    }
    return returnValue;
  }
  
  private Map<Integer, Structure> getUserStructIdsToStructs(Collection<UserStruct> userStructs) {
    Map<Integer, Structure> returnValue = new HashMap<Integer, Structure>();
    Map<Integer, Structure> structIdsToStructs = StructureRetrieveUtils.getStructIdsToStructs();
    
    if(null == userStructs || userStructs.isEmpty()) {
      log.error("There are no user structs.");
    }
    
    for(UserStruct us : userStructs) {
      int structId = us.getStructId();
      int userStructId = us.getId();
      
      Structure s = structIdsToStructs.get(structId);
      if(null != s) {
        returnValue.put(userStructId, s);
      } else {
        log.error("structure with id " + structId + " does not exist, therefore UserStruct is invalid:" + us);
      }
    }
    
    return returnValue;
  }
  
  private int calculateMoneyGainedFromStructs(List<Integer> userStructIds,
      Map<Integer, UserStruct> userStructIdsToUserStructs, Map<Integer, Structure> userStructIdsToStructures) {
    int totalCoinsGained = 0;
    
    for(Integer i : userStructIds) {
      UserStruct userStructure = userStructIdsToUserStructs.get(i);
      Structure struct = userStructIdsToStructures.get(i);
      
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

  private boolean checkLegitRetrieval(Builder resBuilder, User user, List<Integer> userStructIds, 
      Map<Integer, UserStruct> userStructIdsToUserStructs, Map<Integer, Structure> userStructIdsToStructures,
      Map<Integer, Timestamp> userStructIdsToTimesOfRetrieval, List<Integer> duplicates, int coinGain) {

    int userId = user.getId();
    
    if (user == null || userStructIds.isEmpty() || userStructIdsToUserStructs.isEmpty()
        || userStructIdsToStructures.isEmpty() || userStructIdsToTimesOfRetrieval.isEmpty()) { //|| timeOfRetrieval == null || userStruct.getLastRetrieved() == null) {
      resBuilder.setStatus(RetrieveCurrencyFromNormStructureStatus.OTHER_FAIL);
      log.error("user is null, or no struct ids, or no user structs, or no structures, or no retrieval times . user=" + user
          + shallowListToString(userStructIds) + " " + shallowMapToString(userStructIdsToUserStructs) + " " 
          + shallowMapToString(userStructIdsToStructures) + shallowMapToString(userStructIdsToTimesOfRetrieval));
      return false;
    }
    if (!duplicates.isEmpty()) {
      resBuilder.setStatus(RetrieveCurrencyFromNormStructureStatus.OTHER_FAIL);
      log.error("duplicate struct ids in request. ids=" + shallowListToString(duplicates));
      return false;
    }
    for (Integer id : userStructIds) {
      UserStruct userStruct = userStructIdsToUserStructs.get(id);
      Timestamp timeOfRetrieval = userStructIdsToTimesOfRetrieval.get(id);
      Structure struct = userStructIdsToStructures.get(id);
      
      if (userId != userStruct.getUserId() || !userStruct.isComplete()) {
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
    }
    if (coinGain <= 0) {
      resBuilder.setStatus(RetrieveCurrencyFromNormStructureStatus.OTHER_FAIL);
      log.error("coinGain <= 0. coinGain is " + coinGain);
      return false;
    }
    resBuilder.setStatus(RetrieveCurrencyFromNormStructureStatus.SUCCESS);
    return true;
    
  }

  private String shallowListToString(List aList) {
    StringBuilder returnValue = new StringBuilder();
    for(Object o : aList) {
      returnValue.append(" ");
      returnValue.append(o.toString()); 
    }
    return returnValue.toString();
  }
  
  private String shallowMapToString(Map aMap) {
    StringBuilder returnValue = new StringBuilder();
    returnValue.append("[");
    for(Object key : aMap.keySet()) {
      returnValue.append(" ");
      returnValue.append(key);
      returnValue.append("=");
      returnValue.append(aMap.get(key).toString());
    }
    returnValue.append("]");
    return returnValue.toString();
  }
}
