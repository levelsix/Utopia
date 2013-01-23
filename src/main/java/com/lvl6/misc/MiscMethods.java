package com.lvl6.misc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.task.TaskExecutor;

import com.lvl6.events.response.ChangedClanTowerResponseEvent;
import com.lvl6.events.response.GeneralNotificationResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.AnimatedSpriteOffset;
import com.lvl6.info.BossEvent;
import com.lvl6.info.City;
import com.lvl6.info.Clan;
import com.lvl6.info.ClanTierLevel;
import com.lvl6.info.ClanTower;
import com.lvl6.info.Dialogue;
import com.lvl6.info.EquipEnhancement;
import com.lvl6.info.EquipEnhancementFeeder;
import com.lvl6.info.Equipment;
import com.lvl6.info.LeaderboardEvent;
import com.lvl6.info.LeaderboardEventReward;
import com.lvl6.info.Location;
import com.lvl6.info.LockBoxEvent;
import com.lvl6.info.MarketplacePost;
import com.lvl6.info.Task;
import com.lvl6.info.User;
import com.lvl6.info.UserClan;
import com.lvl6.info.UserEquip;
import com.lvl6.info.ValidLocationBox;
import com.lvl6.leaderboards.LeaderBoardUtil;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.properties.DBConstants;
import com.lvl6.properties.Globals;
import com.lvl6.properties.IAPValues;
import com.lvl6.properties.MDCKeys;
import com.lvl6.proto.EventProto.ChangedClanTowerResponseProto;
import com.lvl6.proto.EventProto.ChangedClanTowerResponseProto.ReasonForClanTowerChange;
import com.lvl6.proto.EventProto.GeneralNotificationResponseProto;
import com.lvl6.proto.EventProto.StartupResponseProto.StartupConstants;
import com.lvl6.proto.EventProto.StartupResponseProto.StartupConstants.BattleConstants;
import com.lvl6.proto.EventProto.StartupResponseProto.StartupConstants.BazaarMinLevelConstants;
import com.lvl6.proto.EventProto.StartupResponseProto.StartupConstants.CharacterModConstants;
import com.lvl6.proto.EventProto.StartupResponseProto.StartupConstants.ClanConstants;
import com.lvl6.proto.EventProto.StartupResponseProto.StartupConstants.DownloadableNibConstants;
import com.lvl6.proto.EventProto.StartupResponseProto.StartupConstants.ExpansionConstants;
import com.lvl6.proto.EventProto.StartupResponseProto.StartupConstants.ForgeConstants;
import com.lvl6.proto.EventProto.StartupResponseProto.StartupConstants.FormulaConstants;
import com.lvl6.proto.EventProto.StartupResponseProto.StartupConstants.GoldmineConstants;
import com.lvl6.proto.EventProto.StartupResponseProto.StartupConstants.KiipRewardConditions;
import com.lvl6.proto.EventProto.StartupResponseProto.StartupConstants.LeaderboardEventConstants;
import com.lvl6.proto.EventProto.StartupResponseProto.StartupConstants.LockBoxConstants;
import com.lvl6.proto.EventProto.StartupResponseProto.StartupConstants.ThreeCardMonteConstants;
import com.lvl6.proto.EventProto.UpdateClientUserResponseProto;
import com.lvl6.proto.InfoProto.BossEventProto;
import com.lvl6.proto.InfoProto.ClanTierLevelProto;
import com.lvl6.proto.InfoProto.ClanTowerProto;
import com.lvl6.proto.InfoProto.DefeatTypeJobProto.DefeatTypeJobEnemyType;
import com.lvl6.proto.InfoProto.DialogueProto.SpeechSegmentProto.DialogueSpeaker;
import com.lvl6.proto.InfoProto.EquipClassType;
import com.lvl6.proto.InfoProto.FullEquipProto.Rarity;
import com.lvl6.proto.InfoProto.InAppPurchasePackageProto;
import com.lvl6.proto.InfoProto.LeaderboardEventProto;
import com.lvl6.proto.InfoProto.LockBoxEventProto;
import com.lvl6.proto.InfoProto.UserType;
import com.lvl6.retrieveutils.ClanRetrieveUtils;
import com.lvl6.retrieveutils.ClanTowerRetrieveUtils;
import com.lvl6.retrieveutils.MarketplacePostRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.BoosterItemRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.BoosterPackRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.BossEventRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.BossRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.BossRewardRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.BuildStructJobRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.CityRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.ClanTierLevelRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.DefeatTypeJobRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.EquipmentRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.GoldSaleRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.LeaderboardEventRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.LeaderboardEventRewardRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.LevelsRequiredExperienceRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.LockBoxEventRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.LockBoxItemRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.NeutralCityElementsRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.PossessEquipJobRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.ProfanityRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.QuestRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.StructureRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.TaskEquipReqRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.TaskRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.ThreeCardMonteRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.UpgradeStructJobRetrieveUtils;
import com.lvl6.server.GameServer;
import com.lvl6.spring.AppContext;
import com.lvl6.utils.ConnectedPlayer;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.InsertUtils;
import com.lvl6.utils.utilmethods.UpdateUtils;

public class MiscMethods {


  private static final Logger log = LoggerFactory.getLogger(MiscMethods.class);
  public static final String clanTowersClanAttacked = "clanTowersClanAttacked";
  public static final String clanTowersClanOwned = "clanTowersClanOwned";
  public static final String gold = "gold";
  public static final String silver = "silver";
  
  public static int calculateMinutesToFinishForgeAttempt(Equipment equipment, int goalLevel) {
    return (int)
        (equipment.getMinutesToAttemptForgeBase()*Math.pow(ControllerConstants.FORGE_TIME_BASE_FOR_EXPONENTIAL_MULTIPLIER, goalLevel));
  }
  
  public static float calculateChanceOfSuccessForForge(Equipment equipment, int goalLevel) {
    return  (1-equipment.getChanceOfForgeFailureBase()) - 
        ((1-equipment.getChanceOfForgeFailureBase()) / (ControllerConstants.FORGE_MAX_EQUIP_LEVEL - 1)) * 
        (goalLevel-2);
  }

  public static int calculateDiamondCostToSpeedupForgeWaittime(Equipment equipment, int goalLevel) {
    return (int) Math.ceil(calculateMinutesToFinishForgeAttempt(equipment, goalLevel) / 
        ControllerConstants.FORGE_BASE_MINUTES_TO_ONE_GOLD);
  }

  public static UserEquip chooseUserEquipWithEquipIdPreferrablyNonEquippedIgnoreLevel(User user, List<UserEquip> userEquipsForEquipId) {
    if (user == null || userEquipsForEquipId == null || userEquipsForEquipId.size() <= 0) {
      return null;
    }
    if (userEquipsForEquipId.size() == 1) {
      return userEquipsForEquipId.get(0);
    }
    for (UserEquip ue : userEquipsForEquipId) {
      if (ue != null) {
        if (ue.getId() >= 1) {
          if (ue.getId() == user.getWeaponEquippedUserEquipId() || ue.getId() == user.getArmorEquippedUserEquipId()
              || ue.getId() == user.getAmuletEquippedUserEquipId()) {
            continue;
          } else {
            return ue;
          }
        }
      }
    }
    return null;
  }

  public static Dialogue createDialogue(String dialogueBlob) {
    if (dialogueBlob != null && dialogueBlob.length() > 0) { 
      StringTokenizer st = new StringTokenizer(dialogueBlob, "~");

      List<DialogueSpeaker> speakers = new ArrayList<DialogueSpeaker>();
      List<String> speakerTexts = new ArrayList<String>();

      try {
        while (st.hasMoreTokens()) {
          DialogueSpeaker speaker = DialogueSpeaker.valueOf(Integer.parseInt(st.nextToken()));
          String speakerText = st.nextToken();
          if (speakerText != null) {
            speakers.add(speaker);
            speakerTexts.add(speakerText);
          }
        }
      } catch (Exception e) {
        log.error("problem with creating dialogue object for this dialogueblob: {}", dialogueBlob, e);
      }
      return new Dialogue(speakers, speakerTexts);
    }
    return null;
  }

  /*
   * doesn't check if the user has the equip or not
   */
  public static boolean checkIfEquipIsEquippableOnUser(Equipment equip, User user) {
    if (equip == null || user == null) return false;
    EquipClassType userClass = MiscMethods.getClassTypeFromUserType(user.getType());
    if (user.getLevel() >= equip.getMinLevel() && 
        (userClass == equip.getClassType() || equip.getClassType() == EquipClassType.ALL_AMULET)) {
      return true;
    }
    return false;
  }

