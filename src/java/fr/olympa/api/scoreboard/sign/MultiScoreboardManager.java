package fr.olympa.api.scoreboard.sign;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.customevents.OlympaPlayerLoadEvent;
import fr.olympa.api.lines.AbstractLine;
import fr.olympa.api.module.OlympaModule;
import fr.olympa.api.module.OlympaModule.ModuleApi;
import fr.olympa.api.module.SpigotModule;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.plugin.OlympaAPIPlugin;
import fr.olympa.api.provider.AccountProvider;

public class MultiScoreboardManager<T extends OlympaPlayer> implements Listener, ModuleApi<OlympaAPIPlugin> {

	private Map<T, List<MultiScoreboard<T>>> scoreboards = new HashMap<>();
	private List<MultiScoreboard<T>> scoreboardsType = new ArrayList<>();

	private List<MultiScoreboard<T>> getAllSb() {
		return scoreboards.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
	}

	@Override
	public boolean disable(OlympaAPIPlugin plugin) {
		if (!scoreboards.isEmpty()) {
			getAllSb().forEach(scoreboard -> scoreboard.unload());
			scoreboards.clear();
		}
		this.plugin = null;
		return !isEnabled();
	}

	@Override
	public boolean enable(OlympaAPIPlugin plugin) {
		this.plugin = plugin;
		if (!scoreboards.isEmpty()) {
			getAllSb().forEach(scoreboard -> scoreboard.unload());
			scoreboards.clear();
		}
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
	boolean isDefaultScoreboard = true;
	List<AbstractLine<MultiScoreboard<T>>> lines = new ArrayList<>();
	List<AbstractLine<MultiScoreboard<T>>> footer = new ArrayList<>();

	public MultiScoreboardManager(OlympaAPIPlugin pl) {
		OlympaModule<MultiScoreboardManager<T>, Listener, OlympaAPIPlugin, OlympaCommand> scoreBoardModule = new SpigotModule<>(pl, "scoreboard_" + pl.getName(),
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

	public MultiScoreboardManager<T> addLines(AbstractLine<MultiScoreboard<T>>... lines) {
		for (AbstractLine<MultiScoreboard<T>> line : lines)
			this.lines.add(line);
		return this;
	}

	public MultiScoreboardManager<T> addFooters(AbstractLine<MultiScoreboard<T>>... lines) {
		for (AbstractLine<MultiScoreboard<T>> line : lines)
			this.footer.add(line);
		return this;
	}

	public void create(T p, String displayName) {
		MultiScoreboard<T> scoreboard = new MultiScoreboard<>(p, this, displayName);
		List<MultiScoreboard<T>> list = scoreboards.get(p);
		if (list == null) {
			list = new ArrayList<>();
			scoreboards.put(p, list);
		}
		list.add(scoreboard);
		//		Bukkit.getPluginManager().callEvent(new ScoreboardCreateEvent<>(p, scoreboard, !Bukkit.isPrimaryThread()));
	}

	public void show(MultiScoreboard<T> sb, T p) {
		sb.initScoreboard();
		unloadScoreboards(p);
	}

	public List<MultiScoreboard<T>> getPlayerScoreboards(T p) {
		return scoreboards.get(p);
	}

	public MultiScoreboard<T> getPlayerScoreboard(T p) {
		return scoreboards.get(p).stream().filter(MultiScoreboard::isAlive).findFirst().orElse(null);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onJoin(OlympaPlayerLoadEvent e) {
		if (isDefaultScoreboard && !scoreboards.containsKey(e.getOlympaPlayer()))
			create(e.getOlympaPlayer());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onQuit(PlayerQuitEvent e) {
		removePlayerScoreboards(AccountProvider.get(e.getPlayer().getUniqueId()));
	}

	public void removePlayerScoreboard(MultiScoreboard<T> sb, T p) {
		List<MultiScoreboard<T>> removed = scoreboards.get(p);
		if (removed != null && removed.contains(sb)) {
			sb.unload();
			removed.remove(sb);
		}
	}

	public void unloadPlayerScoreboard(MultiScoreboard<T> sb, T p) {
		List<MultiScoreboard<T>> removed = scoreboards.get(p);
		if (removed != null && removed.contains(sb))
			sb.unload();
	}

	public void removePlayerScoreboards(T p) {
		List<MultiScoreboard<T>> removed = scoreboards.get(p);
		if (removed != null) {
			removed.forEach(sb -> sb.unload());
			scoreboards.remove(p);
		}
	}

	public void unloadScoreboards(T p) {
		List<MultiScoreboard<T>> removed = scoreboards.get(p);
		if (removed != null)
			removed.forEach(sb -> sb.unload());
	}

	public void unload() {
		HandlerList.unregisterAll(this);

		for (Iterator<MultiScoreboard<T>> iterator = getAllSb().iterator(); iterator.hasNext();) {
			MultiScoreboard<T> scoreboard = iterator.next();
			scoreboard.unload();
			iterator.remove();
		}
	}

}
