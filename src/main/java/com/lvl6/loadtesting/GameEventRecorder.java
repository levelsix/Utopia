package com.lvl6.loadtesting;

import java.sql.Timestamp;
import java.util.Date;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;

public class GameEventRecorder implements InitializingBean {
	
	

	Logger log = LoggerFactory.getLogger(getClass());
	
	protected JdbcTemplate jdbc;

	protected DataSource dataSource;
	
	
	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		jdbc = new JdbcTemplate(dataSource);
	}

	protected void setupStorage() {
		log.info("Creating table for load_testing_events");
		jdbc.execute("create table if not exists load_testing_events (" +
				"id int unsigned NOT NULL AUTO_INCREMENT," +
				"user_id int unsigned not null," +
				"log_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
				"event_type int unsigned not null," +
				"event_bytes blob not null," +
				"PRIMARY KEY (id))");
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		setupStorage();
	}
	
	
	public void persistEvent(Integer userId, Integer eventType, byte[] eventBytes) {
		jdbc.update("insert into load_testing_events (user_id, log_time, event_type, event_bytes) values (?,?,?,?)",
				userId,new Timestamp(new Date().getTime()),eventType,eventBytes);
	}
	
	
}
