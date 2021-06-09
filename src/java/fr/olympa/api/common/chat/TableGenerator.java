package fr.olympa.api.common.chat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.common.match.RegexMatcher;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.Connection;

// Copyright by FisheyLP, Version 1.3 (12.08.16)
// Edited by Olympa Dev Team
public class TableGenerator {

	private static int chatMaxSizeOneLine = 50;
	private static int chatMaxSizeOneLineConsole = 200;

	private static String delimiter = "   ";
	private static List<Character> char7 = Arrays.asList('°', '~', '@');
	private static List<Character> char5 = Arrays.asList('"', '{', '}', '(', ')', '*', 'f', 'k', '<', '>');
	private static List<Character> char4 = Arrays.asList('I', 't', ' ', '[', ']', '€');
	private static List<Character> char3 = Arrays.asList('l', '`', '³', '\'');
	private static List<Character> char2 = Arrays.asList(',', '.', '!', 'i', '´', ':', ';', '|');
	private static char char1 = '\u17f2';
	private static Pattern regex = Pattern.compile(char1 + "(?:§r)?(\\s*)"
			+ "(?:§r§8)?" + char1 + "(?:§r)?(\\s*)"
			+ "(?:§r§8)?" + char1 + "(?:§r)?(\\s*)"
			+ "(?:§r§8)?" + char1);
	private static String colorsRegex = RegexMatcher.ALL_CHAT_INVISIBLE_CHARS.getRegex();

	private Alignment[] alignments;
	private List<Row> table;
	private List<RowEntry> table2;
	private int columns;
	private boolean ignoreColors = true;
	private boolean coloredDistances = true;
	private Receiver receiver = Receiver.CLIENT;
	private List<ChatColor> colors = Arrays.asList(ChatColor.YELLOW, ChatColor.GOLD);

	public TableGenerator(Alignment... alignments) {
		if (alignments == null || alignments.length < 1)
			throw new IllegalArgumentException("Must atleast provide 1 alignment.");

		columns = alignments.length;
		this.alignments = alignments;
	}

	public TableGenerator setColors(List<ChatColor> colors) {
		this.colors = colors;
		return this;
	}

	public TableGenerator dontIgnoreColorsForSize() {
		ignoreColors = false;
		return this;
	}

	public TableGenerator dontColoredDistances() {
		coloredDistances = false;
		return this;
	}

	public TableGenerator setForReceiver(Object t) {
		receiver = Receiver.of(t);
		return this;
	}

	public TableGenerator setReceiver(Receiver r) {
		receiver = r;
		return this;
	}

	public boolean isEmpty() {
		return table.isEmpty();
	}

	public String toString(CommandSender sender) {
		return String.join("\n", generate(sender));
	}

	@Override
	public String toString() {
		return String.join("\n", generate());
	}

	public TxtComponentBuilder toTxtComponentBuilder() {
		TxtComponentBuilder out = new TxtComponentBuilder().extraSpliterBN();
		generateTxtComponent().forEach(s -> out.extra(s));
		return out;
	}

	/**
	 * Deprecated Use {@link #generateTxtComponent()}
	 */
	public List<String> generate(CommandSender sender) {
		receiver = sender instanceof ConsoleCommandSender ? Receiver.CONSOLE : Receiver.CLIENT;
		return generate(true);
	}

	/**
	 * Deprecated Use {@link #generateTxtComponent()}
	 */
	public List<String> generate() {
		return generate(true);
	}

