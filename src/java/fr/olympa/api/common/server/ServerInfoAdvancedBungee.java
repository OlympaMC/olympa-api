package fr.olympa.api.common.server;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.common.match.RegexMatcher;
import fr.olympa.api.common.module.OlympaModule;
import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.core.bungee.OlympaBungee;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.ServerPing.Players;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class ServerInfoAdvancedBungee extends ServerInfoAdvanced {

	private static Map<String, Boolean> errorAlreadySend = new HashMap<>();
	private static Map<String, Integer> readTimeException = new HashMap<>();

	public static Map<String, Integer> getReadTimeException() {
		return readTimeException;
	}

	public static void removeReadTimeException(ServerInfo serverInfo) {
		ServerInfoAdvancedBungee.readTimeException.remove(serverInfo.getName());
	}

	public static boolean addReadTimeException(ServerInfo serverInfo) {
		if (OlympaModule.DEBUG)
			OlympaBungee.getInstance().sendMessage("§8[Debug] §7Le serveur §e%s§7 a été ping et une §cReadTimeoutException§7 est apparue.", serverInfo.getName());
		Integer oldValue = ServerInfoAdvancedBungee.readTimeException.get(serverInfo.getName());
		if (oldValue != null)
			if (oldValue <= 2) {
				ServerInfoAdvancedBungee.readTimeException.put(serverInfo.getName(), ServerInfoAdvancedBungee.readTimeException.get(serverInfo.getName()) + 1);
				return true;
			} else
				return false;
		ServerInfoAdvancedBungee.readTimeException.put(serverInfo.getName(), 1);
		return true;
	}

	public static ServerInfoAdvancedBungee getFromPingServer(ServerInfo serverInfo, long time, ServerPing serverPing, Throwable error) {
		int ping = Math.round((System.nanoTime() - time) / 1000000f);
		@Nullable
		ServerInfoAdvancedBungee serverDebugInfo = null;
		@Nullable
		String allMotd = null;
		@Nullable
		String errorMsg = null;
		@Nullable
		ServerStatus status = null;
		String serverName = serverInfo.getName();
		Entry<OlympaServer, Integer> serverInfos = OlympaServer.getOlympaServerWithId(serverName);
		OlympaServer olympaServer = serverInfos.getKey();
		int serverId = serverInfos.getValue();
		try {
			if (error == null) {
				allMotd = serverPing.getDescriptionComponent().toPlainText();
				String[] motd = allMotd.split(" ");
				if (allMotd.startsWith("V2 "))
					// V2
					serverDebugInfo = ServerInfoAdvancedBungee.fromJson(allMotd.substring(3), serverInfo, ping);
				else if (motd.length < 8) {
					// V0.1
					Players players = serverPing.getPlayers();
					int onlinePlayers = players.getOnline();
					int maxPlayers = players.getMax();
					double[] tpsArray = null;
					if (allMotd.startsWith("§"))
						allMotd = allMotd.substring(2);
					if (motd.length >= 1)
						status = ServerStatus.get(motd[0]);
					if (motd.length >= 4 && RegexMatcher.DOUBLE.is(motd[1])) {
						tpsArray = new double[3];
						for (int i = 1; i < 4; i++) {
							tpsArray[i - 1] = RegexMatcher.DOUBLE.parse(motd[i]).doubleValue();
						}
					}
					serverDebugInfo = new ServerInfoAdvancedBungee(serverInfo, olympaServer, serverId, status, onlinePlayers, maxPlayers, ping, tpsArray);
				} else {
					// V1
					String json = String.join(" ", Arrays.copyOfRange(motd, 7, motd.length));
					serverDebugInfo = ServerInfoAdvancedBungee.fromJson(json, serverInfo, ping);
					if (serverDebugInfo == null)
						serverDebugInfo = new ServerInfoAdvancedBungee();
					serverDebugInfo.setPing(ping);
					serverDebugInfo.serverId = serverId;
					serverDebugInfo.cacheServerInfo = serverInfo;
				}

				if (OlympaModule.DEBUG)
					OlympaBungee.getInstance().sendMessage("&7Réponse du serveur &2%s&7 : &d%s", serverInfo.getName(), allMotd);
			} else {
				errorMsg = error.getMessage() == null ? error.getClass().getName() : error.getMessage().replaceFirst("finishConnect\\(\\.\\.\\) failed: Connection refused: .+:\\d+", "");
				if (errorMsg.isEmpty())
					status = ServerStatus.CLOSE;
				else {
					status = ServerStatus.UNKNOWN;
					if (OlympaModule.DEBUG) {
						OlympaBungee.getInstance().sendMessage("&cLe serveur &4%s&c renvoie une erreur lors du ping", serverInfo.getName());
						error.printStackTrace();
					}
				}
			}
		} catch (Exception | Error e) {
			OlympaBungee.getInstance().sendMessage("&4Une erreur est survenu côté bungee, réponse du serveur &c%s&7 : &d%s", serverInfo.getName(), allMotd);
			Boolean alreadySend = errorAlreadySend.get(serverName);
			if (alreadySend == null) {
				alreadySend = errorAlreadySend.put(serverName, true);
				e.printStackTrace();
			} else
				OlympaBungee.getInstance().sendMessage("&c%s", e.getMessage());
		}
		if (serverDebugInfo == null)
			serverDebugInfo = new ServerInfoAdvancedBungee(serverInfo, serverInfos.getKey(), serverInfos.getValue(), status, errorMsg, ping);
		return serverDebugInfo;
	}

	ServerInfo cacheServerInfo;

	public ServerInfoAdvancedBungee() {
		super();
	}

	public ServerInfoAdvancedBungee(ServerInfo serverInfo, OlympaServer olympaServer, int serverId, ServerStatus status, int onlinePlayers, int maxPlayers, int ping, double[] tpsArray) {
		super(serverInfo.getName(), olympaServer, serverId, status, onlinePlayers, maxPlayers, ping, tpsArray);
		cacheServerInfo = serverInfo;
	}

	public ServerInfoAdvancedBungee(ServerInfo serverInfo, OlympaServer olympaServer, int serverId, String errorMsg, int ping) {
		super(serverInfo.getName(), olympaServer, serverId, errorMsg, ping);
		cacheServerInfo = serverInfo;
	}

	public ServerInfoAdvancedBungee(ServerInfo serverInfo, OlympaServer olympaServer, int serverId, ServerStatus status, String errorMsg, int ping) {
		super(serverInfo.getName(), olympaServer, serverId, status, errorMsg, ping);
		cacheServerInfo = serverInfo;
	}

	public ServerInfoAdvancedBungee(OlympaBungee core) {
		super(core);
		ProxyServer server = core.getProxy();
		maxPlayers = server.getOnlineCount();
		Collection<ProxiedPlayer> playersSpigot = server.getPlayers();
		onlinePlayers = playersSpigot.size();
		players = new ArrayList<>();
		server.getPlayers().forEach(player -> {
			players.add(AccountProviderAPI.getter().getPlayerInformations(player.getUniqueId()));
		});
		serverFrameworkVersion = core.getProxy().getVersion();
	}
	
	public void setPing(int ping) {
		this.ping = ping;
	}

	public void setError(String error) {
		this.error = error;
	}

	public void setStatus(ServerStatus status) {
		this.status = status;
	}

	public static ServerInfoAdvancedBungee fromJson(String string, ServerInfo serverInfo, Integer ping) {
		if (string == null || string.isBlank() || string.length() == 2 && string.equals("{}"))
			return null;
		ServerInfoAdvancedBungee serverInfoAdvancedBungee = LinkSpigotBungee.getInstance().getGson().fromJson(string, ServerInfoAdvancedBungee.class);
		serverInfoAdvancedBungee.cacheServerInfo = serverInfo;
		serverInfoAdvancedBungee.ping = ping;
		return serverInfoAdvancedBungee;
	}

	public ServerInfo getServerInfo() {
		if (cacheServerInfo == null)
			cacheServerInfo = ProxyServer.getInstance().getServerInfo(name);
		return cacheServerInfo;
	}

	public ServerInfoBasic getServerInfoBasic() {
		return new ServerInfoBasic(name, olympaServer, serverId, ping, onlinePlayers, maxPlayers, getRawMemUsage(), getThreads(), getAllThreadsCreated(),
				getStatus(), getError(), getTps(), getFirstVersionMinecraft(), getLastVersionMinecraft(), 0);
	}

	public static ServerInfoAdvancedBungee deserialize(ServerInfoAdvancedBungee serverInfoAdvancedBungee, JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		ServerInfoAdvanced.deserialize(serverInfoAdvancedBungee, json, typeOfT, context);
		return serverInfoAdvancedBungee;
	}

	@Override
	public ServerInfoAdvancedBungee deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		return ServerInfoAdvancedBungee.deserialize(new ServerInfoAdvancedBungee(), json, typeOfT, context);
	}
}
