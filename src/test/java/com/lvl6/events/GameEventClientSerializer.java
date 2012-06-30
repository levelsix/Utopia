package com.lvl6.events;

import java.nio.ByteOrder;

import org.apache.log4j.Logger;

import com.lvl6.events.GameEventSerializer;


public class GameEventClientSerializer extends GameEventSerializer {

	
	private Logger log = Logger.getLogger(GameEventClientSerializer.class);
	
	@Override
	protected ByteOrder getByteOrder() {
		return ByteOrder.LITTLE_ENDIAN;
	}

}
