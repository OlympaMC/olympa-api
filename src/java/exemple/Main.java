package exemple;

import org.bukkit.plugin.PluginManager;

import fr.olympa.api.gui.Inventories;
import fr.olympa.api.plugin.OlympaPlugin;

public class Main extends OlympaPlugin {

	public static Main getInstance() {
		return (Main) instance;
	}

	@Override
	public void onDisable() {
		this.disable();
		this.sendMessage("§4" + this.getDescription().getName() + "§c (" + this.getDescription().getVersion() + ") is disabled.");
	}

	@Override
	public void onEnable() {
		this.enable(this);
		instance = this;
		new ExempleCommand(this).register();

		final PluginManager pluginManager = this.getServer().getPluginManager();
		pluginManager.registerEvents(new ExempleListener(), this);
		pluginManager.registerEvents(new SmallDataManagmentListener(), this);
		pluginManager.registerEvents(new Inventories(), this);

		this.sendMessage("§2" + this.getDescription().getName() + "§a (" + this.getDescription().getVersion() + ") is activated.");
	}

}
