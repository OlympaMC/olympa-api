package fr.olympa.api.common.server;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.Expose;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.common.chat.TxtComponentBuilder;
import fr.olympa.api.common.machine.JavaInstanceInfo;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.common.player.OlympaPlayerInformations;
import fr.olympa.api.common.plugin.PluginInfoAdvanced;
import fr.olympa.api.common.plugin.PluginInfoBungee;
import fr.olympa.api.common.plugin.PluginInfoSpigot;
import fr.olympa.api.common.sort.Sorting;
import fr.olympa.api.spigot.utils.ProtocolAPI;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.spigot.OlympaCore;

public class ServerInfoAdvanced extends JavaInstanceInfo {

	private static List<String> principalAuthors = Arrays.asList("SkytAsul", "Tristiisch", "Bullobily");
	private static String prefixOwnPlugins = "Olympa";
	private static List<PluginInfoAdvanced> pluginsOfInstance;
	private static Sorting<PluginInfoAdvanced> sortingPlugins = new Sorting<>(dp -> dp.getName().startsWith(prefixOwnPlugins) ? 0l : 1l,
			dp -> dp.getAuthors().stream().anyMatch(author -> principalAuthors.contains(author)) ? 0l : 1l);

	public static void setPlugins() {
		LinkSpigotBungee<?> link = LinkSpigotBungee.Provider.link;
		if (link.isSpigot())
			pluginsOfInstance = Arrays.stream(((OlympaCore) link).getServer().getPluginManager().getPlugins()).map(PluginInfoSpigot::new).sorted(sortingPlugins).collect(Collectors.toList());
		else
			pluginsOfInstance = ((OlympaBungee) link).getProxy().getPluginManager().getPlugins().stream().map(PluginInfoBungee::new).sorted(sortingPlugins).collect(Collectors.toList());
	}

	/**
	 * @return a list with all plugins register on this server
	 */
	public static List<PluginInfoAdvanced> getCachePlugins() {
		if (pluginsOfInstance == null)
			setPlugins();
		return pluginsOfInstance;
	}

	/**
	 * @return a list with all plugins with prefix Olympa
	 */
	public static List<PluginInfoAdvanced> getOwnOlympaPlugins() {
		return getCachePlugins().stream().filter(dp -> dp.getName().startsWith(prefixOwnPlugins)).collect(Collectors.toList());
	}

	/**
	 * @return a list with all plugins without prefix Olympa but created by one of principal authors
	 */
	public static List<PluginInfoAdvanced> getHomeMadePlugins() {
		return getCachePlugins().stream().filter(dp -> !dp.getName().startsWith(prefixOwnPlugins) && dp.getAuthors().stream().anyMatch(author -> principalAuthors.contains(author))).collect(Collectors.toList());
	}

	/**
	 * @return a list with all plugins with prefix Olympa or created by one of principal authors
	 */
	public static List<PluginInfoAdvanced> getAllHomeMadePlugins() {
		return getCachePlugins().stream().filter(dp -> dp.getName().startsWith(prefixOwnPlugins) ||
				dp.getAuthors().stream().anyMatch(author -> principalAuthors.contains(author))).collect(Collectors.toList());
	}

	/**
	 * @return a list with all plugins without prefix Olympa and didn't created by one of principal authors
	 */
	public static List<PluginInfoAdvanced> getExternalPlugins() {
		return getCachePlugins().stream().filter(dp -> !dp.getName().startsWith(prefixOwnPlugins)
				&& dp.getAuthors().stream().noneMatch(author -> principalAuthors.contains(author)))
				.collect(Collectors.toList());
	}

	public static TxtComponentBuilder getPluginsToString(Iterable<PluginInfoAdvanced> plugins, boolean isConsole, boolean withVersion) {
		TxtComponentBuilder txt = new TxtComponentBuilder().console(isConsole).extraSpliter("&7, ");
		Iterator<PluginInfoAdvanced> it = plugins.iterator();
		while (it.hasNext()) {
			PluginInfoAdvanced debugPlugin = it.next();
			TxtComponentBuilder txt2 = debugPlugin.getToTxtComponent(withVersion);
			txt2.console(isConsole);
			txt.extra(txt2);
		}
		return txt;
	}

	public static ServerInfoAdvanced fromJson(String string) {
		if (string == null || string.isBlank() || string.length() == 2 && string.equals("{}"))
			return null;
		ServerInfoAdvanced serverInfoAdvanced = LinkSpigotBungee.getInstance().getGson().fromJson(string, ServerInfoAdvanced.class);
		return serverInfoAdvanced;
	}

