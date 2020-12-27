package fr.olympa.api.lines;

import java.util.function.BiFunction;

import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;

public class BlinkingLine<T extends LinesHolder<T>> extends TimerLine<T> {
	
	private boolean prim = false;
	
	public BlinkingLine(BiFunction<ChatColor, T, String> value, Plugin plugin, int ticks, ChatColor primary, ChatColor secondary) {
		super(null, plugin, ticks);
		super.value = x -> value.apply(prim ? primary : secondary, x);
	}
	
	@Override
	protected void execute() {
		prim = !prim;
		super.execute();
	}
	
}
