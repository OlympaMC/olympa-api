package fr.olympa.api.common.server;

import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import fr.olympa.api.common.chat.TxtComponentBuilder;
import fr.olympa.api.common.machine.JavaInstanceInfo;
import fr.olympa.api.common.player.OlympaPlayerInformations;
import fr.olympa.api.common.plugin.PluginInfoAdvanced;
import fr.olympa.api.utils.Utils;

public class ServerInfoAdvanced extends JavaInstanceInfo {

	private static List<String> principalAuthors = Arrays.asList("SkytAsul", "Tristiisch", "Bullobily");
	private static String prefixOwnPlugins = "Olympa";
	protected static List<PluginInfoAdvanced> ownPlugins = null;

	/**
	 * @return a list with all plugins register on this server
	 */
	public static List<PluginInfoAdvanced> getCachePlugins() {
		return ownPlugins;
	}

	/**
	 * @return a list with all plugins with prefix Olympa
	 */
	public static List<PluginInfoAdvanced> getOwnOlympaPlugins() {
		return ownPlugins.stream().filter(dp -> dp.getName().startsWith(prefixOwnPlugins)).collect(Collectors.toList());
	}

	/**
	 * @return a list with all plugins without prefix Olympa but created by one of principal authors
	 */
	public static List<PluginInfoAdvanced> getHomeMadePlugins() {
		return ownPlugins.stream().filter(dp -> !dp.getName().startsWith(prefixOwnPlugins) && dp.getAuthors().stream().anyMatch(author -> principalAuthors.contains(author))).collect(Collectors.toList());
	}

	/**
	 * @return a list with all plugins with prefix Olympa or created by one of principal authors
	 */
	public static List<PluginInfoAdvanced> getAllHomeMadePlugins() {
		return ownPlugins.stream().filter(dp -> dp.getName().startsWith(prefixOwnPlugins) ||
				dp.getAuthors().stream().anyMatch(author -> principalAuthors.contains(author))).collect(Collectors.toList());
	}

	/**
	 * @return a list with all plugins without prefix Olympa and didn't created by one of principal authors
	 */
	public static List<PluginInfoAdvanced> getExternalPlugins() {
		return ownPlugins.stream().filter(dp -> !dp.getName().startsWith(prefixOwnPlugins)
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
		return new Gson().fromJson(string, ServerInfoAdvanced.class);
	}

	protected String name;
	protected ServerStatus status;
	protected OlympaServer olympaServer;
	protected long uptime;
	protected float tps;
	protected List<PluginInfoAdvanced> plugins;
	protected String firstVersionMinecraft;
	protected String lastVersionMinecraft;
	protected String bukkitVersion;
	protected boolean databaseConnected;

	protected Boolean redisConnected;
	protected int serverId;
	protected int ping;
	protected int onlinePlayers;
	protected int maxPlayers;
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
		return new Gson().toJson(this);
	}

	public String getUptime() {
		return Utils.tsToShortDur(uptime);
	}

	public String getBukkitVersion() {
		return bukkitVersion;
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