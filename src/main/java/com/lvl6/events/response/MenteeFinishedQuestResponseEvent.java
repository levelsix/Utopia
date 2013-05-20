package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.MenteeFinishedQuestResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class MenteeFinishedQuestResponseEvent extends NormalResponseEvent {
  
  private MenteeFinishedQuestResponseProto menteeFinishedQuestResponseProto;

  public MenteeFinishedQuestResponseEvent(int playerId) {
    super(playerId);
    eventType = EventProtocolResponse.S_MENTEE_FINISHED_QUEST_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = menteeFinishedQuestResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }
  
  public void setMenteeFinishedQuestResponseProto(
      MenteeFinishedQuestResponseProto menteeFinishedQuestResponseProto) {
    this.menteeFinishedQuestResponseProto = menteeFinishedQuestResponseProto;
  }

  public MenteeFinishedQuestResponseProto getMenteeFinishedQuestResponseProto() {
    return menteeFinishedQuestResponseProto;
  }
  
}