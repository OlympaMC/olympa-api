package exemple;

import org.bukkit.Location;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.PluginManager;

import fr.olympa.api.gui.Inventories;
import fr.olympa.api.lines.FixedLine;
import fr.olympa.api.lines.TimerLine;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.plugin.OlympaAPIPlugin;
import fr.olympa.api.report.ReportReason;
import fr.olympa.api.scoreboard.sign.Scoreboard;
import fr.olympa.api.scoreboard.sign.ScoreboardManager;
import fr.olympa.core.spigot.OlympaCore;

public class Main extends OlympaAPIPlugin {

	private static Main instance;

	public static Main getInstance() {
		return instance;
	}

	private ScoreboardManager<OlympaPlayer> scoreboards;

	@Override
	public void onDisable() {
		scoreboards.unload();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onEnable() {
		OlympaPermission.registerPermissions(ExemplePermissions.class);
		ReportReason.registerReason(ExempleReportReasonCustom.class);

		instance = this;

		new ExempleCommand(this).register();
		new ExampleComplexCommand(this).register();
		try {
			scoreboards = new ScoreboardManager<>(this, "Exemple scoreboard").addLines(
					new FixedLine<>("Yo"),
					FixedLine.EMPTY_LINE,
					new TimerLine<Scoreboard<OlympaPlayer>>((x) -> {
						Location lc = x.getOlympaPlayer().getPlayer().getLocation();
						return lc.toString();
					}, this, 5));
		} catch (Exception e) {
			e.printStackTrace();
		}

		final PluginManager pluginManager = getServer().getPluginManager();
		pluginManager.registerEvents(new ExempleListener(), this);
		pluginManager.registerEvents(new SmallDataManagmentListener(), this);
		pluginManager.registerEvents(new Inventories(), this);

		OlympaCore.getInstance().getNameTagApi().addNametagHandler(EventPriority.HIGH, (nametag, player, to) -> {
			nametag.appendPrefix("GROS BG");
			nametag.appendSuffix("(salut " + to.getName() + ")");
		});
	}
}
