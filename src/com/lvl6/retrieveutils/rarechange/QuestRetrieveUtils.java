package com.lvl6.retrieveutils.rarechange;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.lvl6.info.Quest;
import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;
import com.lvl6.utils.QuestGraph;

public class QuestRetrieveUtils {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static Map<Integer, List<Quest>> cityIdToQuests;
  private static Map<Integer, Quest> questIdsToQuests;
  private static QuestGraph questGraph;

  private static final String TABLE_NAME = DBConstants.TABLE_QUESTS;

  public static Map<Integer, Quest> getQuestIdsToQuests() {
    log.info("retrieving all-quest data");
    if (questIdsToQuests == null) {
      setStaticQuestIdsToQuests();
    }
    return questIdsToQuests;
  }

  public static Quest getQuestForQuestId(int questId) {
    log.info("retrieve task data");
    if (questIdsToQuests == null) {
      setStaticQuestIdsToQuests();
    }
    return questIdsToQuests.get(questId);
  }

  public static List<Integer> getAvailableQuests(List<Integer> completed, List<Integer> inProgress) {
    log.info("retrieving available quests");
    if (questIdsToQuests == null) {
      setStaticQuestGraph();
    }
    return questGraph.getQuestsAvailable(completed, inProgress);
  }

  public static List<Quest> getQuestsForCityId(int cityId) {
    if (cityIdToQuests == null) {
      setStaticCityIdsToQuests();
    }
    return cityIdToQuests.get(cityId);
  }

