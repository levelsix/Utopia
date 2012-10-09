package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.RetrieveClanBulletinPostsRequestProto;

public class RetrieveClanBulletinPostsRequestEvent extends RequestEvent{
  private RetrieveClanBulletinPostsRequestProto retrieveClanBulletinPostsRequestProto;
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  @Override
  public void read(ByteBuffer buff) {
    try {
      retrieveClanBulletinPostsRequestProto = RetrieveClanBulletinPostsRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = retrieveClanBulletinPostsRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public RetrieveClanBulletinPostsRequestProto getRetrieveClanBulletinPostsRequestProto() {
    return retrieveClanBulletinPostsRequestProto;
  }
  
}//RetrieveClanBulletinPostsRequestProto