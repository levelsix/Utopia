package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.RetrieveLeaderboardRankingsRequestProto;

public class RetrieveLeaderboardRankingsRequestEvent extends RequestEvent{
  private RetrieveLeaderboardRankingsRequestProto retrieveLeaderboardRankingsRequestProto;
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  @Override
  public void read(ByteBuffer buff) {
    try {
      retrieveLeaderboardRankingsRequestProto = RetrieveLeaderboardRankingsRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = retrieveLeaderboardRankingsRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public RetrieveLeaderboardRankingsRequestProto getRetrieveLeaderboardRankingsRequestProto() {
    return retrieveLeaderboardRankingsRequestProto;
  }
  
}//RetrieveLeaderboardRankingsRequestProto