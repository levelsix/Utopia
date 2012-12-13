package com.lvl6.events;

import java.nio.ByteOrder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GameEventClientSerializer extends GameEventSerializer {

	
	private static final Logger log = LoggerFactory.getLogger(GameEventClientSerializer.class);
	
	@Override
	protected ByteOrder getByteOrder() {
		return ByteOrder.LITTLE_ENDIAN;
	}

}