	/**
	 * Deprecated Use {@link #generateTxtComponent()}
	 */
	@Deprecated
	public List<String> generate(boolean coloredDistances) {
		if (table2 != null)
			throw new IllegalArgumentException("You can't add generate String table if you add row with addRowTxtBuilder().");
		if (table == null)
			throw new IllegalArgumentException("You need to add rows with addRow()");
		if (receiver == null)
			throw new IllegalArgumentException("Receiver must not be null.");

		Integer[] columWidths = new Integer[columns];

		for (Row r : table)
			for (int i = 0; i < columns; i++) {
				@Nullable
				String text = r.texts.get(i);
				int length;
				if (text == null)
					length = 0;
				else if (ignoreColors)
					length = getCustomLength(text.replaceAll(colorsRegex, ""), receiver);
				else
					length = getCustomLength(text, receiver);

				if (columWidths[i] == null || length > columWidths[i])
					columWidths[i] = length;
			}

		List<String> lines = new ArrayList<>();

		for (Row r : table) {
			StringBuilder sb = new StringBuilder();

			if (r.empty) {
				lines.add("");
				continue;
			}

			for (int i = 0; i < columns; i++) {
				Alignment agn = alignments[i];
				@Nullable
				String text = r.texts.get(i);
				int length;

				if (text == null)
					continue;
				if (ignoreColors)
					length = getCustomLength(text.replaceAll(colorsRegex, ""), receiver);
				else
					length = getCustomLength(text, receiver);

				int empty = columWidths[i] - length;
				int spacesAmount = empty;
				if (receiver == Receiver.CLIENT)
					spacesAmount = (int) Math.floor(empty / 4d);
				int char1Amount = 0;
				if (receiver == Receiver.CLIENT)
					char1Amount = empty - 4 * spacesAmount;

				String spaces = concatChars(' ', spacesAmount);
				String char1s = concatChars(char1, char1Amount);

				if (coloredDistances)
					char1s = "§r§8" + char1s + "§r";

				if (agn == Alignment.LEFT) {
					sb.append(text);
					if (i < columns - 1)
						sb.append(char1s).append(spaces);
				}
				if (agn == Alignment.RIGHT)
					sb.append(spaces).append(char1s).append(text);
				if (agn == Alignment.CENTER) {
					int leftAmount = empty / 2;
					int rightAmount = empty - leftAmount;

					int spacesLeftAmount = leftAmount;
					int spacesRightAmount = rightAmount;
					if (receiver == Receiver.CLIENT) {
						spacesLeftAmount = (int) Math.floor(spacesLeftAmount / 4d);
						spacesRightAmount = (int) Math.floor(spacesRightAmount / 4d);
					}

					int char1LeftAmount = 0;
					int char1RightAmount = 0;
					if (receiver == Receiver.CLIENT) {
						char1LeftAmount = leftAmount - 4 * spacesLeftAmount;
						char1RightAmount = rightAmount - 4 * spacesRightAmount;
					}

					String spacesLeft = concatChars(' ', spacesLeftAmount);
					String spacesRight = concatChars(' ', spacesRightAmount);
					String char1Left = concatChars(char1, char1LeftAmount);
					String char1Right = concatChars(char1, char1RightAmount);

					if (coloredDistances) {
						char1Left = "§r§8" + char1Left + "§r";
						char1Right = "§r§8" + char1Right + "§r";
					}

					sb.append(spacesLeft).append(char1Left).append(text);
					if (i < columns - 1)
						sb.append(char1Right).append(spacesRight);
				}

				if (i < columns - 1)
					sb.append("§r" + delimiter);
			}

			String line = sb.toString();
			if (receiver == Receiver.CLIENT)
				for (int i = 0; i < 2; i++) {
					Matcher matcher = regex.matcher(line);
					line = matcher.replaceAll("$1$2$3 ").replace("§r§8§r", "§r")
							.replaceAll("§r(\\s*)§r", "§r$1");
				}
			lines.add(line);
		}
		return lines;
	}

