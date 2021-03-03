package fr.olympa.api.plugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.olympa.api.afk.AfkHandler;
import fr.olympa.api.frame.ImageFrameManager;
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

	protected RegionManager regionManager;
	protected ImageFrameManager imageFrameManager;

	protected AfkHandler afkHandler;
	protected INametagApi nameTagApi;
	protected IVanishApi vanishApi;

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

	protected OlympaServer olympaServer = OlympaServer.ALL;

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
	public ImageFrameManager getImageFrameManager() {
		return imageFrameManager;
	}

	@Override
	public void onDisable() {
		new Thread(() -> {
			while (!Bukkit.getOnlinePlayers().isEmpty()) {
				for (Player p : Bukkit.getOnlinePlayers())
					p.kickPlayer("Server closed");
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
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
