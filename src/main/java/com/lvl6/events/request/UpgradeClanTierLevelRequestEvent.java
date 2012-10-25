package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.UpgradeClanTierLevelRequestProto;

public class UpgradeClanTierLevelRequestEvent extends RequestEvent {

  private UpgradeClanTierLevelRequestProto upgradeClanTierRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      upgradeClanTierRequestProto = UpgradeClanTierLevelRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = upgradeClanTierRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public UpgradeClanTierLevelRequestProto getUpgradeClanTierRequestProto() {
    return upgradeClanTierRequestProto;
  }
}