	@Expose
	protected String name;
	@Expose
	protected OlympaServer olympaServer = OlympaServer.ALL;
	@Expose
	protected Integer serverId;
	@Expose
	protected ServerStatus status = ServerStatus.UNKNOWN;
	@Expose
	protected long uptime;
	@Expose
	protected String serverFrameworkVersion;
	@Expose
	protected Integer onlinePlayers;
	@Expose
	protected Integer maxPlayers;
	@Expose
	@Nullable
	protected Integer ping;
	@Expose
	@Nullable
	protected String error;
	@Expose
	@Nullable
	protected Float tps;
	@Expose
	protected boolean databaseConnected;
	@Expose
	protected boolean redisConnected;
	@Expose
	protected List<OlympaPlayerInformations> players;
	@Expose
	protected List<PluginInfoAdvanced> plugins;
	@Expose
	protected List<ProtocolAPI> versions = new ArrayList<>();

	public ServerInfoAdvanced(String name, OlympaServer olympaServer, int serverId, ServerStatus status, int onlinePlayers, int maxPlayers, int ping, float tps) {
		this.name = name;
		this.olympaServer = olympaServer;
		this.serverId = serverId;
		this.status = status;
		this.onlinePlayers = onlinePlayers;
		this.maxPlayers = maxPlayers;
		this.ping = ping;
		this.tps = tps;
	}

	public ServerInfoAdvanced(String name, OlympaServer olympaServer, int serverId, ServerStatus status, String error, int ping) {
		this.name = name;
		this.olympaServer = olympaServer;
		this.serverId = serverId;
		this.status = status;
		this.error = error;
		this.ping = ping;
	}

	public ServerInfoAdvanced(String name, OlympaServer olympaServer, int serverId, String error, int ping) {
		this.name = name;
		this.olympaServer = olympaServer;
		this.serverId = serverId;
		this.ping = ping;
		this.error = error;
	}

	public ServerInfoAdvanced() {}

	public ServerInfoAdvanced(LinkSpigotBungee<?> core) {
		super(Utils.getCurrentTimeInSeconds());
		name = core.getServerName();
		serverId = OlympaServer.getServerId(name);
		olympaServer = core.getOlympaServer();
		status = core.getStatus();
		databaseConnected = core.isDatabaseConnected();
		redisConnected = core.isRedisConnected();
		uptime = core.getUptimeLong();
		plugins = getCachePlugins();
		versions = core.getProtocols();
	}

	public String getName() {
		return name;
	}

	public String getHumanName() {
		if (olympaServer.getNameCaps() == null)
			return Utils.capitalize(name);
		StringJoiner sj = new StringJoiner(" ");
		sj.add(olympaServer.getNameCaps());
		if (serverId != null)
			sj.add(Utils.intToSymbole(serverId));
		return sj.toString();
	}

	public ServerStatus getStatus() {
		return status;
	}

	public List<PluginInfoAdvanced> getPlugins() {
		return plugins;
	}

	public String getFirstVersionMinecraft() {
		if (!hasInfoVersions())
			return "unknown";
		return versions.get(versions.size() - 1).getName();
	}

	public String getLastVersionMinecraft() {
		if (!hasInfoVersions())
			return "unknown";
		return versions.get(0).getName();
	}

	public String getRangeVersionMinecraft() {
		if (!hasInfoVersions())
			return "unknown";
		return ProtocolAPI.getRange(versions);
	}

	@Override
	public String toString() {
		return LinkSpigotBungee.getInstance().getGson().toJson(this);
	}

	public String getUptime() {
		return Utils.tsToShortDur(uptime);
	}

	public String getServerFrameworkVersion() {
		return serverFrameworkVersion;
	}

	public boolean isDatabaseConnected() {
		return databaseConnected;
	}

	public boolean isRedisConnected() {
		return redisConnected;
	}

	public OlympaServer getOlympaServer() {
		return olympaServer;
	}

	public int getServerId() {
		if (serverId == null)
			return 0;
		return serverId;
	}

	public List<OlympaPlayerInformations> getPlayers() {
		return players;
	}

	public boolean canConnect(OlympaPlayer olympaPlayer, ProtocolAPI protocol) {
		status.hasPermission(olympaPlayer);
		return status != ServerStatus.CLOSE;
	}

