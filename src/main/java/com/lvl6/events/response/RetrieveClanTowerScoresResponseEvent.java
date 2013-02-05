package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.RetrieveClanTowerScoresResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class RetrieveClanTowerScoresResponseEvent extends NormalResponseEvent {

  private RetrieveClanTowerScoresResponseProto retrieveClanTowerScoresResponseProto;
  
  public RetrieveClanTowerScoresResponseEvent(int playerId) {
    super(playerId);
    eventType = EventProtocolResponse.S_RETRIEVE_CLAN_TOWER_SCORES_EVENT;
  }

  /** 
   * write the event to the given ByteBuffer
   * 
   * note we are using 1.4 ByteBuffers for both client and server
   * depending on the deployment you may need to support older java
   * versions on the client and use old-style socket input/output streams
   */
  public int write(ByteBuffer buff) {
    ByteString b = retrieveClanTowerScoresResponseProto.toByteString();
    b.copyTo(buff);
    return b.size();
  }

  public void setRetrieveClanTowerScoresResponseProto(
      RetrieveClanTowerScoresResponseProto retrieveClanTowerScoresResponseProto) {
    this.retrieveClanTowerScoresResponseProto = retrieveClanTowerScoresResponseProto;
  }
  
}
