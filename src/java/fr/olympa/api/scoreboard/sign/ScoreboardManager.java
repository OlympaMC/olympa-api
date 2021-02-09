package fr.olympa.api.scoreboard.sign;

import java.util.ArrayList;
import java.util.Arrays;
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

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.customevents.OlympaPlayerLoadEvent;
import fr.olympa.api.customevents.ScoreboardCreateEvent;
import fr.olympa.api.lines.AbstractLine;
import fr.olympa.api.module.OlympaModule;
import fr.olympa.api.module.OlympaModule.ModuleApi;
import fr.olympa.api.module.PluginModule;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.plugin.OlympaAPIPlugin;
import fr.olympa.api.provider.AccountProvider;

public class ScoreboardManager<T extends OlympaPlayer> implements Listener, ModuleApi<OlympaAPIPlugin> {

	private Map<T, Scoreboard<T>> scoreboards = new HashMap<>();

	@Override
	public boolean disable(OlympaAPIPlugin plugin) {
		this.plugin = null;
		if (!scoreboards.isEmpty()) {
			scoreboards.forEach((op, scoreboard) -> scoreboard.unload());
			scoreboards.clear();
		}
		return !isEnabled();
	}

	@Override
	public boolean enable(OlympaAPIPlugin plugin) {
		this.plugin = plugin;
		if (!scoreboards.isEmpty()) {
			scoreboards.forEach((op, scoreboard) -> scoreboard.unload());
			scoreboards.clear();
		}
		return isEnabled();
	}

	@Override
	public boolean isEnabled() {
		return plugin != null;
	}

	OlympaAPIPlugin plugin;
	String displayName;
	List<AbstractLine<Scoreboard<T>>> lines = new ArrayList<>();
	List<AbstractLine<Scoreboard<T>>> footer = new ArrayList<>();

	public ScoreboardManager(OlympaAPIPlugin pl, String displayName) {
		this.displayName = displayName;
		OlympaModule<ScoreboardManager<T>, Listener, OlympaAPIPlugin, OlympaCommand> scoreBoardModule = new OlympaModule<>(pl, "scoreboard_" + pl.getName(),
				plugin -> this, null, Arrays.asList(this.getClass()), null);
		PluginModule.enableModule(scoreBoardModule);
		PluginModule.addModule(scoreBoardModule);
	}

	public OlympaAPIPlugin getPlugin() {
		return plugin;
	}

	public ScoreboardManager<T> addLines(AbstractLine<Scoreboard<T>>... lines) {
		for (AbstractLine<Scoreboard<T>> line : lines)
			this.lines.add(line);
		return this;
	}

	public ScoreboardManager<T> addFooters(AbstractLine<Scoreboard<T>>... lines) {
		for (AbstractLine<Scoreboard<T>> line : lines)
			this.footer.add(line);
		return this;
	}

	public void create(T p) {
		removePlayerScoreboard(p);
		Scoreboard<T> scoreboard = new Scoreboard<>(p, this);
		Bukkit.getPluginManager().callEvent(new ScoreboardCreateEvent<>(p, scoreboard, !Bukkit.isPrimaryThread()));
		scoreboards.put(p, scoreboard);
	}

	public Scoreboard<T> getPlayerScoreboard(T p) {
		return scoreboards.get(p);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onJoin(OlympaPlayerLoadEvent e) {
		if (!scoreboards.containsKey(e.getOlympaPlayer()))
			create(e.getOlympaPlayer());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onQuit(PlayerQuitEvent e) {
		removePlayerScoreboard(AccountProvider.get(e.getPlayer().getUniqueId()));
	}

	public void removePlayerScoreboard(T p) {
		Scoreboard<T> removed = scoreboards.remove(p);
		if (removed != null)
			removed.unload();
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
