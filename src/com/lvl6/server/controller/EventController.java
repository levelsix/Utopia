package com.lvl6.server.controller;

import com.lvl6.events.GameEvent;
import com.lvl6.events.RequestEvent;
import com.lvl6.events.ResponseEvent;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.server.GameServer;
import com.lvl6.utils.ConnectedPlayer;
import com.lvl6.utils.Wrap;

public abstract class EventController extends Wrap{

  /** reference to the GameServer */
  protected GameServer server;

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
    log.info("Received event: " + event.toString());
    processRequestEvent((RequestEvent) event);
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
