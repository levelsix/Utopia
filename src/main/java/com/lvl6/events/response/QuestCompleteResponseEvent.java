package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.QuestCompleteResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class QuestCompleteResponseEvent extends NormalResponseEvent {

  private QuestCompleteResponseProto questCompleteResponseProto;
  
  public QuestCompleteResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_QUEST_COMPLETE_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = questCompleteResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setQuestCompleteResponseProto(QuestCompleteResponseProto questCompleteResponseProto) {
    this.questCompleteResponseProto = questCompleteResponseProto;
  }

}
