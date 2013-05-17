package com.lvl6.test;

import junit.framework.TestCase;

import org.apache.wicket.util.tester.WicketTester;
import org.elasticsearch.action.search.SearchResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.lvl6.elasticsearch.LoggingElasticSearchQuery;
import com.lvl6.server.DevOps;
import com.lvl6.spring.AppContext;
import com.lvl6.ui.admin.components.StatsPanelDynamic;
import com.lvl6.ui.admin.pages.AdminChatPage;
import com.lvl6.ui.admin.pages.AdminPage;
import com.lvl6.ui.admin.pages.LogViewerPage;
import com.lvl6.ui.admin.pages.MainPage;
import com.lvl6.ui.admin.pages.StatsGraphsPage;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-spring-application-context.xml")
public class AdminUITest extends TestCase {
	private WicketTester tester;
	
	private static Logger log = LoggerFactory.getLogger(AdminUITest.class);
	
	
	@Test
	public void testAdminPage() {
		tester = new WicketTester();
		tester.startPage(AdminPage.class);
		tester.startPage(MainPage.class);
		tester.startPage(StatsGraphsPage.class);
		tester.startComponentInPage(StatsPanelDynamic.class);
		//tester.startPage(HealthCheckPage.class);
		tester.startPage(AdminChatPage.class);
	}
	
	@Test
	public void testLogViewerPage() {
		tester = new WicketTester();
		tester.startPage(LogViewerPage.class);
	}

	
	@Test
	public void testLogIndexer() {
		log.error("Test error for elasticsearch");
		log.warn("Test warning for elasticsearch");
	}
	
	@Test
	public void testSearchLogs() {
		LoggingElasticSearchQuery query = AppContext.getApplicationContext().getBean(LoggingElasticSearchQuery.class);
		query.setLevel("INFO");
		query.setMessage("Spring");
		query.setLimit(10);
		SearchResponse result = query.search();
	}		

	
	//log.info(result.toString());
	//@Test
	public void testContactAdmins() {
		DevOps dev = AppContext.getApplicationContext().getBean(DevOps.class);
		dev.sendAlertToAdmins("Testing alerting system. Disregard this message");
	}
}
