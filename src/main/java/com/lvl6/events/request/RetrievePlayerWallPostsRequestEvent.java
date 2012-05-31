package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.RetrievePlayerWallPostsRequestProto;

public class RetrievePlayerWallPostsRequestEvent extends RequestEvent{
  private RetrievePlayerWallPostsRequestProto retrievePlayerWallPostsRequestProto;
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  @Override
  public void read(ByteBuffer buff) {
    try {
      retrievePlayerWallPostsRequestProto = RetrievePlayerWallPostsRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = retrievePlayerWallPostsRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public RetrievePlayerWallPostsRequestProto getRetrievePlayerWallPostsRequestProto() {
    return retrievePlayerWallPostsRequestProto;
  }
  
}//RetrievePlayerWallPostsRequestProto