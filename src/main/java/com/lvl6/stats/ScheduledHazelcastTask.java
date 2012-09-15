package com.lvl6.stats;

import java.io.Serializable;

public class ScheduledHazelcastTask implements Serializable {
	private static final long serialVersionUID = 1L;
	public ScheduledHazelcastTask(String key, Long time, boolean complete) {
		super();
		this.key = key;
		this.time = time;
		this.complete = complete;
	}
	String key = "";
	Long time = System.currentTimeMillis();
	boolean complete;
}
