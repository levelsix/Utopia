package com.lvl6.utils.utilmethods;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringUtils {
  

	private static final Logger log = LoggerFactory.getLogger(StringUtils.class);

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
  
  public  static String csvIntList(List<Integer> clauses) {
    String toreturn = "";
    if (clauses == null || clauses.size() < 1) {
      log.error("invalid parameters passed into StringUtils csvIntList. clauses=" + clauses);
      return "";
    }
    
    for (Object clause : clauses) {
      toreturn += clause + ",";
    }
    return toreturn.substring(0, toreturn.length() - 1);
  }
}
