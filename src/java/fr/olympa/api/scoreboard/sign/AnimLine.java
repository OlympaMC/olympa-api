package fr.olympa.api.scoreboard.sign;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;

import fr.olympa.api.objects.OlympaPlayer;

public class AnimLine extends ScoreboardLine<OlympaPlayer> {

	//public static final List<String> ANIMATION = getAnim("play.olympa.fr");

	public static List<String> getAnim(String string) {
		ChatColor color1 = ChatColor.DARK_AQUA;
		ChatColor color2 = ChatColor.AQUA;
		final List<String> anim = new ArrayList<>();
		for (int i = 0; i < string.length(); i++) {
			anim.add(color1 + string.substring(0, i) + color2 + string.substring(i, i + 1) + color1 + string.substring(i + 1, string.length()));
		}
		anim.add(color1 + string);
		for (int i = string.length() - 1; i > -1; i--) {
			anim.add(color1 + string.substring(0, i) + color2 + string.substring(i, i + 1) + color1 + string.substring(i + 1, string.length()));
		}
		anim.add(color1 + string);
		return anim;
	}

	private List<String> value;
	private int i = -1;

	public AnimLine(String value) {
		this(value, 0);
	}

	public AnimLine(String value, int length) {
		super(1, length);
		this.value = getAnim(value);
	}

	@Override
	public String getValue(OlympaPlayer player) {
		if (i + 1 == value.size()) {
			i = -1;
		}
		return value.get(++i);
	}

}