	public List<TxtComponentBuilder> generateTxtComponent() {
		if (table != null)
			throw new IllegalArgumentException("You can't add generate String table if you add row with addRow().");
		if (table2 == null)
			throw new IllegalArgumentException("You need to add rows with addRowTxtBuilder()");
		if (receiver == null)
			throw new IllegalArgumentException("Receiver must not be null.");

		Integer[] columWidths = new Integer[columns];

		for (RowEntry r : table2) {
			int length;
			int i1 = 0;
			for (Entry<TxtComponentBuilder, TextComponent> entry : r.texts.entrySet()) {
				TxtComponentBuilder text = entry.getKey();
				if (receiver == Receiver.CONSOLE)
					text = text.console();
				if (ignoreColors)
					length = getCustomLength(text.toLegacyText().replaceAll(colorsRegex, ""), receiver);
				else
					length = getCustomLength(text.toLegacyText(), receiver);

				if (columWidths[i1] == null || length > columWidths[i1])
					columWidths[i1] = length;
				i1++;
			}
			for (int i2 = i1; i2 < columns; i2++)
				columWidths[i2] = 0;
		}
		List<TxtComponentBuilder> lines = new ArrayList<>();
		int iColor = 0;
		for (RowEntry r : table2) {
			TxtComponentBuilder out = new TxtComponentBuilder();
			if (r.empty) {
				lines.add(new TxtComponentBuilder("(empty)"));
				continue;
			}
			int i1 = 0;
			for (Entry<TxtComponentBuilder, TextComponent> entry : r.texts.entrySet()) {
				out.extra(getText(entry.getKey(), alignments[i1], iColor, i1));
				/*TxtComponentBuilder text = entry.getKey();
				Alignment agn = alignments[i1];
				int length;
				if (receiver == Receiver.CONSOLE)
					text.console();
				if (colors != null && !colors.isEmpty())
					text.color(colors.get(iColor));
				if (ignoreColors)
					length = getCustomLength(text.toLegacyText().replaceAll(colorsRegex, ""), receiver);
				else
					length = getCustomLength(text.toLegacyText(), receiver);
				
				int empty = columWidths[i1] - length;
				int spacesAmount = empty;
				if (receiver == Receiver.CLIENT)
					spacesAmount = (int) Math.floor(empty / 4d);
				int char1Amount = 0;
				if (receiver == Receiver.CLIENT)
					char1Amount = empty - 4 * spacesAmount;
				
				String spaces = concatChars(' ', spacesAmount);
				String char1s = concatChars(char1, char1Amount);
				
				if (coloredDistances)
					char1s = "§r§8" + char1s + "§r";
				
				if (agn == Alignment.LEFT) {
					out.extra(text);
					if (i1 < columns - 1) {
						out.extra(char1s);
						out.extra(spaces);
					}
				} else if (agn == Alignment.RIGHT) {
					out.extra(spaces);
					out.extra(char1s);
					out.extra(text);
				} else if (agn == Alignment.CENTER) {
					int leftAmount = empty / 2;
					int rightAmount = empty - leftAmount;
				
					int spacesLeftAmount = leftAmount;
					int spacesRightAmount = rightAmount;
					if (receiver == Receiver.CLIENT) {
						spacesLeftAmount = (int) Math.floor(spacesLeftAmount / 4d);
						spacesRightAmount = (int) Math.floor(spacesRightAmount / 4d);
					}
				
					int char1LeftAmount = 0;
					int char1RightAmount = 0;
					if (receiver == Receiver.CLIENT) {
						char1LeftAmount = leftAmount - 4 * spacesLeftAmount;
						char1RightAmount = rightAmount - 4 * spacesRightAmount;
					}
				
					String spacesLeft = concatChars(' ', spacesLeftAmount);
					String spacesRight = concatChars(' ', spacesRightAmount);
					String char1Left = concatChars(char1, char1LeftAmount);
					String char1Right = concatChars(char1, char1RightAmount);
				
					if (coloredDistances) {
						char1Left = "§r§8" + char1Left + "§r";
						char1Right = "§r§8" + char1Right + "§r";
					}
				
					out.extra(spacesLeft);
					out.extra(char1Left);
					out.extra(text);
					if (i1 < columns - 1) {
						out.extra(char1Right);
						out.extra(spacesRight);
					}
				}
				if (i1 < columns - 1)
					out.extra("§r" + delimiter);*/
				i1++;
			}

			//			LinkSpigotBungee.Provider.link.sendMessage("DEBUG TableGeneratorToTxtBuilder '%s'", out.toLegacyText());
			/*if (receiver == Receiver.CLIENT)
				for (int i = 0; i < 2; i++) {
					Matcher matcher = regex.matcher(line);
					line = matcher.replaceAll("$1$2$3 ").replace("§r§8§r", "§r")
							.replaceAll("§r(\\s*)§r", "§r$1");
				}*/
			lines.add(out);
			if (++iColor >= colors.size())
				iColor = 0;
		}
		return lines;
	}

