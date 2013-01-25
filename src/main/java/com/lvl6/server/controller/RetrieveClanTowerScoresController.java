package com.lvl6.server.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.RetrieveClanTowerScoresRequestEvent;
import com.lvl6.events.response.RetrieveClanTowerScoresResponseEvent;
import com.lvl6.info.ClanTower;
import com.lvl6.info.ClanTowerUser;
import com.lvl6.info.User;
import com.lvl6.info.UserClan;
import com.lvl6.proto.EventProto.RetrieveClanTowerScoresRequestProto;
import com.lvl6.proto.EventProto.RetrieveClanTowerScoresResponseProto;
import com.lvl6.proto.EventProto.RetrieveClanTowerScoresResponseProto.Builder;
import com.lvl6.proto.EventProto.RetrieveClanTowerScoresResponseProto.RetrieveClanTowerScoresStatus;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.ClanTowerRetrieveUtils;
import com.lvl6.retrieveutils.ClanTowerUserRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.RetrieveUtils;

@Component @DependsOn("gameServer") public class RetrieveClanTowerScoresController extends EventController {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  public RetrieveClanTowerScoresController() {
    numAllocatedThreads = 4;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new RetrieveClanTowerScoresRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_RETRIEVE_CLAN_TOWER_SCORES_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    RetrieveClanTowerScoresRequestProto reqProto = ((RetrieveClanTowerScoresRequestEvent)event).getRetrieveClanTowerScoresRequestProto();
    
    int towerId = reqProto.getTowerId();
    
    Builder resBuilder = RetrieveClanTowerScoresResponseProto.newBuilder();
    resBuilder.setSender(reqProto.getSender());
    resBuilder.setTowerId(towerId);

    try {
      ClanTower clanTower = ClanTowerRetrieveUtils.getClanTower(towerId);
      
      boolean legitRetrieve = legitRetrieve(clanTower, resBuilder);
      if (legitRetrieve) {
        resBuilder.setStatus(RetrieveClanTowerScoresStatus.SUCCESS);
        
        int battleId = clanTower.getCurrentBattleId();
        
        List<ClanTowerUser> clanTowerUsers = ClanTowerUserRetrieveUtils.getClanTowerUsersForBattleId(battleId);
        List<UserClan> userClans = RetrieveUtils.userClanRetrieveUtils().getUserClanMembersInClanOr(clanTower.getClanOwnerId(), clanTower.getClanAttackerId());
        
        for (UserClan uc : userClans) {
          boolean foundCtu = false;
          for (ClanTowerUser ctu : clanTowerUsers) {
            if (ctu.getUserId() == uc.getUserId()) {
              foundCtu = true;
            }
          }
          
          if (foundCtu == false) {
            // Create a fake ctu
            boolean isInOwnerClan = (clanTower.getClanOwnerId() == uc.getClanId());
            ClanTowerUser ctu = new ClanTowerUser(battleId, uc.getUserId(), 0, isInOwnerClan, 0);
            clanTowerUsers.add(ctu);
          }
        }
        
        // Sort the lists and make a userId list
        List<ClanTowerUser> ownerUsers = new ArrayList<ClanTowerUser>();
        List<ClanTowerUser> attackerUsers = new ArrayList<ClanTowerUser>();
        List<Integer> userIds = new ArrayList<Integer>();
        
        for (ClanTowerUser ctu : clanTowerUsers) {
          if (ctu.isInOwnerClan()) {
            ownerUsers.add(ctu);
          } else {
            attackerUsers.add(ctu);
          }
          userIds.add(ctu.getUserId());
        }
        
        Map<Integer, User> userMap = RetrieveUtils.userRetrieveUtils().getUsersByIds(userIds);
        
        for (ClanTowerUser ctu : ownerUsers) {
          resBuilder.addOwnerMembers(CreateInfoProtoUtils.createMinUserProtoForClanTowerScores(userMap.get(ctu.getUserId()), ctu.getPointsGained(), ctu.getPointsLost()));
        }
        for (ClanTowerUser ctu : attackerUsers) {
          resBuilder.addAttackerMembers(CreateInfoProtoUtils.createMinUserProtoForClanTowerScores(userMap.get(ctu.getUserId()), ctu.getPointsGained(), ctu.getPointsLost()));
        }
      }
      
      RetrieveClanTowerScoresResponseEvent resEvent = new RetrieveClanTowerScoresResponseEvent(reqProto.getSender().getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setRetrieveClanTowerScoresResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);
    } catch (Exception e) {
      log.error("exception in RetrieveClanTowerScores processEvent", e);
    }
  }
  
  private boolean legitRetrieve(ClanTower tower, Builder resBuilder) {
    if (tower == null) {
      resBuilder.setStatus(RetrieveClanTowerScoresStatus.OTHER_FAIL);
      log.error ("tower is null.");
      return false;
    }
    
    if (tower.getClanAttackerId() <= 0 || tower.getClanOwnerId() <= 0) {
      resBuilder.setStatus(RetrieveClanTowerScoresStatus.OTHER_FAIL);
      log.error ("tower has invalid owner or attacker. tower="+tower);
      return false;
    }
    
    return true;
  }
}
