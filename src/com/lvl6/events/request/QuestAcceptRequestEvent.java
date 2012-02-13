package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.QuestAcceptRequestProto;

public class QuestAcceptRequestEvent extends RequestEvent {

  private QuestAcceptRequestProto questRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      questRequestProto = QuestAcceptRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = questRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public QuestAcceptRequestProto getQuestAcceptRequestProto() {
    return questRequestProto;
  }
}
