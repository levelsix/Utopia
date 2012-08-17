package com.lvl6.loadtesting;

import org.springframework.integration.Message;

import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.InfoProto.UserType;


public interface LoadTestEventGenerator {
	public abstract Message<byte[]> userCreate(String udid);
	public abstract Message<byte[]> startup(String udid);
	public abstract Message<byte[]> userQuestDetails(MinimumUserProto.Builder user);
	public MinimumUserProto.Builder minimumUserProto( Integer userId, UserType type);
}
