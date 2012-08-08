package com.lvl6.test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.Message;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.lvl6.events.ResponseEvent;
import com.lvl6.events.response.StartupResponseEvent;
import com.lvl6.loadtesting.BasicUser;
import com.lvl6.loadtesting.LoadTestEventGenerator;
import com.lvl6.loadtesting.UserQuestTask;
import com.lvl6.proto.InfoProto.UserType;
import com.lvl6.spring.AppContext;
import com.lvl6.utils.ClientAttachment;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-spring-application-context.xml")

public class FakeClientTests {
	private static Logger log = LoggerFactory.getLogger(FakeClientTests.class);
	
	@Resource
	protected LoadTestEventGenerator gen;
	
	@Resource(name="controllersExecutor") 
	protected TaskExecutor te;

	public TaskExecutor getTe() {
		return te;
	}

	public void setTe(TaskExecutor te) {
		this.te = te;
	}


	private JdbcTemplate jdbcTemplate;

	@Resource
    public void setDataSource(DataSource dataSource) {
		//log.info("Setting datasource and creating jdbcTemplate");
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
	

	@Resource(name="outboundFakeClientMessageChannel")
	protected DirectChannel sendToServer;

	@Resource(name="inboundFakeClientChannel")
	protected QueueChannel serverResponses;

	
	public LoadTestEventGenerator getGen() {
		return gen;
	}
	
	public void setGen(LoadTestEventGenerator gen) {
		this.gen = gen;
	}
	
	public DirectChannel getSendToServer() {
		return sendToServer;
	}

	public void setSendToServer(DirectChannel sendToServer) {
		this.sendToServer = sendToServer;
	}

	public QueueChannel getServerResponses() {
		return serverResponses;
	}

	public void setServerResponses(QueueChannel serverResponses) {
		this.serverResponses = serverResponses;
	}

	
	@Test
	public void testFakeClientStartup() throws InterruptedException{
		sendToServer.send(gen.startup("A_Fake_Client"));
		waitForMessage();
	}
	
	//@Test
	public void testGeneratingFakeLoad() {
		List<BasicUser> users = getTestUsers();
		for(int i = 0; i < 100; i++) {
			log.info("Generating UserQuestDetailsRequestEvents for {} users", users.size());
			for(BasicUser user: users) {
				UserQuestTask task = AppContext.getApplicationContext().getBean(UserQuestTask.class);
				task.setUserId(user.getUserId());
				task.setUserType(user.getUserType());
				try {
					te.execute(task);
				}catch(Exception e) {
					log.error("Error sending fake call", e);
				}
			}
			
		}
	}

	private List<BasicUser> getTestUsers() {
		return jdbcTemplate.query("select id, udid, type from users limit 1000", new RowMapper<BasicUser>() {
			@Override
			public BasicUser mapRow(ResultSet rs, int rowNum)
					throws SQLException {
				BasicUser bu = new BasicUser();
				bu.setUdid(rs.getString("udid"));
				bu.setUserId(rs.getInt("id"));
				bu.setUserType(UserType.valueOf(rs.getInt("type")));
				return bu;
			}
		});
	}
	
	protected void waitForMessage() {
		Message<?> msg = serverResponses.receive(1500);
		if(msg != null && msg.getHeaders() != null) {
			log.info("Received response message...size: "+ ((byte[]) msg.getPayload()).length);
			for (String key: msg.getHeaders().keySet()) {
				log.info(key+": "+msg.getHeaders().get(key));
			}
			//log.info("Payload: "+msg.getPayload());
			ClientAttachment attachment = new ClientAttachment();
			attachment.readBuff = ByteBuffer.wrap((byte[]) msg.getPayload()).order(ByteOrder.LITTLE_ENDIAN);
			while(attachment.eventReady()) {
				ResponseEvent response = new StartupResponseEvent(null);
				response.setTag(attachment.tag);
				log.info("Received startupResponseEvent");
			}
		}
	}
	
}

