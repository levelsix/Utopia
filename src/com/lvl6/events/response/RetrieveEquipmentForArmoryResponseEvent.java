package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NonBroadcastResponseEvent;
import com.lvl6.proto.EventProto.RetrieveEquipmentForArmoryResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class RetrieveEquipmentForArmoryResponseEvent extends NonBroadcastResponseEvent {

  private RetrieveEquipmentForArmoryResponseProto retrieveEquipmentForArmoryResponseProto;
  
  public RetrieveEquipmentForArmoryResponseEvent(int playerId) {
    super(playerId);
    eventType = EventProtocolResponse.S_RETRIEVE_EQUIPS_FOR_ARMORY_EVENT;
  }

  /** 
   * write the event to the given ByteBuffer
   * 
   * note we are using 1.4 ByteBuffers for both client and server
   * depending on the deployment you may need to support older java
   * versions on the client and use old-style socket input/output streams
   */
  public int write(ByteBuffer buff) {
    ByteString b = retrieveEquipmentForArmoryResponseProto.toByteString();
    b.copyTo(buff);
    return b.size();
  }

  public void setRetrieveEquipmentForArmoryResponseProto(
      RetrieveEquipmentForArmoryResponseProto retrieveEquipmentForArmoryResponseProto) {
    this.retrieveEquipmentForArmoryResponseProto = retrieveEquipmentForArmoryResponseProto;
  }
  
}
