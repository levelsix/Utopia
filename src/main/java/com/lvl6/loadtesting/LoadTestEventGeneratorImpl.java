package com.lvl6.loadtesting;

import java.nio.ByteBuffer;

import org.springframework.integration.Message;

import com.lvl6.proto.EventProto.StartupRequestProto;
import com.lvl6.proto.EventProto.StartupRequestProto.Builder;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;

public class LoadTestEventGeneratorImpl implements LoadTestEventGenerator {

	@Override
	public Message<byte[]> userCreate(String udid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Message<byte[]> startup(String udid) {
		Builder builder = StartupRequestProto.newBuilder();
		builder.setUdid(udid);
		builder.setVersionNum(1.0f);
		StartupRequestProto startupRequestEvent = builder.build();
		byte[]  bytes = startupRequestEvent.toByteArray();
		ByteBuffer bb = ByteBuffer.allocate(bytes.length+12);
		bb.putInt(EventProtocolRequest.C_STARTUP_EVENT_VALUE);
		bb.putInt(99);
		bb.putInt(bytes.length);
		bb.put(bytes);
		return null;
	}

}
