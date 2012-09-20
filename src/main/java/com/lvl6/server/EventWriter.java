package com.lvl6.server;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.message.GenericMessage;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.ITopic;
import com.lvl6.events.BroadcastResponseEvent;
import com.lvl6.events.GameEvent;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.events.ResponseEvent;
import com.lvl6.info.UserClan;
import com.lvl6.properties.Globals;
import com.lvl6.retrieveutils.UserClanRetrieveUtils;
import com.lvl6.utils.ConnectedPlayer;
import com.lvl6.utils.NIOUtils;
import com.lvl6.utils.Wrap;

public class EventWriter extends Wrap implements HazelcastInstanceAware {
  // reference to game server


  //	@Resource(name="gameEventsHandlerExecutor")
  //	protected Executor gameEventsExecutor;
  //
  //
  //	public Executor getGameEventsExecutor() {
  //		return gameEventsExecutor;
  //	}
  //
  //	public void setGameEventsExecutor(Executor gameEventsExecutor) {
  //		this.gameEventsExecutor = gameEventsExecutor;
  //	}


  @Autowired
  UserClanRetrieveUtils userClanRetrieveUtil;

  public UserClanRetrieveUtils getUserClanRetrieveUtil() {
    return userClanRetrieveUtil;
  }

  public void setUserClanRetrieveUtil(UserClanRetrieveUtils userClanRetrieveUtil) {
    this.userClanRetrieveUtil = userClanRetrieveUtil;
  }

  public Map<String, ConnectedPlayer> getPlayersPreDatabaseByUDID() {
    return playersPreDatabaseByUDID;
  }

  public void setPlayersPreDatabaseByUDID(
      Map<String, ConnectedPlayer> playersPreDatabaseByUDID) {
    this.playersPreDatabaseByUDID = playersPreDatabaseByUDID;
  }

  public Map<Integer, ConnectedPlayer> getPlayersByPlayerId() {
    return playersByPlayerId;
  }

  public void setPlayersByPlayerId(Map<Integer, ConnectedPlayer> playersByPlayerId) {
    this.playersByPlayerId = playersByPlayerId;
  }

  @Resource(name="playersPreDatabaseByUDID")
  protected Map<String, ConnectedPlayer> playersPreDatabaseByUDID;


  @Resource(name="playersByPlayerId")
  protected Map<Integer, ConnectedPlayer> playersByPlayerId;


  @Resource
  protected ServerInstance serverInstance;


  private static org.slf4j.Logger log = LoggerFactory.getLogger(EventWriter.class);

  /**
   * constructor.
   */
  public EventWriter() {

  }

  protected void processEvent(GameEvent event) {
    if (event instanceof ResponseEvent)
      processResponseEvent((ResponseEvent) event);

  }


  /**
   * our own version of processEvent that takes the additional parameter of
   * the writeBuffer
   */
  public void processResponseEvent(ResponseEvent event) {
    log.debug("writer received event=" + event);
    ByteBuffer buff = getBytes(event);
    if (BroadcastResponseEvent.class.isInstance(event)) {
      int[] recipients = ((BroadcastResponseEvent) event).getRecipients();
      for (int i = 0; i < recipients.length; i++) {
        if (recipients[i] > 0) {
          log.info("writing broadcast event with type="+ event.getEventType() + " to players with ids "+ recipients[i]);
          ConnectedPlayer player = playersByPlayerId.get(recipients[i]);
          if(player != null){
            log.info("writing normal event with type=" + event.getEventType()+ " to player with id " + recipients[i] + ", event=" + event);
            write(buff.duplicate(), player);
          }else{
            //throw new Exception("Player "+playerId+" not found in playersByPlayerId");
            log.debug("Broadcast: Player "+recipients[i]+" not found in playersByPlayerId");
          }
        }
      }
    }
    // Otherwise this is just a normal message, send response to sender.
    else {
      int playerId = ((NormalResponseEvent) event).getPlayerId();
      sendMessageToPlayer(event, buff, playerId);
    }

  }

