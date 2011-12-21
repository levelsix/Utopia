package com.lvl6.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import com.lvl6.properties.DBProperties;

public class DBConnection {
  // log4j logger
  protected static Logger log;

  private static final int NUM_CONNECTIONS = 10;
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
      }
      log.info("connection complete");
      selectRows(null, "dumb");
    } catch (SQLException e) {
      e.printStackTrace();
    }
    catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  
  /*
   * assumes that params.length = number of ? fields
   */
  public static ResultSet selectRows(Object[] params, String tablename) {
    String query = "select * from " + tablename;
    ResultSet rs = null;
    try {
      Connection conn = availableConnections.take();
      PreparedStatement stmt = conn.prepareStatement(query);
      if (params != null) {
        for (int i = 1; i <= params.length; i++) {
          stmt.setObject(i, params[i]);
        }
      }
      stmt.execute();
      rs = stmt.getResultSet();
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

