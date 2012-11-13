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


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-spring-application-context.xml")
public class StatsTest extends TestCase {
	
	private static Logger log = LoggerFactory.getLogger(StatsTest.class);
	
	
	@Autowired
	StatsWriter writer;
	
	
	@Test
	public void testStatsWriter() {
		writer.hourlyStats();
	}

}
