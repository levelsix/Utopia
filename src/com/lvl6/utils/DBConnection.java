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

  public static ResultSet selectRowByUserId(int userId, String tablename) {
    return selectRowByIntAttr("user_id", userId, tablename);
  }

  public static ResultSet selectRowById(int id, String tablename) {
    return selectRowByIntAttr("id", id, tablename);
  }

  public static ResultSet selectWholeTable(String tablename) {
    return selectRows(null, tablename, null);
  }

  public static ResultSet selectRowsOr(Map<String, Object> conditionParams, String tablename) {
    return selectRows(conditionParams, tablename, "or");
  }

  public static ResultSet selectRowsAnd(Map<String, Object> conditionParams, String tablename) {
    return selectRows(conditionParams, tablename, "and");
  }


  /*
   * Update a row in a table with both absolute and relative values
   * i.e. combines the functionality of updateTableRowRelative and updateTableRowAbsolute
   * $absParams, $relParams, and $conditions should be associative arrays from column names to values

  public static function updateTableRowGenericBasic($tablename, $absParams, $relParams, $conditions) {
    $mydb = self::getFactory()->getConnection();
    //TODO: after refactor, just eliminate getFactory, change getConnection to static, and call that?

    $values = array();

    $absSetClauses = array();
    foreach($absParams as $key=>$value) {
      $absSetClauses[] = $key . "=?";
      $values[] = $value;
    }

    $relSetClauses = array();
    foreach($relParams as $key=>$value) {
      $relSetClauses[] = $key . "=" . $key . "+?";
      $values[] = $value;
    }

    $condclauses = array();
    foreach($conditions as $key=>$value) {
      $condclauses[] = $key."=?";
      $values[] = $value;
    }

    $stmtString = "UPDATE ". $tablename . " SET ";
    $stmtString .= getArrayInString($absSetClauses, ',') . ", " . getArrayInString($relSetClauses, ',');
    $stmtString .= " WHERE ";
    $stmtString .= getArrayInString($condclauses, 'and');

    $stmt = $mydb->prepare($stmtString);

    $start = microtime(true);  
                $result = $stmt->execute($values);                                
                $time = microtime(true) - $start;  
                self::$log[] = array('query' => $stmt->queryString,
                          'time' => round($time * 1000, 3));   
                return $result;
  }
   * 
   
  public static void updateTableRowsBasic(String tablename, Map<String, Object> relativeParams, 
      Map<String, Object> absoluteParams, Map<String, Object> conditionParams) {
    
    
    ResultSet rs = null;
    try {
      Connection conn = availableConnections.take();
      PreparedStatement stmt = conn.prepareStatement(query);
      stmt.setInt(1, value);
      stmt.execute();
      
      rs = stmt.getResultSet();
      log.info(rs.toString());
    } catch (SQLException e) {
      System.out.println("problem with database call.");
      e.printStackTrace();
    }
  }*/

  private static ResultSet selectRowByIntAttr(String attr, int value, String tablename) {
    String query = "select * from " + tablename + " where " + attr + "=?";

    ResultSet rs = null;
    try {
      Connection conn = availableConnections.take();
      PreparedStatement stmt = conn.prepareStatement(query);
      stmt.setInt(1, value);
      stmt.execute();
      rs = stmt.getResultSet();
      log.info(rs.toString());
    } catch (SQLException e) {
      System.out.println("problem with database call.");
      e.printStackTrace();
    } catch (NullPointerException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return rs;
  }

  private static ResultSet selectRows(Map<String, Object> conditionParams, String tablename, String conddelim) {
    String query = "select * from " + tablename;

    List<String> condclauses = new LinkedList<String>();
    List<Object> values = new LinkedList<Object>();


    if (conditionParams != null && conditionParams.size()>0) {
      for (String param : conditionParams.keySet()) {
        condclauses.add(param + "=?");
        values.add(conditionParams.get(param));
      }

      query += " where ";
      query += StringUtils.getListInString(condclauses, conddelim);
    }

    ResultSet rs = null;
    try {
      Connection conn = availableConnections.take();
      PreparedStatement stmt = conn.prepareStatement(query);
      if (conditionParams != null && conditionParams.size()>0) {
        int i = 1;
        for (Object value : values) {
          stmt.setObject(i, value);
          i++;
        }
      }
      stmt.execute();
      rs = stmt.getResultSet();
      log.info(rs.toString());
    } catch (SQLException e) {
      System.out.println("problem with database call.");
      e.printStackTrace();
    } catch (NullPointerException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return rs;
  }
}

