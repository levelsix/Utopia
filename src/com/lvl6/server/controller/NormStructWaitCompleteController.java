package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

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

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

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
      
      List<UserStruct> newUserStructs = UserStructRetrieveUtils.getUserStructs(userStructIds);
      for (UserStruct userStruct : newUserStructs) {
        resBuilder.addUserStruct(CreateInfoProtoUtils.createFullUserStructureProtoFromUserstruct(userStruct));
      }
      resEvent.setNormStructWaitCompleteResponseProto(resBuilder.build());  

      server.writeEvent(resEvent);
      
      if (legitWaitComplete) {
        for (UserStruct upgradeDone : upgradesDone) {
          QuestUtils.checkAndSendQuestsCompleteBasic(server, senderProto.getUserId(), senderProto, 
              null, upgradeDone.getStructId(), upgradeDone.getLevel()+1, null, null);          
        }
        for (UserStruct buildDone : buildsDone) {
          QuestUtils.checkAndSendQuestsCompleteBasic(server, senderProto.getUserId(), senderProto, 
              buildDone.getStructId(), null, null, null, null);          
        }
      }
    } catch (Exception e) {
      log.error("exception in NormStructWaitCompleteController processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());      
    }
  }

  private void writeChangesToDB(List<UserStruct> upgradesDone, List<UserStruct> buildsDone) {
    if (!UpdateUtils.updateUserStructsLastretrievedpostupgradeIscompleteLevelchange(upgradesDone, 1)) {
      log.error("problem with marking norm struct upgrade as complete for one of these structs: " + upgradesDone);
    }    
    if (!UpdateUtils.updateUserStructsLastretrievedpostbuildIscomplete(buildsDone)) {
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
        if (us.getLastUpgradeTime().getTime() + 60000*MiscMethods.calculateMinutesToUpgradeForUserStruct(struct.getMinutesToUpgradeBase(), us.getLevel()) > clientTime.getTime()) {
          resBuilder.setStatus(NormStructWaitCompleteStatus.NOT_DONE_YET);
          log.error("the upgrading is not done yet. userstruct=" + us + ", and minutes to upgrade user struct is "
              + MiscMethods.calculateMinutesToUpgradeForUserStruct(struct.getMinutesToUpgradeBase(), us.getLevel())
              + ", client time is " + clientTime + ", upgrade time was " + us.getLastUpgradeTime());
          return false;
        }
      } else if (us.getPurchaseTime() != null) {
        if (us.getPurchaseTime().getTime() + 60000*struct.getMinutesToBuild() > clientTime.getTime()) {
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