  public static String getIPOfPlayer(GameServer server, Integer playerId, String udid) {
    ConnectedPlayer player = null;
    if (playerId != null && playerId > 0) {
      player = server.getPlayerById(playerId); 
      if (player != null) {
        return player.getIp_connection_id();
      }
    }
    if (udid != null) {
      player = server.getPlayerByUdId(udid);
      if (player != null) {
        return player.getIp_connection_id();
      }
    }
    return null;
  }

  public static void purgeMDCProperties(){
    MDC.remove(MDCKeys.UDID);
    MDC.remove(MDCKeys.PLAYER_ID);
    MDC.remove(MDCKeys.IP);
  }

  public static void setMDCProperties(String udid, Integer playerId, String ip) {
    purgeMDCProperties();
    if (udid != null) MDC.put(MDCKeys.UDID, udid);
    if (ip != null) MDC.put(MDCKeys.IP, ip);
    if (playerId != null && playerId > 0) MDC.put(MDCKeys.PLAYER_ID.toString(), playerId.toString());
  }

  public static int calculateCoinsGivenToReferrer(User referrer) {
    return Math.min(ControllerConstants.USER_CREATE__MIN_COIN_REWARD_FOR_REFERRER, (int)(Math.ceil(
        (referrer.getVaultBalance() + referrer.getCoins()) * 
        ControllerConstants.USER_CREATE__PERCENTAGE_OF_COIN_WEALTH_GIVEN_TO_REFERRER)));
  }

  public static int calculateCoinsGainedFromTutorialTask(Task firstTaskToComplete) {
    return ((firstTaskToComplete.getMinCoinsGained() + firstTaskToComplete.getMaxCoinsGained())/2)
        * firstTaskToComplete.getNumForCompletion();
  }

  public static boolean unequipUserEquipIfEquipped(User user, UserEquip userEquip) {
    if (user.getWeaponEquippedUserEquipId() == userEquip.getId() || 
        user.getArmorEquippedUserEquipId() == userEquip.getId()||
        user.getAmuletEquippedUserEquipId() == userEquip.getId()) {
      return user.updateUnequip(user.getWeaponEquippedUserEquipId() == userEquip.getId(), 
          user.getArmorEquippedUserEquipId() == userEquip.getId(), user.getAmuletEquippedUserEquipId() == userEquip.getId());
    } 
    return true;
  }

  public static boolean checkClientTimeAroundApproximateNow(Timestamp clientTime) {
    if (clientTime.getTime() < new Date().getTime() + Globals.NUM_MINUTES_DIFFERENCE_LEEWAY_FOR_CLIENT_TIME*60000 && 
        clientTime.getTime() > new Date().getTime() - Globals.NUM_MINUTES_DIFFERENCE_LEEWAY_FOR_CLIENT_TIME*60000) {
      return true;
    }
    return false;
  }

  public static List<City> getCitiesAvailableForUserLevel(int userLevel) {
    List<City> availCities = new ArrayList<City>();
    Map<Integer, City> cities = CityRetrieveUtils.getCityIdsToCities();
    for (Integer cityId : cities.keySet()) {
      City city = cities.get(cityId);
      if (userLevel >= city.getMinLevel()) {
        availCities.add(city);
      }
    }
    return availCities;
  }

  public static int calculateMinutesToUpgradeForUserStruct(int minutesToUpgradeBase, int userStructLevel) {
    return Math.max(1, (int)(minutesToUpgradeBase * userStructLevel * ControllerConstants.MINUTES_TO_UPGRADE_FOR_NORM_STRUCT_MULTIPLIER));
  }

  public static int calculateIncomeGainedFromUserStruct(int structIncomeBase, int userStructLevel) {
    return Math.max(1, (int)(userStructLevel * structIncomeBase * ControllerConstants.INCOME_FROM_NORM_STRUCT_MULTIPLIER));
  }

  public static UpdateClientUserResponseEvent createUpdateClientUserResponseEventAndUpdateLeaderboard(User user) {
    try {
      if (!user.isFake()) {
        LeaderBoardUtil leaderboard = AppContext.getApplicationContext().getBean(LeaderBoardUtil.class);
        leaderboard.updateLeaderboardForUser(user);
      }
    } catch (Exception e) {
      log.error("Failed to update leaderboard.");
    }

    UpdateClientUserResponseEvent resEvent = new UpdateClientUserResponseEvent(user.getId());
    UpdateClientUserResponseProto resProto = UpdateClientUserResponseProto.newBuilder()
        .setSender(CreateInfoProtoUtils.createFullUserProtoFromUser(user))
        .setTimeOfUserUpdate(new Date().getTime()).build();
    resEvent.setUpdateClientUserResponseProto(resProto);
    return resEvent;
  }

  public static EquipClassType getClassTypeFromUserType(UserType userType) {
    if (userType == UserType.BAD_MAGE || userType == UserType.GOOD_MAGE) {
      return EquipClassType.MAGE;
    }
    if (userType == UserType.BAD_WARRIOR || userType == UserType.GOOD_WARRIOR) {
      return EquipClassType.WARRIOR;
    }
    if (userType == UserType.BAD_ARCHER || userType == UserType.GOOD_ARCHER) {
      return EquipClassType.ARCHER;
    }
    return null;
  }

  public static boolean checkIfGoodSide (UserType userType) {
    if (userType == UserType.GOOD_MAGE || userType == UserType.GOOD_WARRIOR || userType == UserType.GOOD_ARCHER) {
      return true;
    }
    return false;
  }

  public static int getRowCount(ResultSet set) {
    int rowCount;
    int currentRow;
    try {
      currentRow = set.getRow();
      rowCount = set.last() ? set.getRow() : 0; 
      if (currentRow == 0)          
        set.beforeFirst(); 
      else      
        set.absolute(currentRow);
      return rowCount;
    } catch (SQLException e) {
      e.printStackTrace();
      return -1;
    }     

  }

  public static Location getRandomValidLocation() {
    ValidLocationBox[] vlbs = ControllerConstants.USER_CREATE__VALIDATION_BOXES;
    if (vlbs != null && vlbs.length >= 1) {
      ValidLocationBox vlb = vlbs[new Random().nextInt(vlbs.length)];
      double latitude = vlb.getBotLeftY() + Math.random()*vlb.getHeight();
      double longitude = vlb.getBotLeftX() + Math.random()*vlb.getWidth();
      return new Location(latitude, longitude);
    }
    return new Location(-117.69765, 33.57793);    //ARBITRARY LAND SPOT
  }

