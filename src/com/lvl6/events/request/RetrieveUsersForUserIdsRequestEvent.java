package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.RetrieveUsersForUserIdsRequestProto;

public class RetrieveUsersForUserIdsRequestEvent extends RequestEvent {

  private RetrieveUsersForUserIdsRequestProto retrieveUsersForUserIdsRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      retrieveUsersForUserIdsRequestProto = RetrieveUsersForUserIdsRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = retrieveUsersForUserIdsRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public RetrieveUsersForUserIdsRequestProto getRetrieveUsersForUserIdsRequestProto() {
    return retrieveUsersForUserIdsRequestProto;
  }
}
