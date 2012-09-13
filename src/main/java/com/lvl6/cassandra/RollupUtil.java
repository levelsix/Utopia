package com.lvl6.cassandra;

import java.util.List;

public interface RollupUtil {
	
	void addRollupEntry(RollupEntry entry);
	void addRollupEntries(List<RollupEntry> entries);
	List<RollupEntry> findEntries(String key, Long start, Long end);
}
