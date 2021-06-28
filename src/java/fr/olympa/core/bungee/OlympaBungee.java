package fr.olympa.core.bungee;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import com.google.gson.Gson;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.bungee.config.BungeeCustomConfig;
import fr.olympa.api.bungee.task.BungeeTaskManager;
import fr.olympa.api.common.chat.ColorUtils;
import fr.olympa.api.common.permission.OlympaPermission;
import fr.olympa.api.common.permission.list.OlympaAPIPermissionsBungee;
import fr.olympa.api.common.permission.list.OlympaAPIPermissionsGlobal;
import fr.olympa.api.common.plugin.OlympaBungeeInterface;
import fr.olympa.api.common.plugin.OlympaPluginInterface;
import fr.olympa.api.common.redis.RedisConnection;
import fr.olympa.api.common.server.OlympaServer;
import fr.olympa.api.common.server.ServerInfoBasic;
import fr.olympa.api.common.server.ServerStatus;
import fr.olympa.api.common.task.OlympaTask;
import fr.olympa.api.spigot.utils.ProtocolAPI;
import fr.olympa.api.spigot.version.VersionHandler;
import fr.olympa.core.bungee.datamanagment.AuthListener;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.config.Configuration;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public class OlympaBungee extends Plugin implements LinkSpigotBungee, OlympaPluginInterface, OlympaBungeeInterface {

	private static OlympaBungee instance;

	public static OlympaBungee getInstance() {
		return instance;
	}

	protected BungeeCustomConfig defaultConfig;
	private BungeeTaskManager task;
	private ServerStatus status;

	@Override
	public void onDisable() {
		sendMessage("&4" + getDescription().getName() + "&c (" + getDescription().getVersion() + ") est désactivé.");
	}

	@Override
	public void onEnable() {
		LinkSpigotBungee.Provider.link = this;
		instance = this;
		task = new BungeeTaskManager(this);
		status = ServerStatus.MAINTENANCE;
		OlympaPermission.registerPermissions(OlympaAPIPermissionsGlobal.class);
		OlympaPermission.registerPermissions(OlympaAPIPermissionsBungee.class);

		PluginManager pluginManager = getProxy().getPluginManager();
		pluginManager.registerListener(this, new AuthListener());
		sendMessage("&2" + getDescription().getName() + "&a (" + getDescription().getVersion() + ") est activé.");

	}

	@Override
	public String getPrefixConsole() {
		return "&f[&6" + getDescription().getName() + "&f] &e";
	}

	@Override
	public Connection getDatabase() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void launchAsync(Runnable run) {
		getTask().runTaskAsynchronously(run);

	}

	@Override
	public String getServerName() {
		return "bungee";
	}

	@Override
	public void sendMessage(String message, Object... args) {
		getProxy().getConsole().sendMessage(TextComponent.fromLegacyText(String.format(ColorUtils.color(getPrefixConsole() + message), args)));

	}

	@Override
	public boolean isSpigot() {
		return false;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public ServerStatus getStatus() {
		return status;
	}

	@Override
	public OlympaTask getTask() {
		return task;
	}

	@Override
	public OlympaServer getOlympaServer() {
		return null;
	}

	@Override
	public List<String> getPlayersNames() {
		return null;
	}

	@Override
	public Gson getGson() {
		return new Gson();
	}

	@Override
	public Configuration getConfig() {
		return null;
	}

	@Override
	public void registerRedisSub(Jedis jedis, JedisPubSub sub, String channel) {

	}

	@Override
	public BungeeCustomConfig getDefaultConfig() {
		return null;
	}

	@Override
	public boolean isServerName(String serverName) {
		return false;
	}

	@Override
	public void setServerName(String serverName) {

	}

	@Override
	public void setStatus(ServerStatus status) {

	}

	@Override
	public void registerRedisSub(JedisPubSub sub, String channel) {}

	@Override
	public RedisConnection getRedisAccess() {
		return null;
	}

	@Override
	public Collection<ServerInfoBasic> getMonitorServers() {
		return null;
	}

	@Override
	public boolean isRedisConnected() {
		return false;
	}

	@Override
	public boolean isDatabaseConnected() {
		return false;
	}

	@Override
	public VersionHandler<?> getVersionHandler() {
		return null;
	}

	@Override
	public List<ProtocolAPI> getProtocols() {
		return null;
	}
}
