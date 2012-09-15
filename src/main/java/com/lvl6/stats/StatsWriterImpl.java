package com.lvl6.stats;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;
import com.hazelcast.core.IMap;
import com.lvl6.cassandra.RollupEntry;
import com.lvl6.cassandra.RollupUtil;
import com.lvl6.retrieveutils.StatisticsRetrieveUtil;
import com.lvl6.stats.StatsWriterImpl.ScheduledTask;
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
	
	@Resource(name="")
	protected IMap<String, ScheduledTask> scheduledTasks;
	
	public IMap<String, ScheduledTask> getScheduledTasks() {
		return scheduledTasks;
	}

	public void setScheduledTasks(IMap<String, ScheduledTask> scheduledTasks) {
		this.scheduledTasks = scheduledTasks;
	}

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
		log.info("Saving stats for {}", period);
		String key = "stats:"+period;
		ILock lock = hazel.getLock(key);
		if(lock.tryLock()) {
			ScheduledTask task = (ScheduledTask) scheduledTasks.get(key);
			if(task == null) {
				long time = System.currentTimeMillis();
				stats(period, time);
				scheduledTasks.put(key, new ScheduledTask(key, time, true), 45, TimeUnit.SECONDS);
			}else {
				log.info("Not saving stats for {}... already save on another node", period);
			}
			lock.unlock();
			lock.destroy();
		}else {
			log.info("Another node is already saving stats for {}", period);
		}
	}
	
	class ScheduledTask{
		public ScheduledTask(String key, Long time, boolean complete) {
			super();
			this.key = key;
			this.time = time;
			this.complete = complete;
		}
		String key = "";
		Long time = System.currentTimeMillis();
		boolean complete;
		
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
