package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.ReceivedGroupChatResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class ReceivedGroupChatResponseEvent extends NormalResponseEvent {

  private ReceivedGroupChatResponseProto receivedGroupChatResponseProto;
  
  public ReceivedGroupChatResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_RECEIVED_GROUP_CHAT_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = receivedGroupChatResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setReceivedGroupChatResponseProto(ReceivedGroupChatResponseProto receivedGroupChatResponseProto) {
    this.receivedGroupChatResponseProto = receivedGroupChatResponseProto;
  }

}
