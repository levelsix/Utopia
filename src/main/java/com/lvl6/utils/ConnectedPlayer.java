package com.lvl6.utils;

import java.nio.channels.SocketChannel;

/**
 * Player.java
 *
 * Basic Player information
 */
public class ConnectedPlayer {
  private int playerId;
  private SocketChannel channel;

  public int getPlayerId() {
    return playerId;
  }
  public void setPlayerId(int id) {
    playerId = id;
  }
  
  public SocketChannel getChannel() {
    return channel;
  }
  public void setChannel(SocketChannel channel) {
    this.channel = channel;
  }
}
