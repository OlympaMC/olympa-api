package exemple;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import fr.tristiisch.olympa.api.objects.OlympaPlugin;
import fr.tristiisch.olympa.api.task.TaskManager;

public class Main extends JavaPlugin implements OlympaPlugin{

	private static Plugin instance;
	
	private TaskManager taskManager = new TaskManager(this);

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

	public TaskManager getTaskManager(){
		return taskManager;
	}
	
}