  public static StartupConstants createStartupConstantsProto() {
    StartupConstants.Builder cb = StartupConstants.newBuilder()
        .setMaxLevelDifferenceForBattle(ControllerConstants.BATTLE__MAX_LEVEL_DIFFERENCE)
        .setArmoryXLength(ControllerConstants.ARMORY_XLENGTH).setArmoryYLength(ControllerConstants.ARMORY_YLENGTH)
        .setVaultXLength(ControllerConstants.VAULT_XLENGTH).setVaultYLength(ControllerConstants.VAULT_YLENGTH)        
        .setMarketplaceXLength(ControllerConstants.MARKETPLACE_XLENGTH).setMarketplaceYLength(ControllerConstants.MARKETPLACE_YLENGTH)        
        .setCarpenterXLength(ControllerConstants.CARPENTER_XLENGTH).setCarpenterYLength(ControllerConstants.CARPENTER_YLENGTH)        
        .setAviaryXLength(ControllerConstants.AVIARY_XLENGTH).setAviaryYLength(ControllerConstants.AVIARY_YLENGTH)
        .setAttackBaseGain(ControllerConstants.USE_SKILL_POINT__ATTACK_BASE_GAIN)
        .setDefenseBaseGain(ControllerConstants.USE_SKILL_POINT__DEFENSE_BASE_GAIN)
        .setEnergyBaseGain(ControllerConstants.USE_SKILL_POINT__ENERGY_BASE_GAIN)
        .setStaminaBaseGain(ControllerConstants.USE_SKILL_POINT__STAMINA_BASE_GAIN)
        .setAttackBaseCost(ControllerConstants.USE_SKILL_POINT__ATTACK_BASE_COST)
        .setDefenseBaseCost(ControllerConstants.USE_SKILL_POINT__DEFENSE_BASE_COST)
        .setEnergyBaseCost(ControllerConstants.USE_SKILL_POINT__ENERGY_BASE_COST)
        .setStaminaBaseCost(ControllerConstants.USE_SKILL_POINT__STAMINA_BASE_COST)
        .setSkillPointsGainedOnLevelup(ControllerConstants.LEVEL_UP__SKILL_POINTS_GAINED)
        .setCutOfVaultDepositTaken(ControllerConstants.VAULT__DEPOSIT_PERCENT_CUT)
        .setMaxLevelForStruct(ControllerConstants.UPGRADE_NORM_STRUCTURE__MAX_STRUCT_LEVEL)
        .setMaxNumOfSingleStruct(ControllerConstants.PURCHASE_NORM_STRUCTURE__MAX_NUM_OF_CERTAIN_STRUCTURE)
        .setPercentReturnedToUserForSellingNormStructure(ControllerConstants.SELL_NORM_STRUCTURE__PERCENT_RETURNED_TO_USER)
        .setMinutesToRefillAEnergy(ControllerConstants.REFILL_STAT_WAIT_COMPLETE__MINUTES_FOR_ENERGY)
        .setMinutesToRefillAStamina(ControllerConstants.REFILL_STAT_WAIT_COMPLETE__MINUTES_FOR_STAMINA)
        .setDiamondCostForFullEnergyRefill(ControllerConstants.REFILL_STAT_WITH_DIAMONDS__DIAMOND_COST_FOR_ENERGY_REFILL)
        .setDiamondCostForFullStaminaRefill(ControllerConstants.REFILL_STAT_WITH_DIAMONDS__DIAMOND_COST_FOR_STAMINA_REFILL)
        .setMaxNumberOfMarketplacePosts(ControllerConstants.POST_TO_MARKETPLACE__MAX_MARKETPLACE_POSTS_FROM_USER)
        .setPercentOfSellingCostTakenFromSellerOnMarketplacePurchase(ControllerConstants.PURCHASE_FROM_MARKETPLACE__PERCENT_CUT_OF_SELLING_PRICE_TAKEN)
        .setPercentOfSellingCostTakenFromSellerOnMarketplaceRetract(ControllerConstants.RETRACT_MARKETPLACE_POST__PERCENT_CUT_OF_SELLING_PRICE_TAKEN)
        .setNumDaysLongMarketplaceLicenseLastsFor(ControllerConstants.PURCHASE_MARKETPLACE_LICENSE__DAYS_FOR_LONG_LICENSE)
        .setNumDaysShortMarketplaceLicenseLastsFor(ControllerConstants.PURCHASE_MARKETPLACE_LICENSE__DAYS_FOR_SHORT_LICENSE)
        .setDiamondCostOfLongMarketplaceLicense(ControllerConstants.PURCHASE_MARKETPLACE_LICENSE__LONG_DIAMOND_COST)
        .setDiamondCostOfShortMarketplaceLicense(ControllerConstants.PURCHASE_MARKETPLACE_LICENSE__SHORT_DIAMOND_COST)
        .setNumDaysUntilFreeRetract(ControllerConstants.RETRACT_MARKETPLACE_POST__MIN_NUM_DAYS_UNTIL_FREE_TO_RETRACT_ITEM)
        .setMaxNumbersOfEnemiesToGenerateAtOnce(ControllerConstants.GENERATE_ATTACK_LIST__NUM_ENEMIES_TO_GENERATE_MAX)
        .setPercentReturnedToUserForSellingEquipInArmory(ControllerConstants.ARMORY__SELL_RATIO)
        .setMaxCityRank(ControllerConstants.TASK_ACTION__MAX_CITY_RANK)
        .setArmoryImgVerticalPixelOffset(ControllerConstants.ARMORY_IMG_VERTICAL_PIXEL_OFFSET)
        .setVaultImgVerticalPixelOffset(ControllerConstants.VAULT_IMG_VERTICAL_PIXEL_OFFSET)
        .setMarketplaceImgVerticalPixelOffset(ControllerConstants.MARKETPLACE_IMG_VERTICAL_PIXEL_OFFSET)
        .setAviaryImgVerticalPixelOffset(ControllerConstants.AVIARY_IMG_VERTICAL_PIXEL_OFFSET)
        .setCarpenterImgVerticalPixelOffset(ControllerConstants.CARPENTER_IMG_VERTICAL_PIXEL_OFFSET)
        .setMaxCharLengthForWallPost(ControllerConstants.POST_ON_PLAYER_WALL__MAX_CHAR_LENGTH)
        .setPlayerWallPostsRetrieveCap(ControllerConstants.RETRIEVE_PLAYER_WALL_POSTS__NUM_POSTS_CAP)
        .setMaxLevelForUser(ControllerConstants.LEVEL_UP__MAX_LEVEL_FOR_USER)
        .setAverageSizeOfLevelBracket(ControllerConstants.AVERAGE_SIZE_OF_LEVEL_BRACKET)
        .setHealthFormulaExponentBase(ControllerConstants.HEALTH__FORMULA_EXPONENT_BASE)
        .setLevelEquipBoostExponentBase(ControllerConstants.LEVEL_EQUIP_BOOST_EXPONENT_BASE)
        .setAdColonyVideosRequiredToRedeemDiamonds(ControllerConstants.EARN_FREE_DIAMONDS__NUM_VIDEOS_FOR_DIAMOND_REWARD)
        .setMinNameLength(ControllerConstants.USER_CREATE__MIN_NAME_LENGTH)
        .setMaxNameLength(ControllerConstants.USER_CREATE__MAX_NAME_LENGTH)
        .setSizeOfAttackList(ControllerConstants.SIZE_OF_ATTACK_LIST)
        .setHoursInAttackedByOneProtectionPeriod(ControllerConstants.BATTLE__HOURS_IN_ATTACKED_BY_ONE_PROTECTION_PERIOD)
        .setMaxNumTimesAttackedByOneInProtectionPeriod(ControllerConstants.BATTLE__MAX_NUM_TIMES_ATTACKED_BY_ONE_IN_PROTECTION_PERIOD)
        .setMinBattlesRequiredForKDRConsideration(ControllerConstants.LEADERBOARD__MIN_BATTLES_REQUIRED_FOR_KDR_CONSIDERATION)
        .setMaxLengthOfChatString(ControllerConstants.SEND_GROUP_CHAT__MAX_LENGTH_OF_CHAT_STRING)
        .setNumChatsGivenPerGroupChatPurchasePackage(ControllerConstants.PURCHASE_GROUP_CHAT__NUM_CHATS_GIVEN_FOR_PACKAGE)
        .setDiamondPriceForGroupChatPurchasePackage(ControllerConstants.PURCHASE_GROUP_CHAT__DIAMOND_PRICE_FOR_PACKAGE)
        .setNumHoursBeforeReshowingGoldSale(ControllerConstants.NUM_HOURS_BEFORE_RESHOWING_GOLD_SALE)
        .setNumHoursBeforeReshowingLockBox(ControllerConstants.NUM_HOURS_BEFORE_RESHOWING_LOCK_BOX)
        .setNumHoursBeforeReshowingBossEvent(ControllerConstants.NUM_HOURS_BEFORE_RESHOWING_BOSS_EVENT)
        .setLevelToShowRateUsPopup(ControllerConstants.LEVEL_TO_SHOW_RATE_US_POPUP)
        .setBossEventNumberOfAttacksUntilSuperAttack(ControllerConstants.BOSS_EVENT__NUMBER_OF_ATTACKS_UNTIL_SUPER_ATTACK)
        .setBossEventSuperAttack(ControllerConstants.BOSS_EVENT__SUPER_ATTACK)
        .setInitStamina(ControllerConstants.TUTORIAL__INIT_STAMINA)
        .setMinClanMembersToHoldClanTower(ControllerConstants.MIN_CLAN_MEMBERS_TO_HOLD_CLAN_TOWER);
 


    if (ControllerConstants.STARTUP__ANIMATED_SPRITE_OFFSETS != null) {
      for (int i = 0; i < ControllerConstants.STARTUP__ANIMATED_SPRITE_OFFSETS.length; i++) {
        AnimatedSpriteOffset aso = ControllerConstants.STARTUP__ANIMATED_SPRITE_OFFSETS[i];
        cb.addAnimatedSpriteOffsets(CreateInfoProtoUtils.createAnimatedSpriteOffsetProtoFromAnimatedSpriteOffset(aso));
      }
    }

    KiipRewardConditions.Builder krcb = KiipRewardConditions.newBuilder();

    int[] levelsThatTriggerKiipRewards = ControllerConstants.STARTUP__LEVELS_THAT_TRIGGER_KIIP_REWARDS;
    if (levelsThatTriggerKiipRewards != null) { 
      for (int i = 0; i < levelsThatTriggerKiipRewards.length; i++) {
        krcb.addLevelUpConditions(levelsThatTriggerKiipRewards[i]);
      }
    }

    int[] questsThatTriggerKiipRewardsOnRedeem = ControllerConstants.STARTUP__QUESTS_THAT_TRIGGER_KIIP_REWARDS_ON_REDEEM;
    if (questsThatTriggerKiipRewardsOnRedeem != null) { 
      for (int i = 0; i < questsThatTriggerKiipRewardsOnRedeem.length; i++) {
        krcb.addQuestRedeemConditions(questsThatTriggerKiipRewardsOnRedeem[i]);
      }
    }

    cb.setKiipRewardConditions(krcb.build());

    CharacterModConstants charModConstants = CharacterModConstants.newBuilder()
        .setDiamondCostToChangeCharacterType(ControllerConstants.CHARACTER_MOD__DIAMOND_COST_OF_CHANGE_CHARACTER_TYPE)
        .setDiamondCostToChangeName(ControllerConstants.CHARACTER_MOD__DIAMOND_COST_OF_CHANGE_NAME)
        .setDiamondCostToResetCharacter(ControllerConstants.CHARACTER_MOD__DIAMOND_COST_OF_NEW_PLAYER)
        .setDiamondCostToResetSkillPoints(ControllerConstants.CHARACTER_MOD__DIAMOND_COST_OF_RESET_SKILL_POINTS)
        .build();

    cb.setCharModConstants(charModConstants);

    FormulaConstants formulaConstants = FormulaConstants.newBuilder()
        .setMinutesToUpgradeForNormStructMultiplier(ControllerConstants.MINUTES_TO_UPGRADE_FOR_NORM_STRUCT_MULTIPLIER)
        .setIncomeFromNormStructMultiplier(ControllerConstants.INCOME_FROM_NORM_STRUCT_MULTIPLIER)
        .setUpgradeStructCoinCostExponentBase(ControllerConstants.UPGRADE_NORM_STRUCTURE__UPGRADE_STRUCT_COIN_COST_EXPONENT_BASE)
        .setUpgradeStructDiamondCostExponentBase(ControllerConstants.UPGRADE_NORM_STRUCTURE__UPGRADE_STRUCT_DIAMOND_COST_EXPONENT_BASE)
        .setDiamondCostForInstantUpgradeMultiplier(ControllerConstants.FINISH_NORM_STRUCT_WAITTIME_WITH_DIAMONDS__DIAMOND_COST_FOR_INSTANT_UPGRADE_MULTIPLIER)
        .build();

    cb.setFormulaConstants(formulaConstants);

    ClanConstants clanConstants = ClanConstants.newBuilder()
        .setMaxCharLengthForClanDescription(ControllerConstants.CREATE_CLAN__MAX_CHAR_LENGTH_FOR_CLAN_DESCRIPTION)
        .setMaxCharLengthForClanName(ControllerConstants.CREATE_CLAN__MAX_CHAR_LENGTH_FOR_CLAN_NAME)
        .setDiamondPriceToCreateClan(ControllerConstants.CREATE_CLAN__DIAMOND_PRICE_TO_CREATE_CLAN)
        .setMaxCharLengthForClanTag(ControllerConstants.CREATE_CLAN__MAX_CHAR_LENGTH_FOR_CLAN_TAG)
        .build();

    cb.setClanConstants(clanConstants);

    ForgeConstants forgeConstants = ForgeConstants.newBuilder()
        .setForgeTimeBaseForExponentialMultiplier(ControllerConstants.FORGE_TIME_BASE_FOR_EXPONENTIAL_MULTIPLIER)
        .setForgeMinDiamondCostForGuarantee(ControllerConstants.FORGE_MIN_DIAMOND_COST_FOR_GUARANTEE)
        .setForgeDiamondCostForGuaranteeExponentialMultiplier(ControllerConstants.FORGE_DIAMOND_COST_FOR_GUARANTEE_EXPONENTIAL_MULTIPLIER)
        .setForgeBaseMinutesToOneGold(ControllerConstants.FORGE_BASE_MINUTES_TO_ONE_GOLD)
        .setForgeMaxEquipLevel(ControllerConstants.FORGE_MAX_EQUIP_LEVEL).build();

    cb.setForgeConstants(forgeConstants);

    BattleConstants battleConstants = BattleConstants.newBuilder()
        .setLocationBarMax(ControllerConstants.BATTLE_LOCATION_BAR_MAX)
        .setBattleWeightGivenToAttackStat(ControllerConstants.BATTLE_WEIGHT_GIVEN_TO_ATTACK_STAT)
        .setBattleWeightGivenToAttackEquipSum(ControllerConstants.BATTLE_WEIGHT_GIVEN_TO_ATTACK_EQUIP_SUM)
        .setBattleWeightGivenToDefenseStat(ControllerConstants.BATTLE_WEIGHT_GIVEN_TO_DEFENSE_STAT)
        .setBattleWeightGivenToDefenseEquipSum(ControllerConstants.BATTLE_WEIGHT_GIVEN_TO_DEFENSE_EQUIP_SUM)
        .setBattleWeightGivenToLevel(ControllerConstants.BATTLE_WEIGHT_GIVEN_TO_LEVEL)
        .setBattlePerfectPercentThreshold(ControllerConstants.BATTLE_PERFECT_PERCENT_THRESHOLD)
        .setBattleGreatPercentThreshold(ControllerConstants.BATTLE_GREAT_PERCENT_THRESHOLD)
        .setBattleGoodPercentThreshold(ControllerConstants.BATTLE_GOOD_PERCENT_THRESHOLD)
        .setBattlePerfectMultiplier(ControllerConstants.BATTLE_PERFECT_MULTIPLIER)
        .setBattleGreatMultiplier(ControllerConstants.BATTLE_GREAT_MULTIPLIER)
        .setBattleGoodMultiplier(ControllerConstants.BATTLE_GOOD_MULTIPLIER)
        .setBattleImbalancePercent(ControllerConstants.BATTLE_IMBALANCE_PERCENT)
        .setBattlePerfectLikelihood(ControllerConstants.BATTLE_PERFECT_LIKELIHOOD)
        .setBattleGreatLikelihood(ControllerConstants.BATTLE_GREAT_LIKELIHOOD)
        .setBattleGoodLikelihood(ControllerConstants.BATTLE_GOOD_LIKELIHOOD)
        .setBattleMissLikelihood(ControllerConstants.BATTLE_MISS_LIKELIHOOD)
        .setBattleHitAttackerPercentOfHealth(ControllerConstants.BATTLE__HIT_ATTACKER_PERCENT_OF_HEALTH)
        .setBattleHitDefenderPercentOfHealth(ControllerConstants.BATTLE__HIT_DEFENDER_PERCENT_OF_HEALTH)
        .setBattlePercentOfWeapon(ControllerConstants.BATTLE__PERCENT_OF_WEAPON)
        .setBattlePercentOfArmor(ControllerConstants.BATTLE__PERCENT_OF_ARMOR)
        .setBattlePercentOfAmulet(ControllerConstants.BATTLE__PERCENT_OF_AMULET)
        .setBattlePercentOfPlayerStats(ControllerConstants.BATTLE__PERCENT_OF_PLAYER_STATS)
        .setBattleAttackExpoMultiplier(ControllerConstants.BATTLE__ATTACK_EXPO_MULTIPLIER)
        .setBattlePercentOfEquipment(ControllerConstants.BATTLE__PERCENT_OF_EQUIPMENT)
        .setBattleIndividualEquipAttackCap(ControllerConstants.BATTLE__INDIVIDUAL_EQUIP_ATTACK_CAP)
        .build();

    cb = cb.setBattleConstants(battleConstants);

    GoldmineConstants gc = GoldmineConstants.newBuilder()
        .setNumHoursBeforeGoldmineRetrieval(ControllerConstants.GOLDMINE__NUM_HOURS_BEFORE_RETRIEVAL)
        .setNumHoursForGoldminePickup(ControllerConstants.GOLDMINE__NUM_HOURS_TO_PICK_UP)
        .setGoldAmountFromGoldminePickup(ControllerConstants.GOLDMINE__GOLD_AMOUNT_FROM_PICK_UP)
        .setGoldCostForGoldmineRestart(ControllerConstants.GOLDMINE__GOLD_COST_TO_RESTART)
        .build();

    cb = cb.setGoldmineConstants(gc);

    LockBoxConstants lbc = LockBoxConstants.newBuilder()
        .setFreeChanceToPickLockBox(ControllerConstants.LOCK_BOXES__FREE_CHANCE_TO_PICK)
        .setGoldChanceToPickLockBox(ControllerConstants.LOCK_BOXES__GOLD_CHANCE_TO_PICK)
        .setNumMinutesToRepickLockBox(ControllerConstants.LOCK_BOXES__NUM_MINUTES_TO_REPICK)
        .setGoldCostToPickLockBox(ControllerConstants.LOCK_BOXES__GOLD_COST_TO_PICK)
        .setGoldCostToResetPickLockBox(ControllerConstants.LOCK_BOXES__GOLD_COST_TO_RESET_PICK)
        .setSilverChanceToPickLockBox(ControllerConstants.LOCK_BOXES__SILVER_CHANCE_TO_PICK)
        .setSilverCostToPickLockBox(ControllerConstants.LOCK_BOXES__SILVER_COST_TO_PICK)
        .build();

    cb = cb.setLockBoxConstants(lbc);

    ExpansionConstants ec = ExpansionConstants.newBuilder()
        .setExpansionPurchaseCostConstant(ControllerConstants.PURCHASE_EXPANSION__COST_CONSTANT)
        .setExpansionPurchaseCostExponentBase(ControllerConstants.PURCHASE_EXPANSION__COST_EXPONENT_BASE)
        .setExpansionWaitCompleteBaseMinutesToOneGold(ControllerConstants.EXPANSION_WAIT_COMPLETE__BASE_MINUTES_TO_ONE_GOLD)
        .setExpansionWaitCompleteHourConstant(ControllerConstants.EXPANSION_WAIT_COMPLETE__HOUR_CONSTANT)
        .setExpansionWaitCompleteHourIncrementBase(ControllerConstants.EXPANSION_WAIT_COMPLETE__HOUR_INCREMENT_BASE)
        .build();

    cb = cb.setExpansionConstants(ec);

    ThreeCardMonteConstants tc = ThreeCardMonteConstants.newBuilder()
        .setDiamondCostToPlayThreeCardMonte(ControllerConstants.THREE_CARD_MONTE__DIAMOND_PRICE_TO_PLAY)
        .setMinLevelToDisplayThreeCardMonte(ControllerConstants.THREE_CARD_MONTE__MIN_LEVEL)
        .setBadMonteCardPercentageChance(ControllerConstants.THREE_CARD_MONTE__BAD_PERCENTAGE)
        .setMediumMonteCardPercentageChance(ControllerConstants.THREE_CARD_MONTE__MEDIUM_PERCENTAGE)
        .setGoodMonteCardPercentageChance(ControllerConstants.THREE_CARD_MONTE__GOOD_PERCENTAGE)
        .build();

    cb = cb.setThreeCardMonteConstants(tc);

    DownloadableNibConstants dnc = DownloadableNibConstants.newBuilder()
        .setThreeCardMonteNibName(ControllerConstants.NIB_NAME__THREE_CARD_MONTE)
        .setLockBoxNibName(ControllerConstants.NIB_NAME__LOCK_BOX)
        .setMapNibName(ControllerConstants.NIB_NAME__TRAVELING_MAP)
        .setGoldMineNibName(ControllerConstants.NIB_NAME__GOLD_MINE)
        .setExpansionNibName(ControllerConstants.NIB_NAME__EXPANSION)
        .setFiltersNibName(ControllerConstants.NIB_NAME__MARKET_FILTERS)
        .setBlacksmithNibName(ControllerConstants.NIB_NAME__BLACKSMITH)
        .setGoldShoppeNibName(ControllerConstants.NIB_NAME__GOLD_SHOPPE)
        .build();

    cb = cb.setDownloadableNibConstants(dnc);

    // For legacy purposes
    for (int i = 0; i < IAPValues.packageNames.size(); i++) {
      cb.addProductIds(IAPValues.packageNames.get(i));
      cb.addProductDiamondsGiven(IAPValues.packageGivenDiamonds.get(i));
    }
    
    for (String id : IAPValues.iapPackageNames) {
      InAppPurchasePackageProto.Builder iapb = InAppPurchasePackageProto.newBuilder();
      iapb.setPackageId(id);
      iapb.setImageName(IAPValues.getImageNameForPackageName(id));
      
      int diamondAmt = IAPValues.getDiamondsForPackageName(id);
      if (diamondAmt > 0) {
        iapb.setCurrencyAmount(diamondAmt);
        iapb.setIsGold(true);
      } else {
        int coinAmt = IAPValues.getCoinsForPackageName(id);
        iapb.setCurrencyAmount(coinAmt);
        iapb.setIsGold(false);
      }
      cb.addInAppPurchasePackages(iapb.build());
    }
    
    BazaarMinLevelConstants bmlc = BazaarMinLevelConstants.newBuilder()
        .setClanHouseMinLevel(ControllerConstants.STARTUP__CLAN_HOUSE_MIN_LEVEL)
        .setVaultMinLevel(ControllerConstants.STARTUP__VAULT_MIN_LEVEL)
        .setArmoryMinLevel(ControllerConstants.STARTUP__ARMORY_MIN_LEVEL)
        .setMarketplaceMinLevel(ControllerConstants.STARTUP__MARKETPLACE_MIN_LEVEL)
        .setBlacksmithMinLevel(ControllerConstants.STARTUP__BLACKSMITH_MIN_LEVEL)
        .setLeaderboardMinLevel(ControllerConstants.STARTUP__LEADERBOARD_MIN_LEVEL)
        .build();
    cb = cb.setMinLevelConstants(bmlc);
    
    LeaderboardEventConstants lec =LeaderboardEventConstants.newBuilder()
        .setWinsWeight(ControllerConstants.LEADERBOARD_EVENT__WINS_WEIGHT)
        .setLossesWeight(ControllerConstants.LEADERBOARD_EVENT__LOSSES_WEIGHT)
        .setFleesWeight(ControllerConstants.LEADERBOARD_EVENT__FLEES_WEIGHT)
        .setNumHoursToShowAfterEventEnd(ControllerConstants.LEADERBOARD_EVENT__NUM_HOURS_TO_SHOW_AFTER_EVENT_END)
        .build();
    
    cb = cb.setLeaderboardConstants(lec);
    
    cb = cb.setMaxEnhancementLevel(ControllerConstants.MAX_ENHANCEMENT_LEVEL);
    
    return cb.build();  
  }

