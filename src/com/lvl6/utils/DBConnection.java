package com.lvl6.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
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

  public static ResultSet selectRowById(int Id, String tablename) {
    String query = "select * from " + tablename + " where id=?";
     
    ResultSet rs = null;
    try {
      Connection conn = availableConnections.take();
      PreparedStatement stmt = conn.prepareStatement(query);
      stmt.setInt(1, Id);
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
  
  /*
   * assumes that params.length = number of ? fields
   */
  public static ResultSet selectRows(TreeMap<String, Object> paramsToVals, String tablename) {
    String query = "select * from " + tablename;
    
    List<String> condclauses = new LinkedList<String>();
    List<Object> values = new LinkedList<Object>();

    
    if (paramsToVals != null && paramsToVals.size()>0) {
      for (String param : paramsToVals.keySet()) {
        condclauses.add(param + "=?");
        values.add(paramsToVals.get(param));
      }

      query += " where ";
      query += StringUtils.getListInString(condclauses, "and");
    }
    
    ResultSet rs = null;
    try {
      Connection conn = availableConnections.take();
      PreparedStatement stmt = conn.prepareStatement(query);
      if (paramsToVals != null && paramsToVals.size()>0) {
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

