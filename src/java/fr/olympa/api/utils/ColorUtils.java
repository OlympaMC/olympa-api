package fr.olympa.api.utils;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Content;

public class ColorUtils {

	public static ChatColor ORANGE = ChatColor.of("#FF4500");

	private static Random RANDOM = new Random();

	public static ChatColor randomColor() {
		int rand_num = RANDOM.nextInt(0xffffff + 1);
		return ChatColor.of(String.format("#%06x", rand_num));
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

	public static String join(Character color1, Character color2, Iterable<? extends CharSequence> elements) {
		return join(color1, color2, elements.iterator());
	}

	public static String join(Iterable<? extends CharSequence> elements) {
		return join(null, null, elements.iterator());
	}

	public static String join(Character color1, Character color2, CharSequence... elements) {
		return join(color1, color2, Arrays.stream(elements).iterator());
	}

	public static String join(Character color1, Character color2, Iterator<? extends CharSequence> it) {
		return join(color1, color2, it, " ou ");
	}

	public static String join(Character c1, Character c2, Iterator<? extends CharSequence> it, String ouOrEt) {
		String color1 = c1 != null ? String.valueOf(ChatColor.COLOR_CHAR + c1) : "";
		String color2 = c2 != null ? String.valueOf(ChatColor.COLOR_CHAR + c2) : "";
		StringBuilder sb = new StringBuilder();
		Boolean hasNext = null;
		while (hasNext == null || hasNext)
			if (hasNext == null) {
				if (hasNext = it.hasNext()) {
					sb.append(color2 + it.next());
					hasNext = it.hasNext();
				} else
					return "";
			} else {
				CharSequence next = it.next();
				if (!(hasNext = it.hasNext()))
					sb.append(color1 + ouOrEt + color2 + next);
				else
					sb.append(color1 + ", " + color2 + next);
			}
		sb.append(color1);
		return sb.toString();
	}

	public static TextComponent textComponentBuilder(String message, ClickEvent.Action clickAction, String clickActionValue, HoverEvent.Action hoverAction, Content... contents) {
		TextComponent text = new TextComponent(TextComponent.fromLegacyText(message.replace("&", "§")));
		if (clickAction != null && clickActionValue != null)
			text.setClickEvent(new ClickEvent(clickAction, clickActionValue.replace("&", "§")));
		if (hoverAction != null && contents != null)
			text.setHoverEvent(new HoverEvent(hoverAction, contents));
		return text;
	}

	public static List<String> color(List<String> l) {
		return l.stream().map(s -> color(s)).collect(Collectors.toList());
	}

	public static String join(CharSequence string, CharSequence... args) {
		return color(String.join(string, args));
	}

}
