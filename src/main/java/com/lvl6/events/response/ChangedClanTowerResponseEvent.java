package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.ChangedClanTowerResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class ChangedClanTowerResponseEvent extends NormalResponseEvent {

  private ChangedClanTowerResponseProto changedClanTowerResponseProto;

  //The input argument is not used.
  public ChangedClanTowerResponseEvent(int playerId){
	super(0);  //0 used, just because
    eventType = EventProtocolResponse.S_CHANGED_CLAN_TOWER_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = changedClanTowerResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setChangedClanTowerResponseProto(
      ChangedClanTowerResponseProto changedClanTowerResponseProto) {
    this.changedClanTowerResponseProto = changedClanTowerResponseProto;
  }

}
