package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.PostOnClanBulletinRequestProto;

public class PostOnClanBulletinRequestEvent extends RequestEvent {

  private PostOnClanBulletinRequestProto postOnClanBulletinRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      postOnClanBulletinRequestProto = PostOnClanBulletinRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = postOnClanBulletinRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public PostOnClanBulletinRequestProto getPostOnClanBulletinRequestProto() {
    return postOnClanBulletinRequestProto;
  }
}
