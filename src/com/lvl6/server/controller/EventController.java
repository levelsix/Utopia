package com.lvl6.server.controller;
import java.util.Collection;
import java.util.Iterator;

import com.lvl6.events.GameEvent;
import com.lvl6.server.GameServer;
import com.lvl6.utils.Globals;
import com.lvl6.utils.Player;
import com.lvl6.utils.Wrap;

public abstract class EventController extends Wrap {
    
  /** reference to the GameServer */
  protected GameServer server;

  /**
   * GameServer will call this init method immediately after construction.
   * It is final so that this initialization does not got overridden by subclasses.
   * Initialization for subclasses is done in the initController() method below.
   */
  public final void init(GameServer s) {
    this.server = s;

    // todo: get the preferred number of workers from the GameConfig
    //  int nw = gc.getInt("NUM_WORKERS", 5);

    // init the Wrap first
    initWrap(Globals.DEFAULT_CONTROLLER_WORKERS);
    // now call the subclasses' init
    initController();
  }
  
  /**
   * utility method for sending events
   */
  protected void sendEvent(GameEvent e, Player p) {
    e.setPlayerId(p.getPlayerId());
    server.writeEvent(e);
  }

  /** 
   * utility method for sending events to multiple players
   */
  protected synchronized void sendBroadcastEvent(GameEvent e, Collection<Player> players) {
    Iterator<Player> i = players.iterator();
    int[] recipients = new int[players.size()];
    int j=0;
    while(i.hasNext()) {
      Player p = i.next();
      if (p.getPlayerId() != e.getPlayerId()) 
        recipients[j++] = p.getPlayerId();
    }
    e.setRecipients(recipients);
    server.writeEvent(e);
  }
  
  /** 
   * GameController subclasses should implement initController 
   * in order to do any initialization they require.
   */
  protected abstract void initController();

  /** 
   * factory method for fetching GameEvent objects
   */
  public abstract GameEvent createEvent();

  /**
   * subclasses must implement to provide their Event type
   */
  public abstract byte getEventType();

  
}
