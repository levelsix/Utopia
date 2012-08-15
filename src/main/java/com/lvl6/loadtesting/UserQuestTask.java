package com.lvl6.loadtesting;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.MessageChannel;

import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.InfoProto.UserType;

public class UserQuestTask implements Runnable{
	
	@Resource(name="outboundFakeClientMessageChannel")
	MessageChannel outboundEvents;
	
	
	public MessageChannel getOutboundEvents() {
		return outboundEvents;
	}



	public void setOutboundEvents(MessageChannel outboundEvents) {
		this.outboundEvents = outboundEvents;
	}

	
	protected Integer iterations = 1;
	
	
	

	public Integer getIterations() {
		return iterations;
	}



	public void setIterations(Integer iterations) {
		this.iterations = iterations;
	}


	@Autowired
	LoadTestEventGenerator lteg;
	
	
	protected Integer userId;
	protected UserType userType;
	


	public UserType getUserType() {
		return userType;
	}



	public void setUserType(UserType userType) {
		this.userType = userType;
	}



	public Integer getUserId() {
		return userId;
	}



	public void setUserId(Integer userId) {
		this.userId = userId;
	}



	@Override
	public void run() {
		MinimumUserProto.Builder user = lteg.minimumUserProto(userId, userType);
		for(Integer i = 0; i< iterations; i++) {
			outboundEvents.send(lteg.userQuestDetails(user));
		}

	}

}
