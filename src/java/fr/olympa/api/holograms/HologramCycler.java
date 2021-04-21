package fr.olympa.api.holograms;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class HologramCycler {
	
	private final Plugin plugin;
	private final List<Hologram> holograms;
	private final int delay;
	
	private BukkitTask task;
	private int shown = 0;
	
	public HologramCycler(Plugin plugin, List<Hologram> holograms, int delay) {
		this.plugin = plugin;
		this.holograms = holograms;
		this.delay = delay;
	}
	
	public void start() {
		if (task == null || task.isCancelled()) {
			shown = 0;
			for (int i = 1; i < holograms.size(); i++) {
				holograms.get(i).hide();
			}
			task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
				holograms.get(shown).hide();
				if (++shown >= holograms.size()) shown = 0;
				holograms.get(shown).show();
			}, delay, delay);
		}
	}
	
	public void stop() {
		if (task != null && !task.isCancelled()) {
			task.cancel();
			task = null;
		}
	}
	
}
