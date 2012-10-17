package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.BeginClanTowerWarResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class BeginClanTowerWarResponseEvent extends NormalResponseEvent {

  private BeginClanTowerWarResponseProto beginClanTowerWarResponseProto;
  
  public BeginClanTowerWarResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_BEGIN_CLAN_TOWER_WAR;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = beginClanTowerWarResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setBeginClanTowerWarResponseProto(BeginClanTowerWarResponseProto beginClanTowerWarResponseProto) {
    this.beginClanTowerWarResponseProto = beginClanTowerWarResponseProto;
  }

  public BeginClanTowerWarResponseProto getBeginClanTowerWarResponseProto() {   //because APNS required
    return beginClanTowerWarResponseProto;
  }
  
}
