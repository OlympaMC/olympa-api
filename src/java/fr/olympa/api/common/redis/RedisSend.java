package fr.olympa.api.common.redis;

import java.util.Collection;

import fr.olympa.api.common.plugin.OlympaCoreInterface;
import fr.olympa.api.common.redis.spigotsub.BungeeServerInfoReceiver;
import fr.olympa.api.common.server.ServerInfoBasic;

public class RedisSend {

	BungeeServerInfoReceiver serverInfoReceiver;

	public void registerAll(OlympaCoreInterface core) {
		serverInfoReceiver = new BungeeServerInfoReceiver(core);
	}

	public boolean sendServerInfos(Collection<ServerInfoBasic> serveursInfoBasic) {
		return serverInfoReceiver.sendServerInfos(serveursInfoBasic);
	}
}
