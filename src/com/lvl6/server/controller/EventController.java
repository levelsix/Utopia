package com.lvl6.server.controller;

import org.apache.log4j.Logger;

import com.lvl6.events.GameEvent;
import com.lvl6.events.RequestEvent;
import com.lvl6.events.ResponseEvent;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.server.GameServer;
import com.lvl6.utils.ConnectedPlayer;
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

  /**
   * utility method for sending events
   */
  protected void sendEvent(ResponseEvent e, ConnectedPlayer p) {
    server.writeEvent(e);
  }

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
    processRequestEvent(reqEvent);
    MiscMethods.purgeMDCProperties();
  }    

  /**
   * subclasses must implement to provide their Event type
   */
  public abstract EventProtocolRequest getEventType();

  protected abstract void processRequestEvent(RequestEvent event);

  protected int numAllocatedThreads = 0;

  public int getNumAllocatedThreads() {
    return numAllocatedThreads;
  }

}
