package fr.olympa.api.common.server;

import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import com.google.gson.annotations.Expose;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.common.chat.TxtComponentBuilder;
import fr.olympa.api.common.machine.JavaInstanceInfo;
import fr.olympa.api.common.player.OlympaPlayerInformations;
import fr.olympa.api.common.plugin.PluginInfoAdvanced;
import fr.olympa.api.common.plugin.PluginInfoBungee;
import fr.olympa.api.common.plugin.PluginInfoSpigot;
import fr.olympa.api.common.sort.Sorting;
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
		LinkSpigotBungee link = LinkSpigotBungee.Provider.link;
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

	public static TxtComponentBuilder getPluginsToString(List<PluginInfoAdvanced> plugins, boolean isConsole) {
		TxtComponentBuilder txt = new TxtComponentBuilder().console(isConsole).extraSpliter("&7, ");
		plugins.forEach(debugPlugin -> {
			TxtComponentBuilder txt2 = debugPlugin.getToTxtComponent();
			txt2.console(isConsole);
			txt.extra(txt2);
		});
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
	protected ServerStatus status;
	@Expose
	protected OlympaServer olympaServer;
	@Expose
	protected long uptime;
	@Expose
	protected float tps;
	@Expose
	protected List<PluginInfoAdvanced> plugins;
	@Expose
	protected String firstVersionMinecraft;
	@Expose
	protected String lastVersionMinecraft;
	@Expose
	protected String serverFrameworkVersion;
	@Expose
	protected boolean databaseConnected;

	protected Boolean redisConnected;
	@Expose
	protected int serverId;
	@Expose
	protected int ping;
	@Expose
	protected int onlinePlayers;
	@Expose
	protected int maxPlayers;
	@Expose
	protected List<OlympaPlayerInformations> players;

	public ServerInfoAdvanced() {
		super();
	}

	public String getName() {
		return name;
	}

	public String getHumainName() {
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

	public float getTps() {
		return tps;
	}

	public List<PluginInfoAdvanced> getPlugins() {
		return plugins;
	}

	public String getFirstVersionMinecraft() {
		return firstVersionMinecraft;
	}

	public String getLastVersionMinecraft() {
		return lastVersionMinecraft;
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
	//	public boolean isDatabaseConnected() {
	//		return databaseConnected;
	//	}

	public Boolean isRedisConnected() {
		return redisConnected;
	}

	public OlympaServer getOlympaServer() {
		return olympaServer;
	}

}