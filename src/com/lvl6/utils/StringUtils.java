package com.lvl6.utils;

import java.util.List;

public class StringUtils {

  public static String getListInString(List<String> clauses, String delimiter) {
    String toreturn = "";
    for (String clause : clauses) {
      toreturn += clause + " " + delimiter + " ";
    }
    return toreturn.substring(0, toreturn.length() - delimiter.length() - 1);
  }
}
