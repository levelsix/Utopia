package com.lvl6.scriptsjava.generatefakeusers;

import java.io.IOException;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

public class NameGeneratorElven extends NameGenerator implements ResourceLoaderAware, InitializingBean {

	protected ResourceLoader resourceLoader;
	protected Resource namerulesElven;
	
	public NameGeneratorElven() throws IOException {

	}

	@Override
	public void setResourceLoader(ResourceLoader rl) {
		this.resourceLoader= rl;  
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		namerulesElven = resourceLoader.getResource("classpath:/namerulesElven.txt");
		changeFile(namerulesElven);
	}
}