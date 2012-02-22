package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.QuestAcceptResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class QuestAcceptResponseEvent extends NormalResponseEvent {

  private QuestAcceptResponseProto questAcceptResponseProto;
  
  public QuestAcceptResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_QUEST_ACCEPT_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = questAcceptResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setQuestAcceptResponseProto(QuestAcceptResponseProto questAcceptResponseProto) {
    this.questAcceptResponseProto = questAcceptResponseProto;
  }

}