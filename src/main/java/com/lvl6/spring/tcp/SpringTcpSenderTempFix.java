package com.lvl6.spring.tcp;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.integration.Message;

public class SpringTcpSenderTempFix {
	
	public void overrideTcpMessageSendHandler(ProceedingJoinPoint call, Message<?> message) {
		
	}
}
