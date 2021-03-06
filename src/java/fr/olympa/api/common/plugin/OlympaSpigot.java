package fr.olympa.api.common.plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.common.bash.OlympaRuntime;
import fr.olympa.api.common.chat.ColorUtils;
import fr.olympa.api.common.server.OlympaServer;
import fr.olympa.api.common.server.ServerInfoAdvanced;
import fr.olympa.api.common.server.ServerStatus;
import fr.olympa.api.spigot.afk.AfkHandler;
import fr.olympa.api.spigot.feedback.FeedbackManager;
import fr.olympa.api.spigot.frame.ImageFrameManager;
import fr.olympa.api.spigot.region.tracking.RegionManager;
import fr.olympa.api.spigot.scoreboard.tab.INametagApi;
import fr.olympa.api.spigot.utils.ProtocolAPI;
import fr.olympa.api.spigot.vanish.IVanishApi;
import fr.olympa.api.spigot.version.VersionHandler;

public abstract class OlympaSpigot extends OlympaAPIPlugin implements OlympaCoreSpigotInterface, LinkSpigotBungee<Player> {

	protected ServerStatus status;
	private String serverNameIp = getServer().getIp() + ":" + getServer().getPort();
	private String serverName;
	protected Location spawn;

	private List<ProtocolAPI> protocols;
	private String version = "unknown";

	protected long lastInfo;
	protected List<ServerInfoAdvanced> monitorInfos = new ArrayList<>();
	protected RegionManager regionManager;
	protected ImageFrameManager imageFrameManager;

	protected FeedbackManager feedbackManager;

	protected AfkHandler afkHandler;
	protected INametagApi nameTagApi;
	protected IVanishApi vanishApi;
	protected VersionHandler<Player> versionHandler;

	protected OlympaServer olympaServer;

	protected OlympaSpigot() {
		LinkSpigotBungee.setInstance(this);
		olympaServer = OlympaServer.ALL;
	}

	@Override
	public VersionHandler<Player> getVersionHandler() {
		return versionHandler;
	}

	@Override
	public @NotNull Collection<? extends Player> getPlayers() {
		return getServer().getOnlinePlayers();
	}

	public void setVersionHandler(VersionHandler<Player> versionHandler) {
		this.versionHandler = versionHandler;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public List<ProtocolAPI> getProtocols() {
		return protocols;
	}

	public void setProtocols(List<ProtocolAPI> protocols) {
		this.protocols = protocols;
	}

	public void setAfkApi(AfkHandler afkHandler) {
		this.afkHandler = afkHandler;
	}

	public AfkHandler getAfkHandler() {
		return afkHandler;
	}

	public void setNameTagApi(INametagApi nameTagApi) {
		this.nameTagApi = nameTagApi;
	}

	public INametagApi getNameTagApi() {
		return nameTagApi;
	}

	public void setVanishApi(IVanishApi vanishApi) {
		this.vanishApi = vanishApi;
	}

	public IVanishApi getVanishApi() {
		return vanishApi;
	}

	@Override
	public OlympaServer getOlympaServer() {
		return olympaServer;
	}

	@Override
	public void setOlympaServer(OlympaServer olympaServer) {
		this.olympaServer = olympaServer;
		sendMessage("Serveur de type ??6??l%s", olympaServer.getNameCaps());
	}

	@Override
	public String getServerName() {
		return serverName != null ? serverName : serverNameIp;
	}

	@Override
	public boolean isServerName(String serverName) {
		if (this.serverName != null)
			return this.serverName.equalsIgnoreCase(serverName) || serverName.equalsIgnoreCase(serverNameIp);
		return serverName.equalsIgnoreCase(serverNameIp);
	}

	@Override
	public ServerStatus getStatus() {
		return status;
	}

	@Override
	public RegionManager getRegionManager() {
		return regionManager;
	}

	public void setRegionManager(RegionManager regionManager) {
		this.regionManager = regionManager;
	}

	public FeedbackManager getFeedbackManager() {
		return feedbackManager;
	}

	public void setFeedbackManager(FeedbackManager feedbackManager) {
		this.feedbackManager = feedbackManager;
	}

	@Override
	public ImageFrameManager getImageFrameManager() {
		return imageFrameManager;
	}

	@Override
	public void onDisable() {
		super.onDisable();
		Collection<? extends Player> player = getServer().getOnlinePlayers();
		if (getServer().getPluginManager().isPluginEnabled("PlugMan")) // TODO ???
			return;
		player.forEach(p -> p.kickPlayer("Server closed"));
	}

	@Override
	public void onEnable() {
		super.onEnable();
		setupGlobalTasks();
		if (config != null) {
			String statusString = config.getString("status");
			if (statusString != null && !statusString.isEmpty()) {
				ServerStatus configStatus = ServerStatus.get(statusString);
				if (configStatus != null)
					setStatus(configStatus);
			} else
				setStatus(ServerStatus.UNKNOWN);
		}
	}
	
	public void restartServer(CommandSender sender) {
		Runtime.getRuntime().addShutdownHook(OlympaRuntime.action("sh start.sh", sender != null ? sender::sendMessage : null));
		stopServer();
	}
	
	public void stopServer() {
		for (Plugin plugin : getServer().getPluginManager().getPlugins()) {
			if (plugin instanceof OlympaAPIPlugin olympaPlugin) olympaPlugin.onBeforeStop();
		}
		getServer().getOnlinePlayers().forEach(p -> p.kickPlayer("Server is restarting"));
		setStatus(ServerStatus.CLOSING);
		sendMessage("??c??lArr??t du serveur dans %d secondes !", 4);
		getTask().runTaskLater(() -> getServer().shutdown(), 4 * 20);
	}

	@Override
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	@Override
	public boolean setStatus(ServerStatus status) {
		if (this.status == status) return false;
		this.status = status;
		sendMessage("Statut du serveur: %s", status.getNameColored());
		return true;
	}

	public Location getSpawn() {
		return spawn;
	}

	public Location setSpawn(Location spawn) {
		return this.spawn = spawn;
	}

	@Override
	@Nullable
	public Player getPlayer(String playerName) {
		return getServer().getPlayer(playerName);
	}

	@Override
	@Nullable
	public Player getPlayer(UUID playerUUID) {
		return getServer().getPlayer(playerUUID);
	}

	@Override
	public void sendRedis(String message, Object... args) {
		getServer().getConsoleSender().sendMessage(ColorUtils.format("??f[??e??lRedis??f] ??7" + message, args));
	}
}
