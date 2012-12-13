package com.lvl6.misc;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.lvl6.properties.ControllerConstants;
import com.lvl6.properties.NotificationConstants;
import com.lvl6.proto.EventProto.GeneralNotificationResponseProto;
import com.lvl6.proto.InfoProto.ColorProto;

public class Notification {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  private Map<String, Object> keysAndValues;
  private ColorProto.Builder rgb;

  public static final String ATTACKER_CONCEDED = "attacker conceded";
  public static final String OWNER_CONCEDED = "owner conceded";
  public static final String ATTACKER_NOT_ENOUGH_MEMBERS = "attacker does not have enough members";
  public static final String OWNER_NOT_ENOUGH_MEMBERS = "owner does not have enough members";
  public static final String CLAN_TOWER_WAR_ENDED = "battle ended";
  public static final String FOUND_AN_EPIC = "found an epic";

  //TODO: Determine the amount of time in between periodic notifications
  public static final long milliseconds_between_periodic_notifications = 
      (long)(1.0 / 5.0 * ControllerConstants.NUM_HOURS_FOR_CLAN_TOWER_WAR * 60 * 60 * 1000);
  /*
   * This class exists so as to not keep writing 
   * new Runnable() {
					@Override
					public void run() {
						...
					}
				}
   * and passing it into executor.execute whenever a message wants to be sent to players online.
   * Example would be in SendGroupChatController.java regarding sending to all players online.
   */
  public Notification () {
    this.keysAndValues = new HashMap<String, Object>();
    this.rgb = ColorProto.newBuilder();
  }

  public GeneralNotificationResponseProto.Builder generateNotificationBuilder() {
    final GeneralNotificationResponseProto.Builder notificationProto = 
        GeneralNotificationResponseProto.newBuilder();
    try {
      notificationProto.setTitle((String)keysAndValues.get("title"));
      notificationProto.setSubtitle((String)keysAndValues.get("subtitle"));
      notificationProto.setRgb((ColorProto)keysAndValues.get("rgb"));
    } catch (Exception e) {
      log.error("Error sending notification");
    }

    return notificationProto;
  }

  public void setAsClanTowerWarClanConceded (String losingClan, String winningClan, String towerName) {
    MessageFormat formatTitle = new MessageFormat(NotificationConstants.CLAN_CONCEDED__TITLE);
    MessageFormat formatSubtitle = new MessageFormat(NotificationConstants.CLAN_CONCEDED__SUBTITLE);
    
    Object[] arguments = { losingClan, winningClan, towerName };
    
    String title = formatTitle.format(arguments);
    String subtitle = formatSubtitle.format(arguments);
    rgb.setBlue(NotificationConstants.CLAN_CONCEDED__BLUE);
    rgb.setGreen(NotificationConstants.CLAN_CONCEDED__GREEN);
    rgb.setRed(NotificationConstants.CLAN_CONCEDED__RED);
    
    keysAndValues.put("title", title);
    keysAndValues.put("subtitle", subtitle);
    keysAndValues.put("rgb", rgb.build());
  }
  
  public void setAsClanTowerWarClanWon (String clanTowerAttackerName, String clanTowerOwnerName, 
      String towerName, boolean attackerWon) {
    MessageFormat formatTitle = new MessageFormat(NotificationConstants.CLAN_WON__TITLE);
    MessageFormat formatSubtitle = new MessageFormat(NotificationConstants.CLAN_WON__SUBTITLE);
    
    Object[] arguments;
    if(attackerWon) {
      arguments = new Object[]{ clanTowerAttackerName, clanTowerOwnerName, towerName};
    } else {
      arguments = new Object[]{ clanTowerOwnerName, clanTowerAttackerName, towerName};
    }
    
    String title = formatTitle.format(arguments);
    String subtitle = formatSubtitle.format(arguments);
    rgb.setBlue(NotificationConstants.CLAN_WON__BLUE);
    rgb.setGreen(NotificationConstants.CLAN_WON__GREEN);
    rgb.setRed(NotificationConstants.CLAN_WON__RED);
    
    keysAndValues.put("title", title);
    keysAndValues.put("subtitle", subtitle);
    keysAndValues.put("rgb", rgb.build());
  }
  
  public void setAsClanTowerWarAttackerOwnerDetermined (String attacker, String owner, String towerName, boolean ownerDetermined) {
    if(ownerDetermined) {
      Object[] arguments = { owner, towerName };

      MessageFormat formatTitle = new MessageFormat(NotificationConstants.CLAN_TOWER_OWNER_DETERMINED__TITLE);
      MessageFormat formatSubtitle = new MessageFormat(NotificationConstants.CLAN_TOWER_OWNER_DETERMINED__SUBTITLE);
      String title = formatTitle.format(arguments);
      String subtitle = formatSubtitle.format(arguments);

      keysAndValues.put("title", title);
      keysAndValues.put("subtitle", subtitle);
    }  else {
      Object[] arguments = { attacker, owner, towerName };

      MessageFormat formatTitle = new MessageFormat(NotificationConstants.CLAN_TOWER_ATTACKER_DETERMINED__TITLE);
      String title = formatTitle.format(arguments);

      keysAndValues.put("title", title);
    }

    keysAndValues.put("rgb", rgb.build());

  }

  public void setAsClanTowerWarDistributeRewards(String towerName, int silverReward, int goldReward) {
    String silver = "";
    String conjunction = "";
    String gold = "";
    
    if(0 < silverReward) {
      silver = silverReward + " silver";
    }
    if(0 < goldReward) {
      conjunction += " and";
      gold += goldReward + " gold.";
    }
    
    Object[] arguments = { towerName };
    MessageFormat formatTitle = new MessageFormat(NotificationConstants.CLAN_TOWER_DISTRIBUTE_REWARDS__TITLE);
    
    String title = formatTitle.format(arguments);
    String subtitle = NotificationConstants.CLAN_TOWER_DISTRIBUTE_REWARDS__SUBTITLE;
    subtitle += silver + conjunction + gold;
    
    keysAndValues.put("title", title);
    keysAndValues.put("subtitle", subtitle);
  }
  
  public void setNotificationAsClanCreated (String clanName) {
    //TODO: write logic for this function
  }

  public void setNotificationAsEpicWeaponDropped (
      String userName, String equipName, String townName) {
    //TODO: write logic for this function
  }
}
