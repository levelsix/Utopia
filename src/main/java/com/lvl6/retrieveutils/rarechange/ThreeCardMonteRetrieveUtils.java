package com.lvl6.retrieveutils.rarechange;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.info.MonteCard;
import com.lvl6.info.MonteCard.MonteCardType;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

@Component @DependsOn("gameServer") public class ThreeCardMonteRetrieveUtils {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static Map<Integer, MonteCard> monteCardIdsToMonteCards;

  private static final String TABLE_NAME = DBConstants.TABLE_THREE_CARD_MONTE;

  public static Map<Integer, MonteCard> getMonteCardIdsToMonteCards() {
    log.debug("retrieving three card monte data");
    if (monteCardIdsToMonteCards == null) {
      setStaticMonteCardIdsToMonteCards();
    }
    return monteCardIdsToMonteCards;
  }

  public static Map<Integer, MonteCard> getMonteCardsForMonteCardIds(List<Integer> ids) {
    log.debug("retrieving monte cards with ids " + ids);
    if (monteCardIdsToMonteCards == null) {
      setStaticMonteCardIdsToMonteCards();
    }
    Map<Integer, MonteCard> toreturn = new HashMap<Integer, MonteCard>();
    for (Integer id : ids) {
      toreturn.put(id,  monteCardIdsToMonteCards.get(id));
    }
    return toreturn;
  }

  public static MonteCard getMonteCardForMonteCardId(int upgradeStructJobId) {
    log.debug("retrieving upgrade struct job data for upgrade struct job id " + upgradeStructJobId);
    if (monteCardIdsToMonteCards == null) {
      setStaticMonteCardIdsToMonteCards();
    }
    return monteCardIdsToMonteCards.get(upgradeStructJobId);
  }

  private static void setStaticMonteCardIdsToMonteCards() {
    log.debug("setting static map of upgrade struct job id to upgrade struct job");

    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = null;
    if (conn != null) {
      rs = DBConnection.get().selectWholeTable(conn, TABLE_NAME);
      if (rs != null) {
        try {
          rs.last();
          rs.beforeFirst();
          Map <Integer, MonteCard> monteCardIdsToMonteCardsTemp = new HashMap<Integer, MonteCard>();
          while(rs.next()) {  //should only be one
            MonteCard mc = convertRSRowToMonteCard(rs);
            if (mc != null)
              monteCardIdsToMonteCardsTemp.put(mc.getId(), mc);
          }
          monteCardIdsToMonteCards = monteCardIdsToMonteCardsTemp;
        } catch (SQLException e) {
          log.error("problem with database call.");
          log.error(e);
        }
      }    
    }
    DBConnection.get().close(rs,  null, conn);
  }

  public static void reload() {
    setStaticMonteCardIdsToMonteCards();
  }

  /*
   * assumes the resultset is apprpriately set up. traverses the row it's on.
   */
  private static MonteCard convertRSRowToMonteCard(ResultSet rs) throws SQLException {
    int i = 1;
    int id = rs.getInt(i++);
    int diamondsGained = rs.getInt(i++);
    if (diamondsGained == 0) diamondsGained = ControllerConstants.NOT_SET;
    int coinsGained = rs.getInt(i++);
    if (coinsGained == 0) coinsGained = ControllerConstants.NOT_SET;
    int warriorEquipId = rs.getInt(i++);
    if (warriorEquipId == 0) warriorEquipId = ControllerConstants.NOT_SET;
    int warriorEquipLevel = rs.getInt(i++);
    if (warriorEquipLevel == 0) warriorEquipLevel = ControllerConstants.NOT_SET;
    int archerEquipId = rs.getInt(i++);
    if (archerEquipId == 0) archerEquipId = ControllerConstants.NOT_SET;
    int archerEquipLevel = rs.getInt(i++);
    if (archerEquipLevel == 0) archerEquipLevel = ControllerConstants.NOT_SET;
    int mageEquipId = rs.getInt(i++);
    if (mageEquipId == 0) mageEquipId = ControllerConstants.NOT_SET;
    int mageEquipLevel = rs.getInt(i++);
    if (mageEquipLevel == 0) mageEquipLevel = ControllerConstants.NOT_SET;
    Date startDate = new Date(rs.getTimestamp(i++).getTime());
    MonteCardType cardType = MonteCardType.valueOf(rs.getInt(i++));

    return new MonteCard(id, diamondsGained, coinsGained, warriorEquipId, warriorEquipLevel, archerEquipId, archerEquipLevel, mageEquipId, mageEquipLevel, startDate, cardType);
  }
}
