package com.lvl6.server.controller;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.RedeemMarketplaceEarningsRequestEvent;
import com.lvl6.events.response.RedeemMarketplaceEarningsResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.User;
import com.lvl6.proto.EventProto.RedeemMarketplaceEarningsRequestProto;
import com.lvl6.proto.EventProto.RedeemMarketplaceEarningsResponseProto;
import com.lvl6.proto.EventProto.RedeemMarketplaceEarningsResponseProto.Builder;
import com.lvl6.proto.EventProto.RedeemMarketplaceEarningsResponseProto.RedeemMarketplaceEarningsStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.utils.utilmethods.MiscMethods;

public class RedeemMarketplaceEarningsController extends EventController {

  @Override
  public RequestEvent createRequestEvent() {
    return new RedeemMarketplaceEarningsRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_REDEEM_MARKETPLACE_EARNINGS_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) {
    RedeemMarketplaceEarningsRequestProto reqProto = ((RedeemMarketplaceEarningsRequestEvent)event).getRedeemMarketplaceEarningsRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    
    RedeemMarketplaceEarningsResponseProto.Builder resBuilder = RedeemMarketplaceEarningsResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId());

    try {
      User user = UserRetrieveUtils.getUserById(senderProto.getUserId());

      boolean legitRedeem = checkLegitRedeem(resBuilder, user);

      RedeemMarketplaceEarningsResponseEvent resEvent = new RedeemMarketplaceEarningsResponseEvent(senderProto.getUserId());
      resEvent.setRedeemMarketplaceEarningsResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legitRedeem) {
        writeChangesToDB(user);
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEvent(user);
        server.writeEvent(resEventUpdate);
      }

    } catch (Exception e) {
      log.error("exception in RedeemMarketplaceEarnings processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());      
    }
  }

  private void writeChangesToDB(User user) {
    if (!user.updateMoveMarketplaceEarningsToRealStat()) {
      log.error("problem with moving earnings to real stat for user in mktplace");
    }
  }

  private boolean checkLegitRedeem(Builder resBuilder, User user) {
    if (user == null) {
      resBuilder.setStatus(RedeemMarketplaceEarningsStatus.OTHER_FAIL);
      return false;
    }
    resBuilder.setStatus(RedeemMarketplaceEarningsStatus.SUCCESS);
    return true;
  }
}
