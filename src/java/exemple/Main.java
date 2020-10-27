package exemple;

import org.bukkit.Location;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import fr.olympa.api.gui.Inventories;
import fr.olympa.api.lines.FixedLine;
import fr.olympa.api.lines.TimerLine;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.scoreboard.sign.Scoreboard;
import fr.olympa.api.scoreboard.sign.ScoreboardManager;
import fr.olympa.core.spigot.OlympaCore;

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
		OlympaPermission.registerPermissions(ExemplePermissions.class);

		instance = this;

		new ExempleCommand(this).register();
		new ExampleComplexCommand(this).register();

		this.scoreboards = new ScoreboardManager<>(this, "Exemple scoreboard").addLines(
				new FixedLine<>("Yo"),
				FixedLine.EMPTY_LINE,
				new TimerLine<Scoreboard<OlympaPlayer>>((x) -> {
					Location lc = x.getOlympaPlayer().getPlayer().getLocation();
					return lc.toString();
				}, this, 5));

		final PluginManager pluginManager = this.getServer().getPluginManager();
		pluginManager.registerEvents(new ExempleListener(), this);
		pluginManager.registerEvents(new SmallDataManagmentListener(), this);
		pluginManager.registerEvents(new Inventories(), this);
		
		OlympaCore.getInstance().getNameTagApi().addNametagHandler(EventPriority.HIGH, (nametag, player, to) -> {
			nametag.appendPrefix("GROS BG");
			nametag.appendSuffix("(salut " + to.getName() + ")");
		});
	}
}