  public static List<LockBoxEventProto> currentLockBoxEventsForUserType(UserType type) {
    Map<Integer, LockBoxEvent> events = LockBoxEventRetrieveUtils.getLockBoxEventIdsToLockBoxEvents();
    long curTime = new Date().getTime();
    List<LockBoxEventProto> toReturn = new ArrayList<LockBoxEventProto>();

    for (LockBoxEvent event : events.values()) {
      // Send all events that are not yet over
      if (event.getEndDate().getTime() > curTime) {
        toReturn.add(CreateInfoProtoUtils.createLockBoxEventProtoFromLockBoxEvent(event, type));
      }
    }
    return toReturn;
  }

  public static List<BossEventProto> currentBossEvents() {
    Map<Integer, BossEvent> events = BossEventRetrieveUtils.getIdsToBossEvents();
    long curTime = new Date().getTime();
    List<BossEventProto> toReturn = new ArrayList<BossEventProto>();

    for (BossEvent event : events.values()) {
      // Send all events that are not yet over
      if (event.getEndDate().getTime() > curTime) {
        toReturn.add(CreateInfoProtoUtils.createBossEventProtoFromBossEvent(event));
      }
    }
    return toReturn;
  }
  
  public static List<LeaderboardEventProto> currentLeaderboardEventProtos() {
    Map<Integer, LeaderboardEvent> idsToEvents = LeaderboardEventRetrieveUtils.getIdsToLeaderboardEvents();
    long curTime = (new Date()).getTime();
    List<Integer> activeEventIds = new ArrayList<Integer>();
    
    //return value
    List<LeaderboardEventProto> protos = new ArrayList<LeaderboardEventProto>();
    
    //get the ids of active leader board events
    for(LeaderboardEvent e : idsToEvents.values()) {
      if (e.getEndDate().getTime()+ControllerConstants.LEADERBOARD_EVENT__NUM_HOURS_TO_SHOW_AFTER_EVENT_END*3600000L > curTime) {
        activeEventIds.add(e.getId());
      }
    }
    
    //get all the rewards for all the current leaderboard events
    Map<Integer, List<LeaderboardEventReward>> eventIdsToRewards = 
        LeaderboardEventRewardRetrieveUtils.getLeaderboardEventRewardsForIds(activeEventIds);
    
    //create the protos
    for(Integer i: activeEventIds) {
      LeaderboardEvent e = idsToEvents.get(i);
      List<LeaderboardEventReward> rList = eventIdsToRewards.get(e.getId()); //rewards for the active event
      
      protos.add(CreateInfoProtoUtils.createLeaderboardEventProtoFromLeaderboardEvent(e, rList));
    }
    return protos;
  }

