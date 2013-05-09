package com.lvl6.test;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.lvl6.retrieveutils.ClanRetrieveUtils;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-spring-application-context.xml")
public class ClanRetrieveTest extends TestCase {
	
	private static Logger log = LoggerFactory.getLogger(StatsTest.class);
	
	
	@Autowired
	ClanRetrieveUtils clanRU;

	
	public ClanRetrieveUtils getClanRU() {
		return clanRU;
	}


	public void setClanRU(ClanRetrieveUtils clanRU) {
		this.clanRU = clanRU;
	}


	@Test
	public void testMasterSlaveDBReads() {
		List<Integer> clans = Arrays.asList(944,945,946,947,948,949,951);
		for(Integer clanId:clans) {
			clanRU.getClanWithId(clanId);
		}
	}
	
	
}
