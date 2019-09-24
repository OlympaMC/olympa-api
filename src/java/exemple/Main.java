package exemple;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

	private static Plugin instance;

	public static Plugin getInstance() {
		return instance;
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
