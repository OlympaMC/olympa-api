package fr.olympa.api.bungee.plugin;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.gson.Gson;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.bungee.config.BungeeCustomConfig;
import fr.olympa.api.bungee.servers.BungeeMonitoring;
import fr.olympa.api.bungee.task.BungeeTaskManager;
import fr.olympa.api.bungee.version.BungeeProtocol;
import fr.olympa.api.common.chat.ColorUtils;
import fr.olympa.api.common.permission.OlympaPermission;
import fr.olympa.api.common.permission.list.OlympaAPIPermissionsBungee;
import fr.olympa.api.common.permission.list.OlympaAPIPermissionsGlobal;
import fr.olympa.api.common.plugin.OlympaBungeeInterface;
import fr.olympa.api.common.plugin.OlympaPluginInterface;
import fr.olympa.api.common.redis.RedisConnection;
import fr.olympa.api.common.server.OlympaServer;
import fr.olympa.api.common.server.ServerInfoAdvanced;
import fr.olympa.api.common.server.ServerInfoAdvancedBungee;
import fr.olympa.api.common.server.ServerStatus;
import fr.olympa.api.common.sql.DatabaseConnection;
import fr.olympa.api.spigot.utils.ProtocolAPI;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public abstract class OlympaBungeeCore extends Plugin implements LinkSpigotBungee<ProxiedPlayer>, OlympaPluginInterface, OlympaBungeeInterface {

	private static OlympaBungeeCore instance;

	public static OlympaBungeeCore getInstance() {
		return instance;
	}

	protected BungeeCustomConfig defaultConfig;
	protected BungeeCustomConfig maintConfig;
	private BungeeTaskManager task;
	private ServerStatus status;
	private String serverName = "bungee1";
	protected boolean redisConnected = false;
	protected boolean dbConnected = false;
	private List<ProtocolAPI> protocols;
	protected BungeeProtocol versionHandler;
	protected RedisConnection redisAccess;
	protected DatabaseConnection database;
	protected BungeeMonitoring monitoring;
	protected boolean isEnable = false;

	public BungeeMonitoring getMonitoring() {
		return monitoring;
	}

	public void setMonitoring(BungeeMonitoring monitoring) {
		this.monitoring = monitoring;
	}

	@Override
	public void onDisable() {
		if (task != null)
			task.cancelTaskByName("monitor_serveurs");
	}

	@Override
	public void onLoad() {
		super.onLoad();
		LinkSpigotBungee.setInstance(this);
	}

	@Override
	public void onEnable() {
		setupGlobalTasks();

		OlympaPermission.registerPermissions(OlympaAPIPermissionsGlobal.class);
		OlympaPermission.registerPermissions(OlympaAPIPermissionsBungee.class);

		task = new BungeeTaskManager(this);
		defaultConfig = new BungeeCustomConfig(this, "config");
		defaultConfig.loadSafe();
		maintConfig = new BungeeCustomConfig(this, "maintenance");
		maintConfig.loadSafe();
		status = ServerStatus.get(maintConfig.getConfig().getString("settings.status"));
		versionHandler = new BungeeProtocol(this);
	}

	@Override
	public void sendMessage(String message, Object... args) {
		getProxy().getConsole().sendMessage(TextComponent.fromLegacyText(String.format(ColorUtils.color(getPrefixConsole() + message), args)));
	}

	@Override
	public void sendRedis(String message, Object... args) {
		getProxy().getConsole().sendMessage(TextComponent.fromLegacyText(String.format(ColorUtils.color("§f[§e§lRedis§f] §7" + message), args)));
	}

	@Override
	public OlympaServer getOlympaServer() {
		return OlympaServer.BUNGEE;
	}

	public void setDefaultConfig(BungeeCustomConfig defaultConfig) {
		this.defaultConfig = defaultConfig;
	}

	public void setMaintConfig(BungeeCustomConfig maintConfig) {
		this.maintConfig = maintConfig;
	}

	@Override
	public ServerStatus getStatus() {
		return status;
	}

	@Override
	public boolean setStatus(ServerStatus status) {
		if (this.status == status) return false;
		this.status = status;
		sendMessage("Statut du serveur: %s", status.getNameColored());
		return true;
	}

	@Override
	public BungeeCustomConfig getDefaultConfig() {
		return defaultConfig;
	}

	public Configuration getMaintConfig() {
		return maintConfig != null ? maintConfig.getConfig() : null;
	}

	public BungeeCustomConfig getMaintCustomConfig() {
		return maintConfig;
	}

	@Override
	public String getPrefixConsole() {
		return "&f[&6" + getDescription().getName() + "&f] &e";
	}

	@Override
	public String getServerName() {
		return "bungee1";
	}

	@Override
	public BungeeTaskManager getTask() {
		return task;
	}

	@Override
	public void launchAsync(Runnable run) {
		getTask().runTaskAsynchronously(run);
	}

	@Override
	public List<String> getPlayersNames() {
		return getProxy().getPlayers().stream().map(ProxiedPlayer::getDisplayName).collect(Collectors.toList());
	}

	@Override
	public boolean isServerName(String serverName) {
		return this.serverName.equals(serverName);
	}

	@Override
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	@Override
	public boolean isRedisConnected() {
		return redisConnected;
	}

	@Override
	public boolean isDatabaseConnected() {
		return dbConnected;
	}

	@Override
	public @NotNull Collection<? extends ProxiedPlayer> getPlayers() {
		return getProxy().getPlayers();
	}

	@Override
	@Nullable
	public ProxiedPlayer getPlayer(String x) {
		return getProxy().getPlayer(x);
	}

	@Override
	@Nullable
	public ProxiedPlayer getPlayer(UUID x) {
		return getProxy().getPlayer(x);
	}

	@Override
	public List<ProtocolAPI> getProtocols() {
		return protocols;
	}

	@Override
	public BungeeProtocol getVersionHandler() {
		return versionHandler;
	}

	@Override
	public Configuration getConfig() {
		return defaultConfig.getConfig();
	}

	@Override
	public boolean isEnabled() {
		return isEnable;
	}

	@Override
	public boolean isSpigot() {
		return false;
	}

	@Override
	public RedisConnection getRedisAccess() {
		return redisAccess;
	}

	public DatabaseConnection getDatabaseHandler() {
		return database;
	}

	@Override
	public void registerRedisSub(JedisPubSub sub, String channel) {
		registerRedisSub(redisAccess.connect(), sub, channel);
	}

	@Override
	public void registerRedisSub(Jedis jedis, JedisPubSub sub, String channel) {
		Thread t = new Thread(() -> {
			jedis.subscribe(sub, channel);
			jedis.disconnect();
		}, "Redis sub " + channel);
		Thread.UncaughtExceptionHandler h = (th, ex) -> {
			ex.printStackTrace();
			if (redisAccess != null)
				registerRedisSub(redisAccess.connect(), sub, channel);
		};
		t.setUncaughtExceptionHandler(h);
		t.start();
	}

	@Override
	public abstract Collection<ServerInfoAdvanced> getMonitorServers();

	public abstract Map<OlympaServer, Map<Integer, ServerInfoAdvancedBungee>> getServersByTypeWithBungee();

	@Override
	public abstract Gson getGson();

	public void setProtocols(List<ProtocolAPI> protocols) {
		this.protocols = protocols;
	}
}
