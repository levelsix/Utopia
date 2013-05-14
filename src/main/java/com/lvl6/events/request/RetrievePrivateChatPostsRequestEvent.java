package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.RetrievePrivateChatPostsRequestProto;

public class RetrievePrivateChatPostsRequestEvent extends RequestEvent{
  private RetrievePrivateChatPostsRequestProto retrievePrivateChatPostsRequestProto;
  /**
   * read the event from the given ByteBuffer to populate this event
   */ 
  @Override
  public void read(ByteBuffer buff) {
    try {
      retrievePrivateChatPostsRequestProto = RetrievePrivateChatPostsRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = retrievePrivateChatPostsRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public RetrievePrivateChatPostsRequestProto getRetrievePrivateChatPostsRequestProto() {
    return retrievePrivateChatPostsRequestProto;
  }
  
}//RetrievePrivateChatPostsRequestProto