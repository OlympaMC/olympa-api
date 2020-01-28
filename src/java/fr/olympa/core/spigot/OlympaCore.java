package fr.olympa.core.spigot;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.messaging.Messenger;

import fr.olympa.api.bpmc.SpigotBPMCEvent;
import fr.olympa.api.gui.Inventories;
import fr.olympa.api.plugin.OlympaAPIPlugin;

// Ceci est un faux ficher qui récupère quelques donnés du Core dans l'API
public class OlympaCore extends OlympaAPIPlugin {

	private static OlympaCore instance;

	public static OlympaCore getInstance() {
		return instance;
	}

	@Override
	public void onDisable() {
		this.sendMessage("§4" + this.getDescription().getName() + "§c (" + this.getDescription().getVersion() + ") is disabled.");
	}

	@Override
	public void onEnable() {
		instance = this;

		PluginManager pluginManager = this.getServer().getPluginManager();
		pluginManager.registerEvents(new Inventories(), this);

		Messenger messenger = this.getServer().getMessenger();
		messenger.registerOutgoingPluginChannel(this, "BungeeCord");
		new SpigotBPMCEvent().register(this);

		this.sendMessage("§2" + this.getDescription().getName() + "§a (" + this.getDescription().getVersion() + ") is activated.");
	}
}
