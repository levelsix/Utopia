package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.PostOnPlayerWallRequestProto;

public class PostOnPlayerWallRequestEvent extends RequestEvent {

  private PostOnPlayerWallRequestProto postOnPlayerWallRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      postOnPlayerWallRequestProto = PostOnPlayerWallRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = postOnPlayerWallRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public PostOnPlayerWallRequestProto getPostOnPlayerWallRequestProto() {
    return postOnPlayerWallRequestProto;
  }
}
