package com.lvl6.utils.utilmethods;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.User;
import com.lvl6.proto.EventProto.UpdateClientUserResponseProto;
import com.lvl6.proto.InfoProto.FullEquipProto.ClassType;
import com.lvl6.proto.InfoProto.UserType;
import com.lvl6.utils.CreateInfoProtoUtils;

public class MiscMethods {
  
  public static int calculateIncomeGainedFromUserStruct(int structIncomeBase, int userStructLevel) {
    return userStructLevel * structIncomeBase;
  }
  
  public static UpdateClientUserResponseEvent createUpdateClientUserResponseEvent(User user) {
    UpdateClientUserResponseEvent resEvent = new UpdateClientUserResponseEvent(user.getId());
    UpdateClientUserResponseProto resProtoAttacker = UpdateClientUserResponseProto.newBuilder()
        .setSender(CreateInfoProtoUtils.createFullUserProtoFromUser(user))
        .setTimeOfUserUpdate(new Date().getTime()).build();
    resEvent.setUpdateClientUserResponseProto(resProtoAttacker);
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
