package com.lvl6.server.controller;

import java.util.List;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.NormStructBuildsCompleteRequestEvent;
import com.lvl6.events.response.NormStructBuildsCompleteResponseEvent;
import com.lvl6.info.UserStruct;
import com.lvl6.proto.EventProto.NormStructBuildsCompleteRequestProto;
import com.lvl6.proto.EventProto.NormStructBuildsCompleteResponseProto;
import com.lvl6.proto.EventProto.NormStructBuildsCompleteResponseProto.Builder;
import com.lvl6.proto.EventProto.NormStructBuildsCompleteResponseProto.NormStructBuildsCompleteStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserStructRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.utilmethods.QuestUtils;
import com.lvl6.utils.utilmethods.UpdateUtils;

public class NormStructBuildsCompleteController extends EventController{
 
  @Override
  public RequestEvent createRequestEvent() {
    return new NormStructBuildsCompleteRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_NORM_STRUCT_BUILDS_COMPLETE_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) {
    
    NormStructBuildsCompleteRequestProto reqProto = ((NormStructBuildsCompleteRequestEvent)event).getNormStructBuildsCompleteRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    List<Integer> userStructIds = reqProto.getUserStructIdList();

    NormStructBuildsCompleteResponseProto.Builder resBuilder = NormStructBuildsCompleteResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId());

    try {
      List<UserStruct> userStructs = UserStructRetrieveUtils.getUserStructs(userStructIds);
      
      boolean legitBuild = checkLegitBuild(resBuilder, userStructs, userStructIds);

      NormStructBuildsCompleteResponseEvent resEvent = new NormStructBuildsCompleteResponseEvent(senderProto.getUserId());
      resEvent.setNormStructBuildsCompleteResponseProto(resBuilder.build());  

      if (legitBuild) {
        writeChangesToDB(userStructs);
      }
      
      List<UserStruct> newUserStructs = UserStructRetrieveUtils.getUserStructs(userStructIds);
      for (UserStruct userStruct : newUserStructs) {
        resBuilder.addUserStruct(CreateInfoProtoUtils.createFullUserStructureProto(userStruct));
      }
      server.writeEvent(resEvent);
      
      if (legitBuild) {
        QuestUtils.checkAndSendQuestsCompleteBasic(server, senderProto.getUserId(), senderProto);
      }
    } catch (Exception e) {
      log.error("exception in NormStructBuildsCompleteController processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());      
    }
  }

  private void writeChangesToDB(List<UserStruct> userStructs) {
    if (!UpdateUtils.updateUserStructsLastretrievedIscomplete(userStructs, true)) {
      log.error("problem with marking norm struct build as complete");
    }
  }

  private boolean checkLegitBuild(Builder resBuilder,
      List<UserStruct> userStructs, List<Integer> userStructIds) {
    if (userStructs == null || userStructIds == null || userStructIds.size() != userStructs.size()) {
      resBuilder.setStatus(NormStructBuildsCompleteStatus.OTHER_FAIL);
      return false;
    }
    resBuilder.setStatus(NormStructBuildsCompleteStatus.SUCCESS);
    return true;  

  }
}
