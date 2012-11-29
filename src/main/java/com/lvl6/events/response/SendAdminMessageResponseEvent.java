package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.SendAdminMessageResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class SendAdminMessageResponseEvent extends NormalResponseEvent {

  private SendAdminMessageResponseProto sendAdminMessageResponseProto;
  
  public SendAdminMessageResponseProto getSendAdminMessageResponseProto() {
	return sendAdminMessageResponseProto;
}

public void setSendAdminMessageResponseProto(SendAdminMessageResponseProto sendAdminMessageResponseProto) {
	this.sendAdminMessageResponseProto = sendAdminMessageResponseProto;
}

public SendAdminMessageResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_SEND_ADMIN_MESSAGE_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = sendAdminMessageResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }


  
}
