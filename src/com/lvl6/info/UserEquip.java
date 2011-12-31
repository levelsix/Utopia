package com.lvl6.info;

import java.util.HashMap;
import java.util.Map;

import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

public class UserEquip {

  private int userId;
  private int equipId;
  private int quantity;
  private boolean isStolen;
  
  public UserEquip(int userId, int equipId, int quantity, boolean isStolen) {
    this.userId = userId;
    this.equipId = equipId;
    this.quantity = quantity;
    this.isStolen = isStolen;
  }
  
  /*
  public synchronized boolean decrementQuantity() {
    quantity--;
    
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER_EQUIP__USER_ID, userId);
    conditionParams.put(DBConstants.USER_EQUIP__EQUIP_ID, equipId);
    
    Map <String, Object> relativeParams = new HashMap<String, Object>();
    relativeParams.put(DBConstants.USER_EQUIP__QUANTITY, -1);
    
     TODO: impl
    
    if (quantity == 0) {
      int numDeleted = 0;
    }
    int numUpdated;
    
    for (Integer i : userIds) {
      paramsToVals.put("user_id", i);
    }
    return convertRSToUserToUserEquips(DBConnection.selectRowsOr(paramsToVals, TABLE_NAME));
    
    return false;
  }*/
  

  public int getUserId() {
    return userId;
  }


  public int getEquipId() {
    return equipId;
  }


  public synchronized int getQuantity() {
    return quantity;
  }


  public boolean isStolen() {
    return isStolen;
  }


  @Override
  public String toString() {
    return "UserEquip [userId=" + userId + ", equipId=" + equipId
        + ", quantity=" + quantity + ", isStolen=" + isStolen + "]";
  }
}
