package fr.olympa.api.scoreboard.sign.lines;

import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.player.OlympaPlayer;

public class TimerLine<T extends OlympaPlayer> extends DynamicLine<T> {

	public TimerLine(Function<T, String> value, Plugin plugin, int ticks) {
		super(value);
		
		Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::updateGlobal, 1, ticks);
	}

}
