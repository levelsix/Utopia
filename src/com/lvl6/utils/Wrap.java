package com.lvl6.utils;

import org.apache.log4j.Logger;

import com.lvl6.events.GameEvent;

public abstract class Wrap implements Runnable{
  // log4j logger
  protected Logger log;

  // ms to sleep between processing runs
  protected static final long WORKER_SLEEP_MILLIS = 10;

  //incoming event queue
  protected EventQueue eventQueue;

  /** are we running? **/
  protected boolean running = false;

  /** our pool of worker threads */
  private Thread workers[];

  /**
   * @param numWorkers number of worker threads to spawn
   */
  public final void initWrap(int numWorkers) {
    //setup the log4j Logger
    log = Logger.getLogger(getClass());
    log.info("initWrap: log started");

    eventQueue = new EventQueue();

    // spawn worker threads
    workers = new Thread[numWorkers];
    for (int i=0; i<numWorkers; i++) {
      workers[i] = new Thread(this);
      workers[i].setDaemon(true);
      workers[i].start();
    }
  }

  /**
   * shutdown the worker threads
   */
  public void shutdown () {
    running = false;
    if (workers != null) {
      for (int i=0;i<workers.length;i++) {
        workers[i].interrupt();
      }
    }
    log.info("shutdown: workers ended");
  }

  /**
   * queue the event for later processing by worker threads
   */
  public void handleEvent(GameEvent event) {
    eventQueue.enQueue(event);
  }

  /** 
   * retrieve events from the queue and process.
   */
  public void run() {
    GameEvent event;
    running = true;
    while (running) {
      try {
        if ((event = eventQueue.deQueue()) != null) {
          processEvent(event);
        }
      }
      catch (InterruptedException e) {
      }
    }
  }

  /**
   * subclasses must implement to do their processing
   */
  protected abstract void processEvent(GameEvent event);
}
