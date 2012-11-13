package com.lvl6.server.controller;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.RedeemMarketplaceEarningsRequestEvent;
import com.lvl6.events.response.RedeemMarketplaceEarningsResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.User;
import com.lvl6.misc.MiscMethods;
import com.lvl6.proto.EventProto.RedeemMarketplaceEarningsRequestProto;
import com.lvl6.proto.EventProto.RedeemMarketplaceEarningsResponseProto;
import com.lvl6.proto.EventProto.RedeemMarketplaceEarningsResponseProto.Builder;
import com.lvl6.proto.EventProto.RedeemMarketplaceEarningsResponseProto.RedeemMarketplaceEarningsStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.utils.RetrieveUtils;

  @Component @DependsOn("gameServer") public class RedeemMarketplaceEarningsController extends EventController {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  public RedeemMarketplaceEarningsController() {
    numAllocatedThreads = 4;
  }
  
  @Override
  public RequestEvent createRequestEvent() {
    return new RedeemMarketplaceEarningsRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_REDEEM_MARKETPLACE_EARNINGS_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    RedeemMarketplaceEarningsRequestProto reqProto = ((RedeemMarketplaceEarningsRequestEvent)event).getRedeemMarketplaceEarningsRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    
    RedeemMarketplaceEarningsResponseProto.Builder resBuilder = RedeemMarketplaceEarningsResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId());

    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());

      boolean legitRedeem = checkLegitRedeem(resBuilder, user);

      RedeemMarketplaceEarningsResponseEvent resEvent = new RedeemMarketplaceEarningsResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setRedeemMarketplaceEarningsResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legitRedeem) {
        writeChangesToDB(user);
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEventAndUpdateLeaderboard(user);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);
      }

    } catch (Exception e) {
      log.error("exception in RedeemMarketplaceEarnings processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());      
    }
  }

  private void writeChangesToDB(User user) {
    if (!user.updateMoveMarketplaceEarningsToRealStatResetNummarketplacesalesunredeemed()) {
      log.error("problem with moving earnings to real stat for user in mktplace. user=" + user);
    }
  }

  private boolean checkLegitRedeem(Builder resBuilder, User user) {
    if (user == null) {
      resBuilder.setStatus(RedeemMarketplaceEarningsStatus.OTHER_FAIL);
      log.error("no user with this id? user=null");
      return false;
    }
    if (user.getMarketplaceCoinsEarnings() <= 0 && user.getMarketplaceDiamondsEarnings() <= 0) {
      resBuilder.setStatus(RedeemMarketplaceEarningsStatus.OTHER_FAIL);
      log.error("user has no marketplace earnings now.");
      return false;      
    }
    resBuilder.setStatus(RedeemMarketplaceEarningsStatus.SUCCESS);
    return true;
  }
}
