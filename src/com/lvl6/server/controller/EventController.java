package com.lvl6.server.controller;

import org.apache.log4j.Logger;

import com.lvl6.events.GameEvent;
import com.lvl6.events.RequestEvent;
import com.lvl6.properties.Globals;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.server.GameServer;
import com.lvl6.utils.Wrap;
import com.lvl6.utils.utilmethods.MiscMethods;

public abstract class EventController extends Wrap{

  /** reference to the GameServer */
  protected GameServer server;

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());
  
  /**
   * GameServer will call this init method immediately after construction.
   * It is final so that this initialization does not got overridden by subclasses.
   * Initialization for subclasses is done in the initController() method below.
   * @param numThreads 
   */
  public final void init(GameServer s, int numThreads) {
    this.server = s;

    if (numThreads >= 1) {
      // init the Wrap first
      initWrap(numThreads);

      // now call the subclasses' init
      initController();
    }
  }

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
   */
  protected void processEvent(GameEvent event) {
    RequestEvent reqEvent = (RequestEvent) event;
    MiscMethods.setMDCProperties(null, reqEvent.getPlayerId(), MiscMethods.getIPOfPlayer(server, reqEvent.getPlayerId(), null));
    log.info("Received event: " + event.toString());
    
    final long startTime = System.nanoTime();
    final long endTime;
    try {
      processRequestEvent(reqEvent);
    } catch (Exception e) {
      log.error("error with processing event.", e);
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

  protected abstract void processRequestEvent(RequestEvent event) throws Exception;

  protected int numAllocatedThreads = 0;

  public int getNumAllocatedThreads() {
    return numAllocatedThreads;
  }

}
