package com.lvl6.utils;

import java.nio.ByteOrder;

public class ClientAttachment extends Attachment {
	
	@Override
	protected ByteOrder getByteOrder() {
		return ByteOrder.LITTLE_ENDIAN;
	}
	
}
