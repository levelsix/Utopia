package com.lvl6.test;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.lvl6.properties.APNSProperties;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-spring-application-context.xml")
public class ApnsTests {
	
	
	Logger log = LoggerFactory.getLogger(getClass());
	
	@Autowired
	protected APNSProperties props;
	
	public APNSProperties getProps() {
		return props;
	}


	public void setProps(APNSProperties props) {
		this.props = props;
	}


	@Test
	public void testApnsCertLocation() throws IOException{
		InputStream stream = ClassLoader.getSystemResourceAsStream(props.pathToCert);
		if(stream.available() > 0) {
			log.info("Apns Cert found");
		}
	}
	
}
