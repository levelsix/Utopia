package com.lvl6.server.controller;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.RetrieveStaticDataRequestEvent;
import com.lvl6.events.response.RetrieveStaticDataResponseEvent;
import com.lvl6.info.City;
import com.lvl6.info.Equipment;
import com.lvl6.info.Quest;
import com.lvl6.info.Structure;
import com.lvl6.info.Task;
import com.lvl6.info.jobs.BuildStructJob;
import com.lvl6.info.jobs.DefeatTypeJob;
import com.lvl6.info.jobs.PossessEquipJob;
import com.lvl6.info.jobs.UpgradeStructJob;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.RetrieveStaticDataRequestProto;
import com.lvl6.proto.EventProto.RetrieveStaticDataResponseProto;
import com.lvl6.proto.EventProto.RetrieveStaticDataResponseProto.Builder;
import com.lvl6.proto.EventProto.RetrieveStaticDataResponseProto.RetrieveStaticDataStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.InfoProto.UserType;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.rarechange.BuildStructJobRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.CityRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.DefeatTypeJobRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.EquipmentRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.LevelsRequiredExperienceRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.PossessEquipJobRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.QuestRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.StructureRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.TaskRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.UpgradeStructJobRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.utilmethods.MiscMethods;

  @Component @DependsOn("gameServer") public class RetrieveStaticDataController extends EventController{

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  public RetrieveStaticDataController() {
    numAllocatedThreads = 15;
  }
  
  @Override
  public RequestEvent createRequestEvent() {
    return new RetrieveStaticDataRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_RETRIEVE_STATIC_DATA_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    RetrieveStaticDataRequestProto reqProto = ((RetrieveStaticDataRequestEvent)event).getRetrieveStaticDataRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();

    RetrieveStaticDataResponseProto.Builder resBuilder = RetrieveStaticDataResponseProto.newBuilder();
    resBuilder.setSender(senderProto);
    resBuilder.setStatus(RetrieveStaticDataStatus.SUCCESS);

    populateResBuilder(resBuilder, reqProto, senderProto.getUserType());
    RetrieveStaticDataResponseProto resProto = resBuilder.build();

    RetrieveStaticDataResponseEvent resEvent = new RetrieveStaticDataResponseEvent(senderProto.getUserId());
    resEvent.setTag(event.getTag());
    resEvent.setRetrieveStaticDataResponseProto(resProto);

    server.writeEvent(resEvent);
  }

  private void populateResBuilder(Builder resBuilder, RetrieveStaticDataRequestProto reqProto, UserType type) {
    List <Integer> structIds = reqProto.getStructIdsList();
    if (structIds != null && structIds.size() > 0) {
      Map<Integer, Structure> structIdsToStructures = StructureRetrieveUtils.getStructIdsToStructs();
      for (Integer structId :  structIds) {
        Structure struct = structIdsToStructures.get(structId);
        if (struct != null) {
          resBuilder.addStructs(CreateInfoProtoUtils.createFullStructureProtoFromStructure(struct));
        } else {
          resBuilder.setStatus(RetrieveStaticDataStatus.SOME_FAIL);
          log.error("problem with retrieving struct with id " + structId);
        }
      }
    }

    List <Integer> taskIds = reqProto.getTaskIdsList();
    if (taskIds != null && taskIds.size() > 0) {
      Map<Integer, Task> taskIdsToTasks = TaskRetrieveUtils.getTaskIdsToTasks();
      for (Integer taskId :  taskIds) {
        Task task = taskIdsToTasks.get(taskId);
        if (task != null) {
          resBuilder.addTasks(CreateInfoProtoUtils.createFullTaskProtoFromTask(reqProto.getSender().getUserType(), task));
        } else {
          resBuilder.setStatus(RetrieveStaticDataStatus.SOME_FAIL);
          log.error("problem with retrieving task with id " + taskId);
        }
      }
    }

    List <Integer> questIds = reqProto.getQuestIdsList();
    if (questIds != null && questIds.size() > 0) {
      Map<Integer, Quest> questIdsToQuests = QuestRetrieveUtils.getQuestIdsToQuests();
      for (Integer questId :  questIds) {
        Quest quest = questIdsToQuests.get(questId);
        if (quest != null) {
          resBuilder.addQuests(CreateInfoProtoUtils.createFullQuestProtoFromQuest(reqProto.getSender().getUserType(), quest));
        } else {
          resBuilder.setStatus(RetrieveStaticDataStatus.SOME_FAIL);
          log.error("problem with retrieving quest with id " + quest);
        }
      }
    }

    List <Integer> cityIds = reqProto.getCityIdsList();
    if (cityIds != null && cityIds.size() > 0) {
      Map<Integer, City> cityIdsToCitys = CityRetrieveUtils.getCityIdsToCities();
      for (Integer cityId :  cityIds) {
        City city = cityIdsToCitys.get(cityId);
        if (city != null) {
          resBuilder.addCities(CreateInfoProtoUtils.createFullCityProtoFromCity(city));
        } else {
          resBuilder.setStatus(RetrieveStaticDataStatus.SOME_FAIL);
          log.error("problem with retrieving city with id " + cityId);
        }
      }
    }

    List <Integer> equipIds = reqProto.getEquipIdsList();
    if (equipIds != null && equipIds.size() > 0) {
      Map<Integer, Equipment> equipIdsToEquips = EquipmentRetrieveUtils.getEquipmentIdsToEquipment();
      for (Integer equipId :  equipIds) {
        Equipment equip = equipIdsToEquips.get(equipId);
        if (equip != null) {
          resBuilder.addEquips(CreateInfoProtoUtils.createFullEquipProtoFromEquip(equip));
        } else {
          resBuilder.setStatus(RetrieveStaticDataStatus.SOME_FAIL);
          log.error("problem with retrieving equip with id " + equipId);
        }
      }
    }

    List <Integer> buildStructJobIds = reqProto.getBuildStructJobIdsList();
    if (buildStructJobIds != null && buildStructJobIds.size() > 0) {
      Map<Integer, BuildStructJob> buildStructJobIdsToBuildStructJobs = BuildStructJobRetrieveUtils.getBuildStructJobIdsToBuildStructJobs();
      for (Integer buildStructJobId :  buildStructJobIds) {
        BuildStructJob buildStructJob = buildStructJobIdsToBuildStructJobs.get(buildStructJobId);
        if (buildStructJob != null) {
          resBuilder.addBuildStructJobs(CreateInfoProtoUtils.createFullBuildStructJobProtoFromBuildStructJob(buildStructJob));
        } else {
          resBuilder.setStatus(RetrieveStaticDataStatus.SOME_FAIL);
          log.error("problem with retrieving build struct job with id " + buildStructJobId);
        }
      }
    }

    List <Integer> defeatTypeJobIds = reqProto.getDefeatTypeJobIdsList();
    if (defeatTypeJobIds != null && defeatTypeJobIds.size() > 0) {
      Map<Integer, DefeatTypeJob> defeatTypeJobIdsToDefeatTypeJobs = DefeatTypeJobRetrieveUtils.getDefeatTypeJobIdsToDefeatTypeJobs();
      for (Integer defeatTypeJobId :  defeatTypeJobIds) {
        DefeatTypeJob defeatTypeJob = defeatTypeJobIdsToDefeatTypeJobs.get(defeatTypeJobId);
        if (defeatTypeJob != null) {
          resBuilder.addDefeatTypeJobs(CreateInfoProtoUtils.createFullDefeatTypeJobProtoFromDefeatTypeJob(defeatTypeJob));
        } else {
          resBuilder.setStatus(RetrieveStaticDataStatus.SOME_FAIL);
          log.error("problem with retrieving defeat type job with id " + defeatTypeJobId);
        }
      }
    }

    List <Integer> possessEquipJobIds = reqProto.getPossessEquipJobIdsList();
    if (possessEquipJobIds != null && possessEquipJobIds.size() > 0) {
      Map<Integer, PossessEquipJob> possessEquipJobIdsToPossessEquipJobs = PossessEquipJobRetrieveUtils.getPossessEquipJobIdsToPossessEquipJobs();
      for (Integer possessEquipJobId :  possessEquipJobIds) {
        PossessEquipJob possessEquipJob = possessEquipJobIdsToPossessEquipJobs.get(possessEquipJobId);
        if (possessEquipJob != null) {
          resBuilder.addPossessEquipJobs(CreateInfoProtoUtils.createFullPossessEquipJobProtoFromPossessEquipJob(possessEquipJob));
        } else {
          resBuilder.setStatus(RetrieveStaticDataStatus.SOME_FAIL);
          log.error("problem with retrieving possess equip job with id " + possessEquipJobId);
        }
      }
    }

    List <Integer> upgradeStructJobIds = reqProto.getUpgradeStructJobIdsList();
    if (upgradeStructJobIds != null && upgradeStructJobIds.size() > 0) {
      Map<Integer, UpgradeStructJob> upgradeStructJobIdsToUpgradeStructJobs = UpgradeStructJobRetrieveUtils.getUpgradeStructJobIdsToUpgradeStructJobs();
      for (Integer upgradeStructJobId :  upgradeStructJobIds) {
        UpgradeStructJob upgradeStructJob = upgradeStructJobIdsToUpgradeStructJobs.get(upgradeStructJobId);
        if (upgradeStructJob != null) {
          resBuilder.addUpgradeStructJobs(CreateInfoProtoUtils.createFullUpgradeStructJobProtoFromUpgradeStructJob(upgradeStructJob));
        } else {
          resBuilder.setStatus(RetrieveStaticDataStatus.SOME_FAIL);
          log.error("problem with retrieving upgrade struct job with id " + upgradeStructJob);
        }
      }
    }

    if (reqProto.hasLevelForExpRequiredRequest()) {
      int level = reqProto.getLevelForExpRequiredRequest();
      if (level > ControllerConstants.LEVEL_UP__MAX_LEVEL_FOR_USER || level < 2) {
        resBuilder.setStatus(RetrieveStaticDataStatus.SOME_FAIL);
        log.error("no exp data stored for levels < 2 and levels > " + ControllerConstants.LEVEL_UP__MAX_LEVEL_FOR_USER);
      } else {
        int expRequired = LevelsRequiredExperienceRetrieveUtils.getRequiredExperienceForLevel(level);
        if (expRequired > 0)
          resBuilder.setExpRequiredForRequestedLevel(expRequired);
        else
          log.error("problem with retrieving exp required for level " + level);
      }
    }

    if (reqProto.getCurrentLockBoxEvents()) {
      resBuilder.addAllLockBoxEvents(MiscMethods.currentLockBoxEventsForUserType(type));
    }
    
    if (reqProto.getClanTierLevels()) {
      resBuilder.addAllClanTierLevels(MiscMethods.getAllClanTierLevelProtos());
    }
  }

}
