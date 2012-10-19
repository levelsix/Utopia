package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.UpgradeClanTierLevelResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class UpgradeClanTierLevelResponseEvent extends NormalResponseEvent {

  private UpgradeClanTierLevelResponseProto upgradeClanTierLevelResponseProto;
  
  public UpgradeClanTierLevelResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_UPGRADE_CLAN_TIER_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = upgradeClanTierLevelResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setUpgradeClanTierResponseProto(UpgradeClanTierLevelResponseProto upgradeClanTierLevelResponseProto) {
    this.upgradeClanTierLevelResponseProto = upgradeClanTierLevelResponseProto;
  }

}