  public static void reloadAllRareChangeStaticData() {
    log.info("Reloading rare change static data");
    BuildStructJobRetrieveUtils.reload();
    CityRetrieveUtils.reload();
    DefeatTypeJobRetrieveUtils.reload();
    EquipmentRetrieveUtils.reload();
    QuestRetrieveUtils.reload();
    TaskEquipReqRetrieveUtils.reload();
    TaskRetrieveUtils.reload();
    UpgradeStructJobRetrieveUtils.reload();
    StructureRetrieveUtils.reload();
    PossessEquipJobRetrieveUtils.reload();
    LevelsRequiredExperienceRetrieveUtils.reload();
    NeutralCityElementsRetrieveUtils.reload(); 
    ThreeCardMonteRetrieveUtils.reload();
    BossRetrieveUtils.reload();
    LockBoxEventRetrieveUtils.reload();
    LockBoxItemRetrieveUtils.reload();
    GoldSaleRetrieveUtils.reload();
    ClanTierLevelRetrieveUtils.reload();
    BossEventRetrieveUtils.reload();
    BossRewardRetrieveUtils.reload();
    LeaderboardEventRetrieveUtils.reload();
    LeaderboardEventRewardRetrieveUtils.reload();
    ProfanityRetrieveUtils.reload();
    BoosterPackRetrieveUtils.reload();
    BoosterItemRetrieveUtils.reload();
  }

