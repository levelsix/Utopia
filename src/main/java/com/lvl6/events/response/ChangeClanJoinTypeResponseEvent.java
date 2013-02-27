package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.ChangeClanJoinTypeResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class ChangeClanJoinTypeResponseEvent extends NormalResponseEvent {

  private ChangeClanJoinTypeResponseProto changeClanJoinTypeResponseProto;
  
  public ChangeClanJoinTypeResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_CHANGE_CLAN_JOIN_TYPE_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = changeClanJoinTypeResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setChangeClanJoinTypeResponseProto(ChangeClanJoinTypeResponseProto changeClanJoinTypeResponseProto) {
    this.changeClanJoinTypeResponseProto = changeClanJoinTypeResponseProto;
  }

}
