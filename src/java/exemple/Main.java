package exemple;

import org.bukkit.plugin.PluginManager;

import fr.olympa.api.plugin.OlympaPlugin;

public class Main extends OlympaPlugin {

	public static Main getInstance() {
		return (Main) instance;
	}

	@Override
	public void onDisable() {

	}

	@Override
	public void onEnable() {
		instance = this;
		new ExempleCommand(this).register();

		final PluginManager pluginManager = this.getServer().getPluginManager();
		pluginManager.registerEvents(new ExempleListener(), this);
	}

}
