package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.ConcedeClanTowerWarResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class ConcedeClanTowerWarResponseEvent extends NormalResponseEvent {

  private ConcedeClanTowerWarResponseProto concedeClanTowerWarResponseProto;
  
  public ConcedeClanTowerWarResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_CONCEDE_CLAN_TOWER_WAR_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = concedeClanTowerWarResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setConcedeClanTowerWarResponseProto(ConcedeClanTowerWarResponseProto concedeClanTowerWarResponseProto) {
    this.concedeClanTowerWarResponseProto = concedeClanTowerWarResponseProto;
  }

  public ConcedeClanTowerWarResponseProto getBeginClanTowerWarResponseProto() {   
    return concedeClanTowerWarResponseProto;
  }
  
}
