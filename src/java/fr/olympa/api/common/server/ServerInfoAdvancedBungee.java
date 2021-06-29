package fr.olympa.api.common.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nullable;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.common.annotation.SpigotOrBungee;
import fr.olympa.api.common.annotation.SpigotOrBungee.AllowedFramework;
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
	private static Set<String> readTimeException = new HashSet<>();

	public static Set<String> getReadTimeException() {
		return readTimeException;
	}

	public static void removeReadTimeException(ServerInfo serverInfo) {
		ServerInfoAdvancedBungee.readTimeException.remove(serverInfo.getName());
	}

	public static boolean addReadTimeException(ServerInfo serverInfo) {
		if (ServerInfoAdvancedBungee.readTimeException.contains(serverInfo.getName())) {
			ServerInfoAdvancedBungee.readTimeException.remove(serverInfo.getName());
			return false;
		}
		ServerInfoAdvancedBungee.readTimeException.add(serverInfo.getName());
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
		int serverID = serverInfos.getValue();
		try {
			if (error == null) {
				allMotd = serverPing.getDescriptionComponent().toPlainText();
				if (allMotd.startsWith("V2 "))
					serverDebugInfo = ServerInfoAdvancedBungee.fromJson(allMotd.substring(3));
				else {
					Players players = serverPing.getPlayers();
					int onlinePlayers = players.getOnline();
					int maxPlayers = players.getMax();
					float tps = 0;
					if (allMotd.startsWith("§"))
						allMotd = allMotd.substring(2);
					String[] motd = allMotd.split(" ");
					if (motd.length >= 1)
						status = ServerStatus.get(motd[0]);
					if (motd.length >= 2 && RegexMatcher.FLOAT.is(motd[1]))
						tps = RegexMatcher.FLOAT.parse(motd[1]);
					if (motd.length >= 8) {
						String json = String.join(" ", Arrays.copyOfRange(motd, 7, motd.length));
						serverDebugInfo = ServerInfoAdvancedBungee.fromJson(json);
						if (serverDebugInfo == null)
							serverDebugInfo = new ServerInfoAdvancedBungee();
						serverDebugInfo.setPing(ping);
					} else
						serverDebugInfo = new ServerInfoAdvancedBungee(serverInfo, olympaServer, serverID, status, onlinePlayers, maxPlayers, ping, tps);
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

	ServerInfo serverInfo;

	public ServerInfoAdvancedBungee() {
		super();
	}

	public ServerInfoAdvancedBungee(ServerInfo serverInfo, OlympaServer olympaServer, int serverId, ServerStatus status, int onlinePlayers, int maxPlayers, int ping, float tps) {
		super(serverInfo.getName(), olympaServer, serverId, status, onlinePlayers, maxPlayers, ping, tps);
		this.serverInfo = serverInfo;
	}

	public ServerInfoAdvancedBungee(ServerInfo serverInfo, OlympaServer olympaServer, int serverId, String errorMsg, int ping) {
		super(serverInfo.getName(), olympaServer, serverId, errorMsg, ping);
		this.serverInfo = serverInfo;
	}

	public ServerInfoAdvancedBungee(ServerInfo serverInfo, OlympaServer olympaServer, int serverId, ServerStatus status, String errorMsg, int ping) {
		super(serverInfo.getName(), olympaServer, serverId, status, errorMsg, ping);
		this.serverInfo = serverInfo;
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

	@SpigotOrBungee(allow = AllowedFramework.BUNGEE)
	public void setPing(int ping) {
		this.ping = ping;
	}

	@SpigotOrBungee(allow = AllowedFramework.BUNGEE)
	public void setError(String error) {
		this.error = error;
	}

	@SpigotOrBungee(allow = AllowedFramework.BUNGEE)
	public void setStatus(ServerStatus status) {
		this.status = status;
	}

	public static ServerInfoAdvancedBungee fromJson(String string) {
		if (string == null || string.isBlank() || string.length() == 2 && string.equals("{}"))
			return null;
		return LinkSpigotBungee.getInstance().getGson().fromJson(string, ServerInfoAdvancedBungee.class);
	}

	public ServerInfo getServerInfo() {
		return serverInfo;
	}

	public ServerInfoBasic getServerInfoBasic() {
		return new ServerInfoBasic(name, olympaServer, serverId, ping, onlinePlayers, maxPlayers, getRawMemUsage(), getThreads(), getAllThreadsCreated(),
				getStatus(), getError(), getTps(), getFirstVersionMinecraft(), getLastVersionMinecraft(), 0);
	}

}
