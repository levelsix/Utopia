package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.RedeemMarketplaceEarningsRequestEvent;
import com.lvl6.events.response.RedeemMarketplaceEarningsResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.User;
import com.lvl6.misc.MiscMethods;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.RedeemMarketplaceEarningsRequestProto;
import com.lvl6.proto.EventProto.RedeemMarketplaceEarningsResponseProto;
import com.lvl6.proto.EventProto.RedeemMarketplaceEarningsResponseProto.Builder;
import com.lvl6.proto.EventProto.RedeemMarketplaceEarningsResponseProto.RedeemMarketplaceEarningsStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.utils.RetrieveUtils;

  @Component @DependsOn("gameServer") public class RedeemMarketplaceEarningsController extends EventController {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

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

    server.lockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());

    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());
      int previousSilver = user.getCoins() + user.getVaultBalance();
      int previousGold = user.getDiamonds();
      
      boolean legitRedeem = checkLegitRedeem(resBuilder, user);

      RedeemMarketplaceEarningsResponseEvent resEvent = new RedeemMarketplaceEarningsResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setRedeemMarketplaceEarningsResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legitRedeem) {
        Map<String, Integer> money = new HashMap<String, Integer>();
        writeChangesToDB(user, money);
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEventAndUpdateLeaderboard(user);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);
        
        writeToUserCurrencyHistory(user, money, previousSilver, previousGold);
      }

    } catch (Exception e) {
      log.error("exception in RedeemMarketplaceEarnings processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());      
    }
  }

  private void writeChangesToDB(User user, Map<String, Integer> money) {
    int goldChange = user.getMarketplaceDiamondsEarnings();
    int silverChange = user.getMarketplaceCoinsEarnings();
    if (0 != goldChange) {
      money.put(MiscMethods.gold, goldChange);
    }
    if (0 != silverChange) {
      money.put(MiscMethods.silver, silverChange);
    }
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
  
  public void writeToUserCurrencyHistory(User aUser, Map<String, Integer> money,
      int previousSilver, int previousGold) {
    Timestamp date = new Timestamp((new Date()).getTime());

    Map<String, Integer> previousGoldSilver = new HashMap<String, Integer>();
    Map<String, String> reasonsForChanges = new HashMap<String, String>();
    String gold = MiscMethods.gold;
    String silver = MiscMethods.silver;
    String reasonForChange = ControllerConstants.UCHRFC__REDEEM_MARKETPLACE_EARNINGS;
    
    previousGoldSilver.put(gold, previousGold);
    previousGoldSilver.put(silver, previousSilver);
    reasonsForChanges.put(gold, reasonForChange);
    reasonsForChanges.put(silver, reasonForChange);
    
    MiscMethods.writeToUserCurrencyOneUserGoldAndOrSilver(aUser, date, money,
        previousGoldSilver, reasonsForChanges);
  }
}
