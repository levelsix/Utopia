package com.lvl6.loadtesting;

import org.springframework.integration.Message;


public interface LoadTestEventGenerator {
	public abstract Message<byte[]> userCreate(String udid);
	public abstract Message<byte[]> startup(String udid);
	
}