  private static void setStaticCityIdsToQuests() {
    log.info("setting static map of cityId to quests");
    ResultSet rs = DBConnection.selectWholeTable(TABLE_NAME);

    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        Map<Integer, List<Quest>> cityIdToQuestsTemp = new HashMap<Integer, List<Quest>>();
        while(rs.next()) {
          Quest quest = convertRSRowToQuest(rs);
          if (quest != null) {
            if (cityIdToQuestsTemp.get(quest.getCityId()) == null) {
              cityIdToQuestsTemp.put(quest.getCityId(), new ArrayList<Quest>());
            }
            cityIdToQuestsTemp.get(quest.getCityId()).add(quest);
          }
        }
        cityIdToQuests = cityIdToQuestsTemp;
      } catch (SQLException e) {
        log.error("problem with database call.");
        log.error(e);
      }
    }    
  }

  private static void setStaticQuestIdsToQuests() {
    log.info("setting static map of questIds to quests");
    ResultSet rs = DBConnection.selectWholeTable(TABLE_NAME);
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        HashMap<Integer, Quest> tmp = new HashMap<Integer, Quest>();
        while(rs.next()) {
          Quest quest = convertRSRowToQuest(rs);
          if (quest != null)
            tmp.put(quest.getId(), quest);
        }
        questIdsToQuests = tmp;
      } catch (SQLException e) {
        log.error("problem with database call.");
        log.error(e);
      }
    }    
  }

  private static void setStaticQuestGraph() {
    log.info("setting static quest graph");
    ResultSet rs = DBConnection.selectWholeTable(TABLE_NAME);
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        List<Quest> quests = new ArrayList<Quest>();
        while(rs.next()) {  //should only be one
          Quest quest = convertRSRowToQuest(rs);
          if (quest != null)
            quests.add(quest);
        }
        QuestGraph tmp = new QuestGraph(quests);
        questGraph = tmp;
      } catch (SQLException e) {
        log.error("problem with database call.");
        log.error(e);
      }
    }    
  }

  public static void reload() {
    setStaticCityIdsToQuests();
    setStaticQuestGraph();
    setStaticQuestIdsToQuests();
  }


  /*
   * assumes the resultset is apprpriately set up. traverses the row it's on.
   */
  private static Quest convertRSRowToQuest(ResultSet rs) throws SQLException {
    StringTokenizer st;

    int i = 1;
    int id = rs.getInt(i++);
    int cityId = rs.getInt(i++);
    String goodName = rs.getString(i++);
    String badName = rs.getString(i++);
    String goodDescription = rs.getString(i++);
    String badDescription = rs.getString(i++);
    String goodDoneResponse = rs.getString(i++);
    String badDoneResponse = rs.getString(i++);
    String goodInProgress = rs.getString(i++);
    String badInProgress = rs.getString(i++);
    int assetNumWithinCity = rs.getInt(i++);
    int coinsGained = rs.getInt(i++);
    int diamondsGained = rs.getInt(i++);
    int woodGained = rs.getInt(i++);
    int expGained = rs.getInt(i++);
    int equipIdGained = rs.getInt(i++);

    String questsRequiredForThisString = rs.getString(i++);
    List<Integer> questsRequiredForThis = new ArrayList<Integer>();
    if (questsRequiredForThisString != null) {
      st = new StringTokenizer(questsRequiredForThisString, ", ");
      while (st.hasMoreTokens()) {
        questsRequiredForThis.add(Integer.parseInt(st.nextToken()));
      }
    }

    String tasksRequiredString = rs.getString(i++);
    List<Integer> tasksRequired = new ArrayList<Integer>();
    if (tasksRequiredString != null) {
      st = new StringTokenizer(tasksRequiredString, ", ");
      while (st.hasMoreTokens()) {
        tasksRequired.add(Integer.parseInt(st.nextToken()));
      }
    }

    String upgradeStructJobsRequiredString = rs.getString(i++);
    List<Integer> upgradeStructJobsRequired = new ArrayList<Integer>();
    if (upgradeStructJobsRequiredString != null) {
      st = new StringTokenizer(upgradeStructJobsRequiredString, ", ");
      while (st.hasMoreTokens()) {
        upgradeStructJobsRequired.add(Integer.parseInt(st.nextToken()));
      }
    }

    String buildStructJobsRequiredString = rs.getString(i++);
    List<Integer> buildStructJobsRequired = new ArrayList<Integer>();
    if (buildStructJobsRequiredString != null) {
      st = new StringTokenizer(buildStructJobsRequiredString, ", ");
      while (st.hasMoreTokens()) {
        buildStructJobsRequired.add(Integer.parseInt(st.nextToken()));
      }
    }

    String defeatGoodGuysRequiredString = rs.getString(i++);
    List<Integer> defeatGoodGuysRequired = new ArrayList<Integer>();
    if (defeatGoodGuysRequiredString != null) {
      st = new StringTokenizer(defeatGoodGuysRequiredString, ", ");
      while (st.hasMoreTokens()) {
        defeatGoodGuysRequired.add(Integer.parseInt(st.nextToken()));
      }
    }

    String defeatBadGuysRequiredString = rs.getString(i++);
    List<Integer> defeatBadGuysRequired = new ArrayList<Integer>();
    if (defeatBadGuysRequiredString != null) {
      st = new StringTokenizer(defeatBadGuysRequiredString, ", ");
      while (st.hasMoreTokens()) {
        defeatBadGuysRequired.add(Integer.parseInt(st.nextToken()));
      }
    }

    String possessEquipJobsRequiredString = rs.getString(i++);
    List<Integer> possessEquipJobsRequired = new ArrayList<Integer>();
    if (possessEquipJobsRequiredString != null) {
      st = new StringTokenizer(possessEquipJobsRequiredString, ", ");
      while (st.hasMoreTokens()) {
        possessEquipJobsRequired.add(Integer.parseInt(st.nextToken()));
      }
    }
    
    Quest quest = new Quest(id, cityId, goodName, badName, goodDescription, badDescription, 
        goodDoneResponse, badDoneResponse, goodInProgress, badInProgress, assetNumWithinCity, 
        coinsGained, diamondsGained, woodGained, expGained, equipIdGained, questsRequiredForThis, 
        tasksRequired, upgradeStructJobsRequired, 
        buildStructJobsRequired, defeatGoodGuysRequired, 
        defeatBadGuysRequired, possessEquipJobsRequired);
    return quest;
  }
}
