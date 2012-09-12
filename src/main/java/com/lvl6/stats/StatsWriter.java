package com.lvl6.stats;

public interface StatsWriter {
	void dailyStats();
	void sixHourStats();
	void hourlyStats();
	void weeklyStats();
}
