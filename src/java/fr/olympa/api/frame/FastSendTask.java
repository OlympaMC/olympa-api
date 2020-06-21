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
	private final ImageFrameManager manager;
	private final int mapsPerRun;
	
	public FastSendTask(ImageFrameManager manager, int mapsPerSend) {
		this.manager = manager;
		mapsPerRun = mapsPerSend;
	}
	
	@Deprecated
	public void addToQueue(int mapId) {
		for (Queue<Integer> queue : status.values())
			queue.add(mapId);
	}
	
	private Queue<Integer> getStatus(Player p) {
		if (!status.containsKey(p.getUniqueId()))
			status.put(p.getUniqueId(), new LinkedList<>(manager.getFastSendList()));
		
		return status.get(p.getUniqueId());
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	@Deprecated
	public void onPlayerJoin(PlayerJoinEvent e) {
		status.put(e.getPlayer().getUniqueId(), new LinkedList<>(manager.getFastSendList()));
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	@Deprecated
	public void onPlayerQuit(PlayerQuitEvent e) {
		status.remove(e.getPlayer().getUniqueId());
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void run() {
		if (manager.getFastSendList().isEmpty())
			return;
		
		for (Player p : manager.plugin.getServer().getOnlinePlayers()) {
			Queue<Integer> state = getStatus(p);
			
			for (int i = 0; i < mapsPerRun && !state.isEmpty(); i++)
				p.sendMap(manager.plugin.getServer().getMap(state.poll()));
		}
	}
	
}
