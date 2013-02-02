package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.PurchaseCityExpansionRequestEvent;
import com.lvl6.events.response.PurchaseCityExpansionResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.User;
import com.lvl6.info.UserCityExpansionData;
import com.lvl6.leaderboards.LeaderBoardUtil;
import com.lvl6.misc.MiscMethods;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.PurchaseCityExpansionRequestProto;
import com.lvl6.proto.EventProto.PurchaseCityExpansionResponseProto;
import com.lvl6.proto.EventProto.PurchaseCityExpansionResponseProto.Builder;
import com.lvl6.proto.EventProto.PurchaseCityExpansionResponseProto.PurchaseCityExpansionStatus;
import com.lvl6.proto.InfoProto.ExpansionDirection;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserCityExpansionRetrieveUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.UpdateUtils;

/*
 * NOT READY/BEING USED YET
 */

  @Component @DependsOn("gameServer") public class PurchaseCityExpansionController extends EventController {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  @Autowired
  protected LeaderBoardUtil leaderboard;

  public LeaderBoardUtil getLeaderboard() {
	return leaderboard;
	}
	
	public void setLeaderboard(LeaderBoardUtil leaderboard) {
		this.leaderboard = leaderboard;
	}
  
  public PurchaseCityExpansionController() {
    numAllocatedThreads = 1;
  }
  
  @Override
  public RequestEvent createRequestEvent() {
    return new PurchaseCityExpansionRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_PURCHASE_CITY_EXPANSION_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    PurchaseCityExpansionRequestProto reqProto = ((PurchaseCityExpansionRequestEvent)event).getPurchaseCityExpansionRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    ExpansionDirection direction = reqProto.getDirection();
    Timestamp timeOfPurchase = new Timestamp(reqProto.getTimeOfPurchase());

    PurchaseCityExpansionResponseProto.Builder resBuilder = PurchaseCityExpansionResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());

    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());
      UserCityExpansionData userCityExpansionData = UserCityExpansionRetrieveUtils.getUserCityExpansionDataForUser(senderProto.getUserId());
      int previousSilver = 0;
      
      boolean legitExpansion = checkLegitExpansion(resBuilder, direction, timeOfPurchase, user, userCityExpansionData);
      
      PurchaseCityExpansionResponseEvent resEvent = new PurchaseCityExpansionResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setPurchaseCityExpansionResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legitExpansion) {
        previousSilver = user.getCoins() + user.getVaultBalance();
        
        Map<String, Integer> currencyChange = new HashMap<String, Integer>();
        writeChangesToDB(user, timeOfPurchase, direction, userCityExpansionData, currencyChange);
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEventAndUpdateLeaderboard(user);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);
        
        writeToUserCurrencyHistory(user, timeOfPurchase, direction, currencyChange, previousSilver);
      }
    } catch (Exception e) {
      log.error("exception in PurchaseCityExpansion processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());      
    }
  }

  private void writeChangesToDB(User user, Timestamp timeOfPurchase, ExpansionDirection direction, 
      UserCityExpansionData userCityExpansionData, Map<String, Integer> currencyChange) {
//    if (!user.updateRelativeCoinsNaive(calculateExpansionCost(userCityExpansionData)*-1)) {
    int coinChange = calculateExpansionCost(userCityExpansionData)*-1;
    if (!user.updateRelativeCoinsNaive(coinChange)) {
      log.error("problem with updating coins after purchasing a city");
    } else {//everything went ok
      currencyChange.put(MiscMethods.silver, coinChange);
    }
    if (!UpdateUtils.get().updateUserExpansionLastexpandtimeLastexpanddirectionIsexpanding(user.getId(), timeOfPurchase, direction, true)) {
      log.error("problem with updating user expansion info after purchase");
    }
  }

  private boolean checkLegitExpansion(Builder resBuilder, ExpansionDirection direction, Timestamp timeOfPurchase, User user, UserCityExpansionData userCityExpansionData) {
    if (direction == null || timeOfPurchase == null || user == null) {
      resBuilder.setStatus(PurchaseCityExpansionStatus.OTHER_FAIL);
      return false;
    }
    if (!MiscMethods.checkClientTimeAroundApproximateNow(timeOfPurchase)) {
      resBuilder.setStatus(PurchaseCityExpansionStatus.CLIENT_TOO_APART_FROM_SERVER_TIME);
      return false;
    }
    if (userCityExpansionData != null && userCityExpansionData.isExpanding()) {
      resBuilder.setStatus(PurchaseCityExpansionStatus.ALREADY_EXPANDING);
      return false;      
    }
    if (user.getCoins() < calculateExpansionCost(userCityExpansionData)) {
      resBuilder.setStatus(PurchaseCityExpansionStatus.NOT_ENOUGH_COINS);
      return false;            
    }
    resBuilder.setStatus(PurchaseCityExpansionStatus.SUCCESS);
    return true;  
  }

  private int calculateExpansionCost(UserCityExpansionData userCityExpansionData) {
    int curNumExpansions = userCityExpansionData != null ? userCityExpansionData.getTotalNumCompletedExpansions() : 0;
    return (int) (ControllerConstants.PURCHASE_EXPANSION__COST_CONSTANT*Math.pow(ControllerConstants.PURCHASE_EXPANSION__COST_EXPONENT_BASE, curNumExpansions));
  }
  
  public void writeToUserCurrencyHistory(User aUser, Timestamp date, ExpansionDirection direction,
      Map<String, Integer> goldSilverChange, int previousSilver) {
    Map<String, Integer> previousGoldSilver = new HashMap<String, Integer>();
    Map<String, String> reasonsForChanges = new HashMap<String, String>();
    String silver = MiscMethods.silver;
    String reasonForChange = ControllerConstants.UCHRFC__PURCHASE_CITY_EXPANSION + direction.getNumber();
    
    previousGoldSilver.put(silver, previousSilver);
    reasonsForChanges.put(silver, reasonForChange);
    
    MiscMethods.writeToUserCurrencyOneUserGoldAndOrSilver(aUser, date, goldSilverChange,
        previousGoldSilver, reasonsForChanges);
  }
}
