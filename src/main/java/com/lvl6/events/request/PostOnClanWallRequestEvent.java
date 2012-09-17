package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.PostOnClanWallRequestProto;

public class PostOnClanWallRequestEvent extends RequestEvent {

  private PostOnClanWallRequestProto postOnClanWallRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      postOnClanWallRequestProto = PostOnClanWallRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = postOnClanWallRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public PostOnClanWallRequestProto getPostOnClanWallRequestProto() {
    return postOnClanWallRequestProto;
  }
}
