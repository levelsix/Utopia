package com.lvl6.test;

import org.apache.wicket.util.tester.WicketTester;
import org.elasticsearch.action.search.SearchResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.lvl6.cassandra.log4j.Log4jElasticSearchQuery;
import com.lvl6.spring.AppContext;
import com.lvl6.ui.admin.components.StatsPanel;
import com.lvl6.ui.admin.pages.AdminPage;
import com.lvl6.ui.admin.pages.HealthCheckPage;
import com.lvl6.ui.admin.pages.LogViewerPage;
import com.lvl6.ui.admin.pages.MainPage;

import junit.framework.TestCase;


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
		tester.startComponentInPage(StatsPanel.class);
		tester.startPage(HealthCheckPage.class);
	}
	
	@Test
	public void testLogViewerPage() {
		tester = new WicketTester();
		tester.startPage(LogViewerPage.class);
	}
	
	
	@Test
	public void testSearchLogs() {
		Log4jElasticSearchQuery query = AppContext.getApplicationContext().getBean(Log4jElasticSearchQuery.class);
		query.setLevel("INFO");
		query.setMessage("Spring");
		SearchResponse result = query.search();
		log.info(result.toString());
	}
}
