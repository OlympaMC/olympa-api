package fr.olympa.api.common.plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.common.server.OlympaServer;
import fr.olympa.api.common.server.ServerInfoAdvanced;
import fr.olympa.api.common.server.ServerStatus;
import fr.olympa.api.spigot.afk.AfkHandler;
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

	protected AfkHandler afkHandler;
	protected INametagApi nameTagApi;
	protected IVanishApi vanishApi;
	protected VersionHandler<Player> versionHandler;

	protected OlympaServer olympaServer;
	
	protected OlympaSpigot() {
		LinkSpigotBungee.setInstance(this);
		this.olympaServer = OlympaServer.ALL;
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

	@Override
	public ImageFrameManager getImageFrameManager() {
		return imageFrameManager;
	}

	@Override
	public void onDisable() {
		super.onDisable();
		Collection<? extends Player> player = Bukkit.getOnlinePlayers();
		if (player.size() < 10 && getServer().getPluginManager().isPluginEnabled("PlugMan"))
			return;
		for (Player p : player)
			p.kickPlayer("Server closed");
	}

	@Override
	public void onEnable() {
		super.onEnable();
		setupGlobalTasks();
		if (config != null) {
			String statusString = config.getString("status");
			if (statusString != null && !statusString.isEmpty()) {
				ServerStatus status2 = ServerStatus.get(statusString);
				if (status2 != null)
					status = status2;
			} else
				setStatus(ServerStatus.UNKNOWN);
		}
	}

	@Override
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	@Override
	public void setStatus(ServerStatus status) {
		this.status = status;
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
}
