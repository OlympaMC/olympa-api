package exemple;

import java.util.Arrays;

import org.bukkit.plugin.PluginManager;

import fr.olympa.api.gui.Inventories;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.plugin.OlympaPlugin;
import fr.olympa.api.scoreboard.FixedLine;
import fr.olympa.api.scoreboard.ScoreboardManager;

public class Main extends OlympaPlugin {

	private static Main instance;
	public static Main getInstance() {
		return (Main) instance;
	}

	private ScoreboardManager scoreboards;

	@Override
	public void onEnable() {
		OlympaPermission.registerPermissions(ExemplePermissions.class);
		
		instance = this;
		super.onEnable();
		
		new ExempleCommand(this).register();
		new ExampleComplexCommand(this).register();
		
		scoreboards = new ScoreboardManager(this, "Exemple scoreboard", Arrays.asList(new FixedLine("Yo"), new FixedLine("ligne très très longue qui sera coupée en deux tous les 15 caractères", 15)));

		final PluginManager pluginManager = this.getServer().getPluginManager();
		pluginManager.registerEvents(new ExempleListener(), this);
		pluginManager.registerEvents(new SmallDataManagmentListener(), this);
		pluginManager.registerEvents(new Inventories(), this);

		this.sendMessage("§2" + this.getDescription().getName() + "§a (" + this.getDescription().getVersion() + ") is activated.");
	}

	@Override
	public void onDisable() {
		this.disable();

		scoreboards.unload();

		this.sendMessage("§4" + this.getDescription().getName() + "§c (" + this.getDescription().getVersion() + ") is disabled.");
	}

}
