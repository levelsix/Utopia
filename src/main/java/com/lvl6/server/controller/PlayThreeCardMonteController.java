package com.lvl6.server.controller;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.PlayThreeCardMonteRequestEvent;
import com.lvl6.events.response.PlayThreeCardMonteResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.MonteCard;
import com.lvl6.info.User;
import com.lvl6.info.UserEquip;
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
import com.lvl6.utils.utilmethods.MiscMethods;
import com.lvl6.utils.utilmethods.QuestUtils;

@Component @DependsOn("gameServer") public class PlayThreeCardMonteController extends EventController {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

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

    PlayThreeCardMonteResponseProto.Builder resBuilder = PlayThreeCardMonteResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    MonteCard card = ThreeCardMonteRetrieveUtils.getMonteCardIdsToMonteCards().get(reqProto.getCardId());
    int diamondsGained = card.getDiamondsGained();
    int coinsGained = card.getCoinsGained();
    int equipId = card.getEquipIdForUserType(senderProto.getUserType());
    int equipLevel = card.getEquipLevelForUserType(senderProto.getUserType());

    server.lockPlayer(senderProto.getUserId());
    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());

      boolean legitPlay = checkLegitPlay(resBuilder, user);
      
      if (legitPlay) {
        if (equipId != ControllerConstants.NOT_SET && equipId > 0 && equipLevel > 0) {
          int newUserEquipId = InsertUtils.get().insertUserEquip(user.getId(), equipId, equipLevel);
          if (newUserEquipId < 0) {
            resBuilder.setStatus(PlayThreeCardMonteStatus.OTHER_FAIL);
            log.error("problem with giving 1 of equip " + equipId + " to forger " + user.getId());
          } else {
            resBuilder.setUserEquip(CreateInfoProtoUtils.createFullUserEquipProtoFromUserEquip(
                new UserEquip(newUserEquipId, user.getId(), equipId, equipLevel)));
          }
          QuestUtils.checkAndSendQuestsCompleteBasic(server, user.getId(), senderProto, null, false);
        }
      }

      PlayThreeCardMonteResponseEvent resEvent = new PlayThreeCardMonteResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setPlayThreeCardMonteResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legitPlay) {
        int diamondsChange = diamondsGained - ControllerConstants.THREE_CARD_MONTE__DIAMOND_PRICE_TO_PLAY;

        writeChangesToDB(user, diamondsChange, coinsGained);
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEventAndUpdateLeaderboard(user);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);
      }

    } catch (Exception e) {
      log.error("exception in PlayThreeCardMonte processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());
    }
  }

  private boolean checkLegitPlay(Builder resBuilder, User user) {
    if (user == null) {
      resBuilder.setStatus(PlayThreeCardMonteStatus.OTHER_FAIL);
      log.error("user is null");
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

  private void writeChangesToDB(User user, int diamondsChange, int coinsGained) {
    if (!user.updateRelativeDiamondsCoinsNumpostsinmarketplaceNaive(diamondsChange, coinsGained, 0)) {
      log.error("problem with changing user's diamonds/coins");
    }
  }
}