  public static UserType getUserTypeFromDefeatTypeJobUserType(
      DefeatTypeJobEnemyType enemyType) {
    switch (enemyType) {
    case BAD_ARCHER:
      return UserType.BAD_ARCHER;
    case BAD_WARRIOR:
      return UserType.BAD_WARRIOR;
    case BAD_MAGE:
      return UserType.BAD_MAGE;
    case GOOD_ARCHER:
      return UserType.GOOD_ARCHER;
    case GOOD_WARRIOR:
      return UserType.GOOD_WARRIOR;
    case GOOD_MAGE:
      return UserType.GOOD_MAGE;
    default:
      log.error("no usertype for this defeat type job enemy type: " + enemyType);
      return null;
    }
  }

  public static int chooseMysteryBoxEquip(User user) {
    int userLevelMin = user.getLevel()-ControllerConstants.STARTUP__DAILY_BONUS_RECEIVE_EQUIP_LEVEL_RANGE;
    int userLevelMax = user.getLevel()+ControllerConstants.STARTUP__DAILY_BONUS_RECEIVE_EQUIP_LEVEL_RANGE;
    double randItem = Math.random();
    double randSelection = Math.random();
    double totalPercentage = 0;
    int retEquipId = ControllerConstants.TUTORIAL__FIRST_DEFEAT_TYPE_JOB_BATTLE_AMULET_LOOT_EQUIP_ID;

    List<Equipment> allEquipment = EquipmentRetrieveUtils.getAllEquipmentForClassType(getClassTypeFromUserType(user.getType()));
    List<Equipment> commonEquips = new ArrayList<Equipment>();
    List<Equipment> uncommonEquips = new ArrayList<Equipment>();
    List<Equipment> rareEquips = new ArrayList<Equipment>();
    List<Equipment> epicEquips = new ArrayList<Equipment>();
    List<Equipment> legendaryEquips = new ArrayList<Equipment>();

    for (Equipment e:allEquipment) {
      if (e.getMinLevel()>=userLevelMin && e.getMinLevel()<=userLevelMax) {
        //the equipment is at the right level	
        if (e.getRarity().equals(Rarity.COMMON)) {
          commonEquips.add(e);
        } else if (e.getRarity().equals(Rarity.UNCOMMON)) {
          uncommonEquips.add(e);
        } else if (e.getRarity().equals(Rarity.RARE)) {
          rareEquips.add(e);
        } else if (e.getRarity().equals(Rarity.EPIC)) {
          epicEquips.add(e);
        } else if (e.getRarity().equals(Rarity.LEGENDARY)) {
          legendaryEquips.add(e);
        } else {
          log.error("ERROR! equipment " + e + " has no rarity");
        }
      }
    }
    if (randItem<=(totalPercentage+=ControllerConstants.STARTUP__DAILY_BONUS_PERCENTAGE_CHANCE_COMMON_EQUIP)) {
      if (commonEquips !=  null) {
        int selection = (int) randSelection*commonEquips.size();
        retEquipId = commonEquips.get(selection).getId();
      }
    } else if (randItem<=(totalPercentage+=ControllerConstants.STARTUP__DAILY_BONUS_PERCENTAGE_CHANCE_UNCOMMON_EQUIP)) {
      if (uncommonEquips != null) {
        int selection = (int) randSelection*uncommonEquips.size();	
        retEquipId = uncommonEquips.get(selection).getId();
      }
    } else if (randItem<=(totalPercentage+=ControllerConstants.STARTUP__DAILY_BONUS_PERCENTAGE_CHANCE_RARE_EQUIP)) {
      if (rareEquips != null) {
        int selection = (int) randSelection*rareEquips.size();	
        retEquipId = rareEquips.get(selection).getId();
      }
    } else if (randItem<=(totalPercentage+=ControllerConstants.STARTUP__DAILY_BONUS_PERCENTAGE_CHANCE_EPIC_EQUIP)) {
      if (epicEquips != null) {
        int selection = (int) randSelection*epicEquips.size();	
        retEquipId = epicEquips.get(selection).getId();
      }
    } else if (randItem<=(totalPercentage+=ControllerConstants.STARTUP__DAILY_BONUS_PERCENTAGE_CHANCE_LEGENDARY_EQUIP)) {
      if (legendaryEquips != null) {
        int selection = (int) randSelection*legendaryEquips.size();
        retEquipId = legendaryEquips.get(selection).getId();
      }
    } 

    return retEquipId;
  }

  /*
   * Returns true if the user's (short or long) marketplace license is still in effect
   */
  public static boolean validateMarketplaceLicense(User aUser, Timestamp timeActionBegan) {
    Date longMarketplaceLicenseTimeOfPurchase = aUser.getLastLongLicensePurchaseTime();
    Date shortMarketplaceLicenseTimeOfPurchase = aUser.getLastShortLicensePurchaseTime();

    boolean longLicenseValid = false;
    boolean shortLicenseValid = false;

    double daysToMilliseconds = 24 * 60 * 60 * 1000;

    double startTime = timeActionBegan.getTime();

    //check if long license valid
    if (null != longMarketplaceLicenseTimeOfPurchase) {
      //time long license was bought
      double timeLicensePurchased = longMarketplaceLicenseTimeOfPurchase.getTime();
      double timeLongLicenseIsEffective = 
          ControllerConstants.PURCHASE_MARKETPLACE_LICENSE__DAYS_FOR_LONG_LICENSE *
          daysToMilliseconds;
      double timeLongLicenseEnds = timeLicensePurchased + timeLongLicenseIsEffective;

      if(startTime < timeLongLicenseEnds) {
        longLicenseValid = true;
      }
    }

    //check if short license valid
    if (null != shortMarketplaceLicenseTimeOfPurchase) {
      //time short license was bought
      double timeLicensePurchased = shortMarketplaceLicenseTimeOfPurchase.getTime();
      double timeShortLicenseIsEffective = 
          ControllerConstants.PURCHASE_MARKETPLACE_LICENSE__DAYS_FOR_SHORT_LICENSE *
          daysToMilliseconds;
      double timeShortLicenseEnds = timeLicensePurchased + timeShortLicenseIsEffective;

      if(startTime < timeShortLicenseEnds) {
        shortLicenseValid = true;
      }
    }	  

    if(longLicenseValid || shortLicenseValid) {
      return true;
    } else {
      return false;
    }
  }

  public static List<ClanTierLevelProto> getAllClanTierLevelProtos() {
    ArrayList<ClanTierLevelProto> toRet = new ArrayList<ClanTierLevelProto>();
    Map<Integer, ClanTierLevel> l = ClanTierLevelRetrieveUtils.getAllClanTierLevels();
    for (ClanTierLevel t : l.values()) {
      toRet.add(CreateInfoProtoUtils.createClanTierLevelProtoFromClanTierLevel(t));
    }
    return toRet;
  }
  
