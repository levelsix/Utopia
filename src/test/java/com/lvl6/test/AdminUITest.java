package com.lvl6.test;

import org.apache.wicket.util.tester.WicketTester;
import org.junit.Test;

import com.lvl6.ui.admin.pages.AdminPage;

import junit.framework.TestCase;

public class AdminUITest extends TestCase {
	private WicketTester tester;
	
	
	@Test
	public void testAdminPage() {
		tester = new WicketTester();
		tester.startPage(AdminPage.class);
	}
}
