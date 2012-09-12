package com.lvl6.cassandra;

import java.util.List;

public interface RollupUtil {
	
	void addRollupEntry(RollupEntry entry);
	void addRollupEntries(List<RollupEntry> entries);
	
}