  //The lock for clan_towers table must be acquired before calling this function.
  //Makes 7 db calls:
  //One to retrieve clan size
  //Two to retrieve the towers the clan owns and is attacking
  //Two to write to the clan_towers_history table: towers the clan owns and is attacking
  //Two to write to the clan_towers table
  //returns ids of clan towers that the clan owned and attacked
  public static Map<String, List<Integer>> updateClanTowersAfterClanSizeDecrease(Clan aClan) {
    int clanId = aClan.getId();

    //can be null if the clan was just deleted, otherwise a list of all members in the clan
    List<UserClan> userClanList = RetrieveUtils.userClanRetrieveUtils().getUserClanMembersInClan(clanId);

    int clanSize = 0;

    if(null == userClanList || userClanList.isEmpty()) {
      clanSize = 0;
    } else {
      clanSize = userClanList.size(); 
    }

    int minSize = ControllerConstants.MIN_CLAN_MEMBERS_TO_HOLD_CLAN_TOWER;

    if (clanSize < minSize) {
      //since member left,
      //need to see if clan loses the towers they own, making the attacker the new owner,
      //or make the tower owner-less; and making the towers they are attacking, attacker-less

      List<ClanTower> towersOwned = ClanTowerRetrieveUtils.getAllClanTowersWithSpecificOwnerAndOrAttackerId(
          clanId, ControllerConstants.NOT_SET, false);
      List<ClanTower> towersAttacked = ClanTowerRetrieveUtils.getAllClanTowersWithSpecificOwnerAndOrAttackerId(
          ControllerConstants.NOT_SET, clanId, false);

      //return value
      Map<String, List<Integer>> towersBeforeUpdate = new HashMap<String, List<Integer>>();

      if(null == towersOwned) {
        towersOwned = new ArrayList<ClanTower>();
      }
      if(null == towersAttacked) {
        towersAttacked = new ArrayList<ClanTower>();
      }

      //if the clan has towers do something.
      if(0 < towersOwned.size() || 0 < towersAttacked.size()) {
        List<Integer> ownedIds = new ArrayList<Integer>();
        List<Integer> attackedIds = new ArrayList<Integer>();
        for(ClanTower ct: towersOwned) {
          ownedIds.add(ct.getId());
        }
        for(ClanTower ct: towersAttacked) {
          attackedIds.add(ct.getId());
        }

        //update clan_towers_history table
        if(!UpdateUtils.get().updateTowerHistory(towersOwned, Notification.OWNER_NOT_ENOUGH_MEMBERS)) {
          log.error("Added more/less towers than the clan owned to clan_towers_history table, when clan " +
              "size decreased below the minimum limit. clan=" + aClan + " towersOwned=" + towersOwned);
        }
        if(!UpdateUtils.get().updateTowerHistory(towersAttacked, Notification.ATTACKER_NOT_ENOUGH_MEMBERS)) {
          log.error("Added more/less towers than the clan attacked to clan_towers_history table, when clan " +
              "size decreased below the minimum limit. clan=" + aClan + " towersAttacked=" + towersAttacked);
        }

        //update clan_towers table
        if(!UpdateUtils.get().resetClanTowerOwnerOrAttacker(ownedIds, true)) { //reset the towers where this clan is the owner
          log.error("reset more/less towers than the clan owned in clan_towers table. clan=" + 
              aClan + "towersOwned=" + towersOwned);
        }
        if(!UpdateUtils.get().resetClanTowerOwnerOrAttacker(attackedIds, false)) {//reset the towers where this clan is the attacker
          log.error("reset more/less towers than the clan attacked in clan_towers table. clan=" + 
              aClan + "towersAttacked=" + towersAttacked);
        }

        //return clan towers that changed
        towersBeforeUpdate.put(clanTowersClanOwned, ownedIds);
        towersBeforeUpdate.put(clanTowersClanAttacked, attackedIds);
      }
      return towersBeforeUpdate;
    }
    return null;

  }

  //returns the clan towers that changed
  public static void sendClanTowerWarNotEnoughMembersNotification(
      Map<Integer, ClanTower> clanTowerIdsToClanTowers, List<Integer> towersAttacked,
      List<Integer> towersOwned, Clan aClan, TaskExecutor executor, 
      Collection<ConnectedPlayer> onlinePlayers, GameServer server) {

    if(null != clanTowerIdsToClanTowers && !clanTowerIdsToClanTowers.isEmpty()) {

      List<Notification> notificationsToSend = new ArrayList<Notification>();
      //make notifications for the towers the clan was attacking
      boolean attackerWon = false;
      generateClanTowerNotEnoughMembersNotification(aClan, towersAttacked, clanTowerIdsToClanTowers, 
          notificationsToSend, attackerWon, onlinePlayers, server);

      //make notifications for the towers the clan owned
      attackerWon = true;
      generateClanTowerNotEnoughMembersNotification(aClan, towersOwned, clanTowerIdsToClanTowers,
          notificationsToSend, attackerWon, onlinePlayers, server);

      for(Notification n: notificationsToSend) {
        writeGlobalNotification(n, server);
      }
      return;
    }
    log.info("no towers changed");
    return;
  }

  private static void generateClanTowerNotEnoughMembersNotification(Clan aClan, List<Integer> towerIds, 
      Map<Integer, ClanTower> clanTowerIdsToClanTowers, List<Notification> notificationsToSend,
      boolean isTowerOwner, Collection<ConnectedPlayer> onlinePlayers, GameServer server) {

    //for each tower make a notification for it
    for(Integer towerId: towerIds) {
      ClanTower aTower = clanTowerIdsToClanTowers.get(towerId);
      String towerName = aTower.getTowerName();
      Notification clanTowerWarNotification = new Notification ();
      Clan losingClan;
      Clan winningClan;
      String losingClanName;
      String winningClanName;

      losingClan = aClan;
      winningClan = ClanRetrieveUtils.getClanWithId(aTower.getClanOwnerId());

      losingClanName = losingClan.getName();
      winningClanName = winningClan != null ? winningClan.getName() : null;
      clanTowerWarNotification.setAsClanTowerWarClanConceded(
          losingClanName, winningClanName, towerName);
      notificationsToSend.add(clanTowerWarNotification);
    }
  }

  //converts the ClanTower objects into ClanTowerProto objects,
  //then sends them all to the client
  public static void sendClanTowerProtosToClient(Collection<ClanTower> changedTowers,
      GameServer server, ReasonForClanTowerChange reason) {
    if(null != changedTowers && 0 < changedTowers.size()) {
      ArrayList<ClanTowerProto> toSend = new ArrayList<ClanTowerProto>();
      for(ClanTower tower: changedTowers) {
        ClanTowerProto towerProto = 
            CreateInfoProtoUtils.createClanTowerProtoFromClanTower(tower);
        toSend.add(towerProto);
      }

      ChangedClanTowerResponseProto.Builder t = ChangedClanTowerResponseProto.newBuilder();
      t.addAllClanTowers(toSend);
      t.setReason(reason);

      ChangedClanTowerResponseEvent e = new ChangedClanTowerResponseEvent(0);
      e.setChangedClanTowerResponseProto(t.build());

      server.writeGlobalEvent(e);
    }
  }

  public static void writeGlobalNotification(Notification n, GameServer server) {
    GeneralNotificationResponseProto.Builder notificationProto = 
        n.generateNotificationBuilder();

    GeneralNotificationResponseEvent aNotification = new GeneralNotificationResponseEvent(0);
    aNotification.setGeneralNotificationResponseProto(notificationProto.build());
    server.writeGlobalEvent(aNotification);
  }

  //Simple (inefficient) word by word censor. If a word appears in 
  //a blacklist then that word is replaced by a number of asterisks 
  //equal to the word's length, e.g. fuck => ****
  //Not sure whether to use String or StringBuilder, so going with latter.
  public static String censorUserInput(String userContent) {
    StringBuilder toReturn = new StringBuilder(userContent.length());
    Set<String> blackList = ProfanityRetrieveUtils.getAllProfanity();

    String[] words = userContent.split(" ");
    String space = " "; //split by space, need to add them back in
    String w = "";

    for(int i = 0; i < words.length; i++) {
      w = words[i];

      //if at the last word, don't add a space after "censoring" it
      if ((words.length - 1) == i) {
        space = "";
      }

      //the profanity table only holds lower case one word profanities
      if(blackList.contains(w.toLowerCase())) {
        toReturn.append(asteriskify(w) + space);
      } else {
        toReturn.append(w + space);
      }
    }
    
    return toReturn.toString();
  }

