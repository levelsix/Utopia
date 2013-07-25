package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.ExpansionWaitCompleteRequestEvent;
import com.lvl6.events.response.ExpansionWaitCompleteResponseEvent;
import com.lvl6.info.User;
import com.lvl6.info.UserCityExpansionData;
import com.lvl6.misc.MiscMethods;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.ExpansionWaitCompleteRequestProto;
import com.lvl6.proto.EventProto.ExpansionWaitCompleteResponseProto;
import com.lvl6.proto.EventProto.ExpansionWaitCompleteResponseProto.Builder;
import com.lvl6.proto.EventProto.ExpansionWaitCompleteResponseProto.ExpansionWaitCompleteStatus;
import com.lvl6.proto.InfoProto.ExpansionDirection;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserCityExpansionRetrieveUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.UpdateUtils;

/*
 * NOT READY/BEING USED YET
 */

  @Component @DependsOn("gameServer") public class ExpansionWaitCompleteController extends EventController{

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  public ExpansionWaitCompleteController() {
    numAllocatedThreads = 1;
  }
  
  @Override
  public RequestEvent createRequestEvent() {
    return new ExpansionWaitCompleteRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_EXPANSION_WAIT_COMPLETE_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    ExpansionWaitCompleteRequestProto reqProto = ((ExpansionWaitCompleteRequestEvent)event).getExpansionWaitCompleteRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    Timestamp clientTime = new Timestamp(reqProto.getCurTime());
    boolean speedUp = reqProto.getSpeedUp();

    ExpansionWaitCompleteResponseProto.Builder resBuilder = ExpansionWaitCompleteResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());

    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());
      UserCityExpansionData userCityExpansionData = UserCityExpansionRetrieveUtils.getUserCityExpansionDataForUser(senderProto.getUserId());
      boolean legitExpansionComplete = checkLegitExpansionComplete(user, resBuilder, userCityExpansionData, clientTime, speedUp);
      int previousGold = 0;
      
      ExpansionWaitCompleteResponseEvent resEvent = new ExpansionWaitCompleteResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setExpansionWaitCompleteResponseProto(resBuilder.build());  

      if (legitExpansionComplete) {
        previousGold = user.getDiamonds();
        
        Map<String, Integer> money = new HashMap<String, Integer>();
        writeChangesToDB(user, userCityExpansionData, speedUp, money, clientTime);
        writeToUserCurrencyHistory(user, clientTime, money, userCityExpansionData, previousGold);
      }
      server.writeEvent(resEvent);
    } catch (Exception e) {
      log.error("exception in ExpansionWaitCompleteController processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());      
    }
  }

  private void writeChangesToDB(User user, UserCityExpansionData userCityExpansionData, boolean speedUp, 
      Map<String, Integer> money, Timestamp clientTime) {
    int farLeftExpansionChange = userCityExpansionData.getFarLeftExpansions(), farRightExpansionChange = userCityExpansionData.getFarRightExpansions(), 
        nearLeftExpansionChange = userCityExpansionData.getNearLeftExpansions(), nearRightExpansionChange = userCityExpansionData.getNearRightExpansions();
    if (userCityExpansionData.getLastExpandDirection() == ExpansionDirection.FAR_LEFT) {
      farLeftExpansionChange++;
    } else if (userCityExpansionData.getLastExpandDirection() == ExpansionDirection.FAR_RIGHT) {
      farRightExpansionChange++;
    } else if (userCityExpansionData.getLastExpandDirection() == ExpansionDirection.NEAR_LEFT) {
      nearLeftExpansionChange++;
    } else if (userCityExpansionData.getLastExpandDirection() == ExpansionDirection.NEAR_RIGHT) {
      nearRightExpansionChange++;
    }
    if (!UpdateUtils.get().updateUserExpansionNumexpansionsIsexpanding(userCityExpansionData.getUserId(), 
        farLeftExpansionChange, farRightExpansionChange, nearLeftExpansionChange, nearRightExpansionChange, false)) {
      log.error("problem with resolving expansion");
    }
    
    if (speedUp) {
      int diamondChange = -expansionDiamondCost(userCityExpansionData, clientTime);
      if (!user.updateRelativeDiamondsNaive(diamondChange)) {
        log.error("problem updating user diamonds");
      } else {
        //everything went ok
        money.put(MiscMethods.gold, diamondChange);
      }
    }
  }

  private boolean checkLegitExpansionComplete(User user, Builder resBuilder, UserCityExpansionData userCityExpansionData, Timestamp clientTime, boolean speedUp) {
    if (userCityExpansionData==null || userCityExpansionData.getLastExpandTime() == null || userCityExpansionData.getLastExpandDirection() == null || clientTime == null) {
      resBuilder.setStatus(ExpansionWaitCompleteStatus.OTHER_FAIL);
      return false;
    }
    if (!MiscMethods.checkClientTimeAroundApproximateNow(clientTime)) {
      resBuilder.setStatus(ExpansionWaitCompleteStatus.CLIENT_TOO_APART_FROM_SERVER_TIME);
      return false;
    }
    if (!userCityExpansionData.isExpanding()) {
      resBuilder.setStatus(ExpansionWaitCompleteStatus.WAS_NOT_EXPANDING);
      return false;      
    }
    if (!speedUp && userCityExpansionData.getLastExpandTime().getTime() + 60000*calculateMinutesForCurrentExpansion(userCityExpansionData) > clientTime.getTime()) {
      resBuilder.setStatus(ExpansionWaitCompleteStatus.NOT_DONE_YET);
      return false;      
    }
    if (speedUp && user.getDiamonds() < expansionDiamondCost(userCityExpansionData, clientTime)) {
      resBuilder.setStatus(ExpansionWaitCompleteStatus.OTHER_FAIL);
      return false;      
    }
    resBuilder.setStatus(ExpansionWaitCompleteStatus.SUCCESS);
    return true;  
  }

  private int calculateMinutesForCurrentExpansion(UserCityExpansionData userCityExpansionData) {
    int numCompletedExpansionsSoFar = userCityExpansionData.getTotalNumCompletedExpansions();
      return (ControllerConstants.EXPANSION_WAIT_COMPLETE__HOUR_CONSTANT + 
          ControllerConstants.EXPANSION_WAIT_COMPLETE__HOUR_INCREMENT_BASE*(numCompletedExpansionsSoFar + 1))*60;
  }

  private int calculateExpansionSpeedupCost(UserCityExpansionData userCityExpansionData) {
    return calculateMinutesForCurrentExpansion(userCityExpansionData)/ControllerConstants.EXPANSION_WAIT_COMPLETE__BASE_MINUTES_TO_ONE_GOLD;
  }
  
  private int expansionDiamondCost(UserCityExpansionData userCityExpansionData, Timestamp clientTime) {
  	long timePassed = (clientTime.getTime() - userCityExpansionData.getLastExpandTime().getTime())/1000;
  	long timeRemaining = calculateMinutesForCurrentExpansion(userCityExpansionData)*60;
  	double percentRemaining = timeRemaining/(double)(timeRemaining+timePassed);
    double speedUpConstant = 1+ControllerConstants.EXPANSION_LATE_SPEEDUP_CONSTANT*(1-percentRemaining);
    
    int diamondCost = (int)Math.ceil(speedUpConstant*percentRemaining*calculateExpansionSpeedupCost(userCityExpansionData));
    return diamondCost;
  }
  
  private void writeToUserCurrencyHistory(User aUser, Timestamp date, Map<String, Integer> money,
      UserCityExpansionData userCityExpansionData, int previousGold) {
    Map<String, Integer> previousGoldSilver = new HashMap<String, Integer>();
    Map<String, String> reasonsForChanges = new HashMap<String, String>();
    String gold = MiscMethods.gold;

    previousGoldSilver.put(gold, previousGold);
    
    String reasonForChange = ControllerConstants.UCHRFC__EXPANSION_WAIT_COMPLETE;
    if (userCityExpansionData.getLastExpandDirection() == ExpansionDirection.FAR_LEFT) {
      reasonsForChanges.put(gold, reasonForChange + "far left");
    } else if (userCityExpansionData.getLastExpandDirection() == ExpansionDirection.FAR_RIGHT) {
      reasonsForChanges.put(gold, reasonForChange + "far right");
    } else if (userCityExpansionData.getLastExpandDirection() == ExpansionDirection.NEAR_LEFT) {
      reasonsForChanges.put(gold, reasonForChange + "near left");
    } else if (userCityExpansionData.getLastExpandDirection() == ExpansionDirection.NEAR_RIGHT) {
      reasonsForChanges.put(gold, reasonForChange + "near right");
    }
    
    MiscMethods.writeToUserCurrencyOneUserGoldAndOrSilver(aUser, date, money, previousGoldSilver, reasonsForChanges);
  }
}

