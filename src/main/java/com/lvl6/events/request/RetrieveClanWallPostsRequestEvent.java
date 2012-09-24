package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.RetrieveClanWallPostsRequestProto;

public class RetrieveClanWallPostsRequestEvent extends RequestEvent{
  private RetrieveClanWallPostsRequestProto retrieveClanWallPostsRequestProto;
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  @Override
  public void read(ByteBuffer buff) {
    try {
      retrieveClanWallPostsRequestProto = RetrieveClanWallPostsRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = retrieveClanWallPostsRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public RetrieveClanWallPostsRequestProto getRetrieveClanWallPostsRequestProto() {
    return retrieveClanWallPostsRequestProto;
  }
  
}//RetrieveClanWallPostsRequestProto