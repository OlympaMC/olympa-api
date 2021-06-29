package exemple;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.PluginManager;

import fr.olympa.api.common.permission.OlympaPermission;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.common.plugin.OlympaAPIPlugin;
import fr.olympa.api.common.report.ReportReason;
import fr.olympa.api.spigot.gui.Inventories;
import fr.olympa.api.spigot.lines.FixedLine;
import fr.olympa.api.spigot.lines.TimerLine;
import fr.olympa.api.spigot.scoreboard.sign.Scoreboard;
import fr.olympa.api.spigot.scoreboard.sign.ScoreboardManager;
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
						Location lc = ((Player) x.getOlympaPlayer().getPlayer()).getLocation();
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
