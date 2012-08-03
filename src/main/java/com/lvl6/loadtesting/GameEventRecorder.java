package com.lvl6.loadtesting;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class GameEventRecorder implements InitializingBean {

	Logger log = LoggerFactory.getLogger(getClass());

	protected JdbcTemplate jdbc;

	@Resource
	public void setDataSource(DataSource dataSource) {
		log.info("Setting datasource and creating jdbcTemplate");
		this.jdbc = new JdbcTemplate(dataSource);
	}

	protected void setupStorage() {
		log.info("Creating table for load_testing_events");
		jdbc.execute("create table if not exists load_testing_events ("
				+ "id int unsigned NOT NULL AUTO_INCREMENT,"
				+ "user_id int unsigned not null,"
				+ "log_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
				+ "event_type int unsigned not null,"
				+ "event_bytes blob not null," + "PRIMARY KEY (id))");
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		setupStorage();
	}

	public void persistEvent(Integer userId, Integer eventType,	byte[] eventBytes) {
		if (userId != null && userId > 0 && eventBytes != null) {
			try {
				log.info("Persisting event for user: {}", userId);
				jdbc.update(
						"insert into load_testing_events (user_id, log_time, event_type, event_bytes) values (?,?,?,?)",
						userId, 
						new Timestamp(new Date().getTime()), 
						eventType,
						eventBytes);
			} catch (Exception e) {
				log.error("Error persisting event:", e);
			}
		}
	}
	
	
	
	public List<LoadTestEvent> getEventsForUser(Integer userId) {
		return jdbc.query("select * from load_testing_events where userId = "+userId+" order by log_time", new RowMapper<LoadTestEvent>(){

			@Override
			public LoadTestEvent mapRow(ResultSet rs, int rowNum)
					throws SQLException {
				LoadTestEvent lte = new LoadTestEvent();
				lte.setEvent(rs.getBytes("event_bytes"));
				lte.setEventTime(rs.getTimestamp("log_time"));
				lte.setEventType(rs.getInt("event_type"));
				lte.setUserId(rs.getInt("user_id"));
				return lte;
			}
			
		});
	}
	
	
	
	
}
