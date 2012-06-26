package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.UpgradeNormStructureRequestProto;

public class UpgradeNormStructureRequestEvent extends RequestEvent {

  private UpgradeNormStructureRequestProto upgradeNormStructureRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      upgradeNormStructureRequestProto = UpgradeNormStructureRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = upgradeNormStructureRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public UpgradeNormStructureRequestProto getUpgradeNormStructureRequestProto() {
    return upgradeNormStructureRequestProto;
  }
}