	private TxtComponentBuilder getText(TxtComponentBuilder text, Alignment agn, int columWidths, int i1) {
		int length;
		if (ignoreColors)
			length = getCustomLength(text.toLegacyText().replaceAll(colorsRegex, ""), receiver);
		else
			length = getCustomLength(text.toLegacyText(), receiver);

		int empty = columWidths - length;
		int spacesAmount = empty;
		if (receiver == Receiver.CLIENT)
			spacesAmount = (int) Math.floor(empty / 4d);
		int char1Amount = 0;
		if (receiver == Receiver.CLIENT)
			char1Amount = empty - 4 * spacesAmount;

		String spaces = concatChars(' ', spacesAmount);
		String char1s = concatChars(char1, char1Amount);

		if (coloredDistances)
			char1s = "§r§8" + char1s + "§r";

		TxtComponentBuilder out = new TxtComponentBuilder();
		if (agn == Alignment.LEFT) {
			out.extra(text);
			if (i1 < columns - 1) {
				out.extra(char1s);
				out.extra(spaces);
			}
		} else if (agn == Alignment.RIGHT) {
			out.extra(spaces);
			out.extra(char1s);
			out.extra(text);
		} else if (agn == Alignment.CENTER) {
			int leftAmount = empty / 2;
			int rightAmount = empty - leftAmount;

			int spacesLeftAmount = leftAmount;
			int spacesRightAmount = rightAmount;
			if (receiver == Receiver.CLIENT) {
				spacesLeftAmount = (int) Math.floor(spacesLeftAmount / 4d);
				spacesRightAmount = (int) Math.floor(spacesRightAmount / 4d);
			}

			int char1LeftAmount = 0;
			int char1RightAmount = 0;
			if (receiver == Receiver.CLIENT) {
				char1LeftAmount = leftAmount - 4 * spacesLeftAmount;
				char1RightAmount = rightAmount - 4 * spacesRightAmount;
			}

			String spacesLeft = concatChars(' ', spacesLeftAmount);
			String spacesRight = concatChars(' ', spacesRightAmount);
			String char1Left = concatChars(char1, char1LeftAmount);
			String char1Right = concatChars(char1, char1RightAmount);

			if (coloredDistances) {
				char1Left = "§r§8" + char1Left + "§r";
				char1Right = "§r§8" + char1Right + "§r";
			}

			out.extra(spacesLeft);
			out.extra(char1Left);
			out.extra(text);
			if (i1 < columns - 1) {
				out.extra(char1Right);
				out.extra(spacesRight);
			}
		}
		if (i1 < columns - 1)
			out.extra("§r" + delimiter);
		return out;
	}

	protected static int getCustomLength(String text, Receiver receiver) {
		if (text == null)
			throw new IllegalArgumentException("Text must not be null.");
		if (receiver == null)
			throw new IllegalArgumentException("Receiver must not be null.");
		if (receiver == Receiver.CONSOLE)
			return text.length();

		int length = 0;
		for (char c : text.toCharArray())
			length += getCustomCharLength(c);

		return length;
	}

	protected static int getCustomCharLength(char c) {
		if (char1 == c)
			return 1;
		if (char2.contains(c))
			return 2;
		if (char3.contains(c))
			return 3;
		if (char4.contains(c))
			return 4;
		if (char5.contains(c))
			return 5;
		if (char7.contains(c))
			return 7;

		return 6;
	}

