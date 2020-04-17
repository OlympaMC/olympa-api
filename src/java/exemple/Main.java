package exemple;

import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import fr.olympa.api.gui.Inventories;
import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.scoreboard.DynamicLine;
import fr.olympa.api.scoreboard.FixedLine;
import fr.olympa.api.scoreboard.ScoreboardManager;

public class Main extends JavaPlugin {

	private static Main instance;

	public static Main getInstance() {
		return instance;
	}

	private ScoreboardManager scoreboards;

	@Override
	public void onDisable() {
		this.scoreboards.unload();
	}

	@Override
	public void onEnable() {
		OlympaPermission.registerPermissions(OlympaAPIPermission.class);

		instance = this;

		new ExempleCommand(this).register();
		new ExampleComplexCommand(this).register();

		this.scoreboards = new ScoreboardManager(this, "Exemple scoreboard", Arrays.asList(
				new FixedLine("Yo"),
				new FixedLine("ligne très très longue qui sera coupée en deux tous les 15 caractères", 15),
				FixedLine.EMPTY_LINE,
				new DynamicLine<OlympaPlayer>((x) -> {
					Location lc = x.getPlayer().getLocation();
					return lc.toString();
				}, 1, 0)));

		final PluginManager pluginManager = this.getServer().getPluginManager();
		pluginManager.registerEvents(new ExempleListener(), this);
		pluginManager.registerEvents(new SmallDataManagmentListener(), this);
		pluginManager.registerEvents(new Inventories(), this);
	}
}
