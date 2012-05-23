package com.lvl6.server.controller;

import java.sql.Timestamp;

import org.apache.log4j.Logger;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.PurchaseCityExpansionRequestEvent;
import com.lvl6.events.response.PurchaseCityExpansionResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.User;
import com.lvl6.info.UserCityExpansionData;
import com.lvl6.proto.EventProto.PurchaseCityExpansionRequestProto;
import com.lvl6.proto.EventProto.PurchaseCityExpansionResponseProto;
import com.lvl6.proto.EventProto.PurchaseCityExpansionResponseProto.Builder;
import com.lvl6.proto.EventProto.PurchaseCityExpansionResponseProto.PurchaseCityExpansionStatus;
import com.lvl6.proto.InfoProto.ExpansionDirection;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserCityExpansionRetrieveUtils;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.utils.utilmethods.MiscMethods;
import com.lvl6.utils.utilmethods.UpdateUtils;

public class PurchaseCityExpansionController extends EventController {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

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
  protected void processRequestEvent(RequestEvent event) {
    PurchaseCityExpansionRequestProto reqProto = ((PurchaseCityExpansionRequestEvent)event).getPurchaseCityExpansionRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    ExpansionDirection direction = reqProto.getDirection();
    Timestamp timeOfPurchase = new Timestamp(reqProto.getTimeOfPurchase());

    PurchaseCityExpansionResponseProto.Builder resBuilder = PurchaseCityExpansionResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId());

    try {
      User user = UserRetrieveUtils.getUserById(senderProto.getUserId());
      UserCityExpansionData userCityExpansionData = UserCityExpansionRetrieveUtils.getUserCityExpansionDataForUser(senderProto.getUserId());
      
      boolean legitExpansion = checkLegitExpansion(resBuilder, direction, timeOfPurchase, user, userCityExpansionData);
      
      PurchaseCityExpansionResponseEvent resEvent = new PurchaseCityExpansionResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setPurchaseCityExpansionResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legitExpansion) {
        writeChangesToDB(user, timeOfPurchase, direction, userCityExpansionData);
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEvent(user);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);
      }

    } catch (Exception e) {
      log.error("exception in PurchaseCityExpansion processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());      
    }
  }

  private void writeChangesToDB(User user, Timestamp timeOfPurchase, ExpansionDirection direction, UserCityExpansionData userCityExpansionData) {
    if (!user.updateRelativeCoinsNaive(calculateExpansionCost(userCityExpansionData)*-1)) {
      log.error("problem with updating coins after purchasing a city");
    }
    if (!UpdateUtils.updateUserExpansionLastexpandtimeLastexpanddirectionIsexpanding(user.getId(), timeOfPurchase, direction, true)) {
      log.error("problem with updating user expansion info after purchase");
    }
  }

  private boolean checkLegitExpansion(Builder resBuilder, ExpansionDirection direction, Timestamp timeOfPurchase, User user, UserCityExpansionData userCityExpansionData) {
    if (direction == null || timeOfPurchase == null || user == null || userCityExpansionData == null) {
      resBuilder.setStatus(PurchaseCityExpansionStatus.OTHER_FAIL);
      return false;
    }
    if (!MiscMethods.checkClientTimeAroundApproximateNow(timeOfPurchase)) {
      resBuilder.setStatus(PurchaseCityExpansionStatus.CLIENT_TOO_AHEAD_OF_SERVER_TIME);
      return false;
    }
    if (direction != ExpansionDirection.FAR_LEFT || direction != ExpansionDirection.FAR_RIGHT) {
      resBuilder.setStatus(PurchaseCityExpansionStatus.OTHER_FAIL);
      return false;      
    }
    if (userCityExpansionData.isExpanding()) {
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
    int curNumExpansions = userCityExpansionData.getTotalNumCompletedExpansions();
    return ((curNumExpansions+1)/2) * 200;
  }
}
