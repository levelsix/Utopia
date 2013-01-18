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
import com.lvl6.events.request.CollectEquipEnhancementRequestEvent;
import com.lvl6.events.response.CollectEquipEnhancementResponseEvent;
import com.lvl6.info.EquipEnhancement;
import com.lvl6.info.EquipEnhancementFeeder;
import com.lvl6.info.Equipment;
import com.lvl6.info.User;
import com.lvl6.info.UserEquip;
import com.lvl6.misc.MiscMethods;
import com.lvl6.proto.EventProto.CollectEquipEnhancementRequestProto;
import com.lvl6.proto.EventProto.CollectEquipEnhancementResponseProto;
import com.lvl6.proto.EventProto.CollectEquipEnhancementResponseProto.Builder;
import com.lvl6.proto.EventProto.CollectEquipEnhancementResponseProto.CollectEquipStatus;
import com.lvl6.proto.InfoProto.EquipEnhancementProto;
import com.lvl6.proto.InfoProto.FullUserEquipProto;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.EquipEnhancementFeederRetrieveUtils;
import com.lvl6.retrieveutils.EquipEnhancementRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.DeleteUtils;
import com.lvl6.utils.utilmethods.InsertUtils;

  @Component @DependsOn("gameServer") public class CollectEquipEnhancementController extends EventController {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  public CollectEquipEnhancementController() {
    numAllocatedThreads = 3;
  }
  
  @Override
  public RequestEvent createRequestEvent() {
    return new CollectEquipEnhancementRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_COLLECT_EQUIP_ENHANCEMENT_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    CollectEquipEnhancementRequestProto reqProto = ((CollectEquipEnhancementRequestEvent)event)
        .getCollectEquipEnhancementRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int equipEnhancementId = reqProto.getEquipEnhancementId(); 
    Timestamp clientTime = new Timestamp(reqProto.getClientTime());
    boolean speedUp = reqProto.getSpeedUp();
    int userId = senderProto.getUserId();
    
    CollectEquipEnhancementResponseProto.Builder resBuilder = CollectEquipEnhancementResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId());
    try {
      //The main equip that is being enhanced.
      EquipEnhancement equipUnderEnhancement = EquipEnhancementRetrieveUtils
          .getEquipEnhancementsForEquipEnhancementId(equipEnhancementId);

      //The equips that were sacrificed.
      List<EquipEnhancementFeeder> feedersForEnhancement = EquipEnhancementFeederRetrieveUtils
          .getEquipEnhancementFeedersForEquipEnhancementId(equipEnhancementId);

      User aUser = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId()); 
      
      //check if the time is right, if speed up, check if user has enough gold, check if enhancement is complete
      boolean legitEquip = checkEquip(resBuilder, aUser, equipUnderEnhancement, feedersForEnhancement, 
          clientTime, speedUp);

      boolean successful = false;
      FullUserEquipProto.Builder userEquipBuilder = FullUserEquipProto.newBuilder();
      if (legitEquip) {
        //add the user equip, delete the equip enhancement and equip enhancement feeders,
        //record what happened
        successful = writeChangesToDB(resBuilder, equipEnhancementId, equipUnderEnhancement,
            feedersForEnhancement, clientTime, speedUp, userEquipBuilder);
      }
      
      if (successful) {
        //create the User Equip for the client
        resBuilder.setResultingEquip(userEquipBuilder.build());
      }
      
      CollectEquipEnhancementResponseEvent resEvent = new CollectEquipEnhancementResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setCollectEquipEnhancementResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);
      
      if (successful) {
        writeIntoEquipEnhancementHistory(equipUnderEnhancement, userEquipBuilder, speedUp, clientTime);
        writeIntoEquipEnhancementFeederHistory(equipEnhancementId, feedersForEnhancement);
      }
      
    } catch (Exception e) {
      log.error("exception in EnhanceEquip processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());      
    }
  }

  //add the user equip, delete the equip enhancement and equip enhancement feeders,
  //record what happened
  private boolean writeChangesToDB(Builder resBuilder, int equipEnhancementId, EquipEnhancement equipUnderEnhancement, 
      List<EquipEnhancementFeeder> feedersForEnhancement, Timestamp clientTime, boolean speedUp, 
      FullUserEquipProto.Builder userEquipBuilder) {
    //stuff to create user equip
    int userId = equipUnderEnhancement.getUserId();
    int equipId = equipUnderEnhancement.getEquipId();
    int equipLevel = equipUnderEnhancement.getEquipLevel();
    //int enhancementPercentageBeforeEnhancement = equipUnderEnhancement.getEnhancementPercentage();

    int enhancementPercentageAfterEnhancement = MiscMethods.calculateEnhancementForEquip(equipUnderEnhancement,
        feedersForEnhancement);
    int userEquipId = InsertUtils.get().insertUserEquip(userId, equipId, equipLevel, enhancementPercentageAfterEnhancement);
    
    
    if(1 > userEquipId) {
      log.error("could not make new user equip after enhancement finished. equipUnderEnhancement="
          + equipUnderEnhancement + ", feedersForEnhancement=" + MiscMethods.shallowListToString(feedersForEnhancement));
      return false;
    }
    
    //delete the equip enhancement stuff
    List<Integer> equipEnhancementIds = new ArrayList<Integer>();
    equipEnhancementIds.add(equipEnhancementId);
    boolean successfulDelete = DeleteUtils.get().deleteEquipEnhancements(equipEnhancementIds);
    if(!successfulDelete) {
      log.error("Did not delete equip enhancements: " + equipUnderEnhancement
          + ", equip enhancement feeders: " + MiscMethods.shallowListToString(feedersForEnhancement));
    }
    //delete equip ehancement feeder stuff
    List<Integer> equipEnhancementFeederIds = getFeederIds(feedersForEnhancement);
    successfulDelete = DeleteUtils.get().deleteEquipEnhancementFeeders(equipEnhancementFeederIds);
    if(!successfulDelete) {
      log.error("Did not delete equip enhancement feeders: " + equipUnderEnhancement
          + ", equip enhancement feeders: " + MiscMethods.shallowListToString(feedersForEnhancement));
    }
    
    //since Enhancing is finished, create user equip for the client
    userEquipBuilder.setUserEquipId(userEquipId);
    userEquipBuilder.setUserId(userId);
    userEquipBuilder.setEquipId(equipId);
    userEquipBuilder.setLevel(equipLevel);
    userEquipBuilder.setEnhancementPercentage(enhancementPercentageAfterEnhancement);
    
    return true;
  }

  //feederUserEquips could be null or empty
  private List<Integer> getFeederIds(List<EquipEnhancementFeeder> feeders) {
    List<Integer> returnValue = new ArrayList<Integer>();
    
    for(EquipEnhancementFeeder aFeeder: feeders) {
      int feederId = aFeeder.getId();
      returnValue.add(feederId);
    }
    
    return returnValue;
  }

  //check if the time is right; if speed up, check if user has enough gold; check if enhancement is complete
  private boolean checkEquip(Builder resBuilder, User aUser, EquipEnhancement equipUnderEnhancement, 
      List<EquipEnhancementFeeder> feedersForEnhancement, Timestamp clientTime, boolean speedUp) {
    if(null == aUser || null == equipUnderEnhancement || null == feedersForEnhancement ||
        feedersForEnhancement.isEmpty()) {
      resBuilder.setStatus(CollectEquipStatus.OTHER_FAIL);
      log.error("user or enhanced equip to collect is null. user=" + aUser + ", equip being enhanced="
          + equipUnderEnhancement + ", feedersForEnhancement=" + MiscMethods.shallowListToString(feedersForEnhancement));
      return false;
    }
    
    //the case where client asked for a user equip and user equip is not there.
    if (!MiscMethods.checkClientTimeAroundApproximateNow(clientTime)) {
      resBuilder.setStatus(CollectEquipStatus.CLIENT_TOO_APART_FROM_SERVER_TIME);
      log.error("client time too apart of server time. client time=" + clientTime + ", servertime~="
          + new Date());
      return false;
    }
    
    if(speedUp) {
      int cost = MiscMethods.calculateCostToSpeedUpEnhancing(equipUnderEnhancement, feedersForEnhancement, clientTime);
      int userGold = aUser.getDiamonds();
      if(userGold >= cost) {
        resBuilder.setStatus(CollectEquipStatus.SUCCESS);
        return true;
      } else {
        resBuilder.setStatus(CollectEquipStatus.NOT_ENOUGH_GOLD);
        log.error("insufficient gold to speed up enhancing. user's gold=" + userGold + ", cost=" + cost);
        return false;
      }
    }
    
    int minutesForEnhancing = 
        MiscMethods.calculateMinutesToFinishEnhancing(equipUnderEnhancement, feedersForEnhancement);
    long millisecondsForEnhancing = minutesForEnhancing * 60 * 1000;
    
    long enhancingStartTime = equipUnderEnhancement.getStartTimeOfEnhancement().getTime();
    long enhancingEndTime = enhancingStartTime + millisecondsForEnhancing;
    
    if(enhancingEndTime > clientTime.getTime()) {
      resBuilder.setStatus(CollectEquipStatus.ENHANCEMENT_NOT_COMPLETE);
      log.info("user tried collecting unfinished enhanced equip. user=" + aUser
          + ", equipUnderEnhancement=" + equipUnderEnhancement + ", enhandingEndTime=" 
          + enhancingEndTime + ", timeOfCollection=" + clientTime);
      return false;
    }
    
    resBuilder.setStatus(CollectEquipStatus.SUCCESS);
    return true;
  }
  
  //the row created will record what user equip was created through enhancing,
  //DELETED USER EQUIPS FOR ENHANCING table used in conjunction with this table
  //can track down which user equip became the new user equip
  private void writeIntoEquipEnhancementHistory(EquipEnhancement equipUnderEnhancement,
      FullUserEquipProto.Builder userEquipBuilder, boolean speedUp, Timestamp clientTime) {
    int equipEnhancementId = equipUnderEnhancement.getId();
    int userId = equipUnderEnhancement.getUserId();
    int equipId = equipUnderEnhancement.getEquipId();
    int equipLevel = equipUnderEnhancement.getEquipLevel();
    int currentEnhancementPercentage = userEquipBuilder.getEnhancementPercentage();
    int previousEnhancementPercentage = equipUnderEnhancement.getEnhancementPercentage();
    Timestamp timeOfEnhancement = null;
    Timestamp timeOfSpeedUp = null;
    int userEquipId = userEquipBuilder.getUserEquipId();
    
    //to prevent possible null pointer exception
    Date startDate = equipUnderEnhancement.getStartTimeOfEnhancement();
    if(null != startDate) {
      long milliSeconds = startDate.getTime();
      timeOfEnhancement = new Timestamp(milliSeconds);
    }
    
    if(speedUp) {
      timeOfSpeedUp = clientTime;
    }
    int numInserted = InsertUtils.get().insertIntoEquipEnhancementHistory(equipEnhancementId,
        userId, equipId, equipLevel, currentEnhancementPercentage, previousEnhancementPercentage,
        timeOfEnhancement, timeOfSpeedUp, userEquipId);
    
    if(1 != numInserted) {
      log.error("could not record equip enhancing collection. equipUnderEnhancement="
          + equipUnderEnhancement + ", speedUp=" + speedUp + ", clientTime=" + clientTime);
    }
  }
  
  private void writeIntoEquipEnhancementFeederHistory(int equipEnhancementId, 
      List<EquipEnhancementFeeder> feeders) {
    int size = feeders.size();
    int numInserted = InsertUtils.get()
        .insertMultipleIntoEquipEnhancementFeedersHistory(equipEnhancementId, feeders);
    if(size != numInserted) {
      log.error("numInserted into feeder history table: " + numInserted + ", should have been:" + size);
    }
  }
}