  protected void sendMessageToPlayer(ResponseEvent event, ByteBuffer buff, int playerId) {
    ConnectedPlayer player = playersByPlayerId.get(playerId);
    if(player != null){
      log.info("writing event with type=" + event.getEventType()+ " to player with id " + playerId + ", event=" + event);
      write(buff, player);
    }else{
      //throw new Exception("Player "+playerId+" not found in playersByPlayerId");
      log.debug("Player "+playerId+" not found in playersByPlayerId");
    }
  }


  public void processClanResponseEvent(GameEvent event, int clanId) {
    log.debug("writer received clan event=" + event);
    ResponseEvent e = (ResponseEvent)event;
    ByteBuffer buff = getBytes(e);
    List<UserClan> playersInClan = userClanRetrieveUtil.getUserClanMembersInClan(clanId);
    for (UserClan uc : playersInClan) {
        sendMessageToPlayer(e, buff, uc.getUserId());
    }
  }


  public void processPreDBResponseEvent(ResponseEvent event, String udid) {
    ConnectedPlayer player = playersPreDatabaseByUDID.get(udid);
    ByteBuffer bytes = getBytes(event);
    write(bytes, player);
  }

  protected ByteBuffer getBytes(ResponseEvent event) {
    ByteBuffer writeBuffer = ByteBuffer.allocateDirect(Globals.MAX_EVENT_SIZE);
    NIOUtils.prepBuffer(event, writeBuffer);
    return writeBuffer;
  }

  /**
   * write the event to the given playerId's channel
   */
  private void write(ByteBuffer event, ConnectedPlayer player) {
    log.debug("EventWriter.write for player on server {}  this server is: {}", player.getServerHostName(), serverInstance.serverId());
    Map<String, Object> headers = new HashMap<String, Object>();
    headers.put("ip_connection_id", player.getIp_connection_id());
    if(player.getPlayerId() != 0) {
      headers.put("playerId", player.getPlayerId());
    }
    byte[] bArray = new byte[event.remaining()];
    event.get(bArray);
    Message<byte[]> msg = new GenericMessage<byte[]>(bArray, headers);
    if(player.getServerHostName().equals(serverInstance.serverId())) {
      log.debug("EventWriter.write... handling message on local server instance");
      com.hazelcast.core.Message<Message<?>> playerMessage = new com.hazelcast.core.Message<Message<?>>(serverInstance.serverId(), msg);
      serverInstance.onMessage(playerMessage);
    }else {
      log.debug("EventWriter.write... sending message to hazel for processing");
      ITopic<Message<?>> serverOutboundMessages = hazel.getTopic(ServerInstance.getOutboundMessageTopicForServer(player.getServerHostName()));
      serverOutboundMessages.publish(msg);
    }
  }



  /***
   * for sending queued messages to reconnected players 
   * @param message
   * @param playerId
   */
  //
  public void sendMessageToPlayer(Message<?> message, Integer playerId) {
    ConnectedPlayer player = playersByPlayerId.get(playerId);
    Map<String, Object> headers = new HashMap<String, Object>();
    headers.put("ip_connection_id", player.getIp_connection_id());
    if(player.getPlayerId() > 0) {
      headers.put("playerId", player.getPlayerId());
    }
    Message<byte[]> msg = new GenericMessage<byte[]>((byte[]) message.getPayload(), headers);
    //don't send to hazelcast topic if player is local to this machine
    if(player.getServerHostName().equals(serverInstance.serverId())) {
      com.hazelcast.core.Message<Message<?>> playerMessage = new com.hazelcast.core.Message<Message<?>>(serverInstance.serverId(), msg);
      serverInstance.onMessage(playerMessage);
    }else {
      ITopic<Message<?>> serverOutboundMessages = hazel.getTopic(ServerInstance.getOutboundMessageTopicForServer(player.getServerHostName()));
      serverOutboundMessages.publish(msg);
    }
  }

  protected HazelcastInstance hazel;
  @Override
  @Autowired
  public void setHazelcastInstance(HazelcastInstance instance) {
    hazel = instance;
  }

}// EventWriter