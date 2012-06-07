package com.lvl6.server.controller;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;

import com.lvl6.events.GameEvent;
import com.lvl6.events.RequestEvent;
import com.lvl6.properties.Globals;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.server.GameServer;
import com.lvl6.utils.Wrap;
import com.lvl6.utils.utilmethods.MiscMethods;

public abstract class EventController extends Wrap{

	
	@Autowired
  protected GameServer server;

  public GameServer getServer() {
	  return server;
  }
  
  public void setServer(GameServer server) {
		this.server = server;
	}


	private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());
  
  

//  we have the controllers call server.writeEvent manually already
//  /**
//   * utility method for sending events
//   */
//  protected void sendEvent(ResponseEvent e, ConnectedPlayer p) {
//    server.writeEvent(e);
//  }

  /** 
   * GameController subclasses should implement initController 
   * in order to do any initialization they require.
   */
  protected void initController() { }

  /** 
   * factory method for fetching GameEvent objects
   */
  public abstract RequestEvent createRequestEvent();

  /**
   * subclasses must implement to do their processing
   * @throws Exception 
   */
  
  protected void processEvent(GameEvent event) throws Exception {
    RequestEvent reqEvent = (RequestEvent) event;
    MiscMethods.setMDCProperties(null, reqEvent.getPlayerId(), MiscMethods.getIPOfPlayer(server, reqEvent.getPlayerId(), null));
    log.info("Received event: " + event.toString());
    
    final long startTime = System.nanoTime();
    final long endTime;
    try {
      processRequestEvent(reqEvent);
    } catch (Exception e) {
      throw e;
    } finally {
      endTime = System.nanoTime();
    }
    double numSeconds = (endTime-startTime) / 1000000000;
    
    log.info("Finished processing event: " + event.toString() + ", took ~" + numSeconds + " seconds");
    
    if (numSeconds > Globals.NUM_SECONDS_FOR_CONTROLLER_PROCESS_EVENT_LONGTIME_LOG_WARNING) {
      log.warn("event: " + event.toString() + " took over " + Globals.NUM_SECONDS_FOR_CONTROLLER_PROCESS_EVENT_LONGTIME_LOG_WARNING+ " seconds");
    }
    
    MiscMethods.purgeMDCProperties();
  }    

  /**
   * subclasses must implement to provide their Event type
   */
  public abstract EventProtocolRequest getEventType();
  
  
  @Async
  protected abstract void processRequestEvent(RequestEvent event) throws Exception;

  
  protected int numAllocatedThreads = 0;

  public int getNumAllocatedThreads() {
    return numAllocatedThreads;
  }

}
