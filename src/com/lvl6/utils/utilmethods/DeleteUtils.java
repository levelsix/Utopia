package com.lvl6.utils.utilmethods;

import java.util.HashMap;
import java.util.Map;

import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

public class DeleteUtils {

  public static boolean deleteMarketplacePost(int mpId) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.MARKETPLACE__ID, mpId);

    int numDeleted = DBConnection.deleteRows(DBConstants.TABLE_MARKETPLACE, conditionParams, "and");
    if (numDeleted == 1) {
      return true;
    }

    return false;
  }

}
