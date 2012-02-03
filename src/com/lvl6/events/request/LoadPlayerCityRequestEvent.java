package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.LoadPlayerCityRequestProto;

public class LoadPlayerCityRequestEvent extends RequestEvent {

  private LoadPlayerCityRequestProto loadPlayerCityRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      loadPlayerCityRequestProto = LoadPlayerCityRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = loadPlayerCityRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public LoadPlayerCityRequestProto getLoadPlayerCityRequestProto() {
    return loadPlayerCityRequestProto;
  }
}
