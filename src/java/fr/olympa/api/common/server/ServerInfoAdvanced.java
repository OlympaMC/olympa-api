package fr.olympa.api.common.server;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.gson.annotations.Expose;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.common.annotation.SpigotOrBungee;
import fr.olympa.api.common.annotation.SpigotOrBungee.AllowedFramework;
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
		return LinkSpigotBungee.getInstance().getGson().fromJson(string, ServerInfoAdvanced.class);
	}

	@Expose
	protected String name;
	@Expose
	protected OlympaServer olympaServer = OlympaServer.ALL;
	@Expose
	protected int serverId;
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
	protected List<ProtocolAPI> versions;

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
		sj.add(" " + Utils.intToSymbole(getCPUSysCore()));
		return sj.toString();
	}

	public ServerStatus getStatus() {
		return status;
	}

	public List<PluginInfoAdvanced> getPlugins() {
		return plugins;
	}

	public String getFirstVersionMinecraft() {
		return versions.get(versions.size() - 1).getName();
	}

	public String getLastVersionMinecraft() {
		return versions.get(0).getName();
	}

	public String getRangeVersionMinecraft() {
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

	public boolean hasMinimalInfo() {
		return getOnlinePlayers() != null && getMaxPlayers() != null && olympaServer != null && name != null;
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
		return Utils.getLetterOfNumber(serverId);
	}

	@SpigotOrBungee(allow = AllowedFramework.BUNGEE)
	public void setPing(int ping) {
		this.ping = ping;
	}
}