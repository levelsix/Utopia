package com.lvl6.test;

import org.apache.wicket.util.tester.WicketTester;
import org.elasticsearch.action.search.SearchResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.lvl6.cassandra.log4j.Log4jElasticSearchQuery;
import com.lvl6.server.DevOps;
import com.lvl6.spring.AppContext;
import com.lvl6.stats.StatsWriter;
import com.lvl6.ui.admin.components.StatsPanel;
import com.lvl6.ui.admin.pages.AdminPage;
import com.lvl6.ui.admin.pages.HealthCheckPage;
import com.lvl6.ui.admin.pages.LogViewerPage;
import com.lvl6.ui.admin.pages.MainPage;
import com.lvl6.ui.admin.pages.StatsGraphsPage;

import junit.framework.TestCase;


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
