package com.lvl6.utils;

import java.util.HashSet;

public class PlayerSet {
  private HashSet<Integer> players;

  /** 
   * Constructor.  Initializes the logger and event list
   */
  public PlayerSet (){
    players = new HashSet<Integer>();
  }

  /** 
   * lock a player
   * @throws InterruptedException 
   */
  public synchronized void addPlayer(int playerId) {
    while (players.contains(playerId)) {
      try {
        wait();
      } catch (InterruptedException e) {
        // Continue waiting??
      }
    }
    players.add(playerId);
  }

  /** 
   * blocks until an event is available 
   * and then removes and returns the first 
   * available event
   */
  public synchronized void removePlayer(int playerId) {
    players.remove(playerId);
    notifyAll();
  }
  
  public synchronized boolean containsPlayer(int playerId) {
    return players.contains(playerId);
  }

}// EventQueue
