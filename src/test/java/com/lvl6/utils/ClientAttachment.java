package com.lvl6.utils;

import java.nio.ByteOrder;

import org.apache.log4j.Logger;

public class ClientAttachment extends Attachment {

	private Logger log = Logger.getLogger(ClientAttachment.class);
	
	@Override
	protected ByteOrder getByteOrder() {
		return ByteOrder.LITTLE_ENDIAN;
	}
	
}
