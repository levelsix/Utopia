package com.lvl6.server.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.BattleRequestEvent;
import com.lvl6.events.response.BattleResponseEvent;
import com.lvl6.info.Equipment;
import com.lvl6.info.User;
import com.lvl6.info.UserEquip;
import com.lvl6.info.Equipment.EquipType;
import com.lvl6.proto.EventProto.BattleRequestProto;
import com.lvl6.proto.EventProto.BattleResponseProto;
import com.lvl6.proto.EventProto.BattleResponseProto.BattleStatus;
import com.lvl6.proto.InfoProto.MinimumEquipProto;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.retrieveutils.UserEquipRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.EquipmentRetrieveUtils;

public class BattleController extends EventController {

  private static final int NOT_SET = -1;
  
  private static final int MAX_DAMAGE = 24;
  private static final int MIN_DAMAGE_DEALT_TO_LOSER = MAX_DAMAGE - 10;

  private static final int MIN_BATTLE_HEALTH_REQUIREMENT = MAX_DAMAGE+1;
  private static final int MIN_EXP_GAIN = 1;
  private static final int MAX_EXP_GAIN = 5;
  private static final String ATTACKER_FLAG = "attacker";
  private static final String DEFENDER_FLAG = "defender";

  /* FORMULA FOR CALCULATING PLAYER'S BATTLE STAT
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

  /* FORMULA FOR CALCULATING COIN TRANSFER
   * (int) Math.rint(Math.min(loser.getCoins() * (Math.random()+1)/A, loser.getLevel()*B)); 
   */
  private static final double A = 10;
  private static final double B = 75000;

  @Override
  protected void initController() {
    log.info("initController for " + this.getClass().toString());
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new BattleRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_BATTLE_EVENT;
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

    UserEquip lostEquip = null;
    int loserHealthLoss = NOT_SET;
    int winnerHealthLoss = NOT_SET;
    int expGained = NOT_SET;
    int lostCoins = NOT_SET;
    User winner = null;
    User loser = null;
    boolean legitBattle = false;
    
    if (isLegitBattle(attacker, defender, resBuilder)) {
      legitBattle = true;
      double attackerStat = computeStat(ATTACKER_FLAG, attacker, attackerEquips, equipmentIdsToEquipment);
      double defenderStat = computeStat(DEFENDER_FLAG, defender, defenderEquips, equipmentIdsToEquipment);

      if (attackerStat >= defenderStat) {
        resBuilder.setWinnerUserId(attacker.getId());
        winner = attacker;
        loser = defender;
        lostEquip = chooseLostEquip(defenderEquips, equipmentIdsToEquipment, defender.getLevel());
        if (lostEquip != null) {
          resBuilder.setEquipGained(MinimumEquipProto.newBuilder().
              setEquipId(lostEquip.getEquipId()).setName(equipmentIdsToEquipment.get(lostEquip.getEquipId()).getName()));
        }
      }
      else {
        resBuilder.setWinnerUserId(defender.getId());
        winner = defender;
        loser = attacker;
      }
      Random random = new Random();
      lostCoins = calculateLostCoins(loser, random);
      resBuilder.setCoinsGained(lostCoins);
      expGained = MIN_EXP_GAIN + random.nextInt(MAX_EXP_GAIN - MIN_EXP_GAIN + 1);
      resBuilder.setExpGained(expGained);
      resBuilder.setStatus(BattleStatus.SUCCESS);
      loserHealthLoss = MIN_DAMAGE_DEALT_TO_LOSER + random.nextInt(MAX_DAMAGE - MIN_DAMAGE_DEALT_TO_LOSER + 1);
      resBuilder.setLoserHealthLoss(loserHealthLoss);
      winnerHealthLoss = calculateWinnerHealthLoss(attackerStat, defenderStat, loserHealthLoss);
      resBuilder.setWinnerHealthLoss(winnerHealthLoss);
    } 

    BattleResponseProto resProto = resBuilder.build();
    BattleResponseEvent resEvent = new BattleResponseEvent();

    int[] recipients = { attacker.getId(), defender.getId()};

    resEvent.setRecipients(recipients);
    resEvent.setBattleResponseProto(resProto);

    server.writeEvent(resEvent);
    
    if (legitBattle) {
      if (lostEquip != null) {
        if (!loser.decrementUserEquip(lostEquip.getEquipId(), lostEquip.getQuantity(), 1)) {
          log.error("problem with decrementUserEquip in battle");
        }
        if (!winner.incrementUserEquip(lostEquip.getEquipId(), 1)) {
          log.error("problem with incrementUserEquip in battle");          
        }
      }
      
      /*
       * TODO: check for achievements- send new response, or package inside battles
       * 
       * TODO: check for levelup- send new response, or package inside battles
       * 
       * TODO: send notification to defender
       */
      
      if (winner == attacker) {
        attacker.updateRelativeStaminaExperienceCoinsHealthBattleswonBattleslost(-1, expGained, lostCoins, 
            winnerHealthLoss*-1, 1, 0);
        defender.updateRelativeStaminaExperienceCoinsHealthBattleswonBattleslost(0, 0, lostCoins*-1, 
            loserHealthLoss*-1, 0, 1);
      } else if (winner == defender) {
        attacker.updateRelativeStaminaExperienceCoinsHealthBattleswonBattleslost(-1, 0, lostCoins*-1, 
            loserHealthLoss*-1, 0, 1);
        defender.updateRelativeStaminaExperienceCoinsHealthBattleswonBattleslost(0, expGained, lostCoins, 
            winnerHealthLoss*-1, 1, 0);
      }
      
      /*
       * TODO: write to DB history
       * id, attackerId, defenderId, winnerId, winnerHealthLoss, loserHealthLoss, coinTransfer, lostEquipId, expGain, timestamp
       */
    }
  }

  
  private int calculateWinnerHealthLoss(double attackerStat, double defenderStat, int loserHealthLoss) {
    double val1 = Math.max(attackerStat, defenderStat);
    double val2 = Math.min(attackerStat, defenderStat);
    if (val1 != 0 || val2 != 0) {
      return Math.max(1, (int)Math.floor((val2/val1)*loserHealthLoss));
    }
    return 1;
  }

