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
	
	public Scoreboard getPlayerScoreboard(OlympaPlayer p) {
		return scoreboards.get(p);
	}
	
	public void removePlayerScoreboard(OlympaPlayer p) {
		if (scoreboards.containsKey(p)) scoreboards.remove(p).unload();
	}
	
	public void create(OlympaPlayer p) {
		removePlayerScoreboard(p);
		scoreboards.put(p, new Scoreboard(p, this));
	}
	
	@EventHandler
	public void onJoin(OlympaPlayerLoadEvent e) {
		create(e.getOlympaPlayer());
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		removePlayerScoreboard(AccountProvider.get(e.getPlayer()));
	}

	public void unload(){
		HandlerList.unregisterAll(this);

		for (Scoreboard s : scoreboards.values()) s.unload();
		if (!scoreboards.isEmpty()) plugin.sendMessage(scoreboards.size() + " scoreboards deleted.");
		scoreboards.clear();
	}
	
}
