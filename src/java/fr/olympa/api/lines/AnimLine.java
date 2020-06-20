package fr.olympa.api.lines;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.scheduler.BukkitRunnable;

import fr.olympa.core.spigot.OlympaCore;

public class AnimLine<T extends LinesHolder<T>> extends AbstractLine<T> implements ConfigurationSerializable {

	public static List<String> getAnim(String string, ChatColor normal, ChatColor up) {
		final List<String> anim = new ArrayList<>();
		anim.add(normal + string);
		for (int i = 0; i < string.length(); i++) {
			anim.add(normal + string.substring(0, i) + up + string.substring(i, i + 1) + normal + string.substring(i + 1, string.length()));
		}
		anim.add(normal + string);
		for (int i = string.length() - 1; i > -1; i--) {
			anim.add(normal + string.substring(0, i) + up + string.substring(i, i + 1) + normal + string.substring(i + 1, string.length()));
		}
		return anim;
	}

	private final List<String> strings;
	private final int ticksAmount;
	private final int ticksBetween;

	private int status = 0;

	public AnimLine(List<String> animation, int ticksAmount, int ticksBetween) {
		this.strings = animation;
		this.ticksAmount = ticksAmount;
		this.ticksBetween = ticksBetween;
		new BukkitRunnable() {
			int timeBefore = -1;
			@Override
			public void run() {
				if (--timeBefore > -1) return;
				if (++status >= strings.size()) {
					status = 0;
					timeBefore = ticksBetween;
				}
				AnimLine.this.updateGlobal();
			}
		}.runTaskTimerAsynchronously(OlympaCore.getInstance(), 1, ticksAmount);
	}

	public int getAnimSize() {
		return strings.size();
	}

	@Override
	public String getValue(T holder) {
		return strings.get(status);
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<>();
		map.put("strings", strings);
		map.put("ticksSpeed", ticksAmount);
		map.put("ticksBetween", ticksBetween);
		return map;
	}

	public static AnimLine<?> deserialize(Map<String, Object> map) {
		return new AnimLine<>((List<String>) map.get("strings"), (int) map.get("ticksSpeed"), (int) map.get("ticksBetween"));
	}

	@SuppressWarnings ("rawtypes")
	public static AnimLine olympaAnimation() {
		return new AnimLine<>(getAnim("play.olympa.fr", ChatColor.DARK_AQUA, ChatColor.AQUA), 1, 10 * 20);
	}

}