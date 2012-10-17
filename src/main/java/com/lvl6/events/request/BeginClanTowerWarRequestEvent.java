package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.BeginClanTowerWarRequestProto;

public class BeginClanTowerWarRequestEvent extends RequestEvent {

  private BeginClanTowerWarRequestProto beginClanTowerWarRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      beginClanTowerWarRequestProto = BeginClanTowerWarRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = beginClanTowerWarRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public BeginClanTowerWarRequestProto getBeginClanTowerWarRequestProto() {
    return beginClanTowerWarRequestProto;
  }
}
