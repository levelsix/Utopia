package com.lvl6.events;

/**
 * Use this event when the recipient is someone other than the sender.
 */

public abstract class BroadcastEvent extends GameEvent{
  /** number of recipients to send to */
  protected int numRecipients;
  protected int recipients[];
  
  public int[] getRecipients() {
    return recipients;    
  }
  
  public void setRecipients(int[] recipients) {
    this.recipients = recipients;
  }
}
