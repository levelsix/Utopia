package com.lvl6.loadtesting;

import java.nio.ByteBuffer;

import org.springframework.integration.Message;
import org.springframework.integration.message.GenericMessage;

import com.lvl6.proto.EventProto.StartupRequestProto;
import com.lvl6.proto.EventProto.StartupRequestProto.Builder;
import com.lvl6.proto.EventProto.UserCreateRequestProto;
import com.lvl6.proto.EventProto.UserQuestDetailsRequestProto;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.InfoProto.UserType;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;

public class LoadTestEventGeneratorImpl implements LoadTestEventGenerator {

	@Override
	public Message<byte[]> userCreate(String udid) {
		UserCreateRequestProto.Builder builder = UserCreateRequestProto.newBuilder();
		builder.setUdid(udid);
		return convertToMessage(builder.build().toByteArray(), EventProtocolRequest.C_USER_CREATE_EVENT_VALUE);
	}

	@Override
	public Message<byte[]> startup(String udid) {
		Builder builder = StartupRequestProto.newBuilder();
		builder.setUdid(udid);
		builder.setVersionNum(1.0f);
		StartupRequestProto startupRequestEvent = builder.build();
		return convertToMessage(startupRequestEvent.toByteArray(), EventProtocolRequest.C_STARTUP_EVENT_VALUE);
	}

	private Message<byte[]> convertToMessage(byte[] bytes, int type) {
		ByteBuffer bb = ByteBuffer.allocate(bytes.length+12);
		bb.putInt(type);
		bb.putInt(99);
		bb.putInt(bytes.length);
		bb.put(bytes);
		return new GenericMessage<byte[]>(bb.array());
	}

	@Override
	public Message<byte[]> userQuestDetails(MinimumUserProto.Builder user) {
		UserQuestDetailsRequestProto.Builder build = UserQuestDetailsRequestProto.newBuilder();
		build.setSender(user);
		return convertToMessage(build.build().toByteArray(), EventProtocolRequest.C_USER_QUEST_DETAILS_EVENT_VALUE);
	}

	@Override
	public MinimumUserProto.Builder minimumUserProto(Integer userId, UserType type) {
		MinimumUserProto.Builder ubuild = MinimumUserProto.newBuilder();
		ubuild.setUserId(userId);
		ubuild.setUserType(type);
		return ubuild;
	}

}
