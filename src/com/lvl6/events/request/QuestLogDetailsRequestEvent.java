package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.QuestLogDetailsRequestProto;

public class QuestLogDetailsRequestEvent extends RequestEvent {

  private QuestLogDetailsRequestProto questLogDetailsRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      questLogDetailsRequestProto = QuestLogDetailsRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = questLogDetailsRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public QuestLogDetailsRequestProto getQuestLogDetailsRequestProto() {
    return questLogDetailsRequestProto;
  }
}
