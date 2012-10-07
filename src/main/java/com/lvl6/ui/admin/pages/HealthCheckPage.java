package com.lvl6.ui.admin.pages;

import org.apache.http.HttpStatus;
import org.apache.wicket.request.http.flow.AbortWithHttpErrorCodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lvl6.server.HealthCheck;
import com.lvl6.spring.AppContext;

public class HealthCheckPage extends TemplatePage {

	private static final long serialVersionUID = -1728365297134290240L;
	private static Logger log = LoggerFactory.getLogger(HealthCheckPage.class);

	public HealthCheckPage() {
		super();
		log.info("Health check");
		HealthCheck hc = AppContext.getApplicationContext().getBean(HealthCheck.class);
		if(!hc.check()) {
			log.error("Health check failed");
			hc.logCurrentSystemInfo();
			throw new AbortWithHttpErrorCodeException(HttpStatus.SC_SERVICE_UNAVAILABLE);
		}
	}
	
}
