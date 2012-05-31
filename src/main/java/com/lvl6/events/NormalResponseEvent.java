package com.lvl6.events;

public abstract class NormalResponseEvent extends ResponseEvent{
  protected int playerId;   //refers to whoever sent the event/triggered it

  public int getPlayerId() {
    return playerId;
  }
  
  public NormalResponseEvent(int playerId) {
    this.playerId = playerId;
  }
}
