package fr.olympa.api.scoreboard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.olympa.api.customevents.OlympaPlayerLoadEvent;
import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.plugin.OlympaPlugin;
import fr.olympa.api.provider.AccountProvider;

public class ScoreboardManager implements Listener {

	private Map<OlympaPlayer, Scoreboard> scoreboards = new HashMap<>();

	OlympaPlugin plugin;
	String scoreboardsName;
	List<ScoreboardLine> lines;

	public ScoreboardManager(OlympaPlugin plugin, String scoreboardsName, List<ScoreboardLine> lines) {
		this.plugin = plugin;
		this.scoreboardsName = scoreboardsName;
		this.lines = lines;

		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	public void create(OlympaPlayer p) {
		this.removePlayerScoreboard(p);
		this.scoreboards.put(p, new Scoreboard(p, this));
	}

	public Scoreboard getPlayerScoreboard(OlympaPlayer p) {
		return this.scoreboards.get(p);
	}

	@EventHandler
	public void onJoin(OlympaPlayerLoadEvent e) {
		this.create(e.getOlympaPlayer());
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		this.removePlayerScoreboard(AccountProvider.get(e.getPlayer().getUniqueId()));
	}

	public void removePlayerScoreboard(OlympaPlayer p) {
		if (this.scoreboards.containsKey(p)) {
			this.scoreboards.remove(p).unload();
		}
	}

	public void unload() {
		HandlerList.unregisterAll(this);

		for (Scoreboard s : this.scoreboards.values()) {
			s.unload();
		}
		if (!this.scoreboards.isEmpty()) {
			this.plugin.sendMessage(this.scoreboards.size() + " scoreboards deleted.");
		}
		this.scoreboards.clear();
	}

}
