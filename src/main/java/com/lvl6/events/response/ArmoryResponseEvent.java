package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.ArmoryResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class ArmoryResponseEvent extends NormalResponseEvent {

  private ArmoryResponseProto armoryResponseProto;
  
  public ArmoryResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_ARMORY_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = armoryResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setArmoryResponseProto(ArmoryResponseProto armoryResponseProto) {
    this.armoryResponseProto = armoryResponseProto;
  }

}
