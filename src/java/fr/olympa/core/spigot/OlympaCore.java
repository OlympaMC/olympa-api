package fr.olympa.core.spigot;

import java.sql.Connection;
import java.sql.SQLException;

import org.bukkit.plugin.PluginManager;

import fr.olympa.api.gui.Inventories;
import fr.olympa.api.plugin.OlympaAPIPlugin;

// Ceci est un faux ficher qui récupère quelques donnés du Core dans l'API
public class OlympaCore extends OlympaAPIPlugin {

	private static OlympaCore instance;

	public static OlympaCore getInstance() {
		return instance;
	}

	@Override
	public Connection getDatabase() throws SQLException {
		return null;
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
		// pluginManager.registerEvents(new DataManagmentListener(), this);

		this.sendMessage("§2" + this.getDescription().getName() + "§a (" + this.getDescription().getVersion() + ") is activated.");
	}
}
