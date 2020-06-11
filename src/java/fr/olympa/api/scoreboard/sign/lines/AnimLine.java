package fr.olympa.api.scoreboard.sign.lines;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import fr.olympa.api.player.OlympaPlayer;

public class AnimLine extends ScoreboardLine<OlympaPlayer> {

	//public static final List<String> ANIMATION = getAnim("play.olympa.fr");

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

	private List<String> strings;
	private int status = -1;

	public AnimLine(Plugin plugin, List<String> animation, int ticksAmount, int ticksBetween) {
		this.strings = animation;
		new BukkitRunnable() {
			int timeBefore = -1;
			@Override
			public void run() {
				if (--timeBefore > -1) return;
				if (++status + 2 >= strings.size()) {
					status = 0;
					timeBefore = ticksBetween;
				}
				AnimLine.this.updateGlobal();
			}
		}.runTaskTimerAsynchronously(plugin, 1, ticksAmount);
	}

	public int getAnimSize() {
		return strings.size();
	}

	@Override
	public String getValue(OlympaPlayer player) {
		return strings.get(status);
	}

	public static AnimLine olympaAnimation(Plugin plugin) {
		return new AnimLine(plugin, getAnim("play.olympa.fr", ChatColor.DARK_AQUA, ChatColor.AQUA), 1, 10 * 20);
	}

}