package com.lvl6.utils;

import java.util.LinkedList;

import com.lvl6.events.ResponseEvent;

/**
 * EventQueue.java
 *
 * Blocking queue of GameEvents.
 */
public class EventQueue {
  private LinkedList <ResponseEvent> events;

  /** 
   * Constructor.  Initializes the logger and event list
   */
  public EventQueue (){
    events = new LinkedList<ResponseEvent>();
  }

  /** 
   * add an event to the queue
   */
  public synchronized void enQueue(ResponseEvent event) {
    events.addLast(event);
    notifyAll();
  }

  /** 
   * blocks until an event is available 
   * and then removes and returns the first 
   * available event
   */
  public synchronized ResponseEvent deQueue() throws InterruptedException {
    while (events.size() == 0) {
      wait();
    }

    ResponseEvent e = (ResponseEvent) events.removeFirst();
    return e;
  }

  /**
   * get the current # of events in the queue
   */
  public synchronized int size() {
    return events.size();
  }

}// EventQueue
