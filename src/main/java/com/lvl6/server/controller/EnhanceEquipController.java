package com.lvl6.server.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.EnhanceEquipRequestEvent;
import com.lvl6.events.response.EnhanceEquipResponseEvent;
import com.lvl6.info.Equipment;
import com.lvl6.info.UserEquip;
import com.lvl6.misc.MiscMethods;
import com.lvl6.proto.EventProto.EnhanceEquipRequestProto;
import com.lvl6.proto.EventProto.EnhanceEquipResponseProto;
import com.lvl6.proto.EventProto.EnhanceEquipResponseProto.Builder;
import com.lvl6.proto.EventProto.EnhanceEquipResponseProto.EnhanceEquipStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.rarechange.EquipmentRetrieveUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.DeleteUtils;
import com.lvl6.utils.utilmethods.UpdateUtils;

  @Component @DependsOn("gameServer") public class EnhanceEquipController extends EventController {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  public EnhanceEquipController() {
    numAllocatedThreads = 3;
  }
  
  @Override
  public RequestEvent createRequestEvent() {
    return new EnhanceEquipRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_ENHANCE_EQUIP_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    EnhanceEquipRequestProto reqProto = ((EnhanceEquipRequestEvent)event).getEnhanceEquipRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();

    UserEquip enhancingUserEquip = RetrieveUtils.userEquipRetrieveUtils().getSpecificUserEquip(reqProto.getEnhancingUserEquipId());
    UserEquip feederUserEquip = RetrieveUtils.userEquipRetrieveUtils().getSpecificUserEquip(reqProto.getFeederUserEquipId());
    int oldEnhancementPercentage = enhancingUserEquip.getEnhancementPercentage();
    
    Map<Integer, Equipment> equipmentIdsToEquipment = EquipmentRetrieveUtils.getEquipmentIdsToEquipment();
    Equipment enhancingEquip = (enhancingUserEquip == null) ? null : equipmentIdsToEquipment.get(enhancingUserEquip.getEquipId());
    Equipment feederEquip = (feederUserEquip == null) ? null : equipmentIdsToEquipment.get(feederUserEquip.getEquipId());

    EnhanceEquipResponseProto.Builder resBuilder = EnhanceEquipResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId());

    try {
      boolean legitEquip = checkEquip(resBuilder);

      EnhanceEquipResponseEvent resEvent = new EnhanceEquipResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setEnhanceEquipResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legitEquip) {
        MiscMethods.enhanceUserEquip(enhancingUserEquip, feederUserEquip);
        writeChangesToDB(enhancingUserEquip, feederUserEquip);
        //TODO: TRACK THIS ENHANCEMENT
      }
    } catch (Exception e) {
      log.error("exception in EnhanceEquip processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());      
    }
  }

  //delete feederuserequip, write enhanceduserequip to the db
  private void writeChangesToDB(UserEquip enhancedUserEquip, UserEquip feederUserEquip) {
    int userEquipId = enhancedUserEquip.getId();
    int enhancementPercent = enhancedUserEquip.getEnhancementPercentage();
    if(!UpdateUtils.get().updateUserEquipAfterEnhancment(userEquipId, enhancementPercent)) {
      
    } else {
      //success, now delete  feederUserEquip
      if (!DeleteUtils.get().deleteUserEquip(userEquipId)) {
        log.error("problem with delete from player userequip with id " + userEquipId);
      }
    }
  }

  private boolean checkEquip(Builder resBuilder) {
    resBuilder.setStatus(EnhanceEquipStatus.SUCCESS);
    return true;
  }
}