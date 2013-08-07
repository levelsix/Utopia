package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.PickLockBoxRequestEvent;
import com.lvl6.events.response.PickLockBoxResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.LockBoxEvent;
import com.lvl6.info.LockBoxItem;
import com.lvl6.info.User;
import com.lvl6.info.UserEquip;
import com.lvl6.info.UserLockBoxEvent;
import com.lvl6.misc.MiscMethods;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.PickLockBoxRequestProto;
import com.lvl6.proto.EventProto.PickLockBoxRequestProto.PickLockBoxMethod;
import com.lvl6.proto.EventProto.PickLockBoxResponseProto;
import com.lvl6.proto.EventProto.PickLockBoxResponseProto.Builder;
import com.lvl6.proto.EventProto.PickLockBoxResponseProto.PickLockBoxStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserLockBoxEventRetrieveUtils;
import com.lvl6.retrieveutils.UserLockBoxItemRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.LockBoxEventRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.LockBoxItemRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.InsertUtils;
import com.lvl6.utils.utilmethods.UpdateUtils;

@Component @DependsOn("gameServer") public class PickLockBoxController extends EventController {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  public PickLockBoxController() {
    numAllocatedThreads = 4;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new PickLockBoxRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_PICK_LOCK_BOX_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    PickLockBoxRequestProto reqProto = ((PickLockBoxRequestEvent)event).getPickLockBoxRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int lockBoxEventId = reqProto.getLockBoxEventId();
    PickLockBoxMethod method = reqProto.getMethod();
    Timestamp curTime = new Timestamp(reqProto.getClientTime());

    PickLockBoxResponseProto.Builder resBuilder = PickLockBoxResponseProto.newBuilder();
    resBuilder.setSender(senderProto);
    resBuilder.setLockBoxEventId(reqProto.getLockBoxEventId());
    resBuilder.setClientTime(reqProto.getClientTime());

