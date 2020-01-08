package fr.olympa.core.spigot;

import org.bukkit.plugin.PluginManager;

import fr.olympa.api.gui.Inventories;
import fr.olympa.api.plugin.OlympaPlugin;

// Ceci est un faux ficher qui récupère quelques donnés du Core dans l'API
public class OlympaCore extends OlympaPlugin {

	private static OlympaCore instance;
	public static OlympaCore getInstance() {
		return (OlympaCore) instance;
	}

	@Override
	public void onEnable() {
		instance = this;
		super.onEnable();

		PluginManager pluginManager = this.getServer().getPluginManager();
		pluginManager.registerEvents(new Inventories(), this);
		// pluginManager.registerEvents(new DataManagmentListener(), this);

		this.sendMessage("§2" + this.getDescription().getName() + "§a (" + this.getDescription().getVersion() + ") is activated.");
	}

	@Override
	public void onDisable() {
		this.disable();
		this.sendMessage("§4" + this.getDescription().getName() + "§c (" + this.getDescription().getVersion() + ") is disabled.");
	}
}
