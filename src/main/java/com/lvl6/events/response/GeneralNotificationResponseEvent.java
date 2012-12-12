package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.GeneralNotificationResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class GeneralNotificationResponseEvent extends NormalResponseEvent {

  private GeneralNotificationResponseProto generalNotificationResponseProto;

  //The input argument is not used.
  public GeneralNotificationResponseEvent(int playerId){
    super(0);  //0 used, just because
    eventType = EventProtocolResponse.S_GENERAL_NOTIFICATION_EVENT;
  }

  @Override
  public int write(ByteBuffer bb) {
    ByteString b = generalNotificationResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setGeneralNotificationResponseProto(
      GeneralNotificationResponseProto generalNotificationResponseProto) {
    this.generalNotificationResponseProto = generalNotificationResponseProto;
  }

}
