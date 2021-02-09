package fr.olympa.api.chat;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import fr.olympa.api.player.OlympaPlayer;
import net.md_5.bungee.api.ChatColor;

public class ColorUtils {

	public static ChatColor ORANGE = ChatColor.of("#FF4500");

	private static final Random RANDOM = new Random();
	private static final float MIN_BRIGHTNESS = 0.8f;

	public static ChatColor randomColor() {
		int rand_num = RANDOM.nextInt(0xffffff + 1);
		return ChatColor.of(String.format("#%06x", rand_num));
	}

	public static ChatColor randomBrightColor() {
		float h = RANDOM.nextFloat();
		float s = 1f;
		float b = MIN_BRIGHTNESS + (1f - MIN_BRIGHTNESS) * RANDOM.nextFloat();
		return ChatColor.of(Color.getHSBColor(h, s, b));
	}

	/**
	 * Permet de colorier chaque lettre une à une dans un mot pour faire une
	 * animation Pour BungeeCord
	 */
	public static List<String> colorString(String string, ChatColor color1, ChatColor color2) {
		List<String> dyn = new ArrayList<>();
		for (int i = 0; i < string.length(); i++)
			dyn.add(color1 + string.substring(0, i) + color2 + string.substring(i, i + 1) + color1 + string.substring(i + 1, string.length()));
		dyn.add(color1 + string);
		return dyn;
	}

	public static String format(String format, Object... args) {
		return color(String.format(format, args));
	}

	public static String color(String string) {
		return string != null ? ChatColor.translateAlternateColorCodes('&', string) : null;
	}

	public static String joinGold(CharSequence... elements) {
		return join('e', '6', elements);
	}

	public static String joinGoldEt(Iterable<? extends CharSequence> elements) {
		return join('e', '6', elements.iterator(), " et ");
	}

	public static String joinGreenEt(Iterable<? extends CharSequence> elements) {
		return join('a', '2', elements.iterator(), " et ");
	}

	public static String joinGold(Iterable<? extends CharSequence> elements) {
		return join('e', '6', elements);
	}

	public static String joinGreen(CharSequence... elements) {
		return join('a', '2', elements);
	}

	public static String joinGreen(Iterable<? extends CharSequence> elements) {
		return join('a', '2', elements);
	}

	public static String joinRed(CharSequence... elements) {
		return join('c', '4', elements);
	}

	public static String joinRed(Iterable<? extends CharSequence> elements) {
		return join('c', '4', elements);
	}

	public static String joinRedEt(Iterable<? extends CharSequence> elements) {
		return join('c', '4', elements.iterator(), " et ");
	}

	public static String joinRedOu(Iterable<? extends CharSequence> elements) {
		return join('c', '4', elements.iterator(), " ou ");
	}

	public static String join(Character color1, Character color2, Iterable<? extends CharSequence> elements) {
		return join(color1, color2, elements.iterator());
	}

	public static String join(Iterable<? extends CharSequence> elements) {
		return join(null, null, elements.iterator());
	}

	public static String joinRedEt(Collection<? extends OlympaPlayer> elements) {
		return join('c', '4', elements.stream().map(OlympaPlayer::getName).iterator(), " et ");
	}

	public static String joinPlayer(Character color1, Character color2, Collection<? extends Player> elements) {
		return join(color1, color2, elements.stream().map(Player::getName).iterator(), " et ");
	}

	public static String joinRedOu(Collection<? extends OlympaPlayer> elements) {
		return join('c', '4', elements.stream().map(OlympaPlayer::getName).iterator(), " ou ");
	}

	public static String join(Character color1, Character color2, CharSequence... elements) {
		return join(color1, color2, Arrays.stream(elements).iterator());
	}

	public static String join(Character color1, Character color2, Iterator<? extends CharSequence> it) {
		return join(color1, color2, it, " ou ");
	}

	public static String join(Iterator<? extends CharSequence> it, String ouOrEt) {
		return join(null, null, it, ouOrEt);
	}

	public static String join(Character c1, Character c2, Iterator<? extends CharSequence> it, String ouOrEt) {
		String color1 = c1 != null ? String.valueOf(ChatColor.COLOR_CHAR) + c1 : "";
		String color2 = c2 != null ? String.valueOf(ChatColor.COLOR_CHAR) + c2 : "";
		StringBuilder sb = new StringBuilder();
		Boolean hasNext = null;
		while (hasNext == null || hasNext)
			if (hasNext == null) {
				if (it.hasNext()) {
					sb.append(color2 + it.next());
					hasNext = it.hasNext();
				} else
					return "";
			} else if (hasNext != null && hasNext) {
				CharSequence next = it.next();
				sb.append(color1);
				if (!(hasNext = it.hasNext()))
					sb.append(ouOrEt);
				else
					sb.append(", ");
				sb.append(color2 + next);
			}
		sb.append(color1);
		return sb.toString();
	}

	//	@Deprecated
	//	public static TextComponent textComponentBuilder(String message, ClickEvent.Action clickAction, String clickActionValue, HoverEvent.Action hoverAction, Content... contents) {
	//		TextComponent text = new TextComponent(TextComponent.fromLegacyText(message.replace("&", "§")));
	//		if (clickAction != null && clickActionValue != null)
	//			text.setClickEvent(new ClickEvent(clickAction, clickActionValue.replace("&", "§")));
	//		if (hoverAction != null && contents != null)
	//			text.setHoverEvent(new HoverEvent(hoverAction, contents));
	//		return text;
	//	}

	public static List<String> color(List<String> l) {
		return l.stream().map(s -> color(s)).collect(Collectors.toList());
	}

	public static String join(CharSequence delimiter, CharSequence... elements) {
		return color(String.join(delimiter, elements));
	}

	public static String join(CharSequence delimiter, Iterable<? extends CharSequence> elements) {
		return color(String.join(delimiter, elements));
	}

	@SuppressWarnings("unchecked")
	public static String joinTry(Object delimiter, Object elements) {
		if (elements instanceof Iterable<?>)
			return join((CharSequence) delimiter, (Iterable<? extends CharSequence>) elements);
		else if (elements instanceof CharSequence[])
			return join((CharSequence) delimiter, (CharSequence[]) elements);
		else if (elements instanceof Object[])
			return join((CharSequence) delimiter, Arrays.stream((Object[]) elements).map(Object::toString).collect(Collectors.toList()));
		else if (elements instanceof Object)
			return join((CharSequence) delimiter, (CharSequence) elements.toString());
		throw new IllegalAccessError("Unknown Type for String.join() in ColorUtils.joinTry().");
	}

}
