package com.lvl6.test;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.lvl6.stats.StatsWriter;
import com.lvl6.ui.admin.components.ApplicationStats;
import com.lvl6.utils.ApplicationUtils;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-spring-application-context.xml")
public class StatsTest extends TestCase {
	
	private static Logger log = LoggerFactory.getLogger(StatsTest.class);
	
	
	@Autowired
	StatsWriter writer;
	
	public StatsWriter getWriter() {
		return writer;
	}


	public void setWriter(StatsWriter writer) {
		this.writer = writer;
	}


	public ApplicationUtils getAppUtils() {
		return appUtils;
	}


	public void setAppUtils(ApplicationUtils appUtils) {
		this.appUtils = appUtils;
	}


	@Autowired
	ApplicationUtils appUtils;
	
	
	@Test
	public void testStatsWriter() {
		writer.hourlyStats();
	}

	
	@Test
	public void testStatisticsRetrieveUtils() {
		ApplicationStats stats = appUtils.getStats();
	}
	
	
}
