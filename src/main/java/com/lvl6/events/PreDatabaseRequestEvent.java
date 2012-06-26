package com.lvl6.events;

public abstract class PreDatabaseRequestEvent extends RequestEvent{
  protected String udid;

  public String getUdid() {
    return udid;
  }
}
