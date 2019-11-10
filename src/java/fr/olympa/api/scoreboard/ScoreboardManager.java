package fr.olympa.api.scoreboard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.olympa.api.plugin.OlympaPlugin;

public class ScoreboardManager implements Listener {
	
	private Map<Player, Scoreboard> scoreboards = new HashMap<>();
	
	OlympaPlugin plugin;
	String scoreboardsName;
	List<ScoreboardLine> lines;

	public ScoreboardManager(OlympaPlugin plugin, String scoreboardsName, List<ScoreboardLine> lines) {
		this.plugin = plugin;
		this.scoreboardsName = scoreboardsName;
		this.lines = lines;

		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	public Scoreboard getPlayerScoreboard(Player p){
		return scoreboards.get(p);
	}
	
	public void removePlayerScoreboard(Player p){
		if (scoreboards.containsKey(p)) scoreboards.remove(p).unload();
	}
	
	public void create(Player p){
		removePlayerScoreboard(p);
		scoreboards.put(p, new Scoreboard(p, this));
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		create(e.getPlayer());
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		removePlayerScoreboard(e.getPlayer());
	}

	public void unload(){
		HandlerList.unregisterAll(this);

		for (Scoreboard s : scoreboards.values()) s.unload();
		if (!scoreboards.isEmpty()) plugin.sendMessage(scoreboards.size() + " scoreboards deleted.");
		scoreboards.clear();
	}
	
}
