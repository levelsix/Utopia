package com.lvl6.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;

import com.lvl6.properties.DBProperties;

public class DBConnection {
  // log4j logger
  protected Logger log;

  private static final int NUM_CONNECTIONS = 10;
  private static BlockingQueue<Connection> availableConnections;

  private static String user = DBProperties.USER;
  private static String password = DBProperties.PASSWORD;
  private static String server = DBProperties.SERVER;
  private static String database = DBProperties.DATABASE;

  private static Connection conn;

  public DBConnection() {
    log = Logger.getLogger(getClass());
    try {
      Class.forName("com.mysql.jdbc.Driver");
      log.info("creating DB connections");
      for (int i = 0; i < NUM_CONNECTIONS; i++) {
        conn = DriverManager.getConnection("jdbc:mysql://" + server, user ,password);
        conn.createStatement().executeQuery("USE " + database);
        availableConnections.put(conn);
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

  /*
   * assumes all params will be strings and that params.length = number of ? fields
   */
  public ResultSet readFromDatabase(String query, String[] params) {
    ResultSet rs = null;
    try {
      Connection conn = availableConnections.take();
      PreparedStatement stmt = conn.prepareStatement(query);
      for (int i = 1; i <= params.length; i++) {
        stmt.setString(i, params[i]);
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

