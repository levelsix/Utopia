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
import com.lvl6.events.request.PlayThreeCardMonteRequestEvent;
import com.lvl6.events.response.PlayThreeCardMonteResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.MonteCard;
import com.lvl6.info.User;
import com.lvl6.info.UserEquip;
import com.lvl6.misc.MiscMethods;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.PlayThreeCardMonteRequestProto;
import com.lvl6.proto.EventProto.PlayThreeCardMonteResponseProto;
import com.lvl6.proto.EventProto.PlayThreeCardMonteResponseProto.Builder;
import com.lvl6.proto.EventProto.PlayThreeCardMonteResponseProto.PlayThreeCardMonteStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.rarechange.ThreeCardMonteRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.InsertUtils;
import com.lvl6.utils.utilmethods.QuestUtils;

@Component @DependsOn("gameServer") public class PlayThreeCardMonteController extends EventController {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  public PlayThreeCardMonteController() {
    numAllocatedThreads = 4;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new PlayThreeCardMonteRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_PLAY_THREE_CARD_MONTE_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    PlayThreeCardMonteRequestProto reqProto = ((PlayThreeCardMonteRequestEvent)event).getPlayThreeCardMonteRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    Timestamp now = new Timestamp((new Date()).getTime());
    
    PlayThreeCardMonteResponseProto.Builder resBuilder = PlayThreeCardMonteResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());
    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());
      MonteCard card = ThreeCardMonteRetrieveUtils.getMonteCardIdsToMonteCards().get(reqProto.getCardId());
      int previousSilver = 0;
      int previousGold = 0;

      boolean legitPlay = checkLegitPlay(resBuilder, user, card);

      int diamondsGained = card.getDiamondsGained() != ControllerConstants.NOT_SET ? card.getDiamondsGained() : 0;
      int coinsGained = card.getCoinsGained() != ControllerConstants.NOT_SET ? card.getCoinsGained() : 0;
      int equipId = card.getEquipIdForUserType(senderProto.getUserType());
      int equipLevel = card.getEquipLevelForUserType(senderProto.getUserType());
      
      if (legitPlay) {
        if (equipId != ControllerConstants.NOT_SET && equipId > 0 && equipLevel > 0) {
          int newUserEquipId = InsertUtils.get().insertUserEquip(user.getId(), equipId, equipLevel,
              ControllerConstants.DEFAULT_USER_EQUIP_ENHANCEMENT_PERCENT, now);
          if (newUserEquipId     < 0) {
            resBuilder.setStatus(PlayThreeCardMonteStatus.OTHER_FAIL);
            log.error("problem with giving 1 of equip " + equipId + " to forger " + user.getId());
          } else {
            resBuilder.setUserEquip(CreateInfoProtoUtils.createFullUserEquipProtoFromUserEquip(
                new UserEquip(newUserEquipId, user.getId(), equipId, equipLevel, 0)));
          }
          QuestUtils.checkAndSendQuestsCompleteBasic(server, user.getId(), senderProto, null, false);
        }
      }

      PlayThreeCardMonteResponseEvent resEvent = new PlayThreeCardMonteResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setPlayThreeCardMonteResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legitPlay) {
        previousSilver = user.getCoins() + user.getVaultBalance();
        previousGold = user.getDiamonds();
        int diamondsChange = diamondsGained - ControllerConstants.THREE_CARD_MONTE__DIAMOND_PRICE_TO_PLAY;
        
        Map<String, Integer> money = new HashMap<String, Integer>();
        writeChangesToDB(user, diamondsChange, coinsGained, money);
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEventAndUpdateLeaderboard(user);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);
        
        writeToUserCurrencyHistory(user, money, previousSilver, previousGold, now);
      }

    } catch (Exception e) {
      log.error("exception in PlayThreeCardMonte processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());
    }
  }

  private boolean checkLegitPlay(Builder resBuilder, User user, MonteCard card) {
    if (user == null) {
      resBuilder.setStatus(PlayThreeCardMonteStatus.OTHER_FAIL);
      log.error("user is null");
      return false;      
    }
    if (card == null) {
      resBuilder.setStatus(PlayThreeCardMonteStatus.OTHER_FAIL);
      log.error("card is null");
      return false;      
    }
    if (user.getDiamonds() < ControllerConstants.THREE_CARD_MONTE__DIAMOND_PRICE_TO_PLAY) {
      resBuilder.setStatus(PlayThreeCardMonteStatus.NOT_ENOUGH_DIAMONDS);
      log.error("buyer doesnt have enough diamonds. has " + user.getDiamonds() + ", needs " + ControllerConstants.THREE_CARD_MONTE__DIAMOND_PRICE_TO_PLAY);
      return false;
    }
    resBuilder.setStatus(PlayThreeCardMonteStatus.SUCCESS);
    return true;
  }

  private void writeChangesToDB(User user, int diamondsChange, int coinsGained, Map<String, Integer> money) {
    boolean changeNumPostsInMarketplace = false;
    if (!user.updateRelativeDiamondsCoinsNumpostsinmarketplaceNaive(diamondsChange, coinsGained, 
        0, changeNumPostsInMarketplace)) {
      log.error("problem with changing user's diamonds/coins");
    } else {
      if (0 != diamondsChange) {
        money.put(MiscMethods.gold, diamondsChange);
      }
      if (0 != coinsGained) {
        money.put(MiscMethods.silver, coinsGained);
      }
    }
  }
  
  public void writeToUserCurrencyHistory(User aUser, Map<String, Integer> money,
      int previousSilver, int previousGold, Timestamp date) {
    Map<String, Integer> previousGoldSilver = new HashMap<String, Integer>();
    Map<String, String> reasonsForChanges = new HashMap<String, String>();
    String gold = MiscMethods.gold;
    String silver = MiscMethods.silver;
    String reasonForChange = ControllerConstants.UCHRFC__PLAY_THREE_CARD_MONTE;
    
    previousGoldSilver.put(gold, previousGold);
    previousGoldSilver.put(silver, previousSilver);
    reasonsForChanges.put(gold, reasonForChange);
    reasonsForChanges.put(silver, reasonForChange);
    
    MiscMethods.writeToUserCurrencyOneUserGoldAndOrSilver(aUser, date, money,
        previousGoldSilver, reasonsForChanges);
  }
}
