package com.lvl6.utils.utilmethods;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.AnimatedSpriteOffset;
import com.lvl6.info.City;
import com.lvl6.info.Dialogue;
import com.lvl6.info.Equipment;
import com.lvl6.info.Location;
import com.lvl6.info.Task;
import com.lvl6.info.User;
import com.lvl6.info.ValidLocationBox;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.properties.Globals;
import com.lvl6.properties.IAPValues;
import com.lvl6.properties.MDCKeys;
import com.lvl6.proto.EventProto.StartupResponseProto.StartupConstants;
import com.lvl6.proto.EventProto.StartupResponseProto.StartupConstants.BattleConstants;
import com.lvl6.proto.EventProto.StartupResponseProto.StartupConstants.FormulaConstants;
import com.lvl6.proto.EventProto.UpdateClientUserResponseProto;
import com.lvl6.proto.InfoProto.DefeatTypeJobProto.DefeatTypeJobEnemyType;
import com.lvl6.proto.InfoProto.DialogueProto.SpeechSegmentProto.DialogueSpeaker;
import com.lvl6.proto.InfoProto.FullEquipProto.ClassType;
import com.lvl6.proto.InfoProto.UserType;
import com.lvl6.retrieveutils.rarechange.BuildStructJobRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.CityRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.DefeatTypeJobRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.EquipmentRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.LevelsRequiredExperienceRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.NeutralCityElementsRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.PossessEquipJobRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.QuestRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.StructureRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.TaskEquipReqRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.TaskRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.UpgradeStructJobRetrieveUtils;
import com.lvl6.server.GameServer;
import com.lvl6.utils.ConnectedPlayer;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.DBConnection;

