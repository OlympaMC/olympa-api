package fr.olympa.api.lines;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.scheduler.BukkitRunnable;

import fr.olympa.api.chat.Gradient;
import fr.olympa.core.spigot.OlympaCore;
import net.md_5.bungee.api.ChatColor;

public class CyclingLine2<T extends LinesHolder<T>> extends AbstractLine<T> implements ConfigurationSerializable {

	// need to finish it
	private final List<String> strings;
	private final int ticksBetween;
	private final int ticksSleep;

	private int status = 0;

	public CyclingLine2(List<String> lines, int ticksBetween) {
		this(lines, ticksBetween, 0);
	}

	public CyclingLine2(List<String> lines, int ticksBetween, int ticksSleep) {
		this.strings = lines;
		this.ticksBetween = ticksBetween;
		this.ticksSleep = ticksSleep;
		new BukkitRunnable() {
			int timeBefore = -1;

			@Override
			public void run() {
				if (--timeBefore > -1)
					return;
				if (++status >= strings.size()) {
					status = 0;
					timeBefore = ticksSleep;
				}
				CyclingLine2.this.updateGlobal();
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

	@SuppressWarnings("unchecked")
	public static CyclingLine2<?> deserialize(Map<String, Object> map) {
		return new CyclingLine2<>((List<String>) map.get("strings"), (int) map.get("ticksBetween"), (int) map.get("ticksSleep"));
	}

	@SuppressWarnings("rawtypes")
	public static CyclingLine2 olympaAnimation() {
		return new CyclingLine2<>(new Gradient("play.olympa.fr", ChatColor.DARK_AQUA, ChatColor.AQUA, ChatColor.DARK_AQUA).getList(), 1, 10 * 20);
	}

}