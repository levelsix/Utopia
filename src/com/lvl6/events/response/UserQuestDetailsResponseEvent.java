package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.UserQuestDetailsResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class UserQuestDetailsResponseEvent extends NormalResponseEvent {

  private UserQuestDetailsResponseProto userQuestDetailsResponseProto;
  
  public UserQuestDetailsResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_USER_QUEST_DETAILS_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = userQuestDetailsResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setUserQuestDetailsResponseProto(UserQuestDetailsResponseProto userQuestDetailsResponseProto) {
    this.userQuestDetailsResponseProto = userQuestDetailsResponseProto;
  }
}