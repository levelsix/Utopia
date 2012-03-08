package com.lvl6.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import com.lvl6.properties.DBConstants;
import com.lvl6.properties.DBProperties;
import com.lvl6.utils.utilmethods.StringUtils;
import com.mysql.jdbc.Statement;

public class DBConnection {
  // log4j logger
  protected static Logger log;

  private static final TimeZone timeZone = TimeZone.getDefault();

  private static final int NUM_CONNECTIONS = 5;
  private static BlockingQueue<Connection> availableConnections;

  private static String user = DBProperties.USER;
  private static String password = DBProperties.PASSWORD;
  private static String server = DBProperties.SERVER;
  private static String database = DBProperties.DATABASE;

  private static final int SELECT_LIMIT_NOT_SET = -1;

  public static void init() {
    log = Logger.getLogger(DBConnection.class);
    availableConnections = new LinkedBlockingQueue<Connection>();
    try {
      Class.forName("com.mysql.jdbc.Driver");
      log.info("creating DB connections");
      for (int i = 0; i < NUM_CONNECTIONS; i++) {
        Properties connectionProps = new Properties();
        connectionProps.put("user", user);
        connectionProps.put("password",password);
        connectionProps.put("useAffectedRows", "true");
        Connection conn = DriverManager.getConnection("jdbc:mysql://" + server, connectionProps);
        conn.createStatement().executeQuery("USE " + database);
        conn.createStatement().executeQuery("SET time_zone='"+timeZone.getID()+"'");
        availableConnections.put(conn);
        log.info("connection added");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public static ResultSet selectRowsByUserId(int userId, String tablename) {
    return selectRowsByIntAttr(null, DBConstants.GENERIC__USER_ID, userId, tablename);
  }

  public static ResultSet selectRowsById(int id, String tablename) {
    return selectRowsByIntAttr(null, DBConstants.GENERIC__ID, id, tablename);
  }

  public static ResultSet selectWholeTable(String tablename) {
    return selectRows(null, null, null, null, tablename, null, null, false, SELECT_LIMIT_NOT_SET, false);
  }

  public static ResultSet selectRowsAbsoluteOr(Map<String, Object> absoluteConditionParams, String tablename) {
    return selectRows(null, absoluteConditionParams, null, null, tablename, "or", null, false, SELECT_LIMIT_NOT_SET, false);
  }

  public static ResultSet selectRowsAbsoluteAnd(Map<String, Object> absoluteConditionParams, String tablename) {
    return selectRows(null, absoluteConditionParams, null, null, tablename, "and", null, false, SELECT_LIMIT_NOT_SET, false);
  }

  public static ResultSet selectRowsAbsoluteAndOrderbydesc(Map<String, Object> absoluteConditionParams, 
      String tablename, String orderByColumn) {
    return selectRows(null, absoluteConditionParams, null, null, tablename, "and", orderByColumn, false, SELECT_LIMIT_NOT_SET, false);
  }

  public static ResultSet selectRowsAbsoluteAndOrderbydescLimit(Map<String, Object> absoluteConditionParams, 
      String tablename, String orderByColumn, int limit) {
    return selectRows(null, absoluteConditionParams, null, null, tablename, "and", orderByColumn, false, limit, false);
  }

  public static ResultSet selectRowsAbsoluteAndOrderbydescLimitLessthan(Map<String, Object> absoluteConditionParams, 
      String tablename, String orderByColumn, int limit, Map<String, Object> lessThanConditionParams) {
    return selectRows(null, absoluteConditionParams, null, lessThanConditionParams, tablename, "and", orderByColumn, false, limit, false);
  }

  public static ResultSet selectRowsAbsoluteAndLimitLessthanGreaterthanRand(Map<String, Object> absoluteConditionParams, 
      String tablename, String orderByColumn, int limit, Map<String, Object> lessThanConditionParams, 
      Map<String, Object> greaterThanConditionParams) {
    return selectRows(null, absoluteConditionParams, greaterThanConditionParams, lessThanConditionParams, tablename, "and", null, false, limit, true);
  }

  public static ResultSet selectRowsAbsoluteAndOrderbydescGreaterthan(Map<String, Object> absoluteConditionParams, 
      String tablename, String orderByColumn, Map<String, Object> greaterThanConditionParams) {
    return selectRows(null, absoluteConditionParams, greaterThanConditionParams, null, tablename, "and", orderByColumn, false, SELECT_LIMIT_NOT_SET, false);
  }


  /*assumes number of ? in the query = values.size()*/
  public static ResultSet selectDirectQueryNaive(String query, List<Object> values) {
    ResultSet rs = null;
    Connection conn = null;

    try {
      conn = availableConnections.take();
      PreparedStatement stmt = conn.prepareStatement(query);
      if (values != null && values.size()>0) {
        int i = 1;
        for (Object value : values) {
          stmt.setObject(i, value);
          i++;
        }
      }
      rs = stmt.executeQuery();
      availableConnections.put(conn);
      conn = null;
    } catch (SQLException e) {
      log.error("problem with " + query, e);
    } catch (NullPointerException e) {
      log.error("problem with " + query, e);
    } catch (InterruptedException e) {
      log.error("problem with " + query, e);
    } finally {
      if (conn != null) {
        try {
          availableConnections.put(conn);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
    return rs;
  }



  /*
   * returns num of rows affected
   */
  public static int updateTableRows(String tablename, Map<String, Object> relativeParams, 
      Map<String, Object> absoluteParams, Map<String, Object> conditionParams, String condDelim) {
    String query = "update " + tablename;
    List<Object> values = new LinkedList<Object>();

    List<String> relUpClauses = null;
    if ((relativeParams != null && relativeParams.size()>0) || (absoluteParams != null && absoluteParams.size()>0)) {
      query += " set ";
      if (relativeParams != null && relativeParams.size()>0) {

        relUpClauses = new LinkedList<String>();
        for (String param : relativeParams.keySet()) {
          relUpClauses.add(param + "=" + param + "+?");
          values.add(relativeParams.get(param));
        }
        query += StringUtils.getListInString(relUpClauses, ",");
      }
      if (absoluteParams != null && absoluteParams.size()>0) {
        List<String> absUpClauses = new LinkedList<String>();
        for (String param : absoluteParams.keySet()) {
          absUpClauses.add(param + "=?");
          values.add(absoluteParams.get(param));
        }
        if (relUpClauses != null && relUpClauses.size() > 0) {
          query += ",";
        }
        query += StringUtils.getListInString(absUpClauses, ",");
      }
    } else {
      return 0;
    }

    if (conditionParams != null && conditionParams.size()>0) {
      query += " where ";
      List<String> condClauses = new LinkedList<String>();
      for (String param : conditionParams.keySet()) {
        condClauses.add(param + "=?");
        values.add(conditionParams.get(param));
      }
      query += StringUtils.getListInString(condClauses, condDelim);
    }

    Connection conn = null;
    try {
      conn = availableConnections.take();
      PreparedStatement stmt = conn.prepareStatement(query);
      if (values.size()>0) {
        int i = 1;
        for (Object value : values) {
          stmt.setObject(i, value);
          i++;
        }
      }
      int numUpdated = stmt.executeUpdate();
      availableConnections.put(conn);
      conn = null;
      return numUpdated;
    } catch (SQLException e) {
      log.error("problem with " + query, e);
      e.printStackTrace();
    } catch (InterruptedException e) {
      log.error("problem with " + query, e);
      e.printStackTrace();
    } finally {
      if (conn != null) {
        try {
          availableConnections.put(conn);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
    return 0;
  }

  public static int insertIntoTableBasic(String tablename, Map<String, Object> insertParams) {
    List<String> questions = new LinkedList<String>();
    List<String> columns = new LinkedList<String>();
    List<Object> values = new LinkedList<Object>();

    if (insertParams != null && insertParams.size() > 0) {
      for (String column : insertParams.keySet()) {
        questions.add("?");
        columns.add(column);
        values.add(insertParams.get(column));
      }
      String query = "insert into " + tablename + "(" + StringUtils.getListInString(columns, ",") + ") VALUES (" +
          StringUtils.getListInString(questions, ",") + ")";

      Connection conn = null;
      try {
        conn = availableConnections.take();
        PreparedStatement stmt = conn.prepareStatement(query);
        if (values.size()>0) {
          int i = 1;
          for (Object value : values) {
            stmt.setObject(i, value);
            i++;
          }
        }
        int numUpdated = stmt.executeUpdate();
        availableConnections.put(conn);
        conn = null;
        return numUpdated;
      } catch (SQLException e) {
        log.error("problem with " + query, e);
        e.printStackTrace();
      } catch (InterruptedException e) {
        log.error("problem with " + query, e);
        e.printStackTrace();
      } finally {
        if (conn != null) {
          try {
            availableConnections.put(conn);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
    }
    return 0;
  }

  /*
   * assumes every list for each column is numRows length
   */
  public static int insertIntoTableMultipleRows(String tablename, Map<String, List<Object>> insertParams, int numRows) {
    List<String> questionsPerRow = new LinkedList<String>();
    List<String> columns = new LinkedList<String>();
    List<Object> values = new LinkedList<Object>();

    if (numRows > 0 && insertParams != null && insertParams.size() > 0) {
      boolean firstTime = true;
      for (int i = 0; i < numRows; i++) {
        for (String column : insertParams.keySet()) {
          List<Object> valuesForColumn = insertParams.get(column);
          values.add(valuesForColumn.get(i));

          if (firstTime) {
            if (valuesForColumn.size() != numRows) {
              return 0;
            }
            columns.add(column);
            questionsPerRow.add("?");
          }
        }
        firstTime = false;
      }

      String query = "insert into " + tablename + "(" + StringUtils.getListInString(columns, ",") + ") VALUES ";

      List<String> valuesStrings = new ArrayList<String>();
      String rowQuestionsString = StringUtils.getListInString(questionsPerRow, ",");
      for (int i = 0; i < numRows; i++) {
        valuesStrings.add("(" + rowQuestionsString + ")");
      }
      query += StringUtils.getListInString(valuesStrings, ",");

      Connection conn = null;
      try {
        conn = availableConnections.take();
        PreparedStatement stmt = conn.prepareStatement(query);
        if (values.size()>0) {
          int i = 1;
          for (Object value : values) {
            stmt.setObject(i, value);
            i++;
          }
        }
        int numUpdated = stmt.executeUpdate();
        availableConnections.put(conn);
        conn = null;
        return numUpdated;
      } catch (SQLException e) {
        log.error("problem with " + query, e);
        e.printStackTrace();
      } catch (InterruptedException e) {
        log.error("problem with " + query, e);
        e.printStackTrace();
      } finally {
        if (conn != null) {
          try {
            availableConnections.put(conn);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
    }
    return 0;
  }

  /*returns 0 if error*/
  public static int insertIntoTableBasicReturnId(String tablename, Map<String, Object> insertParams) {
    List<String> questions = new LinkedList<String>();
    List<String> columns = new LinkedList<String>();
    List<Object> values = new LinkedList<Object>();

    if (insertParams != null && insertParams.size() > 0) {
      for (String column : insertParams.keySet()) {
        questions.add("?");
        columns.add(column);
        values.add(insertParams.get(column));
      }
      String query = "insert into " + tablename + "(" + StringUtils.getListInString(columns, ",") + ") VALUES (" +
          StringUtils.getListInString(questions, ",") + ")";
      Connection conn = null;
      try {
        conn = availableConnections.take();
        PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        if (values.size()>0) {
          int i = 1;
          for (Object value : values) {
            stmt.setObject(i, value);
            i++;
          }
        }
        int numUpdated = stmt.executeUpdate();
        availableConnections.put(conn);
        conn = null;
        int generatedKey = 0;
        if (numUpdated == 1) {
          ResultSet rs = stmt.getGeneratedKeys();
          if (rs.next()){
            generatedKey = rs.getInt(1);
          }
        }
        return generatedKey;
      } catch (SQLException e) {
        log.error("problem with " + query, e);
        e.printStackTrace();
      } catch (InterruptedException e) {
        log.error("problem with " + query, e);
        e.printStackTrace();
      } finally {
        if (conn != null) {
          try {
            availableConnections.put(conn);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
    }
    return 0;
  }

  public static int insertOnDuplicateKeyRelativeUpdate(String tablename, Map<String, Object> insertParams,
      String columnUpdate, Object updateQuantity) {

    List<String> questions = new LinkedList<String>();
    List<String> columns = new LinkedList<String>();
    List<Object> values = new LinkedList<Object>();

    if (insertParams != null && insertParams.size() > 0) {
      for (String column : insertParams.keySet()) {
        questions.add("?");
        columns.add(column);
        values.add(insertParams.get(column));
      }
      values.add(updateQuantity);
      String query = "insert into " + tablename + "(" + StringUtils.getListInString(columns, ",") + ") VALUES (" +
          StringUtils.getListInString(questions, ",") + ") on duplicate key update " + columnUpdate + "=" +
          columnUpdate + "+?";
      Connection conn = null;
      try {
        conn = availableConnections.take();
        PreparedStatement stmt = conn.prepareStatement(query);
        if (values.size()>0) {
          int i = 1;
          for (Object value : values) {
            stmt.setObject(i, value);
            i++;
          }
        }
        int numUpdated = stmt.executeUpdate();
        availableConnections.put(conn);
        conn = null;
        return numUpdated;
      } catch (SQLException e) {
        log.error("problem with " + query, e);
        e.printStackTrace();
      } catch (InterruptedException e) {
        log.error("problem with " + query, e);
        e.printStackTrace();
      } finally {
        if (conn != null) {
          try {
            availableConnections.put(conn);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
    }
    return 0;
  }

  /*
   * returns num of rows affected
   */
  public static int deleteRows(String tablename, Map<String, Object> absoluteConditionParams, String condDelim) {
    String query = "delete from " + tablename;
    List<Object> values = new LinkedList<Object>();

    if (absoluteConditionParams != null && absoluteConditionParams.size()>0) {
      query += " where ";
      List<String> condClauses = new LinkedList<String>();
      for (String param : absoluteConditionParams.keySet()) {
        condClauses.add(param + "=?");
        values.add(absoluteConditionParams.get(param));
      }
      query += StringUtils.getListInString(condClauses, condDelim);
    }

    Connection conn = null;
    try {
      conn = availableConnections.take();
      PreparedStatement stmt = conn.prepareStatement(query);
      if (values.size()>0) {
        int i = 1;
        for (Object value : values) {
          stmt.setObject(i, value);
          i++;
        }
      }
      int numDeleted = stmt.executeUpdate();
      availableConnections.put(conn);
      conn = null;
      return numDeleted;
    } catch (SQLException e) {
      log.error("problem with " + query, e);
      e.printStackTrace();
    } catch (InterruptedException e) {
      log.error("problem with " + query, e);
      e.printStackTrace();
    } finally {
      if (conn != null) {
        try {
          availableConnections.put(conn);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
    return 0;
  }


  private static ResultSet selectRowsByIntAttr(List<String> columns, String attr, int value, 
      String tablename) {
    String query = "select ";
    if (columns != null) {
      query += StringUtils.getListInString(columns, ",");
    } else {
      query += "* ";
    }
    query += " from " + tablename + " where " + attr + "=?";

    ResultSet rs = null;
    Connection conn = null;
    try {
      conn = availableConnections.take();
      PreparedStatement stmt = conn.prepareStatement(query);
      stmt.setInt(1, value);
      rs = stmt.executeQuery();
      availableConnections.put(conn);
      conn = null;
    } catch (SQLException e) {
      log.error("problem with " + query, e);
    } catch (NullPointerException e) {
      log.error("problem with " + query, e);
    } catch (InterruptedException e) {
      log.error("problem with " + query, e);
    } finally {
      if (conn != null) {
        try {
          availableConnections.put(conn);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
    return rs;
  }

  private static ResultSet selectRows(List<String> columns, Map<String, Object> absoluteConditionParams, 
      Map<String, Object> relativeGreaterThanConditionParams, Map<String, Object> relativeLessThanConditionParams, 
      String tablename, String conddelim, String orderByColumn, boolean orderByAsc, int limit, boolean random) {
    String query = "select ";
    if (columns != null) {
      query += StringUtils.getListInString(columns, ",");
    } else {
      query += "* ";
    }
    query += " from " + tablename;

    List<String> absoluteCondClauses = new LinkedList<String>();
    List<String> lessthanCondClauses = new LinkedList<String>();
    List<String> greaterthanCondClauses = new LinkedList<String>();

    List<Object> values = new LinkedList<Object>();

    boolean useWhere = true;
    if (absoluteConditionParams != null && absoluteConditionParams.size()>0) {
      for (String param : absoluteConditionParams.keySet()) {
        absoluteCondClauses.add(param + "=?");
        values.add(absoluteConditionParams.get(param));
      }

      if (useWhere) {
        query += " where (";
      } else {
        query += " and (";
      }
      useWhere = false;
      query += StringUtils.getListInString(absoluteCondClauses, conddelim);
      query += " ) ";
    }

    if (relativeGreaterThanConditionParams != null && relativeGreaterThanConditionParams.size()>0) {
      for (String param : relativeGreaterThanConditionParams.keySet()) {
        greaterthanCondClauses.add(param + ">?");
        values.add(relativeGreaterThanConditionParams.get(param));
      }

      if (useWhere) {
        query += " where (";
      } else {
        query += " and (";
      }
      useWhere = false;
      query += StringUtils.getListInString(greaterthanCondClauses, conddelim);
      query += " ) ";
    }

    if (relativeLessThanConditionParams != null && relativeLessThanConditionParams.size()>0) {
      for (String param : relativeLessThanConditionParams.keySet()) {
        lessthanCondClauses.add(param + "<?");
        values.add(relativeLessThanConditionParams.get(param));
      }

      if (useWhere) {
        query += " where (";
      } else {
        query += " and (";
      }
      useWhere = false;
      query += StringUtils.getListInString(lessthanCondClauses, conddelim);
      query += " ) ";
    }

    if (orderByColumn != null) {
      query += " order by " + orderByColumn;
      if (!orderByAsc) {
        query += " desc";
      }
    } else if (random) {
      query += " order by rand() ";
    }

    if (limit != SELECT_LIMIT_NOT_SET) {
      query += " limit " + limit;
    }

    ResultSet rs = null;
    Connection conn = null;
    try {
      conn = availableConnections.take();
      PreparedStatement stmt = conn.prepareStatement(query);
      if (values.size()>0) {
        int i = 1;
        for (Object value : values) {
          stmt.setObject(i, value);
          i++;
        }
      }
      rs = stmt.executeQuery();
      availableConnections.put(conn);
      conn = null;
    } catch (SQLException e) {
      log.error("problem with " + query, e);
    } catch (NullPointerException e) {
      log.error("problem with " + query, e);
    } catch (InterruptedException e) {
      log.error("problem with " + query, e);
    } finally {
      if (conn != null) {
        try {
          availableConnections.put(conn);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
    return rs;
  }
}

