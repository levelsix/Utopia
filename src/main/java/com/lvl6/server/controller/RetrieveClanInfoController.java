package com.lvl6.server.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.RetrieveClanInfoRequestEvent;
import com.lvl6.events.response.RetrieveClanInfoResponseEvent;
import com.lvl6.info.Clan;
import com.lvl6.info.User;
import com.lvl6.info.UserClan;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.RetrieveClanInfoRequestProto;
import com.lvl6.proto.EventProto.RetrieveClanInfoRequestProto.ClanInfoGrabType;
import com.lvl6.proto.EventProto.RetrieveClanInfoResponseProto;
import com.lvl6.proto.EventProto.RetrieveClanInfoResponseProto.Builder;
import com.lvl6.proto.EventProto.RetrieveClanInfoResponseProto.RetrieveClanInfoStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.InfoProto.MinimumUserProtoForClans;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.ClanRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.RetrieveUtils;

@Component @DependsOn("gameServer") public class RetrieveClanInfoController extends EventController {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  public RetrieveClanInfoController() {
    numAllocatedThreads = 4;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new RetrieveClanInfoRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_RETRIEVE_CLAN_INFO_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    RetrieveClanInfoRequestProto reqProto = ((RetrieveClanInfoRequestEvent)event).getRetrieveClanInfoRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int clanId = reqProto.getClanId();
    String clanName = reqProto.getClanName();
    int beforeClanId = reqProto.getBeforeThisClanId();
    ClanInfoGrabType grabType = reqProto.getGrabType();

    RetrieveClanInfoResponseProto.Builder resBuilder = RetrieveClanInfoResponseProto.newBuilder();
    resBuilder.setSender(senderProto);
    resBuilder.setIsForBrowsingList(reqProto.getIsForBrowsingList());
    resBuilder.setIsForSearch(false);
    
    if (reqProto.hasClanName()) resBuilder.setClanName(clanName);
    if (reqProto.hasClanId()) resBuilder.setClanId(clanId);

    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());

      boolean legitCreate = checkLegitCreate(resBuilder, clanName, clanId);

      if (legitCreate) {
        if (reqProto.hasClanName() || reqProto.hasClanId()) {
          if (grabType == ClanInfoGrabType.ALL || grabType == ClanInfoGrabType.CLAN_INFO) {
            List<Clan> clans = null;
            if (reqProto.hasClanName()) {
              // Can search for clan name or tag name
              clans = ClanRetrieveUtils.getClansWithSimilarNameOrTag(clanName, clanName);
              resBuilder.setIsForSearch(true);
            } else if (reqProto.hasClanId()) {
              Clan clan = ClanRetrieveUtils.getClanWithId(clanId);
              clans = new ArrayList<Clan>();
              clans.add(clan);
            }

            if (clans != null && clans.size() > 0) {
              for (Clan c : clans) {
                resBuilder.addClanInfo(CreateInfoProtoUtils.createFullClanProtoWithClanSize(c));
              }
            }
          }
          if (grabType == ClanInfoGrabType.ALL || grabType == ClanInfoGrabType.MEMBERS) {
            List<UserClan> userClans = RetrieveUtils.userClanRetrieveUtils().getUserClansRelatedToClan(clanId);
            
            Set<Integer> userIds = new HashSet<Integer>();
            //this is because clan with 1k+ users overflows buffer when sending to client and need to 
            //include clan owner
            Clan c = ClanRetrieveUtils.getClanWithId(clanId);
            int ownerId = c.getOwnerId();
            for (UserClan uc: userClans) {
              userIds.add(uc.getUserId());
            }
            userIds.add(ownerId);
            List<Integer> userIdList = new ArrayList<Integer>(userIds);

            Map<Integer, User> usersMap = RetrieveUtils.userRetrieveUtils().getUsersByIds(userIdList);

            for (UserClan uc : userClans) {
              MinimumUserProtoForClans minUser = CreateInfoProtoUtils.createMinimumUserProtoForClans(usersMap.get(uc.getUserId()), uc.getStatus());
              resBuilder.addMembers(minUser);
            }
          }
        } else {
          List<Clan> clans = null;
          if (beforeClanId <= 0) {
            clans = ClanRetrieveUtils.getMostRecentClans(ControllerConstants.RETRIEVE_CLANS__NUM_CLANS_CAP);
          } else {
            clans = ClanRetrieveUtils.getMostRecentClansBeforeClanId(ControllerConstants.RETRIEVE_CLANS__NUM_CLANS_CAP, beforeClanId);
            resBuilder.setBeforeThisClanId(reqProto.getBeforeThisClanId());
          }

          for (Clan clan : clans) {
            resBuilder.addClanInfo(CreateInfoProtoUtils.createFullClanProtoWithClanSize(clan));
          }
        }
      }

      RetrieveClanInfoResponseEvent resEvent = new RetrieveClanInfoResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setRetrieveClanInfoResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);
    } catch (Exception e) {
      log.error("exception in RetrieveClanInfo processEvent", e);
    }
  }

  private boolean checkLegitCreate(Builder resBuilder, String clanName, int clanId) {
    if ((clanName == null || clanName.length() != 0) && clanId != 0) {
      resBuilder.setStatus(RetrieveClanInfoStatus.OTHER_FAIL);
      log.error("clan name and clan id set");
      return false;
    }
    resBuilder.setStatus(RetrieveClanInfoStatus.SUCCESS);
    return true;
  }
}
