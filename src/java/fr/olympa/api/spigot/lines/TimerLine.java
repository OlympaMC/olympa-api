package fr.olympa.api.spigot.lines;

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
	
	protected void execute() {
		super.updateGlobal();
	}
	
	@Override
	public synchronized void addHolder(T holder) {
		super.addHolder(holder);
		if (task == null) {
			//System.out.println("TimerLine.addHolder() add task");
			task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::execute, 1, ticks);
		}
	}

	@Override
	public synchronized void removeHolder(T holder) {
		super.removeHolder(holder);
		if (hasHolders() && task != null) {
			task.cancel();
			task = null;
			//System.out.println("TimerLine.removeHolder() cancel task");
		}
	}
	
}
