package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.RetrieveLeaderboardRequestProto;

public class RetrieveLeaderboardRequestEvent extends RequestEvent{
  private RetrieveLeaderboardRequestProto retrieveLeaderboardRequestProto;
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  @Override
  public void read(ByteBuffer buff) {
    try {
      retrieveLeaderboardRequestProto = RetrieveLeaderboardRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = retrieveLeaderboardRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public RetrieveLeaderboardRequestProto getRetrieveLeaderboardRequestProto() {
    return retrieveLeaderboardRequestProto;
  }
  
}//RetrieveLeaderboardRequestProto