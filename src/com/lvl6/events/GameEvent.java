package com.lvl6.events;

public abstract class GameEvent {
  protected int playerId;   //refers to whoever sent the event/triggered it

  public int getPlayerId() {
    return playerId;
  }
}
