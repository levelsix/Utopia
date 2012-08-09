package com.lvl6.tracking;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lvl6.utils.utilmethods.InsertUtils;

public class AppODayUserTrackerServlet extends HttpServlet {

	
	protected static Logger log = LoggerFactory.getLogger(AppODayUserTrackerServlet.class);
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		Map<String, String> params = req.getParameterMap();
		Set<String> keys = params.keySet();
		for(String key: keys) {
			if(key.equalsIgnoreCase("openudid") || key.equalsIgnoreCase("mac")) {
				String udid = req.getParameter(key);
				if(udid != null && !udid.equals("")) {
					log.info("Iddiction tracking: {} - {}", key, udid);
					trackMacAddress(udid);
					return;
				}
			}
		}
		log.warn("AppODayTracker page loaded but no mac address specified");
	}
	
	
	protected void trackMacAddress(String macAddress) {
		//log.info("Iddiction tracking: " + macAddress);
		InsertUtils.get().insertIddictionIndentifier(macAddress, new Date());
	}
	
	private static final long serialVersionUID = -3045402726876117659L;
	
	

}
