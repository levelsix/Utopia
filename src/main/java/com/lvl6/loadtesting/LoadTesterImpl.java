package com.lvl6.loadtesting;

import org.springframework.core.task.TaskExecutor;

public class LoadTesterImpl implements LoadTester {
	protected Integer numberOfFakePlayersToCreate = 10;
	protected Integer numberOfRuns = -1;
	protected TaskExecutor taskExecutor;
	
	
	public Integer getNumberOfFakePlayersToCreate() {
		return numberOfFakePlayersToCreate;
	}
	public void setNumberOfFakePlayersToCreate(Integer numberOfFakePlayersToCreate) {
		this.numberOfFakePlayersToCreate = numberOfFakePlayersToCreate;
	}
	public Integer getNumberOfRuns() {
		return numberOfRuns;
	}
	public void setNumberOfRuns(Integer numberOfRuns) {
		this.numberOfRuns = numberOfRuns;
	}
	
}