    server.lockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());
    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());
      LockBoxEvent lockBoxEvent = LockBoxEventRetrieveUtils.getLockBoxEventForLockBoxEventId(lockBoxEventId);
      UserLockBoxEvent userEvent = UserLockBoxEventRetrieveUtils.getUserLockBoxEventForUserAndEventId(senderProto.getUserId(), lockBoxEventId);
      int previousSilver = 0;
      int previousGold = 0;

      boolean legitPick = checkLegitPick(resBuilder, user, lockBoxEvent, userEvent, method, userEvent.getNumLockBoxes(), curTime);
      boolean successfulPick = false;
      boolean hadAllItems = false;

      if (legitPick) {
        successfulPick = checkSuccess(method);
        resBuilder.setSuccess(successfulPick);

        if (successfulPick) {
          LockBoxItem item = chooseLockBoxItem(lockBoxEventId, user);

          if (item == null) {
            resBuilder.setStatus(PickLockBoxStatus.OTHER_FAIL);
            legitPick = false;
          } else {
            resBuilder.setItem(CreateInfoProtoUtils.createLockBoxItemProtoFromLockBoxItem(item));
            UserEquip equip = checkIfUserHasAllItems(lockBoxEvent, user, curTime);
            if (equip != null) {
              resBuilder.setPrizeEquip(CreateInfoProtoUtils.createFullUserEquipProtoFromUserEquip(equip));
              hadAllItems = true;
            }
          }
        }
      }

      PickLockBoxResponseEvent resEvent = new PickLockBoxResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setPickLockBoxResponseProto(resBuilder.build());
      server.writeEvent(resEvent);

      if (legitPick) {
        previousSilver = user.getCoins() + user.getVaultBalance();
        previousGold = user.getDiamonds();
        
        //stuff for tracking currency history
        Map<String, Integer> money = new HashMap<String, Integer>();
        List<String> rfcDetails = new ArrayList<String>();
        
        writeChangesToDB(user, method, lockBoxEvent, userEvent, successfulPick, hadAllItems, curTime, money, rfcDetails);

        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEventAndUpdateLeaderboard(user);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);
        
        writeToUserCurrencyHistory(user, curTime, money, previousSilver, previousGold, rfcDetails, successfulPick);
      }
    } catch (Exception e) {
      log.error("exception in PickLockBox processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());
    }
  }

  private boolean checkLegitPick(Builder resBuilder, User user, LockBoxEvent event, UserLockBoxEvent userEvent, PickLockBoxMethod method, int numBoxes, Timestamp curTime) {
    if (user == null || event == null) {
      resBuilder.setStatus(PickLockBoxStatus.OTHER_FAIL);
      log.error("user is " + user + ", event is " + event);
      return false;
    }
    if (!MiscMethods.checkClientTimeAroundApproximateNow(curTime)) {
      resBuilder.setStatus(PickLockBoxStatus.CLIENT_TOO_APART_FROM_SERVER_TIME);
      log.error("client time too apart of server time. client time =" + curTime + ", servertime~="
          + new Date());
      return false;
    }
    if (numBoxes <= 0) {
      resBuilder.setStatus(PickLockBoxStatus.NOT_ENOUGH_LOCKBOXES);
      log.error("not enought lock boxes. numBoxes = " + numBoxes);
      return false;
    }
    int diamondCost = method == PickLockBoxMethod.GOLD ? ControllerConstants.LOCK_BOXES__GOLD_COST_TO_PICK : 0;
    if (userEvent.getLastPickTime() != null && userEvent.getLastPickTime().getTime() + 60000*ControllerConstants.LOCK_BOXES__NUM_MINUTES_TO_REPICK > curTime.getTime())
      diamondCost += ControllerConstants.LOCK_BOXES__GOLD_COST_TO_RESET_PICK;
    int coinCost = method == PickLockBoxMethod.SILVER ? ControllerConstants.LOCK_BOXES__SILVER_COST_TO_PICK : 0;
    if (user.getDiamonds() < diamondCost || user.getCoins() < coinCost) {
      resBuilder.setStatus(PickLockBoxStatus.NOT_ENOUGH_CURRENCY);
      log.error("not enought currency. method = " + method + " diamonds = " + user.getDiamonds() + " coins = " + user.getCoins());
      return false;
    }
    if (event.getEndDate().getTime() < curTime.getTime() || event.getStartDate().getTime() > curTime.getTime()) {
      resBuilder.setStatus(PickLockBoxStatus.EVENT_IS_NOT_IN_PROGRESS);
      log.error("event is not in progress. start date = "+ event.getStartDate() + " end date = " + event.getEndDate() + " current time = " + curTime);
      return false;
    }

    resBuilder.setStatus(PickLockBoxStatus.SUCCESS);
    return true;
  }

  private boolean checkSuccess(PickLockBoxMethod method) {
    float chance = 0.f;
    if (method == PickLockBoxMethod.FREE) chance = ControllerConstants.LOCK_BOXES__FREE_CHANCE_TO_PICK;
    else if (method == PickLockBoxMethod.SILVER) chance = ControllerConstants.LOCK_BOXES__SILVER_CHANCE_TO_PICK;
    else if (method == PickLockBoxMethod.GOLD) chance = ControllerConstants.LOCK_BOXES__GOLD_CHANCE_TO_PICK;

    return Math.random() <= chance;
  }

  private LockBoxItem chooseLockBoxItem(int lockBoxEventId, User user) {
    List<LockBoxItem> items = LockBoxItemRetrieveUtils.getLockBoxItemsForLockBoxEvent(lockBoxEventId);
    double rand = Math.random();

    for (LockBoxItem item : items) {
      if (rand <= item.getChanceToUnlock()) {
        boolean inserted = UpdateUtils.get().incrementQuantityForLockBoxItem(user.getId(), item.getId(), 1);
        if (inserted) {
          return item;
        } else {
          return null;
        }
      } else {
        rand -= item.getChanceToUnlock();
      }
    }

    return null;
  }

  private UserEquip checkIfUserHasAllItems(LockBoxEvent lockBoxEvent,
      User user, Timestamp curTime) {
    List<LockBoxItem> items = LockBoxItemRetrieveUtils.getLockBoxItemsForLockBoxEvent(lockBoxEvent.getId());
    Map<Integer, Integer> map = UserLockBoxItemRetrieveUtils.getLockBoxItemIdsToQuantityForUser(user.getId());

    Map<Integer, Integer> forUpdate = new TreeMap<Integer, Integer>();
    boolean hasAllItems = true;
    for (LockBoxItem item : items) {
      Integer quantity = map.get(item.getId());

      if (quantity == null || quantity == 0) {
        hasAllItems = false;
        break;
      } else {
        forUpdate.put(item.getId(), quantity);
      }
    }

    if (hasAllItems) {
      boolean success = UpdateUtils.get().decrementLockBoxItemsForUser(forUpdate, user.getId(), 1);
      if (success) {
        int userEquipId = InsertUtils.get().insertUserEquip(user.getId(), lockBoxEvent.getPrizeEquipId(), 1,
            ControllerConstants.DEFAULT_USER_EQUIP_ENHANCEMENT_PERCENT, curTime,
            ControllerConstants.UER__PICK_LOCK_BOX);
        if (userEquipId > 0) {
          return new UserEquip(userEquipId, user.getId(), lockBoxEvent.getPrizeEquipId(), 1, 0);
        }
      }
    }

    return null;
  }

  private void writeChangesToDB(User user, PickLockBoxMethod method, LockBoxEvent event, UserLockBoxEvent userEvent, 
      boolean successfulPick, boolean hadAllItems, Timestamp curTime, Map<String, Integer> money, List<String> rfcDetails) {
    //explaining why user was charged (user currency history table)
    String rfcDetail = "";
    
    int diamondCost = 0;
    int coinCost = 0;
    
    if (userEvent.getLastPickTime() != null && userEvent.getLastPickTime().getTime() + 60000*ControllerConstants.LOCK_BOXES__NUM_MINUTES_TO_REPICK > curTime.getTime()) {
      diamondCost += ControllerConstants.LOCK_BOXES__GOLD_COST_TO_RESET_PICK;
      rfcDetail += " reset &";
    }

    if (PickLockBoxMethod.GOLD == method) {
      diamondCost += ControllerConstants.LOCK_BOXES__GOLD_COST_TO_PICK;
      rfcDetail += " pick method gold";
      
    } else if (PickLockBoxMethod.SILVER == method) {
      coinCost += ControllerConstants.LOCK_BOXES__SILVER_COST_TO_PICK;
      rfcDetail += " pick method silver";
      
    } else {
      rfcDetail += " pick method free";
    }
    
    boolean changeNumPostsInMarketplace = false;
    if (!user.updateRelativeDiamondsCoinsNumpostsinmarketplaceNaive(-diamondCost, -coinCost, 0, changeNumPostsInMarketplace)) {
      log.error("problem with updating users currency.");
      return;
    } else {
      if (0 != diamondCost) {
        money.put(MiscMethods.gold, -diamondCost);
      }
      if (0 != coinCost) {
        money.put(MiscMethods.silver, -coinCost);
      }
      //explaining why user was charged (user currency history table)
      rfcDetails.add(rfcDetail);
    }

    if (!UpdateUtils.get().decrementNumLockBoxesIncrementNumTimesCompletedForUser(event.getId(), user.getId(), successfulPick ? 1 : 0, hadAllItems, curTime)) {
      log.error("problem with decrementing users lock boxes for event "+event.getId());
      return;
    }
  }
  
  private void writeToUserCurrencyHistory(User aUser, Timestamp date, Map<String, Integer> money,
      int previousSilver, int previousGold, List<String> rfcDetails, boolean successfulPick) {
    Map<String, Integer> previousGoldSilver = new HashMap<String, Integer>();
    Map<String, String> reasonsForChanges = new HashMap<String, String>();
    String gold = MiscMethods.gold;
    String silver = MiscMethods.silver;
    String reasonForChange = ControllerConstants.UCHRFC__PICK_LOCKBOX;
    
    if (!rfcDetails.isEmpty()) {
      reasonForChange += rfcDetails.get(0);
    }
    
    if (successfulPick) {
      reasonForChange += " & success";
    } else {
      reasonForChange += " & fail";
    }

    previousGoldSilver.put(gold, previousGold);
    previousGoldSilver.put(silver, previousSilver);
    reasonsForChanges.put(gold, reasonForChange);
    reasonsForChanges.put(silver, reasonForChange);
    
    MiscMethods.writeToUserCurrencyOneUserGoldAndOrSilver(aUser, date, 
        money, previousGoldSilver, reasonsForChanges);
  }
}
