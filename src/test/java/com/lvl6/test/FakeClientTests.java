package com.lvl6.test;

import java.util.Random;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.lvl6.info.Location;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.StartupRequestProto;
import com.lvl6.proto.EventProto.StartupRequestProto.Builder;
import com.lvl6.proto.InfoProto.UserType;
import com.lvl6.scriptsjava.generatefakeusers.GenerateFakeUsersWithoutInput;
import com.lvl6.utils.utilmethods.MiscMethods;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-spring-application-context.xml")

public class FakeClientTests {
	private static Logger log = Logger.getLogger(FakeClientTests.class);
	@Test
	public void testFakeUser(){
		Builder builder = StartupRequestProto.newBuilder();
		builder.setUdid("Fake_user_for_tests");
	}
}