  private int calculateLostCoins(User loser, Random random) {
    return (int) Math.rint(Math.min(loser.getCoins() * (Math.random()+1)/A, loser.getLevel()*B));
  }

  /*
   * returns null if no item lost this time
   */
  private UserEquip chooseLostEquip(List<UserEquip> defenderEquips, Map<Integer, Equipment> equipmentIdsToEquipment, int level) {
    List <UserEquip> potentialLosses = new ArrayList<UserEquip>();
    for (UserEquip defenderEquip : defenderEquips) {
      Equipment equip = equipmentIdsToEquipment.get(defenderEquip.getEquipId());
      if (equip.getDiamondPrice() == Equipment.NOT_SET && equip.getMinLevel() < level) {
        double rand = Math.random();
        if (rand <= equip.getChanceOfLoss()) {
          potentialLosses.add(defenderEquip);
        }
      }
    }
    if (potentialLosses.size() > 0) {
      return potentialLosses.get((int)Math.rint(Math.random()*(potentialLosses.size()-1)));
    }
    return null;
  }

  private double computeStat(String flag, User user, List<UserEquip> userEquips, Map<Integer, Equipment> equipmentIdsToEquipment) {
    int skillStat = 0;
    if (flag.equals(ATTACKER_FLAG)) skillStat = user.getAttack();
    else if (flag.equals(DEFENDER_FLAG)) skillStat = user.getDefense();

    int armySize = user.getArmySize();
    int itemStat = computeItemStat(flag, userEquips, equipmentIdsToEquipment, user);

    double lowerBound = X*(armySize*skillStat + itemStat/Z);
    double upperBound = Y*(armySize*skillStat + itemStat/Z);

    return lowerBound + Math.random()*(upperBound-lowerBound);
  }

  private int computeItemStat(String flag, List<UserEquip> userEquips, final Map<Integer, Equipment> equipmentIdsToEquipment, User user) {
    sortUserEquips(userEquips, equipmentIdsToEquipment, flag);
    Map<EquipType, Integer> numUsedForEquipTypes = new HashMap<EquipType, Integer>();

    int itemStat = 0;
    int totalItemsUsed = 0;
    int armySize = user.getArmySize();
    final int maxTotalItems = armySize * EquipType.values().length;
    
    for (UserEquip ue : userEquips) {
      Equipment equip = equipmentIdsToEquipment.get(ue.getEquipId());
      if (equip.getMinLevel() > user.getLevel()) {
        continue;
      }
      
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
