package com.lvl6.events;

public abstract class NonBroadcastResponseEvent extends ResponseEvent{
  protected int playerId;   //refers to whoever sent the event/triggered it

  public int getPlayerId() {
    return playerId;
  }
  
  public NonBroadcastResponseEvent(int playerId) {
    this.playerId = playerId;
  }
}
