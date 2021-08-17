package fr.olympa.api.spigot.scoreboard.sign;

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

import fr.olympa.api.common.module.OlympaModule;
import fr.olympa.api.common.module.OlympaModule.ModuleApi;
import fr.olympa.api.common.module.SpigotModule;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.common.plugin.OlympaAPIPlugin;
import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.api.spigot.command.OlympaCommand;
import fr.olympa.api.spigot.customevents.OlympaPlayerLoadEvent;
import fr.olympa.api.spigot.customevents.ScoreboardCreateEvent;
import fr.olympa.api.spigot.customevents.ScoreboardCreateEvent.Reason;
import fr.olympa.api.spigot.lines.AbstractLine;
import fr.olympa.api.utils.CacheStats;

public class ScoreboardManager<T extends OlympaPlayer> implements Listener, ModuleApi<OlympaAPIPlugin> {

	private Map<T, Scoreboard<T>> scoreboards = new HashMap<>();

	@Override
	public boolean disable(OlympaAPIPlugin plugin) {
		if (!scoreboards.isEmpty()) {
			scoreboards.forEach((op, scoreboard) -> scoreboard.unload());
			scoreboards.clear();
		}
		this.plugin = null;
		return !isEnabled();
	}

	@Override
	public boolean enable(OlympaAPIPlugin plugin) {
		this.plugin = plugin;
		if (!scoreboards.isEmpty()) {
			scoreboards.forEach((op, scoreboard) -> scoreboard.unload());
			scoreboards.clear();
		}
		CacheStats.addDebugMap("scoreboardSign", scoreboards);
		return isEnabled();
	}

	@Override
	public boolean isEnabled() {
		return plugin != null;
	}

	@Override
	public boolean setToPlugin(OlympaAPIPlugin plugin) {
		return true;
	}

	OlympaAPIPlugin plugin;
	String displayName;
	List<AbstractLine<Scoreboard<T>>> lines = new ArrayList<>();
	List<AbstractLine<Scoreboard<T>>> footer = new ArrayList<>();

	public ScoreboardManager(OlympaAPIPlugin pl, String displayName) {
		this.displayName = displayName;
		OlympaModule<ScoreboardManager<T>, Listener, OlympaAPIPlugin, OlympaCommand> scoreBoardModule = new SpigotModule<>(pl, "scoreboard_" + pl.getName(),
				p -> this).listener(this.getClass());
		try {
			scoreBoardModule.enableModule();
		} catch (Exception e) {
			e.printStackTrace();
		}
		scoreBoardModule.registerModule();
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

	private void create(T p, ScoreboardCreateEvent.Reason reason) {
		Scoreboard<T> scoreboard = new Scoreboard<>(p, this);
		scoreboards.put(p, scoreboard);
		Bukkit.getPluginManager().callEvent(new ScoreboardCreateEvent<>(p, scoreboard, !Bukkit.isPrimaryThread(), reason));
		scoreboard.start();
	}

	public void refresh(T p) {
		if (removePlayerScoreboard(p))
			create(p, Reason.RESET);
	}

	public Scoreboard<T> getPlayerScoreboard(T p) {
		return scoreboards.get(p);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onJoin(OlympaPlayerLoadEvent e) {
		if (!scoreboards.containsKey(e.getOlympaPlayer()))
			create(e.getOlympaPlayer(), Reason.JOIN);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onQuit(PlayerQuitEvent e) {
		removePlayerScoreboard(AccountProviderAPI.getter().get(e.getPlayer().getUniqueId()));
	}

	public boolean removePlayerScoreboard(T p) {
		Scoreboard<T> removed = scoreboards.remove(p);
		if (removed != null) {
			removed.unload();
			return true;
		}
		return false;
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
