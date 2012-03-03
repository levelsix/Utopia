package com.lvl6.server.controller;

import java.util.List;
import java.util.Map;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.LoadNeutralCityRequestEvent;
import com.lvl6.events.response.LoadNeutralCityResponseEvent;
import com.lvl6.info.City;
import com.lvl6.info.Quest;
import com.lvl6.info.Task;
import com.lvl6.info.User;
import com.lvl6.info.UserQuest;
import com.lvl6.proto.EventProto.LoadNeutralCityRequestProto;
import com.lvl6.proto.EventProto.LoadNeutralCityResponseProto;
import com.lvl6.proto.EventProto.LoadNeutralCityResponseProto.Builder;
import com.lvl6.proto.EventProto.LoadNeutralCityResponseProto.LoadNeutralCityStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.InfoProto.UserType;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserCityRetrieveUtils;
import com.lvl6.retrieveutils.UserQuestRetrieveUtils;
import com.lvl6.retrieveutils.UserQuestsCompletedDefeatTypeJobsRetrieveUtils;
import com.lvl6.retrieveutils.UserQuestsDefeatTypeJobProgressRetrieveUtils;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.retrieveutils.UserTaskRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.CityRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.QuestRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.TaskRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.utilmethods.MiscMethods;

public class LoadNeutralCityController extends EventController {

  public LoadNeutralCityController() {
    numAllocatedThreads = 3;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new LoadNeutralCityRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_LOAD_NEUTRAL_CITY_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) {
    LoadNeutralCityRequestProto reqProto = ((LoadNeutralCityRequestEvent)event).getLoadNeutralCityRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int cityId = reqProto.getCityId();
    City city = CityRetrieveUtils.getCityForCityId(cityId);

    LoadNeutralCityResponseProto.Builder resBuilder = LoadNeutralCityResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    resBuilder.setStatus(LoadNeutralCityStatus.SUCCESS);
    server.lockPlayer(senderProto.getUserId());

    try {
      User user = UserRetrieveUtils.getUserById(senderProto.getUserId());
      int currentCityRankForUser = UserCityRetrieveUtils.getCurrentCityRankForUser(senderProto.getUserId(), cityId);

      boolean legitCityLoad = checkLegitCityLoad(resBuilder, user, city, currentCityRankForUser);

      if (legitCityLoad) {
        List<Task> tasks = TaskRetrieveUtils.getAllTasksForCityId(cityId);
        if (tasks != null && tasks.size() > 0) {
          setResponseUserTaskInfos(resBuilder, tasks, user.getId(), senderProto.getUserType());
        }
        List<UserQuest> inProgressUserQuests = UserQuestRetrieveUtils.getInProgressUserQuestsForUser(senderProto.getUserId());
        if (inProgressUserQuests != null && inProgressUserQuests.size() > 0) {
          setResponseDefeatTypeJobEnemies(inProgressUserQuests, senderProto.getUserType());
        }
      }

      LoadNeutralCityResponseEvent resEvent = new LoadNeutralCityResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setLoadNeutralCityResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

    } catch (Exception e) {
      log.error("exception in LoadNeutralCity processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());      
    }
  }

  private void setResponseDefeatTypeJobEnemies(List<UserQuest> inProgressUserQuests, UserType userType) {
    /*
    Map<Integer, List<Integer>>  questIdToUserDefeatTypeJobsCompletedForQuestForUser = null;
    Map<Integer, Integer> defeatTypeJobIdsToNumDefeatedForUserQuest = null;

    Map<Integer, Quest> questIdsToQuests = QuestRetrieveUtils.getQuestIdsToQuests();

    for (UserQuest userQuest : inProgressUserQuests) {
      Quest quest = questIdsToQuests.get(userQuest.getQuestId());

      boolean goodSide = MiscMethods.checkIfGoodSide(userType);

      List<Integer> defeatTypeJobsRequired = (goodSide) ? quest.getDefeatBadGuysJobsRequired() : quest.getDefeatGoodGuysJobsRequired();


      if (defeatTypeJobsRequired != null && defeatTypeJobsRequired.size() > 0) {
        if (questIdToUserDefeatTypeJobsCompletedForQuestForUser == null) {
          questIdToUserDefeatTypeJobsCompletedForQuestForUser = UserQuestsCompletedDefeatTypeJobsRetrieveUtils.getQuestIdToUserDefeatTypeJobsCompletedForQuestForUser(userQuest.getUserId());
        }

        List<Integer> userDefeatTypeJobsCompletedForQuest = questIdToUserDefeatTypeJobsCompletedForQuestForUser.get(userQuest.getQuestId());
        for (Integer requiredDefeatTypeJobId : defeatTypeJobsRequired) {
          boolean defeatJobCompletedForQuest = false;
          Integer numTimesUserDidJob = null;
          if (userDefeatTypeJobsCompletedForQuest != null && userDefeatTypeJobsCompletedForQuest.contains(requiredDefeatTypeJobId)) {
            defeatJobCompletedForQuest = true;
          } else {
            if (defeatTypeJobIdsToNumDefeatedForUserQuest == null) {
              defeatTypeJobIdsToNumDefeatedForUserQuest = UserQuestsDefeatTypeJobProgressRetrieveUtils.getDefeatTypeJobIdsToNumDefeatedForUserQuest(userQuest.getUserId(), userQuest.getQuestId());
            }
            numTimesUserDidJob = defeatTypeJobIdsToNumDefeatedForUserQuest.get(requiredDefeatTypeJobId);
            if (numTimesUserDidJob == null) {
              numTimesUserDidJob = 0;
            }
          }
          builder.addRequiredDefeatTypeJobProgress(createMinimumUserDefeatTypeJobProto(userQuest, userType, requiredDefeatTypeJobId, defeatJobCompletedForQuest, numTimesUserDidJob));
        }
      }

    }
    //     repeated FullUserProto defeatTypeJobEnemies = 5;
*/

  }

  private void setResponseUserTaskInfos(Builder resBuilder, List<Task> tasks, int userId, UserType userType) {
    Map<Integer, Integer> taskIdToNumTimesActedInRankForUser = UserTaskRetrieveUtils.getTaskIdToNumTimesActedInRankForUser(userId);
    for (Task t : tasks) {
      int numTimesUserActed = (taskIdToNumTimesActedInRankForUser.containsKey(t.getId())) ? taskIdToNumTimesActedInRankForUser.get(t.getId()) : 0;
      resBuilder.addUserTasksInfo(CreateInfoProtoUtils.createMinimumUserTaskProto(userType, userId, t, numTimesUserActed));
    }
  }

  private boolean checkLegitCityLoad(Builder resBuilder, User user, City city, int currentCityRankForUser) {
    if (city == null || user == null) {
      resBuilder.setStatus(LoadNeutralCityStatus.OTHER_FAIL);
      return false;
    }
    if (currentCityRankForUser < 1) {
      resBuilder.setStatus(LoadNeutralCityStatus.NOT_ACCESSIBLE_TO_USER);
      return false;
    }
    resBuilder.setStatus(LoadNeutralCityStatus.SUCCESS);
    return true;
  }
}