	protected String concatChars(char c, int length) {
		String s = "";
		if (length < 1)
			return s;

		for (int i = 0; i < length; i++)
			s += Character.toString(c);
		return s;
	}

	/**
	 * Deprecated Use {@link #generateTxtComponent()}
	 */
	@Deprecated
	public void addRow(String... texts) {
		if (table2 != null)
			throw new IllegalArgumentException("You can't add row with addRow() if you already have add one row with addRowTxtBuilder().");
		if (table == null)
			table = new ArrayList<>();
		if (texts == null)
			throw new IllegalArgumentException("Texts must not be null.");
		if (texts.length > columns)
			throw new IllegalArgumentException("Too big for the table.");
		table.add(new Row(texts));
	}

	public void addRowTxtBuilder(RowEntry text) {
		if (table != null)
			throw new IllegalArgumentException("You can't add row with addRowTxtBuilder() if you already have add one row with addRow().");
		if (table2 == null)
			table2 = new ArrayList<>();
		if (text == null)
			throw new IllegalArgumentException("RowEntry must not be null.");
		if (text.texts.size() > columns)
			throw new IllegalArgumentException("Too big for the table.");
		table2.add(text);
	}

	public void addRowTxtBuilder(String... texts) {
		if (table != null)
			throw new IllegalArgumentException("You can't add row with addRowTxtBuilder() if you already have add one row with addRow().");
		if (table2 == null)
			table2 = new ArrayList<>();
		if (texts == null)
			throw new IllegalArgumentException("Texts must not be null.");
		if (texts.length > columns)
			throw new IllegalArgumentException("Too big for the table.");
		table2.add(new RowEntry(texts));
	}

	public void addRowTxtBuilder(TxtComponentBuilder... texts) {
		if (table != null)
			throw new IllegalArgumentException("You can't add row with addRowTxtBuilder() if you already have add one row with addRow().");
		if (table2 == null)
			table2 = new ArrayList<>();
		if (texts == null)
			throw new IllegalArgumentException("Texts must not be null.");
		if (texts.length > columns)
			throw new IllegalArgumentException("Too big for the table.");
		table2.add(new RowEntry(texts));
	}

	@Deprecated
	private class Row {

		public List<String> texts = new ArrayList<>();
		public boolean empty = true;

		/**
		 * Deprecated Use {@link #RowEntry(String...)}
		 */
		@Deprecated
		public Row(String... texts) {
			if (texts == null) {
				for (int i = 0; i < columns; i++)
					this.texts.add("");
				return;
			}

			for (String text : texts) {
				if (text != null && !text.isEmpty())
					empty = false;

				this.texts.add(text);
			}

			for (int i = 0; i < columns; i++)
				if (i >= texts.length)
					this.texts.add("");
		}
	}

	private class RowEntry {

		public Map<TxtComponentBuilder, TextComponent> texts = new LinkedHashMap<>();
		public boolean empty = true;

		public RowEntry(String... texts) {
			for (String text : texts) {
				if (text != null && !text.isEmpty()) {
					if (empty)
						empty = false;
				} else
					text = "(null)";
				TxtComponentBuilder txtBuilder = new TxtComponentBuilder(text);
				this.texts.put(txtBuilder, null);
			}
		}

		public RowEntry(TxtComponentBuilder... textsBuilder) {
			for (TxtComponentBuilder text : textsBuilder) {
				if (text != null && !text.isEmpty()) {
					if (empty)
						empty = false;
				} else
					text = new TxtComponentBuilder("(null)");
				texts.put(text, null);
			}
		}
	}

	public enum Receiver {
		CONSOLE,
		CLIENT;

		public static Receiver of(Object sender) {
			if (LinkSpigotBungee.Provider.link.isSpigot()) {
				if (sender instanceof OfflinePlayer)
					return CLIENT;
				return CONSOLE;
			} else {
				if (sender instanceof Connection)
					return CLIENT;
				return CONSOLE;
			}
		}
	}

	public enum Alignment {
		CENTER,
		LEFT,
		RIGHT
	}
}