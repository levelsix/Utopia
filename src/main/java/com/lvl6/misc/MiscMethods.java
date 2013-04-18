package com.lvl6.misc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.task.TaskExecutor;

import com.lvl6.events.response.ChangedClanTowerResponseEvent;
import com.lvl6.events.response.GeneralNotificationResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.AnimatedSpriteOffset;
import com.lvl6.info.BoosterItem;
import com.lvl6.info.BoosterPack;
import com.lvl6.info.BossEvent;
import com.lvl6.info.City;
import com.lvl6.info.Clan;
import com.lvl6.info.ClanTierLevel;
import com.lvl6.info.ClanTower;
import com.lvl6.info.Dialogue;
import com.lvl6.info.EquipEnhancement;
import com.lvl6.info.EquipEnhancementFeeder;
import com.lvl6.info.Equipment;
import com.lvl6.info.GoldSale;
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
import com.lvl6.proto.EventProto.StartupResponseProto.StartupConstants.BoosterPackConstants;
import com.lvl6.proto.EventProto.StartupResponseProto.StartupConstants.CharacterModConstants;
import com.lvl6.proto.EventProto.StartupResponseProto.StartupConstants.ClanConstants;
import com.lvl6.proto.EventProto.StartupResponseProto.StartupConstants.DownloadableNibConstants;
import com.lvl6.proto.EventProto.StartupResponseProto.StartupConstants.EnhancementConstants;
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
import com.lvl6.proto.InfoProto.FullEquipProto.EquipType;
import com.lvl6.proto.InfoProto.FullEquipProto.Rarity;
import com.lvl6.proto.InfoProto.GoldSaleProto;
import com.lvl6.proto.InfoProto.InAppPurchasePackageProto;
import com.lvl6.proto.InfoProto.LeaderboardEventProto;
import com.lvl6.proto.InfoProto.LockBoxEventProto;
import com.lvl6.proto.InfoProto.UserType;
import com.lvl6.retrieveutils.ClanRetrieveUtils;
import com.lvl6.retrieveutils.ClanTowerRetrieveUtils;
import com.lvl6.retrieveutils.MarketplacePostRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.BannedUserRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.BoosterItemRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.BoosterPackRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.BossEventRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.BossRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.BossRewardRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.BuildStructJobRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.CityRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.ClanTierLevelRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.DailyBonusRewardRetrieveUtils;
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
import com.lvl6.utils.DBConnection;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.InsertUtils;
import com.lvl6.utils.utilmethods.UpdateUtils;

public class MiscMethods {


