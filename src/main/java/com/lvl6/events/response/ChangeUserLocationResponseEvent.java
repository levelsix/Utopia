package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.ChangeUserLocationResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class ChangeUserLocationResponseEvent extends NormalResponseEvent {

  private ChangeUserLocationResponseProto changeUserLocationResponseProto;
  
  public ChangeUserLocationResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_CHANGE_USER_LOCATION_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = changeUserLocationResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setChangeUserLocationResponseProto(ChangeUserLocationResponseProto changeUserLocationResponseProto) {
    this.changeUserLocationResponseProto = changeUserLocationResponseProto;
  }

}
