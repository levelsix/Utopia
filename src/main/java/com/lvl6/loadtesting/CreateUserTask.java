package com.lvl6.loadtesting;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;

public class CreateUserTask implements Runnable{
	
	
	@Autowired
	TaskExecutor te;

	@Override
	public void run() {
		

	}

}
