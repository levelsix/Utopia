package com.lvl6.misc;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lvl6.properties.ControllerConstants;
import com.lvl6.properties.NotificationConstants;
import com.lvl6.proto.EventProto.GeneralNotificationResponseProto;
import com.lvl6.proto.InfoProto.ColorProto;

public class Notification {


	private static final Logger log = LoggerFactory.getLogger(Notification.class);

  private Map<String, Object> keysAndValues;
  private ColorProto.Builder rgb;

  //should really move these into notification constants....
  public static final String ATTACKER_CONCEDED = "attacker conceded";
  public static final String OWNER_CONCEDED = "owner conceded";
  public static final String ATTACKER_NOT_ENOUGH_MEMBERS = "attacker does not have enough members";
  public static final String OWNER_NOT_ENOUGH_MEMBERS = "owner does not have enough members";
  public static final String CLAN_TOWER_WAR_ENDED = "battle ended";
  public static final String CLAN_TOWER_CLAIMED = "tower claimed";
  public static final String CLAN_TOWER_WAR_BEGAN = "battle began";
  public static final String FOUND_AN_EPIC = "found an epic";
  
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
      log.error(""+e);
    }

    return notificationProto;
  }

  public void setAsClanTowerWarClanConceded (String losingClan, String winningClan, String towerName) {
    MessageFormat formatTitle = new MessageFormat(NotificationConstants.CLAN_CONCEDED__TITLE);
    MessageFormat formatSubtitle = new MessageFormat(NotificationConstants.CLAN_CONCEDED__SUBTITLE);
    
    if (winningClan == null) {
      winningClan = "";
      formatTitle = new MessageFormat(NotificationConstants.CLAN_CONCEDED__TITLE_NO_OWNER);
      formatSubtitle = new MessageFormat(NotificationConstants.CLAN_CONCEDED__SUBTITLE_NO_OWNER);
    }

    Object[] arguments = { losingClan, winningClan, towerName };

    String title = formatTitle.format(arguments);
    String subtitle = formatSubtitle.format(arguments);
    rgb.setBlue(NotificationConstants.CLAN_CONCEDED__BLUE);
    rgb.setGreen(NotificationConstants.CLAN_CONCEDED__GREEN);
    rgb.setRed(NotificationConstants.CLAN_CONCEDED__RED);

    keysAndValues.put("title", title);
    keysAndValues.put("subtitle", subtitle);
    keysAndValues.put("rgb", rgb.build());

    log.info("some clan conceded." + title + " " + subtitle);
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

    log.info("some clan won. " + title + " " + subtitle);
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

      log.info("owner for tower determined. " + title + " " + subtitle);
    }  else {
      Object[] arguments = { attacker, owner, towerName };

      MessageFormat formatTitle = new MessageFormat(NotificationConstants.CLAN_TOWER_ATTACKER_DETERMINED__TITLE);
      MessageFormat formatSubtitle = new MessageFormat(NotificationConstants.CLAN_TOWER_ATTACKER_DETERMINED__SUBTITLE);
      String title = formatTitle.format(arguments);
      String subtitle = formatSubtitle.format(arguments);

      keysAndValues.put("title", title);
      keysAndValues.put("subtitle", subtitle);

      log.info("attacker for tower determined. " + title + " ");
    }

    rgb.setBlue(NotificationConstants.CLAN_TOWER_ATTACKER_OWNER_DETERMINED__BLUE);
    rgb.setGreen(NotificationConstants.CLAN_TOWER_ATTACKER_OWNER_DETERMINED__GREEN);
    rgb.setRed(NotificationConstants.CLAN_TOWER_ATTACKER_OWNER_DETERMINED__RED);

    keysAndValues.put("rgb", rgb.build());

  }

  public void setAsClanTowerAttackerDeterminedAPNS(String attacker, String owner, 
      String towerName, boolean msgForAttacker) {
    
    if (msgForAttacker) {
      Object[] arguments = { owner, towerName };

      MessageFormat formatTitle = new MessageFormat(NotificationConstants.ATTACKING_CLAN__TITLE_APNS);
      Random rand = new Random();
      String[] choices = NotificationConstants.ATTACKING_CLAN__SUBTITLE_APNS; 
      int size = choices.length;
      int randInt = rand.nextInt(size);
      MessageFormat formatSubtitle = new MessageFormat(choices[randInt]);
      String title = formatTitle.format(arguments);
      String subtitle = formatSubtitle.format(arguments);
      
      keysAndValues.put("title", title);
      keysAndValues.put("subtitle", subtitle);
      log.info("sending apns to attacking clan " + attacker 
          + "in clan tower war. title=" + title);
    } else {
      Object[] arguments = { attacker, towerName };

      MessageFormat formatTitle = new MessageFormat(NotificationConstants.DEFENDING_CLAN__TITLE_APNS);
      Random rand = new Random();
      String[] choices = NotificationConstants.DEFENDING_CLAN__SUBTITLE_APNS; 
      int size = choices.length;
      int randInt = rand.nextInt(size);
      MessageFormat formatSubtitle = new MessageFormat(choices[randInt]);
      String title = formatTitle.format(arguments);
      String subtitle = formatSubtitle.format(arguments);
      
      keysAndValues.put("title", title);
      keysAndValues.put("subtitle", subtitle);
      log.info("sending apns to defending clan " + owner 
          + "in clan tower war. title=" + title);
    }
    
    rgb.setRed(NotificationConstants.ATTACKING_CLAN_DETERMINED_APNS__RED);
    rgb.setGreen(NotificationConstants.ATTACKING_CLAN_DETERMINED_APNS__GREEN);
    rgb.setBlue(NotificationConstants.ATTACKING_CLAN_DETERMINED_APNS__BLUE);
    
    keysAndValues.put("rgb", rgb.build());
  }
  
  public void setAsClanTowerWarDistributeRewards(String towerName, int silverReward, int goldReward, int numHours) {
    String silver = "";
    String conjunction = "";
    String gold = "";

    if (0 < silverReward) {
      silver = silverReward + " silver";
      conjunction = " and ";
    } if (0 < goldReward) {
      gold += goldReward + " gold";
    } else {
      conjunction = "";
    }
    
    String rewards = silver + conjunction + gold;
    String numHoursStr = numHours + "";

    Object[] arguments = { towerName, numHoursStr, rewards };
    MessageFormat formatTitle = new MessageFormat(NotificationConstants.CLAN_TOWER_DISTRIBUTE_REWARDS__TITLE);
    MessageFormat formatSubtitle = new MessageFormat(NotificationConstants.CLAN_TOWER_DISTRIBUTE_REWARDS__SUBTITLE);

    String title = formatTitle.format(arguments);
    String subtitle = formatSubtitle.format(arguments);
    rgb.setBlue(NotificationConstants.CLAN_TOWER_DISTRIBUTE_REWARDS__BLUE);
    rgb.setGreen(NotificationConstants.CLAN_TOWER_DISTRIBUTE_REWARDS__GREEN);
    rgb.setRed(NotificationConstants.CLAN_TOWER_DISTRIBUTE_REWARDS__RED);

    keysAndValues.put("title", title);
    keysAndValues.put("subtitle", subtitle);
    keysAndValues.put("rgb", rgb.build());

    log.info("rewards sent. " + title + " " + subtitle);
  }

  public void setAsClanCreated (String clanOwnerName, String clanName) {
    Object[] arguments = { clanOwnerName, clanName };

    MessageFormat formatTitle = new MessageFormat(NotificationConstants.CLAN_CREATED__TITLE);
    MessageFormat formatSubtitle = new MessageFormat(NotificationConstants.CLAN_CREATED__SUBTITLE);

    String title = formatTitle.format(arguments);
    String subtitle = formatSubtitle.format(arguments);

    rgb.setBlue(NotificationConstants.CLAN_CREATED__BLUE);
    rgb.setGreen(NotificationConstants.CLAN_CREATED__GREEN);
    rgb.setRed(NotificationConstants.CLAN_CREATED__RED);

    keysAndValues.put("title", title);
    keysAndValues.put("subtitle", subtitle);
    keysAndValues.put("rgb", rgb.build());

    log.info("new clan created. " + title + " " + subtitle);
  }

  public void setAsEpicWeaponDropped (
      String userName, String equipName, String townName) {
    Object[] arguments = { userName, equipName, townName };

    MessageFormat formatTitle = new MessageFormat(NotificationConstants.EPIC_WEAPON_DROPPED__TITLE);
    MessageFormat formatSubtitle = new MessageFormat(NotificationConstants.EPIC_WEAPON_DROPPED__SUBTITLE);

    String title = formatTitle.format(arguments);
    String subtitle = formatSubtitle.format(arguments);

    rgb.setBlue(NotificationConstants.EPIC_WEAPON_DROPPED__BLUE);
    rgb.setGreen(NotificationConstants.EPIC_WEAPON_DROPPED__GREEN);
    rgb.setRed(NotificationConstants.EPIC_WEAPON_DROPPED__RED);

    keysAndValues.put("title", title);
    keysAndValues.put("subtitle", subtitle);
    keysAndValues.put("rgb", rgb.build());

    log.info("epic weapon dropped. " + title + " " + subtitle);
  }
  
  //announces the first place winner
  public void setAsLeaderboardEventEndedGlobal(String firstPlaceWinnerName, int gold) {
    Object[] arguments = { firstPlaceWinnerName, gold };
    
    MessageFormat formatTitle = new MessageFormat(
        NotificationConstants.LEADERBOARD_EVENT_ENDED_GLOBAL_TITLE);
    MessageFormat formatSubtitle = new MessageFormat(
        NotificationConstants.LEADERBOARD_EVENT_ENDED_GLOBAL_SUBTITLE);
    
    String title = formatTitle.format(arguments);
    String subtitle = formatSubtitle.format(arguments);
    
    rgb.setBlue(NotificationConstants.LEADERBOARD_EVENT_ENDED_GLOBAL__BLUE);
    rgb.setGreen(NotificationConstants.LEADERBOARD_EVENT_ENDED_GLOBAL__GREEN);
    rgb.setRed(NotificationConstants.LEADERBOARD_EVENT_ENDED_GLOBAL__RED);
    
    keysAndValues.put("title", title);
    keysAndValues.put("subtitle", subtitle);
    keysAndValues.put("rgb", rgb.build());
    
    log.info("global notification for tournament/leaderboard event ending. " 
        + title + " " + subtitle);
  }
  
  public void setAsLeaderboardEventEndedIndividual(boolean playerOnline, 
      int rank, int gold) {
    Object[] arguments = { rank+"", gold };
    
    MessageFormat formatTitle;
    MessageFormat formatSubtitle;
    if(playerOnline) {
      formatTitle = new MessageFormat(
          NotificationConstants.LEADERBOARD_EVENT_ENDED_INDIVIDUAL_ONLINE_TITLE);
      formatSubtitle = new MessageFormat(
          NotificationConstants.LEADERBOARD_EVENT_ENDED_INDIVIDUAL_ONLINE_SUBTITLE);
    } else {
      formatTitle = new MessageFormat(
          NotificationConstants.LEADERBOARD_EVENT_ENDED_INDIVIDUAL_OFFLINE_TITLE);
      formatSubtitle = new MessageFormat(
          NotificationConstants.LEADERBOARD_EVENT_ENDED_INDIVIDUAL_OFFLINE_SUBTITLE);
    }
    
    String title = formatTitle.format(arguments);
    String subtitle = formatSubtitle.format(arguments);
   
    //color does not matter in apns notification, just set it anyways
    //TODO: FIX THESE NUMBERS IN NOTIFICATION CONSTANTS . JAVA
    rgb.setBlue(NotificationConstants.LEADERBOARD_EVENT_ENDED_INDIVIDUAL__BLUE);
    rgb.setGreen(NotificationConstants.LEADERBOARD_EVENT_ENDED_INDIVIDUAL__GREEN);
    rgb.setRed(NotificationConstants.LEADERBOARD_EVENT_ENDED_INDIVIDUAL__RED);

    keysAndValues.put("title", title);
    keysAndValues.put("subtitle", subtitle);
    keysAndValues.put("rgb", rgb.build());
    
    log.info("notification for player. player online=" + playerOnline 
        + " title=" + title + " subtitle=" + subtitle);
  }
  
  public void setAsUserRequestedToJoinClan (int level, String requester) {
    Object[] arguments = { level, requester };
    MessageFormat formatTitle = 
        new MessageFormat(NotificationConstants.REQUEST_TO_JOIN_A_CLAN__TITLE);
    MessageFormat formatSubtitle = 
        new MessageFormat(NotificationConstants.REQUEST_TO_JOIN_A_CLAN__SUBTITLE);
    String title = formatTitle.format(arguments);
    String subtitle = formatSubtitle.format(arguments);
    
    rgb.setBlue(NotificationConstants.REQUEST_TO_JOIN_A_CLAN__BLUE);
    rgb.setGreen(NotificationConstants.REQUEST_TO_JOIN_A_CLAN__GREEN);
    rgb.setRed(NotificationConstants.REQUEST_TO_JOIN_A_CLAN__RED);
    
    keysAndValues.put("title", title);
    keysAndValues.put("subtitle", subtitle);
    keysAndValues.put("rgb", rgb.build());
    
    log.info("sending join clan request for level " + level + " " + requester);
  }
  
  public void setAsUserJoinedClan(int level, String newMember) {
    Object[] arguments = { level, newMember };
    String title = NotificationConstants.USER_JOINED_A_CLAN__TITLE;
    String subtitle = NotificationConstants.USER_JOINED_A_CLAN__SUBTITLE;
    int blue = NotificationConstants.USER_JOINED_A_CLAN__BLUE;
    int green = NotificationConstants.USER_JOINED_A_CLAN__GREEN;
    int red = NotificationConstants.USER_JOINED_A_CLAN__RED;
    
    setUpNotification(arguments, title, subtitle, blue, green, red);
    log.info("created joined clan notification for level" + level + " " + newMember);
  }
  
  public void setAsUserLeftClan(int level, String deserter) {
    Object[] arguments = { level, deserter };
    String title = NotificationConstants.USER_LEFT_A_CLAN__TITLE;
    String subtitle = NotificationConstants.USER_LEFT_A_CLAN__SUBTITLE;
    int blue = NotificationConstants.USER_LEFT_A_CLAN__BLUE;
    int green = NotificationConstants.USER_LEFT_A_CLAN__GREEN;
    int red = NotificationConstants.USER_LEFT_A_CLAN__RED;
    
    setUpNotification(arguments, title, subtitle, blue, green, red);
    log.info("created left clan notification for level" + level + " " + deserter);
  }
  
  public void setAsNotEnoughGoldToPrestige() {
    int silver = ControllerConstants.PRESTIGE__PRESTIGE_SILVER_COST;
    Object[] argumentsToMsgFormat = { silver };
    String title = NotificationConstants.PRESTIGE_NOT_ENOUGH_CURRENCY__TITLE;
    String subtitle = NotificationConstants.PRESTIGE_NOT_ENOUGH_CURRENCY__SUBTITLE;
    int blue = NotificationConstants.PRESTIGE_NOT_ENOUGH_CURRENCY__BLUE;
    int green = NotificationConstants.PRESTIGE_NOT_ENOUGH_CURRENCY__GREEN;
    int red = NotificationConstants.PRESTIGE_NOT_ENOUGH_CURRENCY__RED;
    
    setUpNotification(argumentsToMsgFormat, title, subtitle, blue, green, red);
    log.info("created not-enough-gold-to-prestige notification");
  }
  
  private void setUpNotification(Object[] argumentsToMsgFormat, String title, String subtitle,
      int blue, int green, int red) {
    MessageFormat formatTitle = new MessageFormat(title);
    MessageFormat formatSubtitle = new MessageFormat(subtitle);
    String apnsTitle = formatTitle.format(argumentsToMsgFormat);
    String apnsSubtitle = formatSubtitle.format(argumentsToMsgFormat);
    
    rgb.setBlue(blue);
    rgb.setGreen(green);
    rgb.setRed(red);
    
    keysAndValues.put("title", apnsTitle);
    keysAndValues.put("subtitle", apnsSubtitle);
    keysAndValues.put("rgb", rgb.build());
  }
  
  public String titleAndSubtitle() {
    return keysAndValues.get("title").toString() + " " +
        keysAndValues.get("subtitle").toString();
  }
  
}
