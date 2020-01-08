package fr.olympa;

import fr.olympa.api.plugin.OlympaPlugin;

// Ceci est un faux ficher pour récupérer l'instance du Core
public class OlympaCore extends OlympaPlugin {

	private static OlympaCore instance;

	public static OlympaCore getInstance() {
		return instance;
	}

	/*@Override
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
	}*/
}
