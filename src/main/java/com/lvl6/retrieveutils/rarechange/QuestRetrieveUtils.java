package com.lvl6.retrieveutils.rarechange;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.*;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.info.Dialogue;
import com.lvl6.info.Quest;
import com.lvl6.misc.MiscMethods;
import com.lvl6.properties.DBConstants;
import com.lvl6.proto.InfoProto.SpecialQuestAction;
import com.lvl6.utils.DBConnection;
import com.lvl6.utils.QuestGraph;

@Component @DependsOn("gameServer") public class QuestRetrieveUtils {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  //private static Map<Integer, List<Quest>> cityIdToQuests;
  private static Map<Integer, Quest> questIdsToQuests;
  private static QuestGraph questGraph;

  private static final String TABLE_NAME = DBConstants.TABLE_QUESTS;

  public static Map<Integer, Quest> getQuestIdsToQuests() {
    log.debug("retrieving all quest data");
    if (questIdsToQuests == null) {
      setStaticQuestIdsToQuests();
    }
    return questIdsToQuests;
  }

  public static List<Quest> getQuestsInCity(int cityId) {
    log.debug("retrieving all quest data");
    if (questIdsToQuests == null) {
      setStaticQuestIdsToQuests();
    }
    List <Quest> questsInCity = new ArrayList<Quest>();
    for (Integer questId : questIdsToQuests.keySet()) {
      Quest q = questIdsToQuests.get(questId);
      if (q.getCityId() == cityId) {
        questsInCity.add(q);
      }
    }
    return questsInCity;
  }

  public static Quest getQuestForQuestId(int questId) {
    log.debug("retrieving quest with questId " + questId);
    if (questIdsToQuests == null) {
      setStaticQuestIdsToQuests();
    }
    return questIdsToQuests.get(questId);
  }

  public static QuestGraph getQuestGraph() {
    log.debug("retrieving quest graph");
    if (questGraph == null) {
      setStaticQuestGraph();
    }
    return questGraph;
  }

  private static void setStaticQuestIdsToQuests() {
    log.debug("setting static map of questIds to quests");

    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = null;
    if (conn != null) {
      rs = DBConnection.get().selectWholeTable(conn, TABLE_NAME);
      if (rs != null) {
        try {
          rs.last();
          rs.beforeFirst();
          HashMap<Integer, Quest> tmp = new HashMap<Integer, Quest>();
          while(rs.next()) {
            Quest quest = convertRSRowToQuest(rs);
            if (quest != null) {
              if (quest.getNumComponents(true) > 1 && quest.getSpecialQuestActionRequired() != null) {
                log.error("problem with quest in the db- has a special quest, as well as other components. quest=" + quest);
              } else {
                tmp.put(quest.getId(), quest);
              }
            }
          }
          questIdsToQuests = tmp;
        } catch (SQLException e) {
          log.error("problem with database call.", e);
          
        }
      }
    }
    DBConnection.get().close(rs,  null, conn);
  }

  private static void setStaticQuestGraph() {
    log.debug("setting static quest graph");

    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = null;
    if (conn != null) {
      rs = DBConnection.get().selectWholeTable(conn, TABLE_NAME);

      if (rs != null) {
        try {
          rs.last();
          rs.beforeFirst();
          List<Quest> quests = new ArrayList<Quest>();
          while(rs.next()) {  //should only be one
            Quest quest = convertRSRowToQuest(rs);
            if (quest != null) {
              if (quest.getNumComponents(true) > 1 && quest.getSpecialQuestActionRequired() != null) {
                log.error("problem with quest in the db- has a special quest, as well as other components. quest=" + quest);
              } else {
                quests.add(quest);
              }
            }
          }
          QuestGraph tmp = new QuestGraph(quests);
          questGraph = tmp;
        } catch (SQLException e) {
          log.error("problem with database call.", e);
          
        }
      }    
    }
    DBConnection.get().close(rs, null, conn);
  }

  public static void reload() {
    //setStaticCityIdsToQuests();
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

    String goodAcceptDialogueBlob = rs.getString(i++);
    Dialogue goodAcceptDialogue = MiscMethods.createDialogue(goodAcceptDialogueBlob);
    String badAcceptDialogueBlob = rs.getString(i++);
    Dialogue badAcceptDialogue = MiscMethods.createDialogue(badAcceptDialogueBlob);

    int assetNumWithinCity = rs.getInt(i++);
    int coinsGained = rs.getInt(i++);
    int diamondsGained = rs.getInt(i++);
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

    int coinRetrievalReq = rs.getInt(i++);

    SpecialQuestAction sqaReq = null;
    int specialQuestActionInt = rs.getInt(i++);
    if (!rs.wasNull()) {
      sqaReq = SpecialQuestAction.valueOf(specialQuestActionInt);
    }
    
    String goodQuestGiverImageSuffix = rs.getString(i++);
    String badQuestGiverImageSuffix = rs.getString(i++);
    
    int priority = rs.getInt(i++);
    
    Quest quest = new Quest(id, cityId, goodName, badName, goodDescription, badDescription, 
        goodDoneResponse, badDoneResponse,
        goodAcceptDialogue, badAcceptDialogue, assetNumWithinCity, 
        coinsGained, diamondsGained, expGained, equipIdGained, questsRequiredForThis, 
        tasksRequired, upgradeStructJobsRequired, 
        buildStructJobsRequired, defeatGoodGuysRequired, 
        defeatBadGuysRequired, possessEquipJobsRequired, coinRetrievalReq, sqaReq, 
        goodQuestGiverImageSuffix, badQuestGiverImageSuffix, priority);
    return quest;
  }

}
