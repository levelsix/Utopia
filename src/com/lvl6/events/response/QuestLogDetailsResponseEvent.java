package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.QuestLogDetailsResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class QuestLogDetailsResponseEvent extends NormalResponseEvent {

  private QuestLogDetailsResponseProto questLogDetailsResponseProto;
  
  public QuestLogDetailsResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_QUEST_LOG_DETAILS_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = questLogDetailsResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setQuestLogDetailsResponseProto(QuestLogDetailsResponseProto questLogDetailsResponseProto) {
    this.questLogDetailsResponseProto = questLogDetailsResponseProto;
  }

}