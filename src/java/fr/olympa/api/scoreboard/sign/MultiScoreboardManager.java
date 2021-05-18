package fr.olympa.api.scoreboard.sign;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.customevents.OlympaPlayerLoadEvent;
import fr.olympa.api.module.OlympaModule;
import fr.olympa.api.module.OlympaModule.ModuleApi;
import fr.olympa.api.module.SpigotModule;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.plugin.OlympaAPIPlugin;
import fr.olympa.api.provider.AccountProvider;

public class MultiScoreboardManager<T extends OlympaPlayer> implements Listener, ModuleApi<OlympaAPIPlugin> {

	private Map<T, MultiScoreboard<T>> scoreboards = new HashMap<>();
	private List<ScoreboardManager<T>> allScoreboardsManagers = new ArrayList<>();

	//	private List<Scoreboard<T>> getAllSb() {
	//		return scoreboards.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
	//	}

	@Override
	public boolean disable(OlympaAPIPlugin plugin) {
		if (!scoreboards.isEmpty())
			//			getAllSb().forEach(scoreboard -> scoreboard.unload());
			scoreboards.clear();
		this.plugin = null;
		return !isEnabled();
	}

	@Override
	public boolean enable(OlympaAPIPlugin plugin) {
		this.plugin = plugin;
		if (!scoreboards.isEmpty())
			//			getAllSb().forEach(scoreboard -> scoreboard.unload());
			scoreboards.clear();
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

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onJoin(OlympaPlayerLoadEvent e) {
		if (!scoreboards.containsKey(e.getOlympaPlayer()))
			allScoreboardsManagers.forEach(sm -> {
				sm.create(e.getOlympaPlayer());
				//				addPlayerScoreboard(e.getOlympaPlayer(), sm.create(e.getOlympaPlayer()));
			});
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onQuit(PlayerQuitEvent e) {
		removePlayerScoreboards(AccountProvider.get(e.getPlayer().getUniqueId()));
	}

	OlympaAPIPlugin plugin;

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

	public void addManager(ScoreboardManager<T> manager) {
		allScoreboardsManagers.add(manager);
	}

	public OlympaAPIPlugin getPlugin() {
		return plugin;
	}

	public MultiScoreboard<T> getPlayerScoreboards(T p) {
		return scoreboards.get(p);
	}

	//	public Scoreboard<T> getPlayerScoreboard(T p) {
	//		return scoreboards.get(p).stream().filter(Scoreboard::isAlive).findFirst().orElse(null);
	//	}

	public void addPlayerScoreboard(T player, Scoreboard<T> sb) {
		MultiScoreboard<T> multiSb = scoreboards.get(player);
		if (multiSb == null) {
			multiSb = new MultiScoreboard<>();
			scoreboards.put(player, multiSb);
		}
		multiSb.addSb(sb);
	}

	public void removePlayerScoreboard(Scoreboard<T> sb, T p) {
		MultiScoreboard<T> removed = scoreboards.get(p);
		if (removed != null && removed.containsSb(sb)) {
			sb.unload();
			removed.removeSb(sb);
		}
	}

	public void unloadPlayerScoreboard(Scoreboard<T> sb, T p) {
		MultiScoreboard<T> removed = scoreboards.get(p);
		if (removed != null && removed.containsSb(sb))
			sb.unload();
	}

	public void removePlayerScoreboards(T p) {
		MultiScoreboard<T> removed = scoreboards.get(p);
		if (removed != null) {
			removed.scoreboards.forEach(sb -> sb.unload());
			scoreboards.remove(p);
		}
	}

	public void unloadScoreboards(T p) {
		MultiScoreboard<T> removed = scoreboards.get(p);
		if (removed != null)
			removed.scoreboards.forEach(sb -> sb.unload());
	}

	public void unload() {
		HandlerList.unregisterAll(this);

		//		for (Iterator<Scoreboard<T>> iterator = getAllSb().iterator(); iterator.hasNext();) {
		//			Scoreboard<T> scoreboard = iterator.next();
		//			scoreboard.unload();
		//			iterator.remove();
		//		}
	}

}
