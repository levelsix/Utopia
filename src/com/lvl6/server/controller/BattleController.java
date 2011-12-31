package com.lvl6.server.controller;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lvl6.events.BattleRequestEvent;
import com.lvl6.events.BattleResponseEvent;
import com.lvl6.events.RequestEvent;
import com.lvl6.info.Equipment;
import com.lvl6.info.User;
import com.lvl6.info.UserEquip;
import com.lvl6.info.Equipment.EquipType;
import com.lvl6.properties.EventProtocol;
import com.lvl6.proto.EventProto.BattleRequestProto;
import com.lvl6.proto.EventProto.BattleResponseProto;
import com.lvl6.proto.EventProto.BattleResponseProto.BattleStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.retrieveutils.EquipmentRetrieveUtils;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.retrieveutils.UserEquipRetrieveUtils;

public class BattleController extends EventController {

  private static final int MAX_DAMAGE = 24;
  private static final int MIN_BATTLE_HEALTH_REQUIREMENT = MAX_DAMAGE+1;
  private static final String ATTACKER_FLAG = "attacker";
  private static final String DEFENDER_FLAG = "defender";

  /*
  Let S = Attack or Defense skill points, based on whether the user is the attacker or defender
  Let I = The total attack/defense of the items used in the battle, based on whether the user is the attacker or defender
  Let A = The user’s agency size
  Let F = The final combined stat (attack or defense)
  Then F = RAND(X * (A * S + I / Z), Y * (A * S + I / Z))
  To put it into words, we take (skill points times agency size) and add (total item stats divided by Z), and then multiply by X and Y and return a random number between those two totals.
  Note that the S and I values are already passed into the computeStat() function and the function should return F. Note also that A (agency size) should be passed into computeStat() so the function header needs to be adjusted, as do the two calls to computeStat() in backend/attackplayer.php.
   */
  private static final double X = .9;
  private static final double Y = 1.1;
  private static final double Z = 4;


  @Override
  protected void initController() {
    log.info("initController for " + this.getClass().toString());
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new BattleRequestEvent();
  }

  @Override
  public byte getEventType() {
    return EventProtocol.C_PVP_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) {
    BattleRequestProto reqProto = ((BattleRequestEvent)event).getBattleRequestProto();

    MinimumUserProto attackerProto = reqProto.getAttacker();
    MinimumUserProto defenderProto = reqProto.getDefender();

    Map<Integer, Equipment> equipmentIdsToEquipment = EquipmentRetrieveUtils.getAllEquipmentIdsToEquipment();

    User attacker = UserRetrieveUtils.getUserById(attackerProto.getUserId());
    List<UserEquip> attackerEquips = UserEquipRetrieveUtils.getUserEquipsForUser(attacker.getId());

    User defender = UserRetrieveUtils.getUserById(defenderProto.getUserId());
    List<UserEquip> defenderEquips = UserEquipRetrieveUtils.getUserEquipsForUser(defender.getId());

    BattleResponseProto.Builder resBuilder = BattleResponseProto.newBuilder();

    resBuilder.setAttacker(attackerProto);
    resBuilder.setDefender(defenderProto);

    if (isLegitBattle(attacker, defender, resBuilder)) {
      double attackerStat = computeStat(ATTACKER_FLAG, attacker, attackerEquips, equipmentIdsToEquipment);
      double defenderStat = computeStat(DEFENDER_FLAG, defender, defenderEquips, equipmentIdsToEquipment);

      User winner;
      User loser;
      if (attackerStat >= defenderStat) {
        resBuilder.setWinnerUserId(attacker.getId());
        winner = attacker;
        loser = defender;
        //TODO: impl defender losing item (diamond price != null and != 0) and winner getting it
        /*
        - defender losers lose items (better items have a lower chance of being stolen) to the attacker winner
        - only items with min_level below the player's level and aren't diamond price can be stolen
         */

      }
      else {
        resBuilder.setWinnerUserId(defender.getId());
        winner = defender;
        loser = attacker;
      }
      //TODO: impl the winner getting money from the loser
      //TODO: impl the winner getting experience

      //TODO: impl- set message statuses
      resBuilder.setStatus(BattleStatus.SUCCESS);
    } 

    BattleResponseProto resProto = resBuilder.build();
    BattleResponseEvent resEvent = new BattleResponseEvent();

    int[] recipients = { attacker.getId(), defender.getId()};

    resEvent.setRecipients(recipients);
    resEvent.setBattleResponseProto(resProto);

    server.writeEvent(resEvent);

    //TODO: write to db battle history

  }

