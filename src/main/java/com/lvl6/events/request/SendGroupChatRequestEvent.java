package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.SendGroupChatRequestProto;

public class SendGroupChatRequestEvent extends RequestEvent {

  private SendGroupChatRequestProto sendGroupChatRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      sendGroupChatRequestProto = SendGroupChatRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = sendGroupChatRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public SendGroupChatRequestProto getSendGroupChatRequestProto() {
    return sendGroupChatRequestProto;
  }
}
