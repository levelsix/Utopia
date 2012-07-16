package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.SubmitEquipsToBlacksmithResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class SubmitEquipsToBlacksmithResponseEvent extends NormalResponseEvent {

  private SubmitEquipsToBlacksmithResponseProto submitEquipsToBlacksmithResponseProto;
  
  public SubmitEquipsToBlacksmithResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_SUBMIT_EQUIPS_TO_BLACKSMITH;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = submitEquipsToBlacksmithResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setSubmitEquipsToBlacksmithResponseProto(SubmitEquipsToBlacksmithResponseProto submitEquipsToBlacksmithResponseProto) {
    this.submitEquipsToBlacksmithResponseProto = submitEquipsToBlacksmithResponseProto;
  }

}
