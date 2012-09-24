package com.lvl6.utils;

import org.apache.log4j.Logger;

import com.lvl6.events.GameEvent;
import com.lvl6.events.ResponseEvent;


public abstract class Wrap implements Runnable{
  // log4j logger

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());


  public final void initWrap(int numWorkers) {

  }


  public void shutdown () {

  }


  public void handleEvent(GameEvent event) {
    try {
      processEvent(event);
    } catch (Exception e) {
      log.error("Error handling event: "+event, e);
    }
  }

  
  public void handleClanEvent(ResponseEvent event, int clanId) {
	    try {
	    	processClanResponseEvent(event, clanId);
		} catch (Exception e) {
			log.error("Error handling clan event: "+event, e);
		}
	  }
  

  public void run() {

  }


  /**
   * subclasses must implement to do their processing
   * @throws Exception 
   */

  protected abstract void processEvent(GameEvent event) throws Exception;
  public void processClanResponseEvent(GameEvent event, int clanId) {}
}
