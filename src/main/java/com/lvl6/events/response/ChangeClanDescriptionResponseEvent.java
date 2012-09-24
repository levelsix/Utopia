package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.ChangeClanDescriptionResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class ChangeClanDescriptionResponseEvent extends NormalResponseEvent {

  private ChangeClanDescriptionResponseProto changeClanDescriptionResponseProto;
  
  public ChangeClanDescriptionResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_CHANGE_CLAN_DESCRIPTION_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = changeClanDescriptionResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setChangeClanDescriptionResponseProto(ChangeClanDescriptionResponseProto changeClanDescriptionResponseProto) {
    this.changeClanDescriptionResponseProto = changeClanDescriptionResponseProto;
  }

}
