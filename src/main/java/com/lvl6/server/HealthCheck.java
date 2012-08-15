package com.lvl6.server;

public interface HealthCheck {

	public abstract boolean check();
	public abstract void logCurrentSystemInfo();

}