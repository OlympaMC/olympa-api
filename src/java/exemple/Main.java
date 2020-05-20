package exemple;

import org.bukkit.Location;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import fr.olympa.api.gui.Inventories;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.scoreboard.sign.DynamicLine;
import fr.olympa.api.scoreboard.sign.FixedLine;
import fr.olympa.api.scoreboard.sign.ScoreboardManager;

public class Main extends JavaPlugin {

	private static Main instance;

	public static Main getInstance() {
		return instance;
	}

	private ScoreboardManager<OlympaPlayer> scoreboards;

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

		this.scoreboards = new ScoreboardManager<>(this, "Exemple scoreboard").addLines(
				new FixedLine<>("Yo"),
				FixedLine.EMPTY_LINE,
				new DynamicLine<OlympaPlayer>((x) -> {
					Location lc = x.getPlayer().getLocation();
					return lc.toString();
				}));

		final PluginManager pluginManager = this.getServer().getPluginManager();
		pluginManager.registerEvents(new ExempleListener(), this);
		pluginManager.registerEvents(new SmallDataManagmentListener(), this);
		pluginManager.registerEvents(new Inventories(), this);
	}
}
