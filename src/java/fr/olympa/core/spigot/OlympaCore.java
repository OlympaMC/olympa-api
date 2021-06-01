package fr.olympa.core.spigot;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.function.BiConsumer;

import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.messaging.Messenger;

import fr.olympa.api.common.bpmc.SpigotBPMCEvent;
import fr.olympa.api.common.permission.OlympaPermission;
import fr.olympa.api.common.permission.list.OlympaAPIPermissionsSpigot;
import fr.olympa.api.common.plugin.OlympaSpigot;
import fr.olympa.api.common.server.OlympaServer;
import fr.olympa.api.common.server.ServerInfoBasic;
import fr.olympa.api.spigot.command.CommandListener;
import fr.olympa.api.spigot.frame.ImageFrameManager;
import fr.olympa.api.spigot.gui.Inventories;
import fr.olympa.api.spigot.holograms.HologramsManager;
import fr.olympa.api.spigot.hook.IProtocolSupport;
import fr.olympa.api.spigot.region.tracking.RegionManager;
import fr.olympa.core.spigot.datamanagement.DataManagmentListener;

/**
 * Version minimale du Core, faite pour fonctionner sans lien à la BDD sur des
 * serveurs tests
 */
public class OlympaCore extends OlympaSpigot {

	private static OlympaCore instance;

	public static OlympaCore getInstance() {
		return instance;
	}

	private String serverName;

	private HologramsManager holograms;
	private RegionManager regions;
	private String lastVersion = "unknown";
	private String firstVersion = "unknown";
	private OlympaServer olympaServer = OlympaServer.ALL;

	@Override
	public OlympaServer getOlympaServer() {
		return olympaServer;
	}

	@Override
	public void setOlympaServer(OlympaServer olympaServer) {
		this.olympaServer = olympaServer;
	}

	public String getLastVersion() {
		return lastVersion;
	}

	public void setLastVersion(String lastVersion) {
		this.lastVersion = lastVersion;
	}

	public String getFirstVersion() {
		return firstVersion;
	}

	public String getRangeVersion() {
		return firstVersion + " à " + lastVersion;
	}

	public void setFirstVersion(String firstVersion) {
		this.firstVersion = firstVersion;
	}

	@Override
	public IProtocolSupport getProtocolSupport() {
		return null;
	}

	@Override
	public String getServerName() {
		return serverName;
	}

	@Override
	public RegionManager getRegionManager() {
		return regions;
	}

	@Override
	public HologramsManager getHologramsManager() {
		return holograms;
	}

	@Override
	public void onDisable() {
		holograms.unload();
		sendMessage("§4" + getDescription().getName() + "§c (" + getDescription().getVersion() + ") is disabled.");
	}

	@Override
	public void onEnable() {
		instance = this;

		OlympaPermission.registerPermissions(OlympaAPIPermissionsSpigot.class);

		PluginManager pluginManager = getServer().getPluginManager();
		pluginManager.registerEvents(new Inventories(), this);
		pluginManager.registerEvents(new DataManagmentListener(), this);
		pluginManager.registerEvents(new CommandListener(), this);
		pluginManager.registerEvents(regions = new RegionManager(), this);

		try {
			pluginManager.registerEvents(holograms = new HologramsManager(this, new File(getDataFolder(), "holograms.yml")), this);
		} catch (Exception e) {
			getLogger().severe("Une erreur est survenue lors du chargement des hologrammes.");
			e.printStackTrace();
		}
		Messenger messenger = getServer().getMessenger();
		messenger.registerOutgoingPluginChannel(this, "BungeeCord");
		new SpigotBPMCEvent().register(this);

		sendMessage("§2" + getDescription().getName() + "§a (" + getDescription().getVersion() + ") is enabled.");
	}

	@Override
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	@Override
	public ImageFrameManager getImageFrameManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isServerName(String serverName) {
		return this.serverName.equals(serverName);
	}

	@Override
	public Connection getDatabase() throws SQLException {
		return null;
	}

	@Override
	public void usesPack(Player p) {}

	@Override
	public void retreiveMonitorInfos(BiConsumer<List<ServerInfoBasic>, Boolean> callback, boolean freshDoubleCallBack) {}
}
