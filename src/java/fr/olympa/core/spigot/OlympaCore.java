package fr.olympa.core.spigot;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.messaging.Messenger;

import fr.olympa.api.bpmc.SpigotBPMCEvent;
import fr.olympa.api.command.CommandListener;
import fr.olympa.api.gui.Inventories;
import fr.olympa.api.hook.ProtocolAction;
import fr.olympa.api.plugin.OlympaSpigot;
import fr.olympa.api.region.tracking.RegionManager;
import fr.olympa.core.spigot.datamanagement.listeners.DataManagmentListener;

/**
 * Version minimale du Core, faite pour fonctionner sans lien à la BDD sur des
 * serveurs tests
 */
public class OlympaCore extends OlympaSpigot {
	
	private static OlympaCore instance;
	
	public static OlympaCore getInstance() {
		return instance;
	}
	
	private RegionManager regionManager;
	private String serverName;
	
	@Override
	public ProtocolAction getProtocolSupport() {
		return null;
	}
	
	@Override
	public RegionManager getRegionManager() {
		return regionManager;
	}
	
	@Override
	public String getServerName() {
		return serverName;
	}
	
	@Override
	public void onDisable() {
		sendMessage("§4" + getDescription().getName() + "§c (" + getDescription().getVersion() + ") is disabled.");
	}
	
	@Override
	public void onEnable() {
		instance = this;
		
		PluginManager pluginManager = getServer().getPluginManager();
		pluginManager.registerEvents(new Inventories(), this);
		pluginManager.registerEvents(new DataManagmentListener(), this);
		pluginManager.registerEvents(new CommandListener(), this);
		pluginManager.registerEvents(regionManager = new RegionManager(), this);
		
		Messenger messenger = getServer().getMessenger();
		messenger.registerOutgoingPluginChannel(this, "BungeeCord");
		new SpigotBPMCEvent().register(this);
		
		sendMessage("§2" + getDescription().getName() + "§a (" + getDescription().getVersion() + ") is enabled.");
	}
	
	@Override
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}
	
}
