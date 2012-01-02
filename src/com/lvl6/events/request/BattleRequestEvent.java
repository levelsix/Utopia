package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.BattleRequestProto;

public class BattleRequestEvent extends RequestEvent{
  private BattleRequestProto battleRequestProto;
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  @Override
  public void read(ByteBuffer buff) {
    try {
      battleRequestProto = BattleRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = battleRequestProto.getAttacker().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public BattleRequestProto getBattleRequestProto() {
    return battleRequestProto;
  }
  
}//BattleRequestProto