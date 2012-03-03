package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.LoadNeutralCityRequestProto;

public class LoadNeutralCityRequestEvent extends RequestEvent {

  private LoadNeutralCityRequestProto loadNeutralCityRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      loadNeutralCityRequestProto = LoadNeutralCityRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = loadNeutralCityRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public LoadNeutralCityRequestProto getLoadNeutralCityRequestProto() {
    return loadNeutralCityRequestProto;
  }
}
