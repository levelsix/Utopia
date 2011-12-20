package com.lvl6.utils;

import java.util.LinkedList;
import org.apache.log4j.Logger;

import com.lvl6.events.GameEvent;

/**
 * EventQueue.java
 *
 * Blocking queue of GameEvents.
 */
public class EventQueue {
  private LinkedList <GameEvent> events;

  /** 
   * Constructor.  Initializes the logger and event list
   */
  public EventQueue (){
    events = new LinkedList<GameEvent>();
  }

  /** 
   * add an event to the queue
   */
  public synchronized void enQueue(GameEvent event) {
    events.addLast(event);
    notifyAll();
  }

  /** 
   * blocks until an event is available 
   * and then removes and returns the first 
   * available event
   */
  public synchronized GameEvent deQueue() throws InterruptedException {
    while (events.size() == 0) {
      wait();
    }

    GameEvent e = (GameEvent) events.removeFirst();
    return e;
  }

  /**
   * get the current # of events in the queue
   */
  public synchronized int size() {
    return events.size();
  }

}// EventQueue
