package fr.olympa.api.lines;

import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class TimerLine<T extends LinesHolder<T>> extends DynamicLine<T> {

	public TimerLine(Function<T, String> value, Plugin plugin, int ticks) {
		super(value);
		
		Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::updateGlobal, 1, ticks);
	}

}
