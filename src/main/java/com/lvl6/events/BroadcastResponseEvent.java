package com.lvl6.events;

public abstract class BroadcastResponseEvent extends ResponseEvent {

  protected int numRecipients;
  protected int recipients[];
  
  public int[] getRecipients() {
    return recipients;    
  }
  
  public void setRecipients(int[] recipients) {
    this.recipients = recipients;
  }

}
