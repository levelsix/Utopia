package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.RetrieveTasksForCityRequestProto;

public class RetrieveTasksForCityRequestEvent extends RequestEvent{

  private RetrieveTasksForCityRequestProto retrieveTasksForCityRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      retrieveTasksForCityRequestProto = RetrieveTasksForCityRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = retrieveTasksForCityRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public RetrieveTasksForCityRequestProto getRetrieveTasksForCityRequestProto() {
    return retrieveTasksForCityRequestProto;
  }
  
}