	@Nullable
	public Integer getOnlinePlayers() {
		return onlinePlayers;
	}

	@Nullable
	public Integer getMaxPlayers() {
		return maxPlayers;
	}

	@Nullable
	public Float getTps() {
		return tps;
	}

	@Nullable
	public Integer getPing() {
		return ping;
	}

	@Nullable
	public String getError() {
		return error;
	}

	public boolean hasInfoVersions() {
		return versions != null && !versions.isEmpty();
	}

	public boolean hasMinimalInfo() {
		return status != null && olympaServer != null && name != null;
	}

	public boolean hasFullInfos() {
		return hasMinimalInfo() && hasInstanceInfo() && getTps() != null && ping != null;
	}

	public boolean isUsualError() {
		return status == ServerStatus.CLOSE && (error == null || error.isEmpty());
	}

	public boolean isDefaultError() {
		return status == ServerStatus.CLOSE && error != null && error.isEmpty();
	}

	public boolean isOpen() {
		return status != ServerStatus.CLOSE;
	}

	public boolean canConnect(OlympaPlayer olympaPlayer) {
		return status.canConnect() && olympaServer.canConnect(olympaPlayer) && status.hasPermission(olympaPlayer);
	}

	public String getIdSymbole() {
		if (serverId == null)
			return null;
		return Utils.intToSymbole(serverId);
	}

	@SuppressWarnings("unchecked")
	public static ServerInfoAdvanced deserialize(ServerInfoAdvanced serverInfoAdvanced, JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = json.getAsJsonObject();
		if (object.has("name"))
			serverInfoAdvanced.name = object.get("name").getAsString();
		if (object.has("olympaServer"))
			serverInfoAdvanced.olympaServer = context.deserialize(object.get("olympaServer"), OlympaServer.class);
		if (object.has("serverId"))
			serverInfoAdvanced.serverId = object.get("serverId").getAsInt();
		if (object.has("status"))
			serverInfoAdvanced.status = context.deserialize(object.get("status"), ServerStatus.class);
		if (object.has("uptime"))
			serverInfoAdvanced.uptime = object.get("uptime").getAsLong();
		if (object.has("serverFrameworkVersion"))
			serverInfoAdvanced.serverFrameworkVersion = object.get("serverFrameworkVersion").getAsString();
		if (object.has("onlinePlayers"))
			serverInfoAdvanced.onlinePlayers = object.get("onlinePlayers").getAsInt();
		if (object.has("maxPlayers"))
			serverInfoAdvanced.maxPlayers = object.get("maxPlayers").getAsInt();
		if (object.has("tps"))
			serverInfoAdvanced.tps = object.get("tps").getAsFloat();
		if (object.has("databaseConnected"))
			serverInfoAdvanced.databaseConnected = object.get("databaseConnected").getAsBoolean();
		if (object.has("redisConnected"))
			serverInfoAdvanced.databaseConnected = object.get("redisConnected").getAsBoolean();
		//				if (object.has("players"))
		//					serverInfoAdvanced.players = context.deserialize(object.get("players"), OlympaPlayerInformations.class);
		// com.google.gson.JsonSyntaxException: java.lang.IllegalStateException: Not a JSON Object: []
		// com.google.gson.JsonSyntaxException: java.lang.IllegalStateException: Not a JSON Object: [{"id":1,"pseudo":"Tristiisch","uuid":"193cdd1c-02e4-3642-91dd-9d676956a664"}]
		// com.google.gson.JsonSyntaxException: java.lang.IllegalStateException: Not a JSON Object: [{"id":49,"pseudo":"Yzrohk","uuid":"8df57399-6131-3706-beca-a7dedc0da6da"}]
		if (object.has("plugins"))
			serverInfoAdvanced.plugins = (List<PluginInfoAdvanced>) context.deserialize(object.get("plugins"), List.class);
		if (object.has("versions"))
			((List<String>) context.deserialize(object.get("versions"), List.class)).forEach((name) -> serverInfoAdvanced.versions.add(ProtocolAPI.valueOf(name)));
		//		serverInfoAdvanced.versions = (List<ProtocolAPI>) context.deserialize(object.get("versions"), List.class);
		JavaInstanceInfo.deserialize(serverInfoAdvanced, json, typeOfT, context);
		return serverInfoAdvanced;
	}

	@Override
	public ServerInfoAdvanced deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		return ServerInfoAdvanced.deserialize(new ServerInfoAdvanced(), json, typeOfT, context);
	}
}