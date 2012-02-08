package com.lvl6.properties;

import java.util.Arrays;
import java.util.List;

public class IAPValues {

  /*APPLE'S VARIABLES*/
  public static final String RECEIPT_DATA = "receipt-data";
  public static final String STATUS = "status";
  public static final String RECEIPT = "receipt";
  public static final String TRANSACTION_ID = "transaction_id";
  public static final String PRODUCT_ID = "product_id";
  public static final String QUANTITY = "quantity";
  public static final String BID = "bid";
  public static final String BVRS = "bvrs";
  public static final String APP_ITEM_ID = "app_item_id";
  public static final String PURCHASE_DATE = "purchase_date";
  
  private static final String PACKAGE1 = "com.lvl6.utopia.fewdiamonds";
  private static final String PACKAGE2 = "com.lvl6.utopia.morediamonds";
  private static final String PACKAGE3 = "com.lvl6.utopia.quitesomediamonds";
  private static final String PACKAGE4 = "com.lvl6.utopia.rackdiamonds";
  private static final String PACKAGE5 = "com.lvl6.utopia.shittondiamonds";
  private static final String PACKAGE6 = "com.lvl6.utopia.toomanydiamonds";
  
  private static final int PACKAGE_1_DIAMONDS = 50;
  private static final int PACKAGE_2_DIAMONDS = 100;
  private static final int PACKAGE_3_DIAMONDS = 250;
  private static final int PACKAGE_4_DIAMONDS = 500;
  private static final int PACKAGE_5_DIAMONDS = 1000;
  private static final int PACKAGE_6_DIAMONDS = 2000;
  
  private static final double PACKAGE_1_PRICE = .99;
  private static final double PACKAGE_2_PRICE = 4.99;
  private static final double PACKAGE_3_PRICE = 9.99;
  private static final double PACKAGE_4_PRICE = 19.99;
  private static final double PACKAGE_5_PRICE = 99.99;
  private static final double PACKAGE_6_PRICE = 999.99;

  public static final List<String> packageNames = 
      Arrays.asList(PACKAGE1, PACKAGE2, PACKAGE3, PACKAGE4, PACKAGE5, PACKAGE6);
  public static final List<Integer> packageGivenDiamonds = 
      Arrays.asList(PACKAGE_1_DIAMONDS, PACKAGE_2_DIAMONDS, PACKAGE_3_DIAMONDS, 
          PACKAGE_4_DIAMONDS, PACKAGE_5_DIAMONDS, PACKAGE_6_DIAMONDS);
  public static final List<Double> packagePrices = 
      Arrays.asList(PACKAGE_1_PRICE, PACKAGE_2_PRICE, PACKAGE_3_PRICE, 
          PACKAGE_4_PRICE, PACKAGE_5_PRICE, PACKAGE_6_PRICE);


  
  public static int getDiamondsForPackageName(String packageName) {
    if (packageName.equals(PACKAGE1)) {
      return PACKAGE_1_DIAMONDS;
    }
    if (packageName.equals(PACKAGE2)) {
      return PACKAGE_2_DIAMONDS;
    }
    if (packageName.equals(PACKAGE3)) {
      return PACKAGE_3_DIAMONDS;
    }
    if (packageName.equals(PACKAGE4)) {
      return PACKAGE_4_DIAMONDS;
    }
    if (packageName.equals(PACKAGE5)) {
      return PACKAGE_5_DIAMONDS;
    }
    if (packageName.equals(PACKAGE6)) {
      return PACKAGE_6_DIAMONDS;
    }
    return 0;
  }
  
  public static double getCashSpentForPackageName(String packageName) {
    if (packageName.equals(PACKAGE1)) {
      return PACKAGE_1_PRICE;
    }
    if (packageName.equals(PACKAGE2)) {
      return PACKAGE_2_PRICE;
    }
    if (packageName.equals(PACKAGE3)) {
      return PACKAGE_3_PRICE;
    }
    if (packageName.equals(PACKAGE4)) {
      return PACKAGE_4_PRICE;
    }
    if (packageName.equals(PACKAGE5)) {
      return PACKAGE_5_PRICE;
    }
    if (packageName.equals(PACKAGE6)) {
      return PACKAGE_6_PRICE;
    }
    return 0;
  }
}
