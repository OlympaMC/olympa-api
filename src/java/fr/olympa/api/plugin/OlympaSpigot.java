package fr.olympa.api.plugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.olympa.api.afk.AfkHandler;
import fr.olympa.api.frame.ImageFrameManager;
import fr.olympa.api.holograms.HologramsManager;
import fr.olympa.api.hook.IProtocolSupport;
import fr.olympa.api.region.tracking.RegionManager;
import fr.olympa.api.scoreboard.tab.INametagApi;
import fr.olympa.api.server.OlympaServer;
import fr.olympa.api.server.ServerStatus;
import fr.olympa.api.vanish.IVanishApi;

public abstract class OlympaSpigot extends OlympaAPIPlugin implements OlympaCoreInterface {

	protected ServerStatus status;
	private String serverNameIp = getServer().getIp() + ":" + getServer().getPort();
	private String serverName;
	protected INametagApi nameTagApi;
	protected IVanishApi vanishApi;
	protected AfkHandler afkHandler;

	protected RegionManager regionManager;
	protected HologramsManager hologramsManager;
	protected ImageFrameManager imageFrameManager;

	protected OlympaServer olympaServer = OlympaServer.ALL;

	@Override
	public AfkHandler getAfkHandler() {
		return afkHandler;
	}

	@Override
	public OlympaServer getOlympaServer() {
		return olympaServer;
	}

	@Override
	public void setOlympaServer(OlympaServer olympaServer) {
		this.olympaServer = olympaServer;
	}

	public abstract IProtocolSupport getProtocolSupport();

	@Override
	public String getServerName() {
		return serverName != null ? serverName : serverNameIp;
	}

	@Override
	public IVanishApi getVanishApi() {
		return vanishApi;
	}

	@Override
	public INametagApi getNameTagApi() {
		return nameTagApi;
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

	@Override
	public HologramsManager getHologramsManager() {
		return hologramsManager;
	}

	@Override
	public ImageFrameManager getImageFrameManager() {
		return imageFrameManager;
	}

	@Override
	public void onDisable() {
		hologramsManager.unload();
		while (Bukkit.getOnlinePlayers().size() > 0) {
			for (Player p : Bukkit.getOnlinePlayers())
				p.kickPlayer("Server closed");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		super.onDisable();
	}

	@Override
	public void onEnable() {
		super.onEnable();
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

}
