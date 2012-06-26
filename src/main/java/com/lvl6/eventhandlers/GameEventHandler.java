package com.lvl6.eventhandlers;

import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.MessagingException;
import org.springframework.integration.core.MessageHandler;

import com.lvl6.events.PreDatabaseRequestEvent;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.server.GameServer;
import com.lvl6.server.controller.EventController;
import com.lvl6.utils.Attachment;
import com.lvl6.utils.ConnectedPlayer;

public class GameEventHandler implements MessageHandler {
	private static Logger log = Logger.getLogger(GameEventHandler.class);
	
	
	@Autowired
	GameServer server;
		
	@Resource(name="playersByPlayerId")
	Map<Integer, ConnectedPlayer> playersByPlayerId;
	
	@Resource(name="playersPreDatabaseByUDID")
	Map<String, ConnectedPlayer> playersPreDatabaseByUDID;
	
	
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


	public GameServer getServer() {
		return server;
	}


	public void setServer(GameServer server) {
		this.server = server;
	}


	@Override
	public void handleMessage(Message<?> msg) throws MessagingException {
		log.debug("Received message: ");
		for (String key: msg.getHeaders().keySet()) {
			log.debug(key+": "+msg.getHeaders().get(key));
		}
		//log.info("Payload: "+msg.getPayload());
		Attachment attachment = new Attachment();
		attachment.readBuff = ByteBuffer.wrap((byte[]) msg.getPayload());
		while(attachment.eventReady()) {
            RequestEvent event = getEvent(attachment);
            log.debug("Recieved event from client: "+event.getPlayerId());
            
				delegateEvent(event, (String) msg.getHeaders().get("ip_connection_id"), attachment.eventType);

            attachment.reset();
        }
	}
	
	
	  /**
	   * read an event from the attachment's payload
	   */
	  private RequestEvent getEvent(Attachment attachment) {
	    RequestEvent event = null;
	    ByteBuffer bb = ByteBuffer.wrap(Arrays.copyOf(attachment.payload, attachment.payloadSize));

	    // get the controller and tell it to instantiate an event for us
	    EventController ec = server.getEventControllerByEventType(attachment.eventType);

	    if (ec == null) {
	      return null;
	    }
	    event = ec.createRequestEvent();
	    event.setTag(attachment.tag);

	    // read the event from the payload
	    event.read(bb); 
	    return event;
	  }

	  /**
	   * pass off an event to the appropriate GameController
	   * based on the GameName of the event
	 * @throws FileNotFoundException 
	   */
	  private void delegateEvent(RequestEvent event, String ip_connection_id, EventProtocolRequest eventType){
	    if (event != null && eventType.getNumber() < 0) {
	      log.error("the event type is < 0");
	      return;
	    }
	    EventController ec = server.getEventControllerByEventType(eventType);
	    if (ec == null) {
	      log.error("No EventController for eventType: " + eventType);
	      return;
	    }
	    updatePlayerToServerMaps(event, ip_connection_id);
	    ec.handleEvent(event);
	  }
	  
	  
	  protected void updatePlayerToServerMaps(RequestEvent event, String ip_connection_id) {
		  log.debug("Updating player to server maps for player: "+event.getPlayerId());
		  if(playersByPlayerId.containsKey(event.getPlayerId())) {
			  ConnectedPlayer p = playersByPlayerId.get(event.getPlayerId());
			  if(!p.getIp_connection_id().equals(ip_connection_id) || !p.getServerHostName().equals(server.serverId())) {
				  log.debug("Player is connected to a new socket or server");
				  p.setIp_connection_id(ip_connection_id);
				  p.setServerHostName(server.serverId());
				  playersByPlayerId.put(event.getPlayerId(), p);
			  }
		  }else {
			  ConnectedPlayer newp = new ConnectedPlayer();
			  newp.setIp_connection_id(ip_connection_id);
			  newp.setServerHostName(server.serverId());
			  if(event.getPlayerId() != -1) {
				  newp.setPlayerId(event.getPlayerId());
				  playersByPlayerId.put(event.getPlayerId(), newp);
			  }else {
				  newp.setUdid(((PreDatabaseRequestEvent)event).getUdid());
				  getPlayersPreDatabaseByUDID().put(newp.getUdid(), newp);
			  }
		  }
		  
	  }
	  


}
