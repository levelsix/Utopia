package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.BeginGoldmineTimerRequestProto;

public class BeginGoldmineTimerRequestEvent extends RequestEvent {

  private BeginGoldmineTimerRequestProto beginGoldmineTimerRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      beginGoldmineTimerRequestProto = BeginGoldmineTimerRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = beginGoldmineTimerRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public BeginGoldmineTimerRequestProto getBeginGoldmineTimerRequestProto() {
    return beginGoldmineTimerRequestProto;
  }
}
