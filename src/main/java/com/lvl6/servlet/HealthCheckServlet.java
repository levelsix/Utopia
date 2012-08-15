package com.lvl6.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lvl6.server.HealthCheck;
import com.lvl6.spring.AppContext;

public class HealthCheckServlet extends HttpServlet {

	private static Logger log = LoggerFactory.getLogger(HealthCheckServlet.class);
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		//super.doGet(req, resp);
		log.info("Health check");
		HealthCheck hc = AppContext.getApplicationContext().getBean(HealthCheck.class);
		if(!hc.check()) {
			log.error("Health check failed");
			resp.sendError(500);
		}
	}

	private static final long serialVersionUID = 1L;

}
