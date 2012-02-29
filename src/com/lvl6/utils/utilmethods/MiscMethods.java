package com.lvl6.utils.utilmethods;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.City;
import com.lvl6.info.User;
import com.lvl6.properties.Globals;
import com.lvl6.proto.EventProto.UpdateClientUserResponseProto;
import com.lvl6.proto.InfoProto.FullEquipProto.ClassType;
import com.lvl6.proto.InfoProto.UserType;
import com.lvl6.retrieveutils.rarechange.CityRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;

public class MiscMethods {
 
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
}
