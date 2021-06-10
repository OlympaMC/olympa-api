package fr.olympa.api.common.redis;

import fr.olympa.api.common.plugin.OlympaCoreInterface;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public class RedisSubChannel extends JedisPubSub {

	protected RedisChannel channel;
	protected boolean canSendFromBungee;
	protected boolean canSendFromSpigot;
	protected boolean canReceiveOnSpigot;
	protected boolean canReceiveOnBungee;
	private RedisConnection redisAccess;

	public RedisSubChannel(OlympaCoreInterface core, RedisChannel channel) {
		this.channel = channel;
		redisAccess = core.getRedisAccess();
	}

	public RedisConnection getRedisAccess() {
		return redisAccess;
	}

	public void register(Jedis jedis) {
		if (!jedis.isConnected())
			// TODO Register a callback to call it when jedis is connected
			return;
		Thread t = new Thread(() -> {
			jedis.subscribe(this, channel.name());
			jedis.disconnect();
		}, "Redis sub " + channel.name());
		Thread.UncaughtExceptionHandler h = (th, ex) -> {
			ex.printStackTrace();
			if (jedis != null)
				register(redisAccess.connect());
		};
		t.setUncaughtExceptionHandler(h);
		t.start();
	}
}
