package com.lvl6.stats;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;
import com.lvl6.cassandra.RollupEntry;
import com.lvl6.cassandra.RollupUtil;
import com.lvl6.retrieveutils.StatisticsRetrieveUtil;
import com.lvl6.ui.admin.components.ApplicationStats;
import com.lvl6.utils.ApplicationUtils;

public class StatsWriterImpl implements StatsWriter {
	
	private static Logger log = LoggerFactory.getLogger(StatsWriterImpl.class);
	
	@Autowired
	protected StatisticsRetrieveUtil statsUtil;
	
	@Autowired
	protected ApplicationUtils appUtils;

	@Autowired
	protected RollupUtil rollupUtil;
	
	@Autowired
	protected HazelcastInstance hazel;
	
	public HazelcastInstance getHazel() {
		return hazel;
	}

	public void setHazel(HazelcastInstance hazel) {
		this.hazel = hazel;
	}

	public RollupUtil getRollupUtil() {
		return rollupUtil;
	}

	public void setRollupUtil(RollupUtil rollupUtil) {
		this.rollupUtil = rollupUtil;
	}

	public StatisticsRetrieveUtil getStatsUtil() {
		return statsUtil;
	}

	public void setStatsUtil(StatisticsRetrieveUtil statsUtil) {
		this.statsUtil = statsUtil;
	}
	
	public ApplicationUtils getAppUtils() {
		return appUtils;
	}

	public void setAppUtils(ApplicationUtils appUtils) {
		this.appUtils = appUtils;
	}

	
	@Override
	@Scheduled(cron="0 0 0 * * ?")
	public void dailyStats() {
		saveStats("day");
	}

	//distributed scheduler hack -- should probably fix at some point
	protected void saveStats(String period) {
		ILock lock = hazel.getLock("stats:"+period);
		if(lock.tryLock()) {
			stats(period, new Date().getTime());
			try {
				lock.wait(30000);
				lock.unlock();
				lock.destroy();
			} catch (InterruptedException e) {
				log.error("Thread interrupted: ", e);
			}
		}
	}

	@Override
	@Scheduled(cron="0 0 1,6,12,18 * * ?")
	public void sixHourStats() {
		saveStats("six_hour");
	}

	@Override
	@Scheduled(cron="0 0 * * * ?")
	public void hourlyStats() {
		saveStats("hour");
	}

	@Override
	@Scheduled(cron="0 0 * 1,7,14,21 * ?")
	public void weeklyStats() {
		saveStats("week");
	}
	
	@SuppressWarnings("unchecked")
	protected void stats(String period, Long time) {
		log.info("Setting stats for period: {} and time: {}", period, time);
		List<RollupEntry> entries = new ArrayList<RollupEntry>();
		ApplicationStats stats = getAppUtils().getStats();
		@SuppressWarnings("unchecked")
		Map<String, String> props;
		try {
			props = BeanUtils.describe(stats);
			for(String stat : props.keySet()) {
				String statt = props.get(stat);
				try {
					RollupEntry rollupEntry = new RollupEntry(stat+":"+period, time, Long.valueOf(statt));
					log.info("Saving stat: \n{}", rollupEntry);
					entries.add(rollupEntry);
				} catch (IllegalArgumentException e) {
					log.error("Error setting RollupEntry", e);
				}
			}
			rollupUtil.addRollupEntries(entries);
		} catch (Exception e1) {
			log.error("", e1);
		} 
	}
	

}
