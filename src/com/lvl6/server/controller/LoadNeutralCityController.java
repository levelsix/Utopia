package com.lvl6.server.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.LoadNeutralCityRequestEvent;
import com.lvl6.events.response.LoadNeutralCityResponseEvent;
import com.lvl6.info.City;
import com.lvl6.info.NeutralCityElement;
import com.lvl6.info.Quest;
import com.lvl6.info.Task;
import com.lvl6.info.User;
import com.lvl6.info.UserQuest;
import com.lvl6.info.jobs.DefeatTypeJob;
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
import com.lvl6.retrieveutils.rarechange.DefeatTypeJobRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.NeutralCityElementsRetrieveUtils;
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
    resBuilder.setCityId(cityId);

    resBuilder.setStatus(LoadNeutralCityStatus.SUCCESS);
    server.lockPlayer(senderProto.getUserId());

    try {
      User user = UserRetrieveUtils.getUserById(senderProto.getUserId());
      int currentCityRankForUser = UserCityRetrieveUtils.getCurrentCityRankForUser(senderProto.getUserId(), cityId);

      boolean legitCityLoad = checkLegitCityLoad(resBuilder, user, city, currentCityRankForUser);

      if (legitCityLoad) {
        List<NeutralCityElement> neutralCityElements = NeutralCityElementsRetrieveUtils.getNeutralCityElementsForCity(cityId);
        if (neutralCityElements != null) {
          for (NeutralCityElement nce : neutralCityElements) {
            resBuilder.addCityElements(CreateInfoProtoUtils.createNeutralCityElementProtoFromNeutralCityElement(nce, user.getType()));
          }
        }

        List<Task> tasks = TaskRetrieveUtils.getAllTasksForCityId(cityId);
        if (tasks != null && tasks.size() > 0) {
          setResponseUserTaskInfos(resBuilder, tasks, user.getId(), senderProto.getUserType());
        }
        List<UserQuest> inProgressUserQuests = UserQuestRetrieveUtils.getInProgressUserQuestsForUser(senderProto.getUserId());
        if (inProgressUserQuests != null && inProgressUserQuests.size() > 0) {
          setResponseDefeatTypeJobEnemies(resBuilder, inProgressUserQuests, user, cityId);
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

  private void setResponseDefeatTypeJobEnemies(Builder resBuilder, List<UserQuest> inProgressUserQuests, User user, int cityId) {

    Map<Integer, List<Integer>>  questIdToUserDefeatTypeJobsCompletedForQuestForUser = null;
    Map<Integer, Map<Integer, Integer>> questIdToDefeatTypeJobIdsToNumDefeated = null;

    Map<Integer, Quest> questIdsToQuests = QuestRetrieveUtils.getQuestIdsToQuests();

    Map<UserType, Integer> numToGenerate = new HashMap<UserType, Integer>();

    for (UserQuest userQuest : inProgressUserQuests) {
      Quest quest = questIdsToQuests.get(userQuest.getQuestId());

      boolean goodSide = MiscMethods.checkIfGoodSide(user.getType());

      List<Integer> defeatTypeJobsRequired = (goodSide) ? quest.getDefeatBadGuysJobsRequired() : quest.getDefeatGoodGuysJobsRequired();

      if (defeatTypeJobsRequired != null && defeatTypeJobsRequired.size() > 0) {
        if (questIdToUserDefeatTypeJobsCompletedForQuestForUser == null) {
          questIdToUserDefeatTypeJobsCompletedForQuestForUser = UserQuestsCompletedDefeatTypeJobsRetrieveUtils.getQuestIdToUserDefeatTypeJobsCompletedForQuestForUser(userQuest.getUserId());
        }
        List<Integer> userDefeatTypeJobsCompletedForQuest = questIdToUserDefeatTypeJobsCompletedForQuestForUser.get(userQuest.getQuestId());
        for (Integer requiredDefeatTypeJobId : defeatTypeJobsRequired) {
          DefeatTypeJob dtj = DefeatTypeJobRetrieveUtils.getDefeatTypeJobForDefeatTypeJobId(requiredDefeatTypeJobId);
          if (dtj.getCityId() == cityId) {
            Integer numTimesUserDidJob = null;
            if (userDefeatTypeJobsCompletedForQuest == null || !userDefeatTypeJobsCompletedForQuest.contains(requiredDefeatTypeJobId)) {
              if (questIdToDefeatTypeJobIdsToNumDefeated == null) {
                questIdToDefeatTypeJobIdsToNumDefeated = UserQuestsDefeatTypeJobProgressRetrieveUtils.getQuestIdToDefeatTypeJobIdsToNumDefeated(userQuest.getUserId());
              }
              Map<Integer, Integer> defeatTypeJobIdsToNumDefeatedForUserQuest = questIdToDefeatTypeJobIdsToNumDefeated.get(userQuest.getQuestId());
              if (defeatTypeJobIdsToNumDefeatedForUserQuest != null) {
                numTimesUserDidJob = defeatTypeJobIdsToNumDefeatedForUserQuest.get(requiredDefeatTypeJobId);
                if (numTimesUserDidJob == null) {
                  numTimesUserDidJob = 0;
                }
              } else {
                numTimesUserDidJob = 0;
              }
              int numRemaining = dtj.getNumEnemiesToDefeat() - numTimesUserDidJob;
              if (numRemaining > 0) {
                if (!numToGenerate.containsKey(dtj.getEnemyType()) || numToGenerate.get(dtj.getEnemyType()) < numRemaining) {
                  numToGenerate.put(dtj.getEnemyType(), numRemaining);
                }
              }
            }
          }
        }
      }
      for (UserType type : numToGenerate.keySet()) {
        List<UserType> temp = new ArrayList<UserType>();
        temp.add(type);
        List<User> users = UserRetrieveUtils.getUsers(temp,
            numToGenerate.get(type), user.getLevel(), user.getId(), true, null, null, null, null, true);
        if (users != null) {
          for (User u : users) {
            resBuilder.addDefeatTypeJobEnemies(CreateInfoProtoUtils.createFullUserProtoFromUser(u));
          }
        }
      }
    }

  }

  private void setResponseUserTaskInfos(Builder resBuilder, List<Task> tasks, int userId, UserType userType) {
    Map<Integer, Integer> taskIdToNumTimesActedInRankForUser = UserTaskRetrieveUtils.getTaskIdToNumTimesActedInRankForUser(userId);
    for (Task t : tasks) {
      int numTimesUserActed = (taskIdToNumTimesActedInRankForUser.containsKey(t.getId())) ? taskIdToNumTimesActedInRankForUser.get(t.getId()) : 0;
      resBuilder.addUserTasksInfo(CreateInfoProtoUtils.createMinimumUserTaskProto(userType, userId, t.getId(), numTimesUserActed));
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
