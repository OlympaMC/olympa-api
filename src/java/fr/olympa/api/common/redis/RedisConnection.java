package fr.olympa.api.common.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public interface RedisConnection {

	Jedis connect();

	void disconnect();

	Jedis getConnection();

	JedisPool getJedisPool();

	void initJedis();

	boolean isConnected();

	boolean isPoolOpen();

	void updateClientName(String clientName);

}