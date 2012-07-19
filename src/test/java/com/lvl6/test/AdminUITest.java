package com.lvl6.test;

import org.apache.wicket.util.tester.WicketTester;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.lvl6.ui.admin.pages.AdminPage;
import com.lvl6.ui.admin.pages.MainPage;

import junit.framework.TestCase;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-spring-application-context.xml")
public class AdminUITest extends TestCase {
	private WicketTester tester;
	
	
	@Test
	public void testAdminPage() {
		tester = new WicketTester();
		tester.startPage(AdminPage.class);
		tester.startPage(MainPage.class);
	}
}
