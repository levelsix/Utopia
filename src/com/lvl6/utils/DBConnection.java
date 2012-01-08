package com.lvl6.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import com.lvl6.properties.DBConstants;
import com.lvl6.properties.DBProperties;

public class DBConnection {
  // log4j logger
  protected static Logger log;

  private static final int NUM_CONNECTIONS = 2;
  private static BlockingQueue<Connection> availableConnections;

  private static String user = DBProperties.USER;
  private static String password = DBProperties.PASSWORD;
  private static String server = DBProperties.SERVER;
  private static String database = DBProperties.DATABASE;

  private static Connection conn;

  public static void init() {
    log = Logger.getLogger(DBConnection.class);
    availableConnections = new LinkedBlockingQueue<Connection>();
    try {
      Class.forName("com.mysql.jdbc.Driver");
      log.info("creating DB connections");
      for (int i = 0; i < NUM_CONNECTIONS; i++) {
        conn = DriverManager.getConnection("jdbc:mysql://" + server, user ,password);
        conn.createStatement().executeQuery("USE " + database);
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
    return selectRows(null, null, tablename, null);
  }

  public static ResultSet selectRowsOr(Map<String, Object> conditionParams, String tablename) {
    return selectRows(null, conditionParams, tablename, "or");
  }

  public static ResultSet selectRowsAnd(Map<String, Object> conditionParams, String tablename) {
    return selectRows(null, conditionParams, tablename, "and");
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

    try {
      Connection conn = availableConnections.take();
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
      return numUpdated;
    } catch (SQLException e) {
      log.error("problem with " + query, e);
      e.printStackTrace();
    } catch (InterruptedException e) {
      log.error("problem with " + query, e);
      e.printStackTrace();
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
      try {
        Connection conn = availableConnections.take();
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
        return numUpdated;
      } catch (SQLException e) {
        log.error("problem with " + query, e);
        e.printStackTrace();
      } catch (InterruptedException e) {
        log.error("problem with " + query, e);
        e.printStackTrace();
      }
    }
    return 0;
  }

  /*
   * returns num of rows affected
   */
  public static int deleteRows(String tablename, Map<String, Object> conditionParams, String condDelim) {
    String query = "delete from " + tablename;
    List<Object> values = new LinkedList<Object>();

    if (conditionParams != null && conditionParams.size()>0) {
      query += " where ";
      List<String> condClauses = new LinkedList<String>();
      for (String param : conditionParams.keySet()) {
        condClauses.add(param + "=?");
        values.add(conditionParams.get(param));
      }
      query += StringUtils.getListInString(condClauses, condDelim);
    }

    try {
      Connection conn = availableConnections.take();
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
      return numDeleted;
    } catch (SQLException e) {
      log.error("problem with " + query, e);
      e.printStackTrace();
    } catch (InterruptedException e) {
      log.error("problem with " + query, e);
      e.printStackTrace();
    }
    return 0;
  }


  private static ResultSet selectRowsByIntAttr(List<String> columns, String attr, int value, String tablename) {
    String query = "select ";
    if (columns != null) {
      query += StringUtils.getListInString(columns, ",");
    } else {
      query += "* ";
    }
    query += " from " + tablename + " where " + attr + "=?";
    
    ResultSet rs = null;
    try {
      Connection conn = availableConnections.take();
      PreparedStatement stmt = conn.prepareStatement(query);
      stmt.setInt(1, value);
      rs = stmt.executeQuery();
      availableConnections.put(conn);
      log.info(rs.toString());
    } catch (SQLException e) {
      log.error("problem with " + query, e);
    } catch (NullPointerException e) {
      log.error("problem with " + query, e);
    } catch (InterruptedException e) {
      log.error("problem with " + query, e);
    }
    return rs;
  }

  private static ResultSet selectRows(List<String> columns, Map<String, Object> conditionParams, String tablename, String conddelim) {
    String query = "select ";
    if (columns != null) {
      query += StringUtils.getListInString(columns, ",");
    } else {
      query += "* ";
    }
    query += " from " + tablename;
    
    List<String> condClauses = new LinkedList<String>();
    List<Object> values = new LinkedList<Object>();


    if (conditionParams != null && conditionParams.size()>0) {
      for (String param : conditionParams.keySet()) {
        condClauses.add(param + "=?");
        values.add(conditionParams.get(param));
      }

      query += " where ";
      query += StringUtils.getListInString(condClauses, conddelim);
    }

    ResultSet rs = null;
    try {
      Connection conn = availableConnections.take();
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
      log.info(rs.toString());
    } catch (SQLException e) {
      log.error("problem with " + query, e);
    } catch (NullPointerException e) {
      log.error("problem with " + query, e);
    } catch (InterruptedException e) {
      log.error("problem with " + query, e);
    }
    return rs;
  }
}

