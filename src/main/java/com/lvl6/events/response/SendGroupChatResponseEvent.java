package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.SendGroupChatResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class SendGroupChatResponseEvent extends NormalResponseEvent {

  private SendGroupChatResponseProto sendGroupChatResponseProto;
  
  public SendGroupChatResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_SEND_GROUP_CHAT_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = sendGroupChatResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setSendGroupChatResponseProto(SendGroupChatResponseProto sendGroupChatResponseProto) {
    this.sendGroupChatResponseProto = sendGroupChatResponseProto;
  }

  public SendGroupChatResponseProto getSendGroupChatResponseProto() { //required for APNS
    return sendGroupChatResponseProto;
  }
  
}
