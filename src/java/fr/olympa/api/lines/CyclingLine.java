package fr.olympa.api.lines;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.scheduler.BukkitRunnable;

import fr.olympa.core.spigot.OlympaCore;

public class CyclingLine<T extends LinesHolder<T>> extends AbstractLine<T> implements ConfigurationSerializable {

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
	private final int ticksBetween;
	private final int ticksSleep;

	private int status = 0;

	public CyclingLine(List<String> lines, int ticksBetween) {
		this(lines, ticksBetween, 0);
	}
	
	public CyclingLine(List<String> lines, int ticksBetween, int ticksSleep) {
		this.strings = lines;
		this.ticksBetween = ticksBetween;
		this.ticksSleep = ticksSleep;
		new BukkitRunnable() {
			int timeBefore = -1;
			@Override
			public void run() {
				if (--timeBefore > -1) return;
				if (++status >= strings.size()) {
					status = 0;
					timeBefore = ticksSleep;
				}
				CyclingLine.this.updateGlobal();
			}
		}.runTaskTimerAsynchronously(OlympaCore.getInstance(), 1, ticksBetween);
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
		map.put("ticksBetween", ticksBetween);
		map.put("ticksSleep", ticksSleep);
		return map;
	}

	public static CyclingLine<?> deserialize(Map<String, Object> map) {
		return new CyclingLine<>((List<String>) map.get("strings"), (int) map.get("ticksBetween"), (int) map.get("ticksSleep"));
	}

	@SuppressWarnings ("rawtypes")
	public static CyclingLine olympaAnimation() {
		return new CyclingLine<>(getAnim("play.olympa.fr", ChatColor.DARK_AQUA, ChatColor.AQUA), 1, 10 * 20);
	}

}