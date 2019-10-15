package fr.olympa;

import org.bukkit.plugin.PluginManager;

import fr.olympa.api.plugin.OlympaPlugin;

// Use to getInstance in API ONLY
public class OlympaCore extends OlympaPlugin {

	public static OlympaCore getInstance() {
		return (OlympaCore) instance;
	}

	@Override
	public void onDisable() {
		this.sendMessage("§4" + this.getDescription().getName() + "§c (" + this.getDescription().getVersion() + ") is disabled.");
	}

	@Override
	public void onEnable() {
		final PluginManager pluginManager = this.getServer().getPluginManager();
		// pluginManager.registerEvents(new DataManagmentListener(), this);

		this.sendMessage("§2" + this.getDescription().getName() + "§a (" + this.getDescription().getVersion() + ") is activated.");
	}
}
