package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.NormStructWaitCompleteRequestEvent;
import com.lvl6.events.response.NormStructWaitCompleteResponseEvent;
import com.lvl6.info.Structure;
import com.lvl6.info.UserStruct;
import com.lvl6.proto.EventProto.NormStructWaitCompleteRequestProto;
import com.lvl6.proto.EventProto.NormStructWaitCompleteResponseProto;
import com.lvl6.proto.EventProto.NormStructWaitCompleteResponseProto.Builder;
import com.lvl6.proto.EventProto.NormStructWaitCompleteResponseProto.NormStructWaitCompleteStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserStructRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.StructureRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.utilmethods.MiscMethods;
import com.lvl6.utils.utilmethods.QuestUtils;
import com.lvl6.utils.utilmethods.UpdateUtils;

public class NormStructWaitCompleteController extends EventController{
 
  @Override
  public RequestEvent createRequestEvent() {
    return new NormStructWaitCompleteRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_NORM_STRUCT_WAIT_COMPLETE_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) {
    
    NormStructWaitCompleteRequestProto reqProto = ((NormStructWaitCompleteRequestEvent)event).getNormStructWaitCompleteRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    List<Integer> userStructIds = reqProto.getUserStructIdList();
    Timestamp clientTime = new Timestamp(reqProto.getCurTime());

    NormStructWaitCompleteResponseProto.Builder resBuilder = NormStructWaitCompleteResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId());

    try {
      List<UserStruct> userStructs = UserStructRetrieveUtils.getUserStructs(userStructIds);
      
      boolean legitWaitComplete = checkLegitWaitComplete(resBuilder, userStructs, userStructIds, senderProto.getUserId(), clientTime);

      NormStructWaitCompleteResponseEvent resEvent = new NormStructWaitCompleteResponseEvent(senderProto.getUserId());

      if (legitWaitComplete) {
        writeChangesToDB(userStructs);
      }
      
      List<UserStruct> newUserStructs = UserStructRetrieveUtils.getUserStructs(userStructIds);
      for (UserStruct userStruct : newUserStructs) {
        resBuilder.addUserStruct(CreateInfoProtoUtils.createFullUserStructureProtoFromUserstruct(userStruct));
      }
      resEvent.setNormStructWaitCompleteResponseProto(resBuilder.build());  

      server.writeEvent(resEvent);
      
      if (legitWaitComplete) {
        QuestUtils.checkAndSendQuestsCompleteBasic(server, senderProto.getUserId(), senderProto);
      }
    } catch (Exception e) {
      log.error("exception in NormStructWaitCompleteController processEvent", e);
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
      List<UserStruct> userStructs, List<Integer> userStructIds, int userId, Timestamp clientTime) {
    if (userStructs == null || userStructIds == null || clientTime == null || userStructIds.size() != userStructs.size()) {
      resBuilder.setStatus(NormStructWaitCompleteStatus.OTHER_FAIL);
      return false;
    }

    Map<Integer, Structure> structures = StructureRetrieveUtils.getStructIdsToStructs();
    for (UserStruct us : userStructs) {
      if (us.getUserId() != userId) {
        resBuilder.setStatus(NormStructWaitCompleteStatus.OTHER_FAIL);
        return false;
      }
      Structure struct = structures.get(us.getStructId());
      if (struct == null) {
        resBuilder.setStatus(NormStructWaitCompleteStatus.OTHER_FAIL);
        return false;        
      }
      if (us.getLastUpgradeTime() != null) {
        if (us.getLastUpgradeTime().getTime() + 60000*MiscMethods.calculateMinutesToUpgradeForUserStruct(struct.getMinutesToUpgradeBase(), us.getLevel()) > clientTime.getTime()) {
          resBuilder.setStatus(NormStructWaitCompleteStatus.NOT_DONE_YET);
          return false;
        }
      } else if (us.getPurchaseTime() != null) {
        if (us.getPurchaseTime().getTime() + 60000*struct.getMinutesToBuild() > clientTime.getTime()) {
          resBuilder.setStatus(NormStructWaitCompleteStatus.NOT_DONE_YET);
          return false;
        }        
      } else {
        resBuilder.setStatus(NormStructWaitCompleteStatus.OTHER_FAIL);
        return false;                
      }
    }
    resBuilder.setStatus(NormStructWaitCompleteStatus.SUCCESS);
    return true;  

  }
}
