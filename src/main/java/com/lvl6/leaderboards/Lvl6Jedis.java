package com.lvl6.leaderboards;

import redis.clients.jedis.Jedis;

public class Lvl6Jedis extends Jedis {

	public Lvl6Jedis(String host, String auth) {
		super(host);
		this.auth(auth);
	}

}
