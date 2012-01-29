package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NonBroadcastResponseEvent;
import com.lvl6.proto.EventProto.UpgradeNormStructureResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class UpgradeNormStructureResponseEvent extends NonBroadcastResponseEvent {

  private UpgradeNormStructureResponseProto upgradeNormStructureResponseProto;
  
  public UpgradeNormStructureResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_UPGRADE_NORM_STRUCTURE_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = upgradeNormStructureResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setUpgradeNormStructureResponseProto(UpgradeNormStructureResponseProto upgradeNormStructureResponseProto) {
    this.upgradeNormStructureResponseProto = upgradeNormStructureResponseProto;
  }

}
