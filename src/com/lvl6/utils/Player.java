package com.lvl6.utils;

import java.nio.channels.SocketChannel;

/**
 * Player.java
 *
 * Basic Player information
 */
public class Player {
  private String playerId;
  private SocketChannel channel;

  public String getPlayerId() {
    return playerId;
  }
  public void setPlayerId(String id) {
    playerId = id;
  }
  
  public SocketChannel getChannel() {
    return channel;
  }
  public void setChannel(SocketChannel channel) {
    this.channel = channel;
  }
}
