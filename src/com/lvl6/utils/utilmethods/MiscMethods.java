package com.lvl6.utils.utilmethods;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.City;
import com.lvl6.info.Location;
import com.lvl6.info.Task;
import com.lvl6.info.User;
import com.lvl6.info.ValidLocationBox;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.properties.Globals;
import com.lvl6.properties.IAPValues;
import com.lvl6.proto.EventProto.StartupResponseProto.StartupConstants;
import com.lvl6.proto.EventProto.UpdateClientUserResponseProto;
import com.lvl6.proto.InfoProto.FullEquipProto.ClassType;
import com.lvl6.proto.InfoProto.UserType;
import com.lvl6.retrieveutils.rarechange.CityRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;

public class MiscMethods {
  
  public static int calculateCoinsGainedFromTutorialTask(Task firstTaskToComplete) {
    return ((firstTaskToComplete.getMinCoinsGained() + firstTaskToComplete.getMaxCoinsGained())/2)
        * firstTaskToComplete.getNumForCompletion();
  }

  public static boolean unequipUserEquip(User user, int equipId) {
    if (user.getWeaponEquipped() == equipId || user.getArmorEquipped() == equipId || user.getAmuletEquipped() == equipId) {
      return user.updateUnequip(equipId, user.getWeaponEquipped() == equipId, user.getArmorEquipped() == equipId, user.getAmuletEquipped() == equipId);
    }
    return false;
  }

  public static boolean checkClientTimeBeforeApproximateNow(Timestamp clientTime) {
    if (clientTime.getTime() < new Date().getTime() + Globals.NUM_MINUTES_DIFFERENCE_LEEWAY_FOR_CLIENT_TIME*60000) {
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
    return Math.max(1, (minutesToUpgradeBase * userStructLevel)/2);
  }

  public static int calculateIncomeGainedFromUserStruct(int structIncomeBase, int userStructLevel) {
    return userStructLevel * structIncomeBase;
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
        .setDiamondRewardForReferrer(ControllerConstants.USER_CREATE__DIAMOND_REWARD_FOR_REFERRER)
        .setMaxCityRank(ControllerConstants.TASK_ACTION__MAX_CITY_RANK)
        .setMinLevelForArmory(ControllerConstants.PLACE_CRITSTRUCT__MIN_LEVEL_ARMORY)
        .setMinLevelForMarketplace(ControllerConstants.PLACE_CRITSTRUCT__MIN_LEVEL_MARKETPLACE)
        .setMinLevelForVault(ControllerConstants.PLACE_CRITSTRUCT__MIN_LEVEL_VAULT);
    for (int i = 0; i < IAPValues.packageNames.size(); i++) {
      cb.addProductIds(IAPValues.packageNames.get(i));
      cb.addProductDiamondsGiven(IAPValues.packageGivenDiamonds.get(i));
    }
    return cb.build();  
  }
}
