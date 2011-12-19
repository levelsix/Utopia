package com.lvl6.server;

import org.apache.log4j.*;

public class Listener extends Thread{
  private Logger log = Logger.getLogger("Listener");
  
  public static void main(String args[]) {
    BasicConfigurator.configure();
    Listener server = new Listener();
    server.start();
  }
  
  public Listener() {
    
  }
  
  public void init() {
    log.info("Server initializing");
    loadEventControllers();
  
  }
  
  public void run() {
    
  }
  
  private void loadEventControllers() {
    log.info("Loading event controllers");
    
    
  }
  
}