  private static final Logger log = LoggerFactory.getLogger(MiscMethods.class);
  public static final String clanTowersClanAttacked = "clanTowersClanAttacked";
  public static final String clanTowersClanOwned = "clanTowersClanOwned";
  public static final String gold = "gold";
  public static final String silver = "silver";
  public static final String boosterPackId = "boosterPackId";
  public static final String coins = "coins";
  public static final String diamonds = "diamonds";

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
        (float)ControllerConstants.FORGE_BASE_MINUTES_TO_ONE_GOLD);
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
    int userEquipId = userEquip.getId();
    boolean isWeaponOne = user.getWeaponEquippedUserEquipId() == userEquipId;
    boolean isArmorOne = user.getArmorEquippedUserEquipId() == userEquipId; 
    boolean isAmuletOne = user.getAmuletEquippedUserEquipId() == userEquipId;
    //for players who have prestige
    boolean isWeaponTwo = user.getWeaponTwoEquippedUserEquipId() == userEquipId; 
    boolean isArmorTwo = user.getArmorTwoEquippedUserEquipId() == userEquipId;
    boolean isAmuletTwo = user.getAmuletTwoEquippedUserEquipId() == userEquipId;
    if ( isWeaponOne || isWeaponTwo || isArmorOne || isArmorTwo || isAmuletOne || isAmuletTwo) {
      return user.updateUnequip(isWeaponOne, isArmorOne, isAmuletOne, isWeaponTwo, isArmorTwo,
          isAmuletTwo);
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
        .setMinClanMembersToHoldClanTower(ControllerConstants.MIN_CLAN_MEMBERS_TO_HOLD_CLAN_TOWER)
        .setUseOldBattleFormula(ControllerConstants.STARTUP__USE_OLD_BATTLE_FORMULA);

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
        .setForgeMaxEquipLevel(ControllerConstants.FORGE_MAX_EQUIP_LEVEL)
        .setForgeMaxForgeSlots(ControllerConstants.FORGE__ADDITIONAL_MAX_FORGE_SLOTS)
        .setCostOfPurchasingSlotTwo(ControllerConstants.FORGE_COST_OF_PURCHASING_SLOT_TWO)
        .setCostOfPurchasingSlotThree(ControllerConstants.FORGE_COST_OF_PURCHASING_SLOT_THREE)
        .build();

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
        .setBattleEquipAndStatsWeight(ControllerConstants.BATTLE__EQUIP_AND_STATS_WEIGHT)
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
        .setBossEventNibName(ControllerConstants.NIB_NAME__BOSS_EVENT)
        .setDailyBonusNibName(ControllerConstants.NIB_NAME__DAILY_BONUS)
        .build();

    cb = cb.setDownloadableNibConstants(dnc);

    EnhancementConstants enc = EnhancementConstants.newBuilder()
        .setMaxEnhancementLevel(ControllerConstants.MAX_ENHANCEMENT_LEVEL)
        .setEnhanceLevelExponentBase(ControllerConstants.ENHANCEMENT__ENHANCE_LEVEL_EXPONENT_BASE)
        .setEnhancePercentPerLevel(ControllerConstants.ENHANCEMENT__PERCENTAGE_PER_LEVEL)
        .setEnhanceTimeConstantA(ControllerConstants.ENHANCEMENT__TIME_FORMULA_CONSTANT_A)
        .setEnhanceTimeConstantB(ControllerConstants.ENHANCEMENT__TIME_FORMULA_CONSTANT_B)
        .setEnhanceTimeConstantC(ControllerConstants.ENHANCEMENT__TIME_FORMULA_CONSTANT_C)
        .setEnhanceTimeConstantD(ControllerConstants.ENHANCEMENT__TIME_FORMULA_CONSTANT_D)
        .setEnhanceTimeConstantE(ControllerConstants.ENHANCEMENT__TIME_FORMULA_CONSTANT_E)
        .setEnhanceTimeConstantF(ControllerConstants.ENHANCEMENT__TIME_FORMULA_CONSTANT_F)
        .setEnhanceTimeConstantG(ControllerConstants.ENHANCEMENT__TIME_FORMULA_CONSTANT_G)
        .setEnhancePercentConstantA(ControllerConstants.ENHANCEMENT__PERCENT_FORMULA_CONSTANT_A)
        .setEnhancePercentConstantB(ControllerConstants.ENHANCEMENT__PERCENT_FORMULA_CONSTANT_B)
        .build();

    cb = cb.setEnhanceConstants(enc);

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
        .setEnhancingMinLevel(ControllerConstants.STARTUP__ENHANCING_MIN_LEVEL_TO_UNLOCK)
        .build();
    cb = cb.setMinLevelConstants(bmlc);

    LeaderboardEventConstants lec =LeaderboardEventConstants.newBuilder()
        .setWinsWeight(ControllerConstants.LEADERBOARD_EVENT__WINS_WEIGHT)
        .setLossesWeight(ControllerConstants.LEADERBOARD_EVENT__LOSSES_WEIGHT)
        .setFleesWeight(ControllerConstants.LEADERBOARD_EVENT__FLEES_WEIGHT)
        .setNumHoursToShowAfterEventEnd(ControllerConstants.LEADERBOARD_EVENT__NUM_HOURS_TO_SHOW_AFTER_EVENT_END)
        .build();
    cb = cb.setLeaderboardConstants(lec);
    
    BoosterPackConstants bpc = BoosterPackConstants.newBuilder()
        .setPurchaseOptionOneNumBoosterItems(ControllerConstants.BOOSTER_PACK__PURCHASE_OPTION_ONE_NUM_BOOSTER_ITEMS)
        .setPurchaseOptionTwoNumBoosterItems(ControllerConstants.BOOSTER_PACK__PURCHASE_OPTION_TWO_NUM_BOOSTER_ITEMS)
        .setInfoImageName(ControllerConstants.BOOSTER_PACK__INFO_IMAGE_NAME)
        .setNumTimesToBuyStarterPack(ControllerConstants.BOOSTER_PACK__NUM_TIMES_TO_BUY_STARTER_PACK)
        .setNumDaysToBuyStarterPack(ControllerConstants.BOOSTER_PACK__NUM_DAYS_TO_BUY_STARTER_PACK)
        .build();
    cb = cb.setBoosterPackConstants(bpc);

    cb.setQuestIdForFirstLossTutorial(ControllerConstants.STARTUP__QUEST_ID_FOR_FIRST_LOSS_TUTORIAL);
    List<Integer> questIdsGuaranteedWin = new ArrayList<Integer>();
    int[] questIdsForWin = ControllerConstants.STARTUP__QUEST_IDS_FOR_GUARANTEED_WIN; 
    for(int i = 0; i < questIdsForWin.length; i++) {
      questIdsGuaranteedWin.add(questIdsForWin[i]);
    }
    cb.addAllQuestIdsGuaranteedWin(questIdsGuaranteedWin);
    
    cb.setFbConnectRewardDiamonds(ControllerConstants.EARN_FREE_DIAMONDS__FB_CONNECT_REWARD);
    
    cb.setMaxNumTowersClanCanHold(ControllerConstants.CLAN_TOWER__MAX_NUM_TOWERS_CLAN_CAN_HOLD);
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
    Map<Integer, LeaderboardEvent> idsToEvents = LeaderboardEventRetrieveUtils.getIdsToLeaderboardEvents(false);
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
    BannedUserRetrieveUtils.reload();
    DailyBonusRewardRetrieveUtils.reload();
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

//  public static int chooseMysteryBoxEquip(User user) {
//    int userLevelMin = user.getLevel()-ControllerConstants.STARTUP__DAILY_BONUS_RECEIVE_EQUIP_LEVEL_RANGE;
//    int userLevelMax = user.getLevel()+ControllerConstants.STARTUP__DAILY_BONUS_RECEIVE_EQUIP_LEVEL_RANGE;
//    double randItem = Math.random();
//    double randSelection = Math.random();
//    double totalPercentage = 0;
//    int retEquipId = ControllerConstants.TUTORIAL__FAKE_QUEST_AMULET_LOOT_EQUIP_ID;
//
//    List<Equipment> allEquipment = EquipmentRetrieveUtils.getAllEquipmentForClassType(getClassTypeFromUserType(user.getType()));
//    List<Equipment> commonEquips = new ArrayList<Equipment>();
//    List<Equipment> uncommonEquips = new ArrayList<Equipment>();
//    List<Equipment> rareEquips = new ArrayList<Equipment>();
//    List<Equipment> epicEquips = new ArrayList<Equipment>();
//    List<Equipment> legendaryEquips = new ArrayList<Equipment>();
//
//    for (Equipment e:allEquipment) {
//      if (e.getMinLevel()>=userLevelMin && e.getMinLevel()<=userLevelMax) {
//        //the equipment is at the right level	
//        if (e.getRarity().equals(Rarity.COMMON)) {
//          commonEquips.add(e);
//        } else if (e.getRarity().equals(Rarity.UNCOMMON)) {
//          uncommonEquips.add(e);
//        } else if (e.getRarity().equals(Rarity.RARE)) {
//          rareEquips.add(e);
//        } else if (e.getRarity().equals(Rarity.EPIC)) {
//          epicEquips.add(e);
//        } else if (e.getRarity().equals(Rarity.LEGENDARY)) {
//          legendaryEquips.add(e);
//        } else {
//          log.error("ERROR! equipment " + e + " has no rarity");
//        }
//      }
//    }
//    if (randItem<=(totalPercentage+=ControllerConstants.STARTUP__DAILY_BONUS_PERCENTAGE_CHANCE_COMMON_EQUIP)) {
//      if (commonEquips !=  null) {
//        int selection = (int) randSelection*commonEquips.size();
//        retEquipId = commonEquips.get(selection).getId();
//      }
//    } else if (randItem<=(totalPercentage+=ControllerConstants.STARTUP__DAILY_BONUS_PERCENTAGE_CHANCE_UNCOMMON_EQUIP)) {
//      if (uncommonEquips != null) {
//        int selection = (int) randSelection*uncommonEquips.size();	
//        retEquipId = uncommonEquips.get(selection).getId();
//      }
//    } else if (randItem<=(totalPercentage+=ControllerConstants.STARTUP__DAILY_BONUS_PERCENTAGE_CHANCE_RARE_EQUIP)) {
//      if (rareEquips != null) {
//        int selection = (int) randSelection*rareEquips.size();	
//        retEquipId = rareEquips.get(selection).getId();
//      }
//    } else if (randItem<=(totalPercentage+=ControllerConstants.STARTUP__DAILY_BONUS_PERCENTAGE_CHANCE_EPIC_EQUIP)) {
//      if (epicEquips != null) {
//        int selection = (int) randSelection*epicEquips.size();	
//        retEquipId = epicEquips.get(selection).getId();
//      }
//    } else if (randItem<=(totalPercentage+=ControllerConstants.STARTUP__DAILY_BONUS_PERCENTAGE_CHANCE_LEGENDARY_EQUIP)) {
//      if (legendaryEquips != null) {
//        int selection = (int) randSelection*legendaryEquips.size();
//        retEquipId = legendaryEquips.get(selection).getId();
//      }
//    } 
//
//    return retEquipId;
//  }

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
        List<Integer> wOwnedList = new ArrayList<Integer>();
        List<Integer> wAttackedList = new ArrayList<Integer>();
        for(ClanTower ct: towersOwned) {
          ownedIds.add(ct.getId());
          wOwnedList.add(ct.getClanAttackerId());
        }
        for(ClanTower ct: towersAttacked) {
          attackedIds.add(ct.getId());
          wAttackedList.add(ct.getClanOwnerId());
        }

        //update clan_towers_history table
        if(!UpdateUtils.get().updateTowerHistory(towersOwned, Notification.OWNER_NOT_ENOUGH_MEMBERS, wOwnedList)) {
          log.error("Added more/less towers than the clan owned to clan_towers_history table, when clan " +
              "size decreased below the minimum limit. clan=" + aClan + " towersOwned=" + towersOwned);
        }
        if(!UpdateUtils.get().updateTowerHistory(towersAttacked, Notification.ATTACKER_NOT_ENOUGH_MEMBERS, wAttackedList)) {
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

  public static void sendClanTowerProtosToClient(Collection<ClanTower> changedTowers,
      GameServer server, ReasonForClanTowerChange reason, User attacker, User defender,
      boolean attackerWon, int pointsGained) {
    if(null != changedTowers && 0 < changedTowers.size()) {
      ArrayList<ClanTowerProto> toSend = new ArrayList<ClanTowerProto>();
      for(ClanTower tower: changedTowers) {
        ClanTowerProto towerProto = 
            CreateInfoProtoUtils.createClanTowerProtoFromClanTower(tower);
        toSend.add(towerProto);
      }

      ChangedClanTowerResponseProto.Builder t = ChangedClanTowerResponseProto.newBuilder();
      t.addAllClanTowers(toSend);
      t.setAttackerUser(CreateInfoProtoUtils.createMinimumUserProtoFromUser(attacker));
      t.setDefenderUser(CreateInfoProtoUtils.createMinimumUserProtoFromUser(defender));
      t.setAttackerWon(attackerWon);
      t.setPointsGained(pointsGained);
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

  public static void writeClanApnsNotification(Notification n, GameServer server, int clanId) {
    GeneralNotificationResponseProto.Builder notificationProto =
        n.generateNotificationBuilder();

    GeneralNotificationResponseEvent aNotification = new GeneralNotificationResponseEvent(0);
    aNotification.setGeneralNotificationResponseProto(notificationProto.build());
    server.writeApnsClanEvent(aNotification, clanId);
  }
  
  public static void writeNotificationToUser(Notification aNote, GameServer server, int userId) {
    GeneralNotificationResponseProto.Builder notificationProto =
        aNote.generateNotificationBuilder();
    GeneralNotificationResponseEvent aNotification =
        new GeneralNotificationResponseEvent(userId);
    aNotification.setGeneralNotificationResponseProto(notificationProto.build());
    
    server.writeAPNSNotificationOrEvent(aNotification);
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
      //get rid of all punctuation
      String wWithNoPunctuation = w.replaceAll("\\p{Punct}", "");
      
      //the profanity table only holds lower case one word profanities
      if(blackList.contains(wWithNoPunctuation.toLowerCase())) {
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

  public static void writeToUserCurrencyOneUserGoldAndSilver(
      User aUser, Timestamp date, Map<String,Integer> goldSilverChange, 
      Map<String, Integer> previousGoldSilver, Map<String, String> reasons) {
    //try, catch is here just in case this blows up, not really necessary;
    try {
      List<Integer> userIds = new ArrayList<Integer>();
      List<Timestamp> dates = new ArrayList<Timestamp>();
      List<Integer> areSilver = new ArrayList<Integer>();
      List<Integer> changesToCurrencies = new ArrayList<Integer>();
      List<Integer> previousCurrencies = new ArrayList<Integer>();
      List<Integer> currentCurrencies = new ArrayList<Integer>();
      List<String> reasonsForChanges = new ArrayList<String>();

      int userId = aUser.getId();
      int goldChange = goldSilverChange.get(gold);
      int silverChange = goldSilverChange.get(silver);
      int previousGold = 0;
      int previousSilver = 0;
      int currentGold = aUser.getDiamonds();
      //recording total silver user has, including the vault
      int currentSilver = aUser.getCoins() + aUser.getVaultBalance();

      //record gold change first
      if (0 < goldChange) {
        userIds.add(userId);
        dates.add(date);
        areSilver.add(0); //gold
        changesToCurrencies.add(goldChange);
        if(null == previousGoldSilver || previousGoldSilver.isEmpty()) {
          //difference instead of sum because of example:
          //(previous gold) u.gold = 10; 
          //change = -5 
          //current gold = 10 - 5 = 5
          //previous gold = currenty gold - change
          //previous_gold = 5 - -5 = 10
          previousGold = currentGold - goldChange;
        } else {
          previousGold = previousGoldSilver.get(gold);
        }

        previousCurrencies.add(previousGold);
        currentCurrencies.add(currentGold);
        reasonsForChanges.add(reasons.get(gold));
      }

      //record silver change next
      if (0 < silverChange) {
        userIds.add(userId);
        dates.add(date);
        areSilver.add(1); //silver
        changesToCurrencies.add(silverChange);
        if(null == previousGoldSilver || previousGoldSilver.isEmpty()) {
          previousSilver = currentSilver - silverChange;
        } else {
          previousSilver = previousGoldSilver.get(silver);
        }

        previousCurrencies.add(previousSilver);
        currentCurrencies.add(currentSilver);
        reasonsForChanges.add(reasons.get(silver));
      }

      //using multiple rows because could be 2 entries: one for silver, other for gold
      InsertUtils.get().insertIntoUserCurrencyHistoryMultipleRows(userIds, dates, areSilver,
          changesToCurrencies, previousCurrencies, currentCurrencies, reasonsForChanges);
    } catch(Exception e) {
      log.error("Maybe table's not there or duplicate keys? ", e);
    }
  }

  public static void writeToUserCurrencyOneUserGoldOrSilver(
      User aUser, Timestamp date, Map<String,Integer> goldSilverChange, 
      Map<String, Integer> previousGoldSilver, Map<String, String> reasons) {
    try {
      //determine what changed, gold or silver
      Set<String> keySet = goldSilverChange.keySet();
      Object[] keyArray = keySet.toArray();
      String key = (String) keyArray[0];

      //arguments to insertIntoUserCurrency
      int userId = aUser.getId();
      int isSilver = 0;
      int currencyChange = goldSilverChange.get(key);
      int previousCurrency = 0;
      int currentCurrency = 0;
      String reasonForChange = reasons.get(key);

      if (0 == currencyChange) {
        return;//don't write a non change to history table to avoid bloat
      }

      if (key.equals(gold)) {
        currentCurrency = aUser.getDiamonds();
      } else if(key.equals(silver)) {
        //record total silver, including vault
        currentCurrency = aUser.getCoins() + aUser.getVaultBalance();
        isSilver = 1;
      } else {
        log.error("invalid key for map representing currency change. key=" + key);
        return;
      }

      if(null == previousGoldSilver || previousGoldSilver.isEmpty()) {
        previousCurrency = currentCurrency - currencyChange;
      } else {
        previousCurrency = previousGoldSilver.get(key);
      }

      InsertUtils.get().insertIntoUserCurrencyHistory(
          userId, date, isSilver, currencyChange, previousCurrency, currentCurrency, reasonForChange);
    } catch(Exception e) {
      log.error("null pointer exception?", e);
    }
  }

  //goldSilverChange should represent how much user's silver and, or gold increased or decreased and
  //this should be called after the user is updated
  //only previousGoldSilver can be null.
  public static void writeToUserCurrencyOneUserGoldAndOrSilver(
      User aUser, Timestamp date, Map<String,Integer> goldSilverChange, 
      Map<String, Integer> previousGoldSilver, Map<String, String> reasonsForChanges) {
    try {
      int amount = goldSilverChange.size();
      if(2 == amount) {
        writeToUserCurrencyOneUserGoldAndSilver(aUser, date, goldSilverChange, 
            previousGoldSilver, reasonsForChanges);
      } else if(1 == amount) {
        writeToUserCurrencyOneUserGoldOrSilver(aUser, date, goldSilverChange,
            previousGoldSilver, reasonsForChanges);
      }
    } catch(Exception e) {
      log.error("error updating user_curency_history; reasonsForChanges=" + shallowMapToString(reasonsForChanges), e);
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
      int enhancementId, List<Integer> enhancementFeederIds) {
    String tableName = DBConstants.TABLE_DELETED_USER_EQUIPS_FOR_ENHANCING;
    try {
      log.info("writing into deleted user equips for enhancing");
      int numRows = 1 + feederUserEquips.size();

      Map<String, List<Object>> insertParams = new HashMap<String, List<Object>>();

      List<Object> userEquipIds = new ArrayList<Object>();
      List<Object> userIds = new ArrayList<Object>();
      List<Object> equipIds = new ArrayList<Object>();
      List<Object> levels = new ArrayList<Object>();
      List<Object> enhancementPercents = new ArrayList<Object>();
      List<Object> areFeeders = new ArrayList<Object>();
      List<Object> equipEnhancementIds = new ArrayList<Object>();

      userEquipIds.add(mainUserEquip.getId());
      userIds.add(mainUserEquip.getUserId());
      equipIds.add(mainUserEquip.getEquipId());
      levels.add(mainUserEquip.getLevel());
      enhancementPercents.add(mainUserEquip.getEnhancementPercentage());
      areFeeders.add(0);
      equipEnhancementIds.add(enhancementId);

      for(int i = 0; i < feederUserEquips.size(); i++) {
        UserEquip ue = feederUserEquips.get(i);
        userEquipIds.add(ue.getId());
        userIds.add(ue.getUserId());
        equipIds.add(ue.getEquipId());
        levels.add(ue.getLevel());
        enhancementPercents.add(ue.getEnhancementPercentage());
        areFeeders.add(1);
        
        int enhancementFeederId = enhancementFeederIds.get(i);
        equipEnhancementIds.add(enhancementFeederId);
      }

      insertParams.put(DBConstants.DUEFE__USER_EQUIP__ID, userEquipIds);
      insertParams.put(DBConstants.DUEFE__USER_EQUIP__USER_ID, userIds);
      insertParams.put(DBConstants.DUEFE__USER_EQUIP__EQUIP_ID, equipIds);
      insertParams.put(DBConstants.DUEFE__USER_EQUIP__LEVEL, levels);
      insertParams.put(DBConstants.DUEFE__USER_EQUIP__ENHANCEMENT_PERCENT, enhancementPercents);
      insertParams.put(DBConstants.DUEFE__IS_FEEDER, areFeeders);
      insertParams.put(DBConstants.DUEFE__EQUIP_ENHANCEMENT_ID, equipEnhancementIds);

      int numInserted = DBConnection.get().insertIntoTableMultipleRows(tableName, insertParams, numRows);
    } catch (Exception e) {
      log.error("could not write into " + tableName, e);
    }
  }

  public static boolean isEquipAtMaxEnhancementLevel(UserEquip enhancingUserEquip) {
    int currentEnhancementLevel = enhancingUserEquip.getEnhancementPercentage();
    int maxEnhancementLevel = ControllerConstants.MAX_ENHANCEMENT_LEVEL 
        * ControllerConstants.ENHANCEMENT__PERCENTAGE_PER_LEVEL;

    return currentEnhancementLevel >= maxEnhancementLevel;
  }

  public static int attackPowerForEquip(int equipId, int forgeLevel, int enhanceLevel) {
    Equipment eq = EquipmentRetrieveUtils.getEquipmentIdsToEquipment().get(equipId);
    double forge = Math.pow(ControllerConstants.LEVEL_EQUIP_BOOST_EXPONENT_BASE, forgeLevel-1);
    double enhance = Math.pow(ControllerConstants.ENHANCEMENT__ENHANCE_LEVEL_EXPONENT_BASE, enhanceLevel);

    int result = (int)Math.ceil(eq.getAttackBoost()*forge*enhance);
    //    log.info("attack="+result);
    return result;
  }

  public static int defensePowerForEquip(int equipId, int forgeLevel, int enhanceLevel) {
    Equipment eq = EquipmentRetrieveUtils.getEquipmentIdsToEquipment().get(equipId);
    double forge = Math.pow(ControllerConstants.LEVEL_EQUIP_BOOST_EXPONENT_BASE, forgeLevel-1);
    double enhance = Math.pow(ControllerConstants.ENHANCEMENT__ENHANCE_LEVEL_EXPONENT_BASE, enhanceLevel);

    int result = (int)Math.ceil(eq.getDefenseBoost()*forge*enhance);
    //    log.info("defense="+result);
    return result;
  }

  private static int totalMinutesToLevelUpEnhancementEquip(EquipEnhancement e) {
    Equipment eq = EquipmentRetrieveUtils.getEquipmentIdsToEquipment().get(e.getEquipId());
    double result = ControllerConstants.ENHANCEMENT__TIME_FORMULA_CONSTANT_A*Math.pow(e.getEquipLevel(), ControllerConstants.ENHANCEMENT__TIME_FORMULA_CONSTANT_B);
    result = Math.pow(result, (ControllerConstants.ENHANCEMENT__TIME_FORMULA_CONSTANT_C+ControllerConstants.ENHANCEMENT__TIME_FORMULA_CONSTANT_D*(eq.getRarity().getNumber()+1)));
    result *= Math.pow(ControllerConstants.ENHANCEMENT__TIME_FORMULA_CONSTANT_E, (eq.getMinLevel()/ControllerConstants.AVERAGE_SIZE_OF_LEVEL_BRACKET*ControllerConstants.ENHANCEMENT__TIME_FORMULA_CONSTANT_F));
    result *= Math.pow(ControllerConstants.ENHANCEMENT__TIME_FORMULA_CONSTANT_G, e.getEnhancementPercentage()/ControllerConstants.ENHANCEMENT__PERCENTAGE_PER_LEVEL+1);

    //    log.info("minutes="+result);
    return (int)Math.max(result, 1);
  }

  private static int calculateEnhancementForEquip(EquipEnhancement mainEquip, EquipEnhancementFeeder feederEquip) {
    int mainStats = attackPowerForEquip(mainEquip.getEquipId(), mainEquip.getEquipLevel(), mainEquip.getEnhancementPercentage()/ControllerConstants.ENHANCEMENT__PERCENTAGE_PER_LEVEL) +
        defensePowerForEquip(mainEquip.getEquipId(), mainEquip.getEquipLevel(), mainEquip.getEnhancementPercentage()/ControllerConstants.ENHANCEMENT__PERCENTAGE_PER_LEVEL);
    int feederStats = attackPowerForEquip(feederEquip.getEquipId(), feederEquip.getEquipLevel(), feederEquip.getEnhancementPercentageBeforeEnhancement()/ControllerConstants.ENHANCEMENT__PERCENTAGE_PER_LEVEL) +
        defensePowerForEquip(feederEquip.getEquipId(), feederEquip.getEquipLevel(), feederEquip.getEnhancementPercentageBeforeEnhancement()/ControllerConstants.ENHANCEMENT__PERCENTAGE_PER_LEVEL);
    int result = (int)((((float)feederStats)/mainStats)/(ControllerConstants.ENHANCEMENT__PERCENT_FORMULA_CONSTANT_A*
        Math.pow(ControllerConstants.ENHANCEMENT__PERCENT_FORMULA_CONSTANT_B, mainEquip.getEnhancementPercentage()/ControllerConstants.ENHANCEMENT__PERCENTAGE_PER_LEVEL+1))*
        ControllerConstants.ENHANCEMENT__PERCENTAGE_PER_LEVEL);

    //    log.info("percentage="+result);
    return result;
  }

  public static int calculateEnhancementForEquip(EquipEnhancement mainEquip,
      List<EquipEnhancementFeeder> feederEquips) {
    int totalChange = 0;
    for (EquipEnhancementFeeder f : feederEquips) {
      totalChange += calculateEnhancementForEquip(mainEquip, f);
    }

    int maxChange = (mainEquip.getEnhancementPercentage()/ControllerConstants.ENHANCEMENT__PERCENTAGE_PER_LEVEL+1)*ControllerConstants.ENHANCEMENT__PERCENTAGE_PER_LEVEL-mainEquip.getEnhancementPercentage();
    maxChange = Math.min(maxChange, ControllerConstants.MAX_ENHANCEMENT_LEVEL*ControllerConstants.ENHANCEMENT__PERCENTAGE_PER_LEVEL-mainEquip.getEnhancementPercentage());
    //    log.info("totalChange="+totalChange+" maxChange="+maxChange);
    return Math.min(maxChange, totalChange);
  }

  public static int calculateMinutesToFinishEnhancing(EquipEnhancement mainEquip, List<EquipEnhancementFeeder> feederEquips) {
    int pChange = calculateEnhancementForEquip(mainEquip, feederEquips);
    float percent = ((float)pChange)/ControllerConstants.ENHANCEMENT__PERCENTAGE_PER_LEVEL;
    int totalTime = totalMinutesToLevelUpEnhancementEquip(mainEquip);
    int result = (int)Math.ceil(percent*totalTime);

    //    log.info("time for enhance="+result);
    return result;
  }

  public static int calculateCostToSpeedUpEnhancing(EquipEnhancement e, List<EquipEnhancementFeeder> feeder,
      Timestamp timeOfSpeedUp) {
    int mins = calculateMinutesToFinishEnhancing(e, feeder);
    int result = (int)Math.ceil(((float)mins)/ControllerConstants.FORGE_BASE_MINUTES_TO_ONE_GOLD);

    // log.info("diamonds="+result);
    return result;
  }

  public static int pointsGainedForClanTowerUserBattle(User winner, User loser) {
    int d = winner.getLevel()-loser.getLevel();
    int pts;
    if (d > 10) {
      pts = 1;
    } else if (d < -8) {
      pts = 100;
    } else {
      pts = (int)Math.round((-0.0997*Math.pow(d, 3)+1.4051*Math.pow(d, 2)-14.252*d+90.346)/10.);
    }
    return Math.min(100, Math.max(1, pts));
  }
  
public static GoldSaleProto createFakeGoldSaleForNewPlayer(User user) {
    int id = 0;
    Date startDate = user.getCreateTime();
    Date endDate = new Date(startDate.getTime()+(long)(ControllerConstants.NUM_DAYS_FOR_NEW_USER_GOLD_SALE*24*60*60*1000));

    if (endDate.getTime() < new Date().getTime()) {
      return null;
    }

    String package1SaleIdentifier = IAPValues.PACKAGE1BSALE;
    String package2SaleIdentifier = IAPValues.PACKAGE2BSALE;
    String package3SaleIdentifier = IAPValues.PACKAGE3BSALE;
    String package4SaleIdentifier = IAPValues.PACKAGE4BSALE;
    String package5SaleIdentifier = IAPValues.PACKAGE5BSALE;
    String packageS1SaleIdentifier = IAPValues.PACKAGES1BSALE;
    String packageS2SaleIdentifier = IAPValues.PACKAGES2BSALE;
    String packageS3SaleIdentifier = IAPValues.PACKAGES3BSALE;
    String packageS4SaleIdentifier = IAPValues.PACKAGES4BSALE;
    String packageS5SaleIdentifier = IAPValues.PACKAGES5BSALE;

    String goldShoppeImageName = ControllerConstants.GOLD_SHOPPE_IMAGE_NAME_NEW_USER_GOLD_SALE;
    String goldBarImageName = ControllerConstants.GOLD_BAR_IMAGE_NAME_NEW_USER_GOLD_SALE;

    GoldSale sale = new GoldSale(id, startDate, endDate, goldShoppeImageName, goldBarImageName, package1SaleIdentifier, package2SaleIdentifier, package3SaleIdentifier, package4SaleIdentifier, package5SaleIdentifier,
        packageS1SaleIdentifier, packageS2SaleIdentifier, packageS3SaleIdentifier, packageS4SaleIdentifier, packageS5SaleIdentifier);

    return CreateInfoProtoUtils.createGoldSaleProtoFromGoldSale(sale);
  }
  
  //given a date time, e.g. 2013-02-08 00:33:57 (UTC),
  //spits out the start of day in UTC relative to PST
  //using prior example, returns 2013-02-07 08:00:00
  public static Timestamp getPstDateAndHourFromUtcTime(Date now) {
    Calendar cal = Calendar.getInstance();
    cal.setTimeZone(TimeZone.getTimeZone("Europe/London"));
    cal.setTime(now);
    
    //PST = UTC - 8 or 7(because of daylight savings time) hours
    TimeZone laTimeZone = TimeZone.getTimeZone("America/Los_Angeles");
    Calendar laCal = Calendar.getInstance(laTimeZone);
    
    //get the offset from gmt in hours and account for daylight savings time
    int offset = (laCal.get(Calendar.ZONE_OFFSET) + laCal.get(Calendar.DST_OFFSET)) / (1000*60*60);
    
    cal.add(Calendar.HOUR_OF_DAY, offset);
    //hopefully this gives me YYYY-MM-DD HH:00:00
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    
    cal.set(Calendar.HOUR_OF_DAY, -offset);
    long millis = cal.getTimeInMillis();
    Timestamp PSTDateAndHourInUTC = new Timestamp(millis);
    return PSTDateAndHourInUTC;
  }
  
  public static int dateDifferenceInDays(Date start, Date end) {
    DateMidnight previous = (new DateTime(start)).toDateMidnight(); //
    DateMidnight current = (new DateTime(end)).toDateMidnight();
    int days = Days.daysBetween(previous, current).getDays();
    return days;
  }
  
  public static void writeToUserBoosterPackHistoryOneUser(int userId, int packId,
      int numBought, Timestamp nowTimestamp, List<BoosterItem> itemsUserReceives) {
    List<Integer> raritiesCollected = getRaritiesCollected(itemsUserReceives);
    int rarityOne = raritiesCollected.get(0);
    int rarityTwo = raritiesCollected.get(1);
    int rarityThree = raritiesCollected.get(2);
    InsertUtils.get().insertIntoUserBoosterPackHistory(userId,
        packId, numBought, nowTimestamp, rarityOne, rarityTwo, rarityThree);
  }
  
  private static List<Integer> getRaritiesCollected(List<BoosterItem> itemsUserReceives) {
    List<Integer> raritiesCollected = new ArrayList<Integer>();
    
    Map<Integer, Equipment> equipIdsToEquips = 
        EquipmentRetrieveUtils.getEquipmentIdsToEquipment();
    int rarityOne = 0;
    int rarityTwo = 0;
    int rarityThree = 0;
    for (BoosterItem bi : itemsUserReceives) {
      int equipId = bi.getEquipId();
      Equipment tempEquip = null;
      if (equipIdsToEquips.containsKey(equipId)) {
        tempEquip = equipIdsToEquips.get(equipId);
      } else {
        log.error("No equiment exists for equipId=" + equipId
            + ". BoosterItem has invalid equipId, boosterItem=" + bi);
        continue;
      }
      Rarity equipRarity = tempEquip.getRarity();
      if (isRarityOne(equipRarity)) {
        rarityOne++;
      } else if (isRarityTwo(equipRarity)) {
        rarityTwo++;
      } else if (isRarityThree(equipRarity)) {
        rarityThree++;
      } else {
        log.error("unexpected_error: booster item has unknown equip rarity. " +
        		"booster item=" + bi + ".  Equip rarity=" + equipRarity);
      }
    }
    raritiesCollected.add(rarityOne);
    raritiesCollected.add(rarityTwo);
    raritiesCollected.add(rarityThree);
    return raritiesCollected;
  }
  
  private static boolean isRarityOne(Rarity equipRarity) {
    if (Rarity.COMMON == equipRarity || Rarity.RARE == equipRarity) {
      return true;
    } else {
      return false;
    }
  }
  
  private static boolean isRarityTwo(Rarity equipRarity) {
    if (Rarity.UNCOMMON == equipRarity || Rarity.SUPERRARE == equipRarity) {
      return true;
    } else {
      return false;
    }
  }
  
  private static boolean isRarityThree(Rarity equipRarity) {
    if (Rarity.RARE == equipRarity || Rarity.EPIC == equipRarity) {
      return true;
    } else {
      return false;
    }
  }
  
  //csi: comma separated ints
  public static List<Integer> unCsvStringIntoIntList(String csi) {
    List<Integer> ints = new ArrayList<Integer>();
    if (null != csi) {
      StringTokenizer st = new StringTokenizer(csi, ", ");
      while (st.hasMoreTokens()) {
        ints.add(Integer.parseInt(st.nextToken()));
      }
    }
    return ints;
  }
  
//  //one of the arguments will be set and the other one will be null
//  public static long getDateDMYinMillis(Timestamp nowTimestamp, Date nowDate) {
//    Date nowTemp = null;
//    
//    if (null == nowTimestamp) {
//      nowTemp = nowDate;
//    } else {
//      nowTemp = new Date(nowTimestamp.getTime());
//    }
//    Calendar curDate = Calendar.getInstance();
//    curDate.setTime(nowTemp);
//    curDate.set(Calendar.HOUR_OF_DAY, 0);
//    curDate.set(Calendar.HOUR, 0);
//    curDate.set(Calendar.MINUTE, 0);
//    curDate.set(Calendar.SECOND, 0);
//    curDate.set(Calendar.MILLISECOND, 0);
//    curDate.set(Calendar.AM_PM, 0);
//    
//    return curDate.getTimeInMillis();
//  }
  
  public static int getRandomIntFromList(List<Integer> numList) {
    int upperBound = numList.size();
    Random rand = new Random();
    int randInt = rand.nextInt(upperBound);
    
    int returnValue = numList.get(randInt);
    return returnValue;
  }
  
  /*cut out from purchase booster pack controller*/
  //Returns all the booster items the user purchased and whether or not the use reset the chesst.
  //If the user buys out deck start over from a fresh deck 
  //(boosterItemIdsToNumCollected is changed to reflect none have been collected).
  //Also, keep track of which items were purchased before and/or after the reset (via collectedBeforeReset)
  public static boolean getAllBoosterItemsForUser(Map<Integer, BoosterItem> allBoosterItemIdsToBoosterItems, 
      Map<Integer, Integer> boosterItemIdsToNumCollected, int numBoosterItemsUserWants, User aUser, 
      BoosterPack aPack, List<BoosterItem> returnValue, List<Boolean> collectedBeforeReset) {
    boolean resetOccurred = false;
    int boosterPackId = aPack.getId();
    
    //the possible items user can get
    List<Integer> boosterItemIdsUserCanGet = new ArrayList<Integer>();
    List<Integer> quantitiesInStock = new ArrayList<Integer>();
    
    //populate boosterItemIdsUserCanGet, and quantitiesInStock
    int sumQuantitiesInStock = determineBoosterItemsLeft(allBoosterItemIdsToBoosterItems, 
        boosterItemIdsToNumCollected, boosterItemIdsUserCanGet, quantitiesInStock, aUser, boosterPackId);
    
    //just in case user is allowed to purchase a lot more than what is available in a chest
    //should take care of the case where user buys out the exact amount remaining in the chest
    while (numBoosterItemsUserWants >= sumQuantitiesInStock) {
      resetOccurred = true;
      //give all the remaining booster items to the user, 
      for (int i = 0; i < boosterItemIdsUserCanGet.size(); i++) {
        int bItemId = boosterItemIdsUserCanGet.get(i);
        BoosterItem bi = allBoosterItemIdsToBoosterItems.get(bItemId);
        int quantityInStock = quantitiesInStock.get(i);
        for (int quant = 0; quant < quantityInStock; quant++) {
          returnValue.add(bi);
          collectedBeforeReset.add(true);
        }
      }
      //decrement number user still needs to receive, and then reset deck
      numBoosterItemsUserWants -= sumQuantitiesInStock;
      
      //start from a clean slate as if it is the first time user is purchasing
      boosterItemIdsUserCanGet.clear();
      boosterItemIdsToNumCollected.clear();
      quantitiesInStock.clear();
      sumQuantitiesInStock = 0;
      for (int boosterItemId : allBoosterItemIdsToBoosterItems.keySet()) {
        BoosterItem boosterItemUserCanGet = allBoosterItemIdsToBoosterItems.get(boosterItemId);
        boosterItemIdsUserCanGet.add(boosterItemId);
        boosterItemIdsToNumCollected.put(boosterItemId, 0);
        int quantityInStock = boosterItemUserCanGet.getQuantity();
        quantitiesInStock.add(quantityInStock);
        sumQuantitiesInStock += quantityInStock;
      }
    }

    //set the booster item(s) the user will receieve  
    List<BoosterItem> itemUserReceives = new ArrayList<BoosterItem>();
    if (aPack.isStarterPack()) {
      itemUserReceives = determineStarterBoosterItemsUserReceives(boosterItemIdsUserCanGet,
          quantitiesInStock, numBoosterItemsUserWants, sumQuantitiesInStock, allBoosterItemIdsToBoosterItems);
    } else {
      itemUserReceives = determineBoosterItemsUserReceives(boosterItemIdsUserCanGet, 
          quantitiesInStock, numBoosterItemsUserWants, sumQuantitiesInStock, allBoosterItemIdsToBoosterItems);
    }
    returnValue.addAll(itemUserReceives);
    collectedBeforeReset.addAll(Collections.nCopies(itemUserReceives.size(), false));
    return resetOccurred;
  }

  /*cut out from purchase booster pack controller*/
  //populates ids, quantitiesInStock; determines the remaining booster items the user can get
  private static int determineBoosterItemsLeft(Map<Integer, BoosterItem> allBoosterItemIdsToBoosterItems, 
      Map<Integer, Integer> boosterItemIdsToNumCollected, List<Integer> boosterItemIdsUserCanGet, 
      List<Integer> quantitiesInStock, User aUser, int boosterPackId) {
    //max number randon number can go
    int sumQuantitiesInStock = 0;

    //determine how many BoosterItems are left that user can get
    for (int boosterItemId : allBoosterItemIdsToBoosterItems.keySet()) {
      BoosterItem potentialEquip = allBoosterItemIdsToBoosterItems.get(boosterItemId);
      int quantityLimit = potentialEquip.getQuantity();
      int quantityPurchasedPreviously = ControllerConstants.NOT_SET;

      if (boosterItemIdsToNumCollected.containsKey(boosterItemId)) {
        quantityPurchasedPreviously = boosterItemIdsToNumCollected.get(boosterItemId);
      }

      if(ControllerConstants.NOT_SET == quantityPurchasedPreviously) {
        //user has never bought this BoosterItem before
        boosterItemIdsUserCanGet.add(boosterItemId);
        quantitiesInStock.add(quantityLimit);
        sumQuantitiesInStock += quantityLimit;
      } else if (quantityPurchasedPreviously < quantityLimit) {
        //user bought before, but has not reached the limit
        int numLeftInStock = quantityLimit - quantityPurchasedPreviously;
        boosterItemIdsUserCanGet.add(boosterItemId);
        quantitiesInStock.add(numLeftInStock);
        sumQuantitiesInStock += numLeftInStock;
      } else if (quantityPurchasedPreviously == quantityLimit) {
        continue;
      } else {//will this ever be reached?
        log.error("somehow user has bought more than the allowed limit for a booster item for a booster pack. "
            + "quantityLimit: " + quantityLimit + ", quantityPurchasedPreviously: " + quantityPurchasedPreviously
            + ", userId: " + aUser.getId() + ", boosterItem: " + potentialEquip + ", boosterPackId: " + boosterPackId);
      }
    }

    return sumQuantitiesInStock;
  }
  
  /*cut out from purchase booster pack controller*/
  //no arguments are modified
  private static List<BoosterItem> determineStarterBoosterItemsUserReceives(List<Integer> boosterItemIdsUserCanGet, 
      List<Integer> quantitiesInStock, int amountUserWantsToPurchase, int sumOfQuantitiesInStock,
      Map<Integer, BoosterItem> allBoosterItemIdsToBoosterItems) {
    //return value
    List<BoosterItem> returnValue = new ArrayList<BoosterItem>();
    if (0 == amountUserWantsToPurchase) {
      return returnValue;
    } else if (3 != amountUserWantsToPurchase) {
      log.error("unexpected error: buying " + amountUserWantsToPurchase + " more equips instead of 3.");
      return returnValue; 
    } else if (0 != (sumOfQuantitiesInStock % 3)) {
      log.error("unexpected error: num remaining equips, " + sumOfQuantitiesInStock
          + ", for this chest is not a multiple of 3");
      return returnValue;
    }
    
    Map<Integer, Equipment> allEquips = EquipmentRetrieveUtils.getEquipmentIdsToEquipment();
    Set<EquipType> receivedEquipTypes = new HashSet<EquipType>();
    
    //loop through equips user can get; select one weapon, one armor, one amulet
    for (int boosterItemId : boosterItemIdsUserCanGet) {
      BoosterItem bi = allBoosterItemIdsToBoosterItems.get(boosterItemId);
      int equipId = bi.getEquipId();
      Equipment equip = allEquips.get(equipId);
      EquipType eType = equip.getType();
      
      if (receivedEquipTypes.contains(eType)) {
        //user already got this equip type
        continue;
      } else {
        //record user got a new equip type
        returnValue.add(bi);
        receivedEquipTypes.add(eType);
      }
    }
    
    if (3 != returnValue.size()) {
      log.error("unexpected error: user did not receive one type of each equip."
          + " User would have received (but now will not receive): " + MiscMethods.shallowListToString(returnValue) 
          + ". Chest either intialized improperly or code assigns equips incorrectly.");
      return new ArrayList<BoosterItem>();
    }
    return returnValue;
  }
  
  /*cut out from purchase booster pack controller*/
  //no arguments are modified
  private static List<BoosterItem> determineBoosterItemsUserReceives(List<Integer> boosterItemIdsUserCanGet, 
      List<Integer> quantitiesInStock, int amountUserWantsToPurchase, int sumOfQuantitiesInStock,
      Map<Integer, BoosterItem> allBoosterItemIdsToBoosterItems) {
    //return value
    List<BoosterItem> itemsUserReceives = new ArrayList<BoosterItem>();
    
    Random rand = new Random();
    List<Integer> newBoosterItemIdsUserCanGet = new ArrayList<Integer>(boosterItemIdsUserCanGet);
    List<Integer> newQuantitiesInStock = new ArrayList<Integer>(quantitiesInStock);
    int newSumOfQuantities = sumOfQuantitiesInStock;
    
    //selects one of the ids at random without replacement
    for(int purchaseN = 0; purchaseN < amountUserWantsToPurchase; purchaseN++) {
      int sumSoFar = 0;
      int randomNum = rand.nextInt(newSumOfQuantities) + 1; //range [1, newSumOfQuantities]
      
      for(int i = 0; i < newBoosterItemIdsUserCanGet.size(); i++) {
        int bItemId = newBoosterItemIdsUserCanGet.get(i);
        int quantity = newQuantitiesInStock.get(i);
        
        sumSoFar += quantity;
        
        if(randomNum <= sumSoFar) {
          //we have a winner! current boosterItemId is what the user gets
          BoosterItem selectedBoosterItem = allBoosterItemIdsToBoosterItems.get(bItemId);
          itemsUserReceives.add(selectedBoosterItem);
          
          //preparation for next BoosterItem to be selected
          if (1 == quantity) {
            newBoosterItemIdsUserCanGet.remove(i);
            newQuantitiesInStock.remove(i);
          } else if (1 < quantity){
            //booster item id has more than one quantity
            int decrementedQuantity = newQuantitiesInStock.remove(i) - 1;
            newQuantitiesInStock.add(i, decrementedQuantity);
          } else {
            //ignore those with quantity of 0
            continue;
          }
          
          newSumOfQuantities -= 1;
          break;
        }
      }
    }
    
    return itemsUserReceives;
  }
  /*cut out from purchase booster pack controller*/
  public static List<Integer> insertNewUserEquips(int userId, List<BoosterItem> itemsUserReceives) {
    int amount = itemsUserReceives.size();
    int forgeLevel = ControllerConstants.DEFAULT_USER_EQUIP_LEVEL;
    int enhancementLevel = ControllerConstants.DEFAULT_USER_EQUIP_ENHANCEMENT_PERCENT;
    List<Integer> equipIds = new ArrayList<Integer>();
    List<Integer> levels = new ArrayList<Integer>(Collections.nCopies(amount, forgeLevel));
    List<Integer> enhancement = new ArrayList<Integer>(Collections.nCopies(amount, enhancementLevel));
    
    for(BoosterItem bi : itemsUserReceives) {
      int equipId = bi.getEquipId();
      equipIds.add(equipId);
    }
    
    return InsertUtils.get().insertUserEquips(userId, equipIds, levels, enhancement);
  }
  /*cut out from purchase booster pack controller*/
  public static boolean updateUserBoosterItems(List<BoosterItem> itemsUserReceives, 
      List<Boolean> collectedBeforeReset, Map<Integer, Integer> boosterItemIdsToNumCollected, 
      Map<Integer, Integer> newBoosterItemIdsToNumCollected, int userId, boolean resetOccurred) {
    
    Map<Integer, Integer> changedBoosterItemIdsToNumCollected = new HashMap<Integer, Integer>();
    int numCollectedBeforeReset = 0;

    //for each booster item received record it in the map above, and record how many
    //booster items user has in aggregate
    for (int i = 0; i < itemsUserReceives.size(); i++) {
      boolean beforeReset = collectedBeforeReset.get(i);
      if (!beforeReset) {
        BoosterItem received = itemsUserReceives.get(i);
        int boosterItemId = received.getId();
        
        //default quantity user gets if user has no quantity of specific boosterItem
        int newQuantity = 1; 
        if(newBoosterItemIdsToNumCollected.containsKey(boosterItemId)) {
          newQuantity = newBoosterItemIdsToNumCollected.get(boosterItemId) + 1;
        }
        changedBoosterItemIdsToNumCollected.put(boosterItemId, newQuantity);
        newBoosterItemIdsToNumCollected.put(boosterItemId, newQuantity);
      } else {
        numCollectedBeforeReset++;
      }
    }
    
    //loop through newBoosterItemIdsToNumCollected and make sure the quantities
    //collected is itemsUserReceives.size() amount more than boosterItemIdsToNumCollected
    int changeInCollectedQuantity = 0;
    for (int id : changedBoosterItemIdsToNumCollected.keySet()) {
      int newAmount = newBoosterItemIdsToNumCollected.get(id);
      int oldAmount = 0;
      if (boosterItemIdsToNumCollected.containsKey(id)) {
        oldAmount = boosterItemIdsToNumCollected.get(id);
      }
      changeInCollectedQuantity += newAmount - oldAmount;
    }
    //for when user buys out a pack and then some
    changeInCollectedQuantity += numCollectedBeforeReset;
    if (itemsUserReceives.size() != changeInCollectedQuantity) {
      log.error("quantities of booster items do not match how many items user receives. "
          + "amount user receives that is recorded (user_booster_items table): " + changeInCollectedQuantity
          + ", amount user receives (unrecorded): " + itemsUserReceives.size());
      return false;
    }

    recordBoosterItemsThatReset(changedBoosterItemIdsToNumCollected, newBoosterItemIdsToNumCollected, resetOccurred);
    
    return UpdateUtils.get().updateUserBoosterItemsForOneUser(userId, changedBoosterItemIdsToNumCollected);
  }
  /*cut out from purchase booster pack controller*/
  //if the user has bought out the whole deck, then for the booster items
  //the user did not get, record in the db that the user has 0 of them collected
  private static void recordBoosterItemsThatReset(Map<Integer, Integer> changedBoosterItemIdsToNumCollected,
      Map<Integer, Integer> newBoosterItemIdsToNumCollected, boolean refilled) {
    if (refilled) {
      for (int boosterItemId : newBoosterItemIdsToNumCollected.keySet()) {
        if (!changedBoosterItemIdsToNumCollected.containsKey(boosterItemId)) {
          int value = newBoosterItemIdsToNumCollected.get(boosterItemId);
          changedBoosterItemIdsToNumCollected.put(boosterItemId, value);
        }
      }
    }
  }
  
  public static Set<Integer> getEquippedEquips(User aUser) {
    Set<Integer> equippedUserEquipIds = new HashSet<Integer>();
    equippedUserEquipIds.add(aUser.getAmuletEquippedUserEquipId());
    equippedUserEquipIds.add(aUser.getAmuletTwoEquippedUserEquipId());
    equippedUserEquipIds.add(aUser.getArmorEquippedUserEquipId());
    equippedUserEquipIds.add(aUser.getArmorTwoEquippedUserEquipId());
    equippedUserEquipIds.add(aUser.getWeaponEquippedUserEquipId());
    equippedUserEquipIds.add(aUser.getWeaponTwoEquippedUserEquipId());
    
    equippedUserEquipIds.remove(ControllerConstants.NOT_SET);
    
    return equippedUserEquipIds;
  }
  
  //arguments don't take into account the 1 forge slot the user has by default
  public static int costToBuyForgeSlot(int goalNumAdditionalForgeSlots,
      int currentNumAdditionalForgeSlots) {
    
    return 250;
  }
}