  //average length of word is 4 characters. So based on this, not using
  //StringBuilder
  public static String asteriskify(String wordToAskerify) {
    int len = wordToAskerify.length();
    String s = "";

    for(int i = 0; i < len; i++) {
      s += "*";
    }
    return s;
  }

  public static int getNumPostsInMarketPlaceForUser(int userId) {
    List<MarketplacePost> posts = MarketplacePostRetrieveUtils
        .getMostRecentActiveMarketplacePostsForPoster(
            ControllerConstants.POST_TO_MARKETPLACE__MAX_MARKETPLACE_POSTS_FROM_USER, 
            userId);
    return posts.size();
  }
  
  //gold silver change should be a signed number (positive or negative) and
  //this should be called after the user is updated
  public static void writeToUserCurrencyOneUserGoldAndSilver(
      User aUser, Timestamp date, Map<String,Integer> goldSilverChange, 
      Map<String, Integer> previousGoldSilver, String reasonForChange) {
    //try, catch is here just in case this blows up, not really necessary;
    try {
      int amount = 2;

      int userId = aUser.getId();
      List<Integer> userIds = new ArrayList<Integer>(Collections.nCopies(amount, userId));
      List<Timestamp> dates = new ArrayList<Timestamp>(Collections.nCopies(amount, date));
      List<Integer> areSilver = new ArrayList<Integer>(amount);
      //gold first then silver
      List<Integer> changesToCurrencies = new ArrayList<Integer>(amount);
      List<Integer> previousCurrencies = new ArrayList<Integer>(amount);
      List<String> reasonsForChanges = new ArrayList<String>(Collections.nCopies(amount, reasonForChange));

      areSilver.add(0); //gold
      areSilver.add(1); //silver

      int goldChange = goldSilverChange.get(gold);
      int silverChange = goldSilverChange.get(silver);
      changesToCurrencies.add(goldChange);
      changesToCurrencies.add(silverChange);

      int previousGold = 0;
      int previousSilver = 0;
      if(null == previousGoldSilver) {
        //difference instead of sum because of example:
        //u.gold = 10; change = -5 => u.gold = 5
        //previous_gold = 5 - -5 = 10
        previousGold = aUser.getDiamonds() - goldChange;
        previousSilver = aUser.getCoins() - silverChange;
      } else {
        previousGold = previousGoldSilver.get(gold);
        previousSilver = previousGoldSilver.get(silver);
      }
      previousCurrencies.add(previousGold);
      previousCurrencies.add(previousSilver);
      
      //using multiple rows because 2 entries: one for silver, other for gold
      int numInserted = InsertUtils.get().insertIntoUserCurrencyHistoryMultipleRows(userIds, dates, areSilver,
          changesToCurrencies, previousCurrencies, reasonsForChanges);
      log.info("Should be 2. Rows inserted into user_currency_history: " + numInserted);
    } catch(Exception e) {
      log.error("Maybe table's not there or duplicate keys? ", e);
    }
    
  }
  
  public static void writeToUserCurrencyOneUserGoldOrSilver(
      User aUser, Timestamp date, Map<String,Integer> goldSilverChange, 
      Map<String, Integer> previousGoldSilver, String reasonForChange) {
    try {
      //determine what changed, gold or silver
      Set<String> keySet = goldSilverChange.keySet();
      Object[] keyArray = keySet.toArray();
      String key = (String) keyArray[0];
      int currentCurrency = 0;

      //arguments to insertIntoUserCurrency
      int userId = aUser.getId();
      int isSilver = 0;
      int currencyChange = goldSilverChange.get(key);
      int currencyBefore = 0;
      
      if (0 == currencyChange) {
        return;//don't write a non change to history table to avoid bloat
      }
      
      if (key.equals(gold)) {
        currentCurrency = aUser.getDiamonds();
        
      } else if(key.equals(silver)) {
        currentCurrency = aUser.getCoins();
        isSilver = 1;
        
      } else {
        log.error("invalid key for map representing currency change. key=" + key);
        return;
      }
      
      if(null == previousGoldSilver || previousGoldSilver.isEmpty()) {
        currencyBefore = currentCurrency - currencyChange;
      } else {
        currencyBefore = previousGoldSilver.get(key);
      }
      
      int numInserted = InsertUtils.get().insertIntoUserCurrencyHistory(
          userId, date, isSilver, currencyChange, currencyBefore, reasonForChange);
      log.info("Should be 1. number or rows inserted=" + numInserted);
    } catch(Exception e) {
      log.error("null pointer exception?", e);
    }
  }
  
  //only previousGoldSilver can be null.
  public static void writeToUserCurrencyOneUserGoldAndOrSilver(
      User aUser, Timestamp date, Map<String,Integer> goldSilverChange, 
      Map<String, Integer> previousGoldSilver, String reasonForChange) {
    try {
      int amount = goldSilverChange.size();
      if(2 == amount) {
        writeToUserCurrencyOneUserGoldAndSilver(aUser, date, goldSilverChange, 
            previousGoldSilver, reasonForChange);
      } else if(1 == amount) {
        writeToUserCurrencyOneUserGoldOrSilver(aUser, date, goldSilverChange,
            previousGoldSilver, reasonForChange);
      }
    } catch(Exception e) {
      log.error("error updating user_curency_history; reasonForChange=" + reasonForChange, e);
    }
  }
  
  public static String shallowListToString(List aList) {
    StringBuilder returnValue = new StringBuilder();
    for(Object o : aList) {
      returnValue.append(" ");
      returnValue.append(o.toString()); 
    }
    return returnValue.toString();
  }
  
  public static String shallowMapToString(Map aMap) {
    StringBuilder returnValue = new StringBuilder();
    returnValue.append("[");
    for(Object key : aMap.keySet()) {
      returnValue.append(" ");
      returnValue.append(key);
      returnValue.append("=");
      returnValue.append(aMap.get(key).toString());
    }
    returnValue.append("]");
    return returnValue.toString();
  }
  
  public static void writeIntoDUEFE(UserEquip mainUserEquip, List<UserEquip> feederUserEquips,
      int enhancementId) {
    List<Map<String, Object>> newRows = new ArrayList<Map<String, Object>>();
    Map<String, Object> newRow = new HashMap<String, Object>();
    
    newRow.put(DBConstants.DUEFE__USER_EQUIP__ID, mainUserEquip.getId());
    newRow.put(DBConstants.DUEFE__USER_EQUIP__USER_ID, mainUserEquip.getUserId());
    newRow.put(DBConstants.DUEFE__USER_EQUIP__EQUIP_ID, mainUserEquip.getEquipId());
    newRow.put(DBConstants.DUEFE__USER_EQUIP__LEVEL, mainUserEquip.getLevel());
    newRow.put(DBConstants.DUEFE__USER_EQUIP__ENHANCEMENT_PERCENT, mainUserEquip.getEnhancementPercentage());
    newRow.put(DBConstants.DUEFE__IS_FEEDER, 0);
    newRow.put(DBConstants.DUEFE__EQUIP_ENHANCEMENT_ID, enhancementId);

    newRows.add(newRow);
    for(UserEquip ue : feederUserEquips) {
      Map<String, Object> newRow2 = new HashMap<String, Object>();
      newRow2.put(DBConstants.DUEFE__USER_EQUIP__ID, ue.getId());
      newRow2.put(DBConstants.DUEFE__USER_EQUIP__USER_ID, ue.getUserId());
      newRow2.put(DBConstants.DUEFE__USER_EQUIP__EQUIP_ID, ue.getEquipId());
      newRow2.put(DBConstants.DUEFE__USER_EQUIP__LEVEL, ue.getLevel());
      newRow2.put(DBConstants.DUEFE__USER_EQUIP__ENHANCEMENT_PERCENT, ue.getEnhancementPercentage());
      newRow2.put(DBConstants.DUEFE__IS_FEEDER, 1);
      
      newRows.add(newRow2);
    }
  }
  
  public static boolean isEquipAtMaxEnhancementLevel(UserEquip enhancingUserEquip) {
    return false;
  }
  
  public static int calculateEnhancementForEquip(EquipEnhancement mainEquip,
      List<EquipEnhancementFeeder> feederEquips) {
    
    return 0;
  }
  
  public static int calculateMinutesToFinishEnhancing(EquipEnhancement e, List<EquipEnhancementFeeder> feeder) {
    
    return 0;
  }
  
  public static int calculateCostToSpeedUpEnhancing(EquipEnhancement e, List<EquipEnhancementFeeder> feeder,
      Timestamp timeOfSpeedUp) {
    
    return 0;
  }
}
