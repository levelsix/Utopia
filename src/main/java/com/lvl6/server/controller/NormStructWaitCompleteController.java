package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.NormStructWaitCompleteRequestEvent;
import com.lvl6.events.response.NormStructWaitCompleteResponseEvent;
import com.lvl6.info.Structure;
import com.lvl6.info.UserStruct;
import com.lvl6.misc.MiscMethods;
import com.lvl6.proto.EventProto.NormStructWaitCompleteRequestProto;
import com.lvl6.proto.EventProto.NormStructWaitCompleteResponseProto;
import com.lvl6.proto.EventProto.NormStructWaitCompleteResponseProto.Builder;
import com.lvl6.proto.EventProto.NormStructWaitCompleteResponseProto.NormStructWaitCompleteStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.rarechange.StructureRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.QuestUtils;
import com.lvl6.utils.utilmethods.UpdateUtils;

  @Component @DependsOn("gameServer") public class NormStructWaitCompleteController extends EventController{

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  public NormStructWaitCompleteController() {
    numAllocatedThreads = 5;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new NormStructWaitCompleteRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_NORM_STRUCT_WAIT_COMPLETE_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    NormStructWaitCompleteRequestProto reqProto = ((NormStructWaitCompleteRequestEvent)event).getNormStructWaitCompleteRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    List<Integer> userStructIds = reqProto.getUserStructIdList();
    Timestamp clientTime = new Timestamp(reqProto.getCurTime());

    NormStructWaitCompleteResponseProto.Builder resBuilder = NormStructWaitCompleteResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());

    try {
      List<UserStruct> userStructs = RetrieveUtils.userStructRetrieveUtils().getUserStructs(userStructIds);

      boolean legitWaitComplete = checkLegitWaitComplete(resBuilder, userStructs, userStructIds, senderProto.getUserId(), clientTime);

      NormStructWaitCompleteResponseEvent resEvent = new NormStructWaitCompleteResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());

      List<UserStruct> upgradesDone = new ArrayList<UserStruct>();
      List<UserStruct> buildsDone = new ArrayList<UserStruct>();

      if (legitWaitComplete) {
        for (UserStruct userStruct : userStructs) {
          if (userStruct.getLastUpgradeTime() != null) {
            upgradesDone.add(userStruct);
          } else {
            buildsDone.add(userStruct);
          }
        }

        writeChangesToDB(upgradesDone, buildsDone);
      }

      List<UserStruct> newUserStructs = RetrieveUtils.userStructRetrieveUtils().getUserStructs(userStructIds);
      for (UserStruct userStruct : newUserStructs) {
        resBuilder.addUserStruct(CreateInfoProtoUtils.createFullUserStructureProtoFromUserstruct(userStruct));
      }
      resEvent.setNormStructWaitCompleteResponseProto(resBuilder.build());  

      server.writeEvent(resEvent);

      if (legitWaitComplete) {
        QuestUtils.checkAndSendQuestsCompleteBasic(server, senderProto.getUserId(), senderProto, null, false);          
      }
    } catch (Exception e) {
      log.error("exception in NormStructWaitCompleteController processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());      
    }
  }

  private void writeChangesToDB(List<UserStruct> upgradesDone, List<UserStruct> buildsDone) {
    if (!UpdateUtils.get().updateUserStructsLastretrievedpostupgradeIscompleteLevelchange(upgradesDone, 1)) {
      log.error("problem with marking norm struct upgrade as complete for one of these structs: " + upgradesDone);
    }    
    if (!UpdateUtils.get().updateUserStructsLastretrievedpostbuildIscomplete(buildsDone)) {
      log.error("problem with marking norm struct builds as complete for one of these structs: " + upgradesDone);
    }
  }

  private boolean checkLegitWaitComplete(Builder resBuilder,
      List<UserStruct> userStructs, List<Integer> userStructIds, int userId, Timestamp clientTime) {
    if (userStructs == null || userStructIds == null || clientTime == null || userStructIds.size() != userStructs.size()) {
      resBuilder.setStatus(NormStructWaitCompleteStatus.OTHER_FAIL);
      log.error("userStructs is null, userStructIds is null, clientTime is null, or array lengths different. userStructs="
          + userStructs + ", userStructIds=" + userStructIds + ", clientTime=" + clientTime);
      return false;
    }
    if (!MiscMethods.checkClientTimeAroundApproximateNow(clientTime)) {
      resBuilder.setStatus(NormStructWaitCompleteStatus.CLIENT_TOO_APART_FROM_SERVER_TIME);
      log.error("client time too apart of server time. client time=" + clientTime + ", servertime~="
          + new Date());
      return false;
    }

    Map<Integer, Structure> structures = StructureRetrieveUtils.getStructIdsToStructs();
    for (UserStruct us : userStructs) {
      if (us.getUserId() != userId) {
        resBuilder.setStatus(NormStructWaitCompleteStatus.OTHER_FAIL);
        log.error("user struct's owner's id is " + us.getUserId() + ", and user id is " + userId);
        return false;
      }
      Structure struct = structures.get(us.getStructId());
      if (struct == null) {
        resBuilder.setStatus(NormStructWaitCompleteStatus.OTHER_FAIL);
        log.error("no struct in db exists with id " + us.getStructId());
        return false;        
      }
      if (us.getLastUpgradeTime() != null) {
        if (us.getLastUpgradeTime().getTime() + 60000*MiscMethods.calculateMinutesToBuildOrUpgradeForUserStruct(struct.getMinutesToUpgradeBase(), us.getLevel()) > clientTime.getTime()) {
          resBuilder.setStatus(NormStructWaitCompleteStatus.NOT_DONE_YET);
          log.error("the upgrading is not done yet. userstruct=" + us + ", and minutes to upgrade user struct is "
              + MiscMethods.calculateMinutesToBuildOrUpgradeForUserStruct(struct.getMinutesToUpgradeBase(), us.getLevel())
              + ", client time is " + clientTime + ", upgrade time was " + us.getLastUpgradeTime());
          return false;
        }
      } else if (us.getPurchaseTime() != null) {
        if (us.getPurchaseTime().getTime() + 60000*MiscMethods.calculateMinutesToBuildOrUpgradeForUserStruct(struct.getMinutesToUpgradeBase(), 0) > clientTime.getTime()) {
          resBuilder.setStatus(NormStructWaitCompleteStatus.NOT_DONE_YET);
          log.error("the building is not done yet. userstruct="
              + ", client time is " + clientTime + ", purchase time was " + us.getPurchaseTime());
          return false;
        }        
      } else {
        resBuilder.setStatus(NormStructWaitCompleteStatus.OTHER_FAIL);
        log.error("user struct has never been bought or purchased according to db. " + us);
        return false;                
      }
    }
    resBuilder.setStatus(NormStructWaitCompleteStatus.SUCCESS);
    return true;  

  }
}