  private double computeStat(String flag, User user, List<UserEquip> userEquips, Map<Integer, Equipment> equipmentIdsToEquipment) {
    int skillStat = 0;
    if (flag.equals(ATTACKER_FLAG)) skillStat = user.getAttack();
    else if (flag.equals(DEFENDER_FLAG)) skillStat = user.getDefense();

    int armySize = user.getArmySize();
    int itemStat = computeItemStat(flag, userEquips, equipmentIdsToEquipment, user.getArmySize());

    double lowerBound = X*(armySize*skillStat + itemStat/Z);
    double upperBound = Y*(armySize*skillStat + itemStat/Z);

    return lowerBound + Math.random()*(upperBound-lowerBound);
  }

  private int computeItemStat(String flag, List<UserEquip> userEquips, final Map<Integer, Equipment> equipmentIdsToEquipment, int armySize) {
    sortUserEquips(userEquips, equipmentIdsToEquipment, flag);
    Map<EquipType, Integer> numUsedForEquipTypes = new HashMap<EquipType, Integer>();

    int itemStat = 0;
    int totalItemsUsed = 0;
    final int maxTotalItems = armySize * EquipType.values().length;
    
    for (UserEquip ue : userEquips) {
      Equipment equip = equipmentIdsToEquipment.get(ue.getEquipId());
      
      int numSlotsUsedForThisEquip = numUsedForEquipTypes.get(equip.getType());
      int numSlotsLeftForThisEquip = armySize - numSlotsUsedForThisEquip;
      if (numSlotsLeftForThisEquip <= 0) {
        if (numSlotsLeftForThisEquip < 0) log.error("problem with calculating item stats");
        continue;
      }
      int quantity = ue.getQuantity();
      
      if (quantity <= numSlotsLeftForThisEquip) {
        if (flag.equals(ATTACKER_FLAG)) itemStat += quantity * equip.getAttackBoost();
        else itemStat += quantity * equip.getDefenseBoost();
        numUsedForEquipTypes.put(equip.getType(), numSlotsUsedForThisEquip + quantity);
        totalItemsUsed += quantity;
      } else {              //last item for that equipType
        if (flag.equals(ATTACKER_FLAG)) itemStat += numSlotsLeftForThisEquip * equip.getAttackBoost();
        else itemStat += numSlotsLeftForThisEquip * equip.getDefenseBoost();
        numUsedForEquipTypes.put(equip.getType(), armySize);        
        totalItemsUsed += numSlotsLeftForThisEquip;
      }
      
      if (totalItemsUsed >= maxTotalItems) {
        if (totalItemsUsed > maxTotalItems) {
          log.error("extra item erroneously used in a battle");
        }
        break;
      }
    }
    return itemStat;
  }
  
  private void sortUserEquips(List<UserEquip> userEquips, final Map<Integer, Equipment> equipmentIdsToEquipment, String flag) {
    Comparator<UserEquip> equipComparator;
    if (flag.equals(ATTACKER_FLAG)) {
      equipComparator = new Comparator<UserEquip>(){
        public int compare(UserEquip ue1, UserEquip ue2) {
          int p1 = equipmentIdsToEquipment.get(ue1.getEquipId()).getAttackBoost();
          int p2 = equipmentIdsToEquipment.get(ue2.getEquipId()).getAttackBoost();
          return p2-p1;
        }
      };
    } else {
      equipComparator = new Comparator<UserEquip>(){
        public int compare(UserEquip ue1, UserEquip ue2) {
          int p1 = equipmentIdsToEquipment.get(ue1.getEquipId()).getDefenseBoost();
          int p2 = equipmentIdsToEquipment.get(ue2.getEquipId()).getDefenseBoost();
          return p2-p1;
        }
      };
    }
    Collections.sort(userEquips, equipComparator);
  }

  private boolean isLegitBattle(User attacker, User defender, BattleResponseProto.Builder builder) {
    if (attacker.getHealth() < MIN_BATTLE_HEALTH_REQUIREMENT) {
      builder.setStatus(BattleStatus.ATTACKER_NOT_ENOUGH_HEALTH);
    }
    if (defender.getHealth() < MIN_BATTLE_HEALTH_REQUIREMENT) {
      builder.setStatus(BattleStatus.DEFENDER_NOT_ENOUGH_HEALTH);
    }
    if (attacker.getStamina() <= 0) {
      builder.setStatus(BattleStatus.ATTACKER_NOT_ENOUGH_STAMINA);
    }
    return true;
  }
}
