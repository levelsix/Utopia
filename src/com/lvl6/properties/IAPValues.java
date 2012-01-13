package com.lvl6.properties;

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
  public static final String PURCHASE_DATE = "purchase-date";
  
  private static final String PACKAGE1 = "package1";
  private static final String PACKAGE2 = "package2";
  private static final String PACKAGE3 = "package3";
  private static final String PACKAGE4 = "package4";
  private static final String PACKAGE5 = "package5";
  
  private static final int PACKAGE_1_DIAMONDS = 50;
  private static final int PACKAGE_2_DIAMONDS = 100;
  private static final int PACKAGE_3_DIAMONDS = 250;
  private static final int PACKAGE_4_DIAMONDS = 500;
  private static final int PACKAGE_5_DIAMONDS = 1000;
  
  private static final double PACKAGE_1_PRICE = .99;
  private static final double PACKAGE_2_PRICE = 4.99;
  private static final double PACKAGE_3_PRICE = 9.99;
  private static final double PACKAGE_4_PRICE = 19.99;
  private static final double PACKAGE_5_PRICE = 99.99;

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
    return 0;
  }
}
