package com.lvl6.server;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

public class DevOps {
	
	private static Logger log = LoggerFactory.getLogger(DevOps.class);
	
	
	protected List<String> adminEmails;
	protected Date lastAlertSentToAdmins;
	
	@Autowired
	protected SimpleMailMessage adminAlertEmail;
	
	@Autowired
	protected JavaMailSender mailSender;
	
	
	
	public JavaMailSender getMailSender() {
		return mailSender;
	}

	public void setMailSender(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	public SimpleMailMessage getAdminAlertEmail() {
		return adminAlertEmail;
	}

	public void setAdminAlertEmail(SimpleMailMessage adminAlertEmail) {
		this.adminAlertEmail = adminAlertEmail;
	}

	public Date getLastAlertSentToAdmins() {
		return lastAlertSentToAdmins;
	}

	public void setLastAlertSentToAdmins(Date lastAlertSentToAdmins) {
		this.lastAlertSentToAdmins = lastAlertSentToAdmins;
	}

	public List<String> getAdminEmails() {
		return adminEmails;
	}

	public void setAdminEmails(List<String> adminEmails) {
		this.adminEmails = adminEmails;
	}
	
	
	public void sendAlertToAdmins(String alert) {
		log.warn("Sending alert to admins: {}", alert);
		try {
			SimpleMailMessage message = getAdminAlertEmail();
			message.setTo(getAdminEmails().toArray(new String[2]));
			message.setText(alert);
			mailSender.send(message);
			setLastAlertSentToAdmins(new Date());
		}catch(Exception e) {
			log.error("Error contacting admins", e);
		}
	}
	
	
	
}
