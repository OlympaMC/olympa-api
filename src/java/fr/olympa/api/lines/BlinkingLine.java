package fr.olympa.api.lines;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;

public class BlinkingLine<T extends LinesHolder<T>> extends TimerLine<T> {
	
	public BlinkingLine(BiFunction<ChatColor, T, String> value, Plugin plugin, int ticks, ChatColor primary, ChatColor secondary) {
		super(new Function<T, String>() {
			private boolean prim = false;
			
			@Override
			public String apply(T x) {
				return value.apply((prim = !prim) ? primary : secondary, x);
			}
		}, plugin, ticks);
	}
	
}
