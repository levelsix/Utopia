package com.lvl6.utils.utilmethods;

import java.util.List;

import org.apache.log4j.Logger;

public class StringUtils {
  
  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());


  public static String getListInString(List<String> clauses, String delimiter) {
    String toreturn = "";
    if (clauses == null || clauses.size() < 1) {
      log.error("invalid parameters passed into StringUtils getListInString. clauses=" + clauses + ", delimiter=" + delimiter);
      return "";
    }
    for (String clause : clauses) {
      toreturn += clause + " " + delimiter + " ";
    }
    return toreturn.substring(0, toreturn.length() - delimiter.length() - 1);
  }
}
