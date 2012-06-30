package com.lvl6.test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.message.GenericMessage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.hazelcast.core.HazelcastInstance;
import com.lvl6.events.ResponseEvent;
import com.lvl6.events.response.StartupResponseEvent;
import com.lvl6.proto.EventProto.StartupRequestProto;
import com.lvl6.proto.EventProto.StartupRequestProto.Builder;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.server.GameServer;
import com.lvl6.utils.ClientAttachment;
import com.lvl6.utils.PlayerSet;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-spring-application-context.xml")
public class PlayersInActionTests {
	private static Logger log = Logger.getLogger(PlayersInActionTests.class);
	
	
	@Autowired
	protected HazelcastInstance hazel;
	public HazelcastInstance getHazel() {
		return hazel;
	}

	public void setHazel(HazelcastInstance hazel) {
		this.hazel = hazel;
	}

	public PlayerSet getPlayers() {
		return players;
	}

	public void setPlayers(PlayerSet players) {
		this.players = players;
	}

	@Autowired
	protected PlayerSet players;
	
	@Autowired 
	protected GameServer server;
	
	@Test
	public void testLocking() {
		log.info("Locking player 1");
//		server.lockPlayer(1);
//		server.lockPlayer(2);
//		server.lockPlayer(1);
	}
	
}

