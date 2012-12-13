package com.lvl6.elasticsearch;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

public class ElasticSearchLogSetup implements InitializingBean {

	
	
	public ElasticSearchLogSetup() {
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		System.out.println("ElasticSearchLogSetup starting");
		try {
			JoranConfigurator configurator = new JoranConfigurator();
			configurator.setContext(context);
			// Call context.reset() to clear any previous configuration, e.g. default 
			// configuration. For multi-step configuration, omit calling context.reset().
			context.reset(); 
			configurator.doConfigure("logback-elasticsearch.xml");
		} catch (JoranException je) {
			// StatusPrinter will handle this
		}
		StatusPrinter.printInCaseOfErrorsOrWarnings(context);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		System.out.println("ElasticSearchLogSetup done");
	}

}
