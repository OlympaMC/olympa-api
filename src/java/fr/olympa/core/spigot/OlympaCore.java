package fr.olympa.core.spigot;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.messaging.Messenger;

import fr.olympa.api.bpmc.SpigotBPMCEvent;
import fr.olympa.api.gui.Inventories;
import fr.olympa.api.plugin.OlympaSpigot;
import fr.olympa.api.region.RegionManager;
import fr.olympa.core.spigot.datamanagement.listeners.DataManagmentListener;

/**
 * Version minimale du Core, faite pour fonctionner sans lien à la BDD sur des serveurs tests
 */
public class OlympaCore extends OlympaSpigot {

	private static OlympaCore instance;

	public static OlympaCore getInstance() {
		return instance;
	}

	private RegionManager regionManager = new RegionManager();

	@Override
	public void onDisable() {
		this.sendMessage("§4" + this.getDescription().getName() + "§c (" + this.getDescription().getVersion() + ") is disabled.");
	}

	@Override
	public void onEnable() {
		instance = this;

		PluginManager pluginManager = this.getServer().getPluginManager();
		pluginManager.registerEvents(new Inventories(), this);
		pluginManager.registerEvents(new DataManagmentListener(), this);
		pluginManager.registerEvents(regionManager, this);

		Messenger messenger = this.getServer().getMessenger();
		messenger.registerOutgoingPluginChannel(this, "BungeeCord");
		new SpigotBPMCEvent().register(this);

		this.sendMessage("§2" + this.getDescription().getName() + "§a (" + this.getDescription().getVersion() + ") is enabled.");
	}

	@Override
	public RegionManager getRegionManager() {
		return regionManager;
	}

}
