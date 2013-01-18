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
import com.lvl6.events.request.SubmitEquipEnhancementRequestEvent;
import com.lvl6.events.response.SubmitEquipEnhancementResponseEvent;
import com.lvl6.info.EquipEnhancement;
import com.lvl6.info.Equipment;
import com.lvl6.info.UserEquip;
import com.lvl6.misc.MiscMethods;
import com.lvl6.proto.EventProto.SubmitEquipEnhancementRequestProto;
import com.lvl6.proto.EventProto.SubmitEquipEnhancementResponseProto;
import com.lvl6.proto.EventProto.SubmitEquipEnhancementResponseProto.Builder;
import com.lvl6.proto.EventProto.SubmitEquipEnhancementResponseProto.EnhanceEquipStatus;
import com.lvl6.proto.InfoProto.EquipEnhancementProto;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.rarechange.EquipmentRetrieveUtils;
import com.lvl6.retrieveutils.EquipEnhancementRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.DeleteUtils;
import com.lvl6.utils.utilmethods.InsertUtils;

  @Component @DependsOn("gameServer") public class SubmitEquipEnhancementController extends EventController {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  public SubmitEquipEnhancementController() {
    numAllocatedThreads = 3;
  }
  
  @Override
  public RequestEvent createRequestEvent() {
    return new SubmitEquipEnhancementRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_SUBMIT_EQUIP_ENHANCEMENT_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    SubmitEquipEnhancementRequestProto reqProto = ((SubmitEquipEnhancementRequestEvent)event)
        .getSubmitEquipEnhancementRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int enhancingUserEquipId = reqProto.getEnhancingUserEquipId(); 
    List<Integer> feederUserEquipIds = reqProto.getFeederUserEquipIdsList();
    Timestamp clientTime = new Timestamp(reqProto.getClientTime());
    int userId = senderProto.getUserId();
    
    SubmitEquipEnhancementResponseProto.Builder resBuilder = SubmitEquipEnhancementResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId());
    try {
      //The main user equip to enhance.
      UserEquip enhancingUserEquip = RetrieveUtils.userEquipRetrieveUtils()
          .getSpecificUserEquip(enhancingUserEquipId);

      //The user equips to be sacrified for enhancing.
      //this could be null, or does not contain null but could still be empty
      List<UserEquip> feederUserEquips = RetrieveUtils.userEquipRetrieveUtils()
          .getSpecificUserEquips(feederUserEquipIds);
      
      Map<Integer, Equipment> equipmentIdsToEquipment = EquipmentRetrieveUtils.getEquipmentIdsToEquipment();

      boolean legitEquip = checkEquip(resBuilder, enhancingUserEquip, feederUserEquipIds, feederUserEquips, 
          equipmentIdsToEquipment, clientTime);

      boolean successful = false;
      List<Integer> enhancementInteger = new ArrayList<Integer>();
      if (legitEquip) {
        successful = writeChangesToDB(resBuilder, enhancingUserEquipId, enhancingUserEquip,
            feederUserEquipIds, feederUserEquips, clientTime, enhancementInteger);
      }
      int enhancementId = 0;
      if (successful) {
        enhancementId = enhancementInteger.get(0);
        EquipEnhancementProto eep = CreateInfoProtoUtils.createEquipEnhancementProto(
            enhancementId, userId, enhancingUserEquip, feederUserEquips, clientTime.getTime());
        resBuilder.setEquipToEnhance(eep);
      }
      
      SubmitEquipEnhancementResponseEvent resEvent = new SubmitEquipEnhancementResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setSubmitEquipEnhancementResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);
      
      if (successful) {
        MiscMethods.writeIntoDUEFE(enhancingUserEquip, feederUserEquips, enhancementId);
      }
      
    } catch (Exception e) {
      log.error("exception in EnhanceEquip processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());      
    }
  }

  //delete enhancing user equip; make entry in equip enhancement table
  //delete all the feeder user equips; make entries in equip enhancement feeders table
  //delete feederuserequip, write enhanceduserequip to the db
  private boolean writeChangesToDB(Builder resBuilder, int userEquipId, UserEquip enhancedUserEquip, 
      List<Integer> feederUserEquipIds, List<UserEquip> feederUserEquips, Timestamp clientTime,
      List<Integer> enhancementInteger) {
    int userId = enhancedUserEquip.getUserId();
    int equipId = enhancedUserEquip.getEquipId();
    int equipLevel = enhancedUserEquip.getLevel();
    int enhancementPercentageBeforeEnhancement = enhancedUserEquip.getEnhancementPercentage();
    Timestamp startTimeOfEnhancement = clientTime;
    
    //make entry in equip enhancement table
    int equipEnhancementId = InsertUtils.get().insertEquipEnhancement(userId, equipId, equipLevel, 
        enhancementPercentageBeforeEnhancement, startTimeOfEnhancement); 
    enhancementInteger.add(equipEnhancementId);
    
    if(1 > equipEnhancementId) { //this rarely happens...maybe
      resBuilder.setStatus(EnhanceEquipStatus.OTHER_FAIL);
      log.error("could not enhance equip=" + enhancedUserEquip + ". Id returned: " + equipEnhancementId);
      return false;
    }
    //maybe there should be a check to see if this fails...eh
    //make entries in equip enhancement feeders table
    List<Integer> equipEnhancementFeederIds = InsertUtils.get().insertEquipEnhancementFeeders(
        equipEnhancementId, feederUserEquips);
    
    //maybe there should be a check to see if this fails...eh
    //delete the user equips
    List<Integer> allUserEquipIds = new ArrayList<Integer>(feederUserEquipIds);
    allUserEquipIds.add(userEquipId);
    if(!DeleteUtils.get().deleteUserEquips(allUserEquipIds)) {
      resBuilder.setStatus(EnhanceEquipStatus.OTHER_FAIL);
      log.error("could not delete userEquips with ids: "
                + MiscMethods.shallowListToString(allUserEquipIds));
      return false;
    }
    return true;
  }

  //feederUserEquips could be null or empty
  private List<Equipment> getFeederEquips(List<UserEquip> feederUserEquips, Map<Integer, Equipment> equipmentIdsToEquipment) {
    List<Equipment> returnValue = new ArrayList<Equipment>();
    
    for(UserEquip feederUserEquip: feederUserEquips) {
      int equipId = feederUserEquip.getEquipId();
      Equipment e = equipmentIdsToEquipment.get(equipId);
      returnValue.add(e);
    }
    
    return returnValue;
  }
  
  private boolean checkEquip(Builder resBuilder, UserEquip enhancingUserEquip, List<Integer> feederUserEquipIds, 
      List<UserEquip> feederUserEquips, Map<Integer, Equipment> equipmentIdsToEquipment, Timestamp startTime) {
    int userId = enhancingUserEquip.getUserId();
    //the case where client asked for a user equip and user equip is not there.
    if (!MiscMethods.checkClientTimeAroundApproximateNow(startTime)) {
      resBuilder.setStatus(EnhanceEquipStatus.CLIENT_TOO_APART_FROM_SERVER_TIME);
      log.error("client time too apart of server time. client time=" + startTime + ", servertime~="
          + new Date());
      return false;
    }
    
    List<EquipEnhancement> enhancements = EquipEnhancementRetrieveUtils.getEquipEnhancementsForUser(userId); 
    if (null != enhancements && !enhancements.isEmpty()) {
      resBuilder.setStatus(EnhanceEquipStatus.ALREADY_ENHANCING);
      log.error("user is already enhancing an equip:" + MiscMethods.shallowListToString(enhancements));
      return false;
    }
    
    if (null == enhancingUserEquip || null == feederUserEquips ||
        feederUserEquipIds.size() != feederUserEquips.size()) {
      resBuilder.setStatus(EnhanceEquipStatus.MAIN_OR_FEEDER_OR_EQUIPS_NONEXISTENT);
      log.error("main/feeder equips non existent: enhancingUserEquip=" + enhancingUserEquip
          + ", feederUserEquips=" + MiscMethods.shallowListToString(feederUserEquips) + ", requested feederUserEquipIds="
          + MiscMethods.shallowListToString(feederUserEquipIds));
      return false;
    }
    //user equips exist, check if equips exist
    int equipId = enhancingUserEquip.getEquipId();
    Equipment anEquip = equipmentIdsToEquipment.get(equipId);
    List<Equipment> feederEquips = getFeederEquips(feederUserEquips, equipmentIdsToEquipment);
    if(null == anEquip || feederEquips.isEmpty() || feederEquips.size() != feederUserEquips.size()) {
      resBuilder.setStatus(EnhanceEquipStatus.MAIN_OR_FEEDER_OR_EQUIPS_NONEXISTENT);
      log.error("equip to enhance=" + anEquip + ", feederEquips=" + MiscMethods.shallowListToString(feederEquips)
          + ", feederUserEquips=" + MiscMethods.shallowListToString(feederUserEquips));
      return false;
    }
    
    if (MiscMethods.isEquipAtMaxEnhancementLevel(enhancingUserEquip)) {
      resBuilder.setStatus(EnhanceEquipStatus.TRYING_TO_SURPASS_MAX_LEVEL);
      log.error("user is trying to enhance equip past the max level. user equip to enhance = " 
      + enhancingUserEquip);
      return false;
    }
    resBuilder.setStatus(EnhanceEquipStatus.SUCCESS);
    return true;
  }
  
}
