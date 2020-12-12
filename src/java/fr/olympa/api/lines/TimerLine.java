package fr.olympa.api.lines;

import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class TimerLine<T extends LinesHolder<T>> extends DynamicLine<T> {

	private final Plugin plugin;
	private final int ticks;
	
	private BukkitTask task = null;
	
	public TimerLine(Function<T, String> value, Plugin plugin, int ticks) {
		super(value);
		this.plugin = plugin;
		this.ticks = ticks;
	}
	
	@Override
	public synchronized void addHolder(T holder) {
		super.addHolder(holder);
		if (task == null) {
			System.out.println("TimerLine.addHolder() add task");
			task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::updateGlobal, 1, ticks);
		}
	}

	@Override
	public synchronized void removeHolder(T holder) {
		super.removeHolder(holder);
		if (getHolders().isEmpty() && task != null) {
			task.cancel();
			task = null;
		}
	}
	
}
