package fr.olympa.api.scoreboard.sign;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.customevents.OlympaPlayerLoadEvent;
import fr.olympa.api.customevents.ScoreboardCreateEvent;
import fr.olympa.api.lines.AbstractLine;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;

public class ScoreboardManager<T extends OlympaPlayer> implements Listener {

	private Map<T, Scoreboard<T>> scoreboards = new HashMap<>();

	Plugin plugin;
	String displayName;
	List<AbstractLine<Scoreboard<T>>> lines = new ArrayList<>();
	List<AbstractLine<Scoreboard<T>>> footer = new ArrayList<>();

	public ScoreboardManager(Plugin plugin, String displayName) {
		this.plugin = plugin;
		this.displayName = displayName;

		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	public ScoreboardManager<T> addLines(AbstractLine<Scoreboard<T>>... lines) {
		for (AbstractLine<Scoreboard<T>> line : lines) this.lines.add(line);
		return this;
	}

	public ScoreboardManager<T> addFooters(AbstractLine<Scoreboard<T>>... lines) {
		for (AbstractLine<Scoreboard<T>> line : lines) this.footer.add(line);
		return this;
	}

	public void create(T p) {
		removePlayerScoreboard(p);
		Scoreboard<T> scoreboard = new Scoreboard<>(p, this);
		scoreboards.put(p, scoreboard);
		Bukkit.getPluginManager().callEvent(new ScoreboardCreateEvent<>(p, scoreboard, !Bukkit.isPrimaryThread()));
	}

	public Scoreboard<T> getPlayerScoreboard(T p) {
		return scoreboards.get(p);
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void onJoin(OlympaPlayerLoadEvent e) {
		create(e.getOlympaPlayer());
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		removePlayerScoreboard(AccountProvider.get(e.getPlayer().getUniqueId()));
	}

	public void removePlayerScoreboard(T p) {
		Scoreboard<T> removed = scoreboards.remove(p);
		if (removed != null) removed.unload();
	}

	public void unload() {
		HandlerList.unregisterAll(this);

		for (Iterator<Scoreboard<T>> iterator = scoreboards.values().iterator(); iterator.hasNext();) {
			Scoreboard<T> scoreboard = iterator.next();
			scoreboard.unload();
			iterator.remove();
		}
	}

}
