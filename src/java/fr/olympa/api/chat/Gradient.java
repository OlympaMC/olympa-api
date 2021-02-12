package fr.olympa.api.chat;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableMap;

import net.md_5.bungee.api.ChatColor;

public class Gradient {

	private static final boolean SUPPORTS_RGB = true;

	private static final Map<Color, ChatColor> COLORS = ImmutableMap.<Color, ChatColor>builder()
			.put(new Color(0), ChatColor.getByChar('0'))
			.put(new Color(170), ChatColor.getByChar('1'))
			.put(new Color(43520), ChatColor.getByChar('2'))
			.put(new Color(43690), ChatColor.getByChar('3'))
			.put(new Color(11141120), ChatColor.getByChar('4'))
			.put(new Color(11141290), ChatColor.getByChar('5'))
			.put(new Color(16755200), ChatColor.getByChar('6'))
			.put(new Color(11184810), ChatColor.getByChar('7'))
			.put(new Color(5592405), ChatColor.getByChar('8'))
			.put(new Color(5592575), ChatColor.getByChar('9'))
			.put(new Color(5635925), ChatColor.getByChar('a'))
			.put(new Color(5636095), ChatColor.getByChar('b'))
			.put(new Color(16733525), ChatColor.getByChar('c'))
			.put(new Color(16733695), ChatColor.getByChar('d'))
			.put(new Color(16777045), ChatColor.getByChar('e'))
			.put(new Color(16777215), ChatColor.getByChar('f')).build();

	@Nonnull
	private static ChatColor getClosestColor(Color color) {
		Color nearestColor = null;
		double nearestDistance = Integer.MAX_VALUE;

		for (Color constantColor : COLORS.keySet()) {
			double distance = Math.pow(color.getRed() - constantColor.getRed(), 2) + Math.pow(color.getGreen() - constantColor.getGreen(), 2) + Math.pow(color.getBlue() - constantColor.getBlue(), 2);
			if (nearestDistance > distance) {
				nearestColor = constantColor;
				nearestDistance = distance;
			}
		}
		return COLORS.get(nearestColor);
	}

	@Nonnull
	private static ChatColor[] createGradient(@Nonnull Color start, @Nonnull Color end, int step) {
		ChatColor[] colors = new ChatColor[step];
		int stepR = Math.abs(start.getRed() - end.getRed()) / step;
		int stepG = Math.abs(start.getGreen() - end.getGreen()) / step;
		int stepB = Math.abs(start.getBlue() - end.getBlue()) / step;
		int[] direction = new int[] {
				start.getRed() < end.getRed() ? +1 : -1,
				start.getGreen() < end.getGreen() ? +1 : -1,
				start.getBlue() < end.getBlue() ? +1 : -1
		};

		for (int i = 0; i <= step; i++) {
			Color color = new Color(start.getRed() + stepR * i * direction[0], start.getGreen() + stepG * i * direction[1], start.getBlue() + stepB * i * direction[2]);
			if (SUPPORTS_RGB)
				colors[i] = ChatColor.of(color);
			else
				colors[i] = getClosestColor(color);
		}
		return colors;
	}

	@Nonnull
	private static String color(@Nonnull String string, @Nonnull Color color) {
		return (SUPPORTS_RGB ? ChatColor.of(color) : getClosestColor(color)) + string;
	}

	@Nonnull
	private static String color(@Nonnull String string, @Nonnull Color start, @Nonnull Color end) {
		StringBuilder stringBuilder = new StringBuilder();
		ChatColor[] colors = createGradient(start, end, string.length());
		String[] characters = string.split("");
		for (int i = 0; i < string.length(); i++)
			stringBuilder.append(colors[i]).append(characters[i]);
		return stringBuilder.toString();
	}

	@Nonnull
	private static String color(@Nonnull String string, @Nonnull Color start, @Nonnull Color middle, @Nonnull Color end, int pos) {
		if (string.length() > pos || pos < 0)
			return "";
		StringBuilder stringBuilder = new StringBuilder();
		ChatColor[] colorsBefore = createGradient(start, middle, pos);
		ChatColor[] colorsAfter = createGradient(middle, end, string.length() - pos);
		char[] characters = string.toCharArray();
		for (int i = 0; i < colorsBefore.length; i++)
			stringBuilder.append(colorsBefore[i]).append(characters[i]);
		for (int i = 1; i < colorsAfter.length; i++)
			stringBuilder.append(colorsAfter[i]).append(characters[i]);
		return stringBuilder.toString();
	}

	String message;
	Color color1;
	Color color2;
	Color color3;

	public Gradient(String message, Color color1, Color color2, Color color3) {
		this(message, color1, color2);
		this.color3 = color3;
	}

	public Gradient(String message, ChatColor color1, ChatColor color2, ChatColor color3) {
		this(message, color1.getColor(), color2.getColor(), color3.getColor());
	}

	public Gradient(String message, ChatColor color1, ChatColor color2) {
		this(message, color1.getColor(), color2.getColor());
	}

	public Gradient(String message, Color color1, Color color2) {
		this.message = message;
		this.color1 = color1;
		this.color2 = color2;
	}

	public String get() {
		return color(message, color1, color2);
	}

	public List<String> getList() {
		List<String> allPosibilities = new ArrayList<>();
		for (int i = 0; i < message.length(); i++)
			allPosibilities.add(color(message, color1, color2, color3, i));
		return allPosibilities;
	}
}
