package com.lvl6.server.controller;

import java.util.ArrayList;
import java.util.List;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.RefillStatWaitCompleteRequestEvent;
import com.lvl6.events.response.RefillStatWaitCompleteResponseEvent;
import com.lvl6.info.UserStruct;
import com.lvl6.proto.EventProto.RefillStatWaitCompleteRequestProto;
import com.lvl6.proto.EventProto.RefillStatWaitCompleteResponseProto;
import com.lvl6.proto.EventProto.RefillStatWaitCompleteResponseProto.Builder;
import com.lvl6.proto.EventProto.RefillStatWaitCompleteResponseProto.RefillStatWaitCompleteStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserStructRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.utilmethods.QuestUtils;
import com.lvl6.utils.utilmethods.UpdateUtils;

public class RefillStatWaitCompleteController extends EventController{
 
  @Override
  public RequestEvent createRequestEvent() {
    return new RefillStatWaitCompleteRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_REFILL_STAT_WAIT_COMPLETE_EVENT;
  }

  /*
   * 
  message RefillStatWaitCompleteRequestProto {
  required MinimumUserProto sender = 1;
  required int64 curTime = 2;
  required RefillStatWaitCompleteType type = 3;
  
  enum RefillStatWaitCompleteType { 
    ENERGY = 0;
    STAMINA = 1;
  }
}

message RefillStatWaitCompleteResponseProto {
     required MinimumUserProto sender = 1;
     required RefillStatWaitCompleteStatus status = 2;
     
     enum RefillStatWaitCompleteStatus {
      SUCCESS = 0;
      NOT_READY_YET = 1;
      OTHER_FAIL = 2;
     }
   */
  
  @Override
  protected void processRequestEvent(RequestEvent event) {
    
    RefillStatWaitCompleteRequestProto reqProto = ((RefillStatWaitCompleteRequestEvent)event).getRefillStatWaitCompleteRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();

    RefillStatWaitCompleteResponseProto.Builder resBuilder = RefillStatWaitCompleteResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId());

    try {
      List<UserStruct> userStructs = UserStructRetrieveUtils.getUserStructs(userStructIds);
      
      boolean legitWaitComplete = checkLegitWaitComplete(resBuilder, userStructs, userStructIds, senderProto.getUserId());

      RefillStatWaitCompleteResponseEvent resEvent = new RefillStatWaitCompleteResponseEvent(senderProto.getUserId());

      if (legitWaitComplete) {
        writeChangesToDB(userStructs);
      }
      
      List<UserStruct> newUserStructs = UserStructRetrieveUtils.getUserStructs(userStructIds);
      for (UserStruct userStruct : newUserStructs) {
        resBuilder.addUserStruct(CreateInfoProtoUtils.createFullUserStructureProtoFromUserstruct(userStruct));
      }
      resEvent.setRefillStatWaitCompleteResponseProto(resBuilder.build());  

      server.writeEvent(resEvent);
      
      if (legitWaitComplete) {
        QuestUtils.checkAndSendQuestsCompleteBasic(server, senderProto.getUserId(), senderProto);
      }
    } catch (Exception e) {
      log.error("exception in RefillStatWaitCompleteController processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());      
    }
  }

  private void writeChangesToDB(List<UserStruct> userStructs) {
    List<UserStruct> upgradesDone = new ArrayList<UserStruct>();
    List<UserStruct> buildsDone = new ArrayList<UserStruct>();
    
    for (UserStruct userStruct : userStructs) {
      if (userStruct.getLastUpgradeTime() != null) {
        upgradesDone.add(userStruct);
      } else {
        buildsDone.add(userStruct);
      }
    }
    
    if (!UpdateUtils.updateUserStructsLastretrievedpostupgradeIscompleteLevelchange(buildsDone, 1)) {
      log.error("problem with marking norm struct upgrade as complete");
    }    
    if (!UpdateUtils.updateUserStructsLastretrievedpostbuildIscomplete(buildsDone)) {
      log.error("problem with marking norm struct build as complete");
    }
  }

  private boolean checkLegitWaitComplete(Builder resBuilder,
      List<UserStruct> userStructs, List<Integer> userStructIds, int userId) {
    if (userStructs == null || userStructIds == null || userStructIds.size() != userStructs.size()) {
      resBuilder.setStatus(RefillStatWaitCompleteStatus.OTHER_FAIL);
      return false;
    }
    for (UserStruct us : userStructs) {
      if (us.getUserId() != userId) {
        resBuilder.setStatus(RefillStatWaitCompleteStatus.OTHER_FAIL);
        return false;
      }
    }
    resBuilder.setStatus(RefillStatWaitCompleteStatus.SUCCESS);
    return true;  

  }
}
