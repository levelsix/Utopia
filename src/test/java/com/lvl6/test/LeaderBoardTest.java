package com.lvl6.test;

import java.util.Set;

import javax.annotation.Resource;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import redis.clients.jedis.Tuple;

import com.lvl6.leaderboards.LeaderBoardUtil;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-spring-application-context.xml")
public class LeaderBoardTest extends TestCase {

	Logger log = LoggerFactory.getLogger(getClass());

	@Resource
	protected LeaderBoardUtil lb;
	
	public LeaderBoardUtil getLb() {
		return lb;
	}

	public void setLb(LeaderBoardUtil lb) {
		this.lb = lb;
	}

	
	
	
	@Test
	public void testLeadboardSet() {
		int rank = 0;
		Set<Tuple> ex = lb.getExperienceTopN(rank, rank+100);
		String leaderBoard = "experience";
		printRanks(rank, ex, leaderBoard);
		rank = 0;
		ex = lb.getBattlesWonTopN(rank, rank+100);
		leaderBoard = "battles won";
		printRanks(rank, ex, leaderBoard);
		rank = 0;
		ex = lb.getTotalCoinValueForTopN(rank, rank+100);
		leaderBoard = "total coins";
		printRanks(rank, ex, leaderBoard);
		rank = 0;
		ex = lb.getBattlesWonOverTotalBattlesRatioTopN(rank, rank+100);
		leaderBoard = "best kdr";
		printRanks(rank, ex, leaderBoard);
	}
	
	@Test
	public void testLeaderboardEvents() {
		Set<Tuple> top5 = lb.getEventTopN(1, 0, 5);
		int index = 0;
		boolean ordered = true;
		for(Tuple t : top5) {
			long rank = lb.getRankForEventAndUser(1, Integer.valueOf(t.getElement()));
			Object[] args = {index, rank, t.getScore(), t.getElement()};
			log.info("Index: {} Rank: {} Score: {} User: {}", args);
			if(rank != index) ordered = false;
			index++;
		}
		Assert.assertTrue(ordered);
	}

	private void printRanks(int rank, Set<Tuple> ex, String leaderBoard) {
		/*log.info("Ranks for "+leaderBoard);
		for(Tuple t : ex) {
			log.info("Rank: "+rank+" User: "+t.getElement()+" Score: "+t.getScore());
			rank++;
		}*/
	}
}
