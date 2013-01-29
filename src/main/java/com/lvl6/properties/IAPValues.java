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

  private static final String PACKAGE1 = Globals.APPLE_BUNDLE_ID + ".package1";
  private static final String PACKAGE2 = Globals.APPLE_BUNDLE_ID + ".package2";
  private static final String PACKAGE3 = Globals.APPLE_BUNDLE_ID + ".package3";
  private static final String PACKAGE4 = Globals.APPLE_BUNDLE_ID + ".package4";
  private static final String PACKAGE5 = Globals.APPLE_BUNDLE_ID + ".package5";

  private static final String PACKAGE1SALE = Globals.APPLE_BUNDLE_ID + ".package1sale";
  private static final String PACKAGE2SALE = Globals.APPLE_BUNDLE_ID + ".package2sale";
  private static final String PACKAGE3SALE = Globals.APPLE_BUNDLE_ID + ".package3sale";
  private static final String PACKAGE4SALE = Globals.APPLE_BUNDLE_ID + ".package4sale";
  private static final String PACKAGE5SALE = Globals.APPLE_BUNDLE_ID + ".package5sale";

  private static final String PACKAGES1 = Globals.APPLE_BUNDLE_ID + ".packageS1";
  private static final String PACKAGES2 = Globals.APPLE_BUNDLE_ID + ".packageS2";
  private static final String PACKAGES3 = Globals.APPLE_BUNDLE_ID + ".packageS3";
  private static final String PACKAGES4 = Globals.APPLE_BUNDLE_ID + ".packageS4";
  private static final String PACKAGES5 = Globals.APPLE_BUNDLE_ID + ".packageS5";

  private static final String PACKAGES1SALE = Globals.APPLE_BUNDLE_ID + ".packageS1sale";
  private static final String PACKAGES2SALE = Globals.APPLE_BUNDLE_ID + ".packageS2sale";
  private static final String PACKAGES3SALE = Globals.APPLE_BUNDLE_ID + ".packageS3sale";
  private static final String PACKAGES4SALE = Globals.APPLE_BUNDLE_ID + ".packageS4sale";
  private static final String PACKAGES5SALE = Globals.APPLE_BUNDLE_ID + ".packageS5sale";

  private static final String PACKAGE1IMG = "goldstack1.png";
  private static final String PACKAGE2IMG = "goldstack2.png";
  private static final String PACKAGE3IMG = "goldstack3.png";
  private static final String PACKAGE4IMG = "goldstack4.png";
  private static final String PACKAGE5IMG = "goldstack5.png";
  private static final String PACKAGES1IMG = "silverstack1.png";
  private static final String PACKAGES2IMG = "silverstack2.png";
  private static final String PACKAGES3IMG = "silverstack3.png";
  private static final String PACKAGES4IMG = "silverstack4.png";
  private static final String PACKAGES5IMG = "silverstack5.png";

  /*
   * 1- $1 for 10 diamonds
   * 2- $1 for 12 diamonds
   * 3- $1 for 12.5 diamonds
   * 4- $1 for 13 diamonds
   * 5- $1 for 15 diamonds
   */

  private static final int PACKAGE_1_DIAMONDS = 50;
  private static final int PACKAGE_2_DIAMONDS = 120;
  private static final int PACKAGE_3_DIAMONDS = 250;
  private static final int PACKAGE_4_DIAMONDS = 650;
  private static final int PACKAGE_5_DIAMONDS = 1500;

  private static final int PACKAGE_S1_COINS = 50000;
  private static final int PACKAGE_S2_COINS = 120000;
  private static final int PACKAGE_S3_COINS = 250000;
  private static final int PACKAGE_S4_COINS = 650000;
  private static final int PACKAGE_S5_COINS = 1500000;

  private static final double PACKAGE_1_PRICE = 4.99;
  private static final double PACKAGE_2_PRICE = 9.99;
  private static final double PACKAGE_3_PRICE = 19.99;
  private static final double PACKAGE_4_PRICE = 49.99;
  private static final double PACKAGE_5_PRICE = 99.99;

  private static final double PACKAGE_1_SALE_PRICE = 3.99;
  private static final double PACKAGE_2_SALE_PRICE = 7.99;
  private static final double PACKAGE_3_SALE_PRICE = 15.99;
  private static final double PACKAGE_4_SALE_PRICE = 39.99;
  private static final double PACKAGE_5_SALE_PRICE = 74.99;

  private static final double PACKAGE_S1_PRICE = 4.99;
  private static final double PACKAGE_S2_PRICE = 9.99;
  private static final double PACKAGE_S3_PRICE = 19.99;
  private static final double PACKAGE_S4_PRICE = 49.99;
  private static final double PACKAGE_S5_PRICE = 99.99;

  private static final double PACKAGE_S1_SALE_PRICE = 3.99;
  private static final double PACKAGE_S2_SALE_PRICE = 7.99;
  private static final double PACKAGE_S3_SALE_PRICE = 15.99;
  private static final double PACKAGE_S4_SALE_PRICE = 39.99;
  private static final double PACKAGE_S5_SALE_PRICE = 74.99;

  public static final List<String> iapPackageNames = 
      Arrays.asList(PACKAGE1, PACKAGES1, PACKAGE2, PACKAGES2, PACKAGE3, PACKAGES3, PACKAGE4, PACKAGES4, PACKAGE5, PACKAGES5);

  public static final List<String> packageNames = 
      Arrays.asList(PACKAGE1, PACKAGE2, PACKAGE3, PACKAGE4, PACKAGE5);
  public static final List<Integer> packageGivenDiamonds = 
      Arrays.asList(PACKAGE_1_DIAMONDS, PACKAGE_2_DIAMONDS, PACKAGE_3_DIAMONDS, 
          PACKAGE_4_DIAMONDS, PACKAGE_5_DIAMONDS);


  public static int getDiamondsForPackageName(String packageName) {
    if (packageName.equals(PACKAGE1) || packageName.equals(PACKAGE1SALE)) {
      return PACKAGE_1_DIAMONDS;
    }
    if (packageName.equals(PACKAGE2) || packageName.equals(PACKAGE2SALE)) {
      return PACKAGE_2_DIAMONDS;
    }
    if (packageName.equals(PACKAGE3) || packageName.equals(PACKAGE3SALE)) {
      return PACKAGE_3_DIAMONDS;
    }
    if (packageName.equals(PACKAGE4) || packageName.equals(PACKAGE4SALE)) {
      return PACKAGE_4_DIAMONDS;
    }
    if (packageName.equals(PACKAGE5) || packageName.equals(PACKAGE5SALE)) {
      return PACKAGE_5_DIAMONDS;
    }
    return 0;
  }

  public static int getCoinsForPackageName(String packageName) {
    if (packageName.equals(PACKAGES1) || packageName.equals(PACKAGES1SALE)) {
      return PACKAGE_S1_COINS;
    }
    if (packageName.equals(PACKAGES2) || packageName.equals(PACKAGES2SALE)) {
      return PACKAGE_S2_COINS;
    }
    if (packageName.equals(PACKAGES3) || packageName.equals(PACKAGES3SALE)) {
      return PACKAGE_S3_COINS;
    }
    if (packageName.equals(PACKAGES4) || packageName.equals(PACKAGES4SALE)) {
      return PACKAGE_S4_COINS;
    }
    if (packageName.equals(PACKAGES5) || packageName.equals(PACKAGES5SALE)) {
      return PACKAGE_S5_COINS;
    }
    return 0;
  }

  public static String getImageNameForPackageName(String packageName) {
    if (packageName.equals(PACKAGE1)) {
      return PACKAGE1IMG;
    }
    if (packageName.equals(PACKAGE2)) {
      return PACKAGE2IMG;
    }
    if (packageName.equals(PACKAGE3)) {
      return PACKAGE3IMG;
    }
    if (packageName.equals(PACKAGE4)) {
      return PACKAGE4IMG;
    }
    if (packageName.equals(PACKAGE5)) {
      return PACKAGE5IMG;
    }
    if (packageName.equals(PACKAGES1)) {
      return PACKAGES1IMG;
    }
    if (packageName.equals(PACKAGES2)) {
      return PACKAGES2IMG;
    }
    if (packageName.equals(PACKAGES3)) {
      return PACKAGES3IMG;
    }
    if (packageName.equals(PACKAGES4)) {
      return PACKAGES4IMG;
    }
    if (packageName.equals(PACKAGES5)) {
      return PACKAGES5IMG;
    }
    return null;
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
    if (packageName.equals(PACKAGE1SALE)) {
      return PACKAGE_1_SALE_PRICE;
    }
    if (packageName.equals(PACKAGE2SALE)) {
      return PACKAGE_2_SALE_PRICE;
    }
    if (packageName.equals(PACKAGE3SALE)) {
      return PACKAGE_3_SALE_PRICE;
    }
    if (packageName.equals(PACKAGE4SALE)) {
      return PACKAGE_4_SALE_PRICE;
    }
    if (packageName.equals(PACKAGE5SALE)) {
      return PACKAGE_5_SALE_PRICE;
    }
    if (packageName.equals(PACKAGES1)) {
      return PACKAGE_S1_PRICE;
    }
    if (packageName.equals(PACKAGES2)) {
      return PACKAGE_S2_PRICE;
    }
    if (packageName.equals(PACKAGES3)) {
      return PACKAGE_S3_PRICE;
    }
    if (packageName.equals(PACKAGES4)) {
      return PACKAGE_S4_PRICE;
    }
    if (packageName.equals(PACKAGES5)) {
      return PACKAGE_S5_PRICE;
    }
    if (packageName.equals(PACKAGES1SALE)) {
      return PACKAGE_S1_SALE_PRICE;
    }
    if (packageName.equals(PACKAGES2SALE)) {
      return PACKAGE_S2_SALE_PRICE;
    }
    if (packageName.equals(PACKAGES3SALE)) {
      return PACKAGE_S3_SALE_PRICE;
    }
    if (packageName.equals(PACKAGES4SALE)) {
      return PACKAGE_S4_SALE_PRICE;
    }
    if (packageName.equals(PACKAGES5SALE)) {
      return PACKAGE_S5_SALE_PRICE;
    }
    return 0;
  }
}
