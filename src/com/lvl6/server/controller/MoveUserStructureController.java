package com.lvl6.server.controller;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.MoveUserStructureRequestEvent;
import com.lvl6.events.response.MoveUserStructureResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.CoordinatePair;
import com.lvl6.info.Structure;
import com.lvl6.info.User;
import com.lvl6.proto.EventProto.MoveUserStructureRequestProto;
import com.lvl6.proto.EventProto.MoveUserStructureResponseProto;
import com.lvl6.proto.EventProto.MoveUserStructureResponseProto.Builder;
import com.lvl6.proto.EventProto.MoveUserStructureResponseProto.MoveUserStructureStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.StructureRetrieveUtils;
import com.lvl6.utils.utilmethods.InsertUtils;
import com.lvl6.utils.utilmethods.MiscMethods;

public class MoveUserStructureController extends EventController {

  @Override
  public RequestEvent createRequestEvent() {
    return new MoveUserStructureRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_MOVE_USER_STRUCTURE_EVENT;
  }

  /*
   * theres a problem with using userid, structid, and coordinates as the primary key identifier
   * what if playerB loads playerA's map, and then playerA moves his struct, and B tries to rob it
   * what would B pass to the server? the old coordinates won't be in the database anymore.
   * 
   * solution: give every userstruct its specific id
   * new problem: need to return that id of what was inserted.
   * 
   */
  
  @Override
  protected void processRequestEvent(RequestEvent event) {
    MoveUserStructureRequestProto reqProto = ((MoveUserStructureRequestEvent)event).getMoveUserStructureRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int structId = reqProto.getStructId();
    CoordinatePair oldCoords = new CoordinatePair(reqProto.getOldStructCoordinates().getX(), reqProto.getOldStructCoordinates().getY());
    CoordinatePair newCoords = new CoordinatePair(reqProto.getCurStructCoordinates().getX(), reqProto.getCurStructCoordinates().getY());

    MoveUserStructureResponseProto.Builder resBuilder = MoveUserStructureResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId());

    try {
      User user = UserRetrieveUtils.getUserById(senderProto.getUserId());
      Structure struct = StructureRetrieveUtils.getStructForStructId(structId);

      boolean legitMoveUser = checkLegitMoveUser(resBuilder, struct, user);
      MoveUserStructureResponseEvent resEvent = new MoveUserStructureResponseEvent(senderProto.getUserId());
      resEvent.setMoveUserStructureResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legitMoveUser) {
        CoordinatePair cp = new CoordinatePair(reqProto.getStructCoordinates().getX(), reqProto.getStructCoordinates().getY());
        writeChangesToDB(user, struct, cp);
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEvent(user);
        server.writeEvent(resEventUpdate);
      }

    } catch (Exception e) {
      log.error("exception in MoveUserStructure processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());      
    }
  }

  private void writeChangesToDB(User user, Structure struct, CoordinatePair coordinatePair) {
    int diamondChange = Math.max(0, struct.getDiamondPrice());
    int coinChange = Math.max(0, struct.getCoinPrice());
    int woodChange = Math.max(0, struct.getWoodPrice());

    if (!user.updateRelativeDiamondsCoinsWoodNaive(diamondChange, coinChange, woodChange)) {
      log.error("problem with changing user stats after purchasing a structure");
    }
    if (!InsertUtils.insertUserStruct(user.getId(), struct.getId(), coordinatePair)) {
      log.error("error in inserting the user struct");
    }
  }

  private boolean checkLegitMoveUser(Builder resBuilder, Structure struct,
      User user) {
    if (user == null || struct == null) {
      resBuilder.setStatus(MoveUserStructureStatus.OTHER_FAIL);
      return false;
    }
    if (user.getLevel() < struct.getMinLevel()) {
      resBuilder.setStatus(MoveUserStructureStatus.LEVEL_TOO_LOW);
      return false;
    }
    if (user.getCoins() < struct.getCoinPrice()) {
      resBuilder.setStatus(MoveUserStructureStatus.NOT_ENOUGH_COINS);
      return false;
    }
    if (user.getDiamonds() < struct.getDiamondPrice()) {
      resBuilder.setStatus(MoveUserStructureStatus.NOT_ENOUGH_DIAMONDS);
      return false;
    }
    if (user.getWood() < struct.getWoodPrice()) {
      resBuilder.setStatus(MoveUserStructureStatus.NOT_ENOUGH_WOOD);
      return false;
    }
    resBuilder.setStatus(MoveUserStructureStatus.SUCCESS);
    return true;
  }
}
