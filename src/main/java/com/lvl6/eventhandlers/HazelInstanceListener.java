package com.lvl6.eventhandlers;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Instance;
import com.hazelcast.core.InstanceEvent;
import com.hazelcast.core.InstanceListener;

public class HazelInstanceListener implements InstanceListener, InitializingBean {

	Logger log = LoggerFactory.getLogger(getClass());
	
	@Autowired
	protected HazelcastInstance hazel;

	@Autowired
	protected Map<String, Instance> instances;
	
	
	
	public HazelcastInstance getHazel() {
		return hazel;
	}

	public void setHazel(HazelcastInstance hazel) {
		this.hazel = hazel;
	}

	public Map<String, Instance> getInstances() {
		return instances;
	}

	public void setInstances(Map<String, Instance> instances) {
		this.instances = instances;
	}

	
	
	@Override
	public void instanceCreated(InstanceEvent instanceEvent) {
		log.info("Created hazel instance: {} type: {}", instanceEvent.getInstance().getId(), instanceEvent.getEventType().name() );
		if(instanceEvent != null) {
			getInstances().put(instanceEvent.getInstance().getId().toString(), instanceEvent.getInstance());
		}
	}

	@Override
	public void instanceDestroyed(InstanceEvent instanceEvent) {
		log.info("Destroyed hazel instance: {} type: {}", instanceEvent.getInstance().getId(), instanceEvent.getEventType().name() );
		if(instanceEvent != null && getInstances().containsKey(instanceEvent.getInstance().getId().toString())) {
			getInstances().remove(instanceEvent.getInstance().getId().toString());
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		getHazel().addInstanceListener(this);
	}

}
