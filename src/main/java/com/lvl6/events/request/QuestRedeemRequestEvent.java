package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.QuestRedeemRequestProto;

public class QuestRedeemRequestEvent extends RequestEvent {

  private QuestRedeemRequestProto questRedeemRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      questRedeemRequestProto = QuestRedeemRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = questRedeemRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public QuestRedeemRequestProto getQuestRedeemRequestProto() {
    return questRedeemRequestProto;
  }
}