public class MiscMethods {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

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
        log.error("problem with creating dialogue object for this dialogueblob: " + dialogueBlob);
        log.error(e);
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
    ClassType userClass = MiscMethods.getClassTypeFromUserType(user.getType());
    if (user.getLevel() >= equip.getMinLevel() && 
        (userClass == equip.getClassType() || equip.getClassType() == ClassType.ALL_AMULET)) {
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
    if (playerId != null && playerId > 0) MDC.put(MDCKeys.PLAYER_ID, playerId);
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

  public static boolean unequipUserEquipIfEquipped(User user, int equipId) {
    if (user.getWeaponEquipped() == equipId || user.getArmorEquipped() == equipId || user.getAmuletEquipped() == equipId) {
      return user.updateUnequip(equipId, user.getWeaponEquipped() == equipId, user.getArmorEquipped() == equipId, user.getAmuletEquipped() == equipId);
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

  public static UpdateClientUserResponseEvent createUpdateClientUserResponseEvent(User user) {
    UpdateClientUserResponseEvent resEvent = new UpdateClientUserResponseEvent(user.getId());
    UpdateClientUserResponseProto resProto = UpdateClientUserResponseProto.newBuilder()
        .setSender(CreateInfoProtoUtils.createFullUserProtoFromUser(user))
        .setTimeOfUserUpdate(new Date().getTime()).build();
    resEvent.setUpdateClientUserResponseProto(resProto);
    return resEvent;
  }

  public static ClassType getClassTypeFromUserType(UserType userType) {
    if (userType == UserType.BAD_MAGE || userType == UserType.GOOD_MAGE) {
      return ClassType.MAGE;
    }
    if (userType == UserType.BAD_WARRIOR || userType == UserType.GOOD_WARRIOR) {
      return ClassType.WARRIOR;
    }
    if (userType == UserType.BAD_ARCHER || userType == UserType.GOOD_ARCHER) {
      return ClassType.ARCHER;
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
        .setHealthBaseGain(ControllerConstants.USE_SKILL_POINT__HEALTH_BASE_GAIN)
        .setStaminaBaseGain(ControllerConstants.USE_SKILL_POINT__STAMINA_BASE_GAIN)
        .setAttackBaseCost(ControllerConstants.USE_SKILL_POINT__ATTACK_BASE_COST)
        .setDefenseBaseCost(ControllerConstants.USE_SKILL_POINT__DEFENSE_BASE_COST)
        .setEnergyBaseCost(ControllerConstants.USE_SKILL_POINT__ENERGY_BASE_COST)
        .setHealthBaseCost(ControllerConstants.USE_SKILL_POINT__HEALTH_BASE_COST)
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
        .setMaxNumbersOfEnemiesToGenerateAtOnce(ControllerConstants.GENERATE_ATTACK_LIST__NUM_ENEMIES_TO_GENERATE_MAX)
        .setPercentReturnedToUserForSellingEquipInArmory(ControllerConstants.ARMORY__SELL_RATIO)
        .setMaxCityRank(ControllerConstants.TASK_ACTION__MAX_CITY_RANK)
        .setArmoryImgVerticalPixelOffset(ControllerConstants.ARMORY_IMG_VERTICAL_PIXEL_OFFSET)
        .setVaultImgVerticalPixelOffset(ControllerConstants.VAULT_IMG_VERTICAL_PIXEL_OFFSET)
        .setMarketplaceImgVerticalPixelOffset(ControllerConstants.MARKETPLACE_IMG_VERTICAL_PIXEL_OFFSET)
        .setAviaryImgVerticalPixelOffset(ControllerConstants.AVIARY_IMG_VERTICAL_PIXEL_OFFSET)
        .setCarpenterImgVerticalPixelOffset(ControllerConstants.CARPENTER_IMG_VERTICAL_PIXEL_OFFSET)
        .setMaxCharLengthForWallPost(ControllerConstants.POST_ON_PLAYER_WALL__MAX_CHAR_LENGTH)
        .setPlayerWallPostsRetrieveCap(ControllerConstants.RETRIEVE_PLAYER_WALL_POSTS__NUM_POSTS_CAP);

    if (ControllerConstants.STARTUP__ANIMATED_SPRITE_OFFSETS != null) {
      for (int i = 0; i < ControllerConstants.STARTUP__ANIMATED_SPRITE_OFFSETS.length; i++) {
        AnimatedSpriteOffset aso = ControllerConstants.STARTUP__ANIMATED_SPRITE_OFFSETS[i];
        cb.addAnimatedSpriteOffsets(CreateInfoProtoUtils.createAnimatedSpriteOffsetProtoFromAnimatedSpriteOffset(aso));
      }
    }

    FormulaConstants formulaConstants = FormulaConstants.newBuilder()
        .setMinutesToUpgradeForNormStructMultiplier(ControllerConstants.MINUTES_TO_UPGRADE_FOR_NORM_STRUCT_MULTIPLIER)
        .setIncomeFromNormStructMultiplier(ControllerConstants.INCOME_FROM_NORM_STRUCT_MULTIPLIER)
        .setUpgradeStructCoinCostExponentBase(ControllerConstants.UPGRADE_NORM_STRUCTURE__UPGRADE_STRUCT_COIN_COST_EXPONENT_BASE)
        .setUpgradeStructDiamondCostExponentBase(ControllerConstants.UPGRADE_NORM_STRUCTURE__UPGRADE_STRUCT_DIAMOND_COST_EXPONENT_BASE)
        .setDiamondCostForInstantUpgradeMultiplier(ControllerConstants.FINISH_NORM_STRUCT_WAITTIME_WITH_DIAMONDS__DIAMOND_COST_FOR_INSTANT_UPGRADE_MULTIPLIER)
        .setBattleWeightGivenToAttackStat(ControllerConstants.BATTLE_WEIGHT_GIVEN_TO_ATTACK_STAT)
        .setBattleWeightGivenToAttackEquipSum(ControllerConstants.BATTLE_WEIGHT_GIVEN_TO_ATTACK_EQUIP_SUM)
        .setBattleWeightGivenToDefenseStat(ControllerConstants.BATTLE_WEIGHT_GIVEN_TO_DEFENSE_STAT)
        .setBattleWeightGivenToDefenseEquipSum(ControllerConstants.BATTLE_WEIGHT_GIVEN_TO_DEFENSE_EQUIP_SUM)
        .build();

    cb = cb.setFormulaConstants(formulaConstants);

    BattleConstants battleConstants = BattleConstants.newBuilder()
        .setLocationBarMax(ControllerConstants.BATTLE__LOCATION_BAR_MAX)
        .setMaxAttackMultiplier(ControllerConstants.BATTLE__MAX_ATTACK_MULTIPLIER)
        .setMinPercentOfEnemyHealth(ControllerConstants.BATTLE__MIN_PERCENT_OF_ENEMY_HEALTH)
        .setMaxPercentOfEnemyHealth(ControllerConstants.BATTLE__MAX_PERCENT_OF_ENEMY_HEALTH)
        .setBattleDifferenceMultiplier(ControllerConstants.BATTLE__BATTLE_DIFFERENCE_MULTIPLIER)
        .setBattleDifferenceTuner(ControllerConstants.BATTLE__BATTLE_DIFFERENCE_TUNER)
        .build();

    cb = cb.setBattleConstants(battleConstants);

    for (int i = 0; i < IAPValues.packageNames.size(); i++) {
      cb.addProductIds(IAPValues.packageNames.get(i));
      cb.addProductDiamondsGiven(IAPValues.packageGivenDiamonds.get(i));
    }
    return cb.build();  
  }

  public static void reloadAllRareChangeStaticData() {
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
    try {
		DBConnection.get().getConnection().close();
	} catch (SQLException e) {
		log.error("Error closing DBConnection after loading RareChangeStaticData", e);
	}
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
}
