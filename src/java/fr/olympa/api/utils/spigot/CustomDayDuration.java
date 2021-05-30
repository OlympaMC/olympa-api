package fr.olympa.api.utils.spigot;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.world.TimeSkipEvent;
import org.bukkit.event.world.TimeSkipEvent.SkipReason;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class CustomDayDuration implements Listener {
	
	public static final int DEFAULT_DAY_DURATION = 12000;
	public static final int DEFAULT_NIGHT_DURATION = 12000;
	
	public static final int NIGHT_TIME = 12000;
	
	private BukkitTask task;
	
	private double nextTick = -1;
	
	public CustomDayDuration(Plugin plugin, World world, int dayDuration, int nightDuration, double tolerance) {
		
		double dayMultiplier = (double) DEFAULT_DAY_DURATION / (double) dayDuration;
		double nightMultiplier = (double) DEFAULT_NIGHT_DURATION / (double) nightDuration;
		
		//world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
		task = Bukkit.getScheduler().runTaskTimer/*Asynchronously*/(plugin, () -> {
			double time = world.getFullTime();
			double mult = world.getTime() >= NIGHT_TIME ? nightMultiplier : dayMultiplier;
			if (mult == 1) return;
			if (nextTick != -1) {
				if (tolerance == 0 ? (Math.floor(time) != Math.floor(nextTick)) : Math.abs(time - nextTick) >= tolerance) world.setFullTime((long) nextTick);
				time = nextTick;
			}
			nextTick = time + mult;
		}, 1, 1);
		
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	public void unload() {
		if (task != null) {
			task.cancel();
			task = null;
		}
		HandlerList.unregisterAll(this);
	}
	
	@EventHandler
	public void onTimeChange(TimeSkipEvent e) {
		if (e.getSkipReason() != SkipReason.CUSTOM) nextTick = -1;
	}
	
}
