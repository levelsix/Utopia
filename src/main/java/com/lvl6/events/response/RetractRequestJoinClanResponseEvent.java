package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.RetractRequestJoinClanResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class RetractRequestJoinClanResponseEvent extends NormalResponseEvent {

  private RetractRequestJoinClanResponseProto retractRequestJoinClanResponseProto;
  
  public RetractRequestJoinClanResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_RETRACT_REQUEST_JOIN_CLAN_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = retractRequestJoinClanResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setRetractRequestJoinClanResponseProto(RetractRequestJoinClanResponseProto retractRequestJoinClanResponseProto) {
    this.retractRequestJoinClanResponseProto = retractRequestJoinClanResponseProto;
  }

}
