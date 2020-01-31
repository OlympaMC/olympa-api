package fr.olympa.api.frame;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class FastSendTask extends BukkitRunnable implements Listener {
	@Deprecated
	private Map<UUID, Queue<Integer>> status = new HashMap<>();
	private final ImageMaps plugin;
	private final int mapsPerRun;

	public FastSendTask(ImageMaps plugin, int mapsPerSend) {
		this.plugin = plugin;
		this.mapsPerRun = mapsPerSend;
	}

	@Deprecated
	public void addToQueue(int mapId) {
		for (Queue<Integer> queue : this.status.values()) {
			queue.add(mapId);
		}
	}

	private Queue<Integer> getStatus(Player p) {
		if (!this.status.containsKey(p.getUniqueId())) {
			this.status.put(p.getUniqueId(), new LinkedList<>(this.plugin.getFastSendList()));
		}

		return this.status.get(p.getUniqueId());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	@Deprecated
	public void onPlayerJoin(PlayerJoinEvent e) {
		this.status.put(e.getPlayer().getUniqueId(), new LinkedList<>(this.plugin.getFastSendList()));
	}

	@EventHandler(priority = EventPriority.MONITOR)
	@Deprecated
	public void onPlayerQuit(PlayerQuitEvent e) {
		this.status.remove(e.getPlayer().getUniqueId());
	}

	@SuppressWarnings("deprecation")
	@Override
	public void run() {
		if (this.plugin.getFastSendList().isEmpty()) {
			return;
		}

		for (Player p : this.plugin.plugin.getServer().getOnlinePlayers()) {
			Queue<Integer> state = this.getStatus(p);

			for (int i = 0; i < this.mapsPerRun && !state.isEmpty(); i++) {
				p.sendMap(this.plugin.plugin.getServer().getMap(state.poll()));
			}
		}
	}

}
