package fr.olympa.api.common.chat;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

/**
 *
 * Created by SirSpoodles
 * Modified by ZombieHDGaming for MotD Usage
 * Edit by Olympa Dev Team
 *
 * https://www.spigotmc.org/threads/free-code-sending-perfectly-centered-chat-message.95872/
 *
 * Contains original method, method MK.2, and MotD method
 *
 */
public enum Chat {
	A('A', 5),
	LOWER_A('a', 5),
	B('B', 5),
	LOWER_B('b', 5),
	C('C', 5),
	LOWER_C('c', 5),
	D('D', 5),
	LOWER_D('d', 5),
	E('E', 5),
	LOWER_E('e', 5),
	F('F', 5),
	LOWER_F('f', 4),
	G('G', 5),
	LOWER_G('g', 5),
	H('H', 5),
	LOWER_H('h', 5),
	I('I', 3),
	LOWER_I('i', 1),
	J('J', 5),
	LOWER_J('j', 5),
	K('K', 5),
	LOWER_K('k', 4),
	L('L', 5),
	LOWER_L('l', 1),
	M('M', 5),
	LOWER_M('m', 5),
	N('N', 5),
	LOWER_N('n', 5),
	O('O', 5),
	LOWER_O('o', 5),
	P('P', 5),
	LOWER_P('p', 5),
	Q('Q', 5),
	LOWER_Q('q', 5),
	R('R', 5),
	LOWER_R('r', 5),
	S('S', 5),
	LOWER_S('s', 5),
	T('T', 5),
	LOWER_T('t', 3),
	U('U', 5),
	LOWER_U('u', 5),
	V('V', 5),
	LOWER_V('v', 5),
	W('W', 5),
	LOWER_W('w', 5),
	X('X', 5),
	LOWER_X('x', 5),
	Y('Y', 5),
	LOWER_Y('y', 5),
	Z('Z', 5),
	LOWER_Z('z', 5),
	NUM_1('1', 5),
	NUM_2('2', 5),
	NUM_3('3', 5),
	NUM_4('4', 5),
	NUM_5('5', 5),
	NUM_6('6', 5),
	NUM_7('7', 5),
	NUM_8('8', 5),
	NUM_9('9', 5),
	NUM_0('0', 5),
	EXCLAMATION_POINT('!', 1),
	AT_SYMBOL('@', 6),
	NUM_SIGN('#', 5),
	DOLLAR_SIGN('$', 5),
	PERCENT('%', 5),
	UP_ARROW('^', 5),
	AMPERSAND('&', 5),
	ASTERISK('*', 5),
	LEFT_PARENTHESIS('(', 4),
	RIGHT_PERENTHESIS(')', 4),
	MINUS('-', 5),
	UNDERSCORE('_', 5),
	PLUS_SIGN('+', 5),
	EQUALS_SIGN('=', 5),
	LEFT_CURL_BRACE('{', 4),
	RIGHT_CURL_BRACE('}', 4),
	LEFT_BRACKET('[', 3),
	RIGHT_BRACKET(']', 3),
	COLON(':', 1),
	SEMI_COLON(';', 1),
	DOUBLE_QUOTE('"', 3),
	SINGLE_QUOTE('\'', 1),
	LEFT_ARROW('<', 4),
	RIGHT_ARROW('>', 4),
	QUESTION_MARK('?', 5),
	SLASH('/', 5),
	BACK_SLASH('\\', 5),
	LINE('|', 1),
	TILDE('~', 5),
	TICK('`', 2),
	PERIOD('.', 1),
	COMMA(',', 1),
	SPACE(' ', 3),
	HEXA('???', 11),
	WARNING('???', 11),
	CROSS('???', 6),
	DEFAULT('a', 4);

	private char character;
	private int length;

	private final static int CENTER_PX = 120;
	//	private final static int MAX_PX = 240;
	private final static int MAX_PX = 300;

	private final static int CENTER_CHAT_PX = 154;
	private final static int MAX_CHAT_PX = 250;

	Chat(char character, int length) {
		this.character = character;
		this.length = length;
	}

	public char getCharacter() {
		return character;
	}

	public int getLength() {
		return length;
	}

	public int getBoldLength() {
		if (this == Chat.SPACE)
			return getLength();
		return length + 1;
	}

	public static Chat getDefaultFontInfo(char c) {
		for (Chat dFI : Chat.values())
			if (dFI.getCharacter() == c)
				return dFI;
		return Chat.DEFAULT;
	}

	public static String centerMotD(String message) {
		message = ChatColor.translateAlternateColorCodes('&', message);
		int messagePxSize = 0;
		boolean previousCode = false;
		boolean isBold = false;
		int charIndex = 0;
		int lastSpaceIndex = 0;
		String toSendAfter = null;
		String recentColorCode = "";
		for (char c : message.toCharArray()) {
			if (c == '??') {
				previousCode = true;
				continue;
			} else if (previousCode == true) {
				previousCode = false;
				recentColorCode = "??" + c;
				if (c == 'l' || c == 'L') {
					isBold = true;
					continue;
				} else
					isBold = false;
			} else if (c == ' ')
				lastSpaceIndex = charIndex;
			else {
				Chat dFI = Chat.getDefaultFontInfo(c);
				messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
				messagePxSize++;
			}
			if (messagePxSize >= MAX_PX) {
				toSendAfter = recentColorCode + message.substring(lastSpaceIndex + 1, message.length());
				message = message.substring(0, lastSpaceIndex + 1);
				break;
			}
			charIndex++;
		}
		int halvedMessageSize = messagePxSize / 2;
		int toCompensate = CENTER_PX - halvedMessageSize;
		int spaceLength = Chat.SPACE.getLength() + 1;
		int compensated = 0;
		StringBuilder sb = new StringBuilder();
		while (compensated < toCompensate) {
			sb.append(" ");
			compensated += spaceLength;
		}
		if (toSendAfter != null)
			centerMotD(toSendAfter);
		return sb.toString() + message;
	}

	@Deprecated
	public static void sendCenteredMessageOld(Player player, String message) {
		if (message == null || message.equals(""))
			player.sendMessage("");
		message = ChatColor.translateAlternateColorCodes('&', message);

		int messagePxSize = 0;
		boolean previousCode = false;
		boolean isBold = false;

		for (char c : message.toCharArray())
			if (c == '??') {
				previousCode = true;
				continue;
			} else if (previousCode == true) {
				previousCode = false;
				if (c == 'l' || c == 'L') {
					isBold = true;
					continue;
				} else
					isBold = false;
			} else {
				Chat dFI = Chat.getDefaultFontInfo(c);
				messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
				messagePxSize++;
			}

		int halvedMessageSize = messagePxSize / 2;
		int toCompensate = CENTER_PX - halvedMessageSize;
		int spaceLength = Chat.SPACE.getLength() + 1;
		int compensated = 0;
		StringBuilder sb = new StringBuilder();
		while (compensated < toCompensate) {
			sb.append(" ");
			compensated += spaceLength;
		}
		player.sendMessage(sb.toString() + message);
	}

	public static String getCenteredMessage(List<String> list) {
		return list.stream().map(msg -> getCenteredMessage(msg)).collect(Collectors.joining("\n"));
	}

	public static int getPxSize(String message, boolean countSpaces) {
		message = ChatColor.translateAlternateColorCodes('&', message);
		int messagePxSize = 0;
		boolean previousCode = false;
		boolean isBold = false;
		for (char c : message.toCharArray())
			if (c == '??') {
				previousCode = true;
				continue;
			}else if (previousCode) {
				previousCode = false;
				ChatColor code = ChatColor.getByChar(Character.toLowerCase(c));
				if (ChatColor.BOLD.equals(code)) {
					isBold = true;
					continue;
				}else if (code != null && code.getColor() != null) // if ChatColor#getColor == null then it's a formatting code
					isBold = false;
			} else if (c == ' ') {
				if (countSpaces)
					messagePxSize += Chat.SPACE.getLength() + 1;
			} else {
				Chat dFI = Chat.getDefaultFontInfo(c);
				messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
				messagePxSize++;
			}
		return messagePxSize;
	}

	public static String getCenteredMessage(String message) {
		int messagePxSize = getPxSize(message, false);
		int halvedMessageSize = messagePxSize / 2;
		int toCompensate = CENTER_CHAT_PX - halvedMessageSize;
		int spaceLength = Chat.SPACE.getLength() + 1;
		int compensated = 0;
		StringBuilder sb = new StringBuilder();
		while (compensated < toCompensate) {
			sb.append(" ");
			compensated += spaceLength;
		}
		return sb.toString() + message;
		//		player.sendMessage(sb.toString() + message);
		//		if (toSendAfter != null)
		//			sendCenteredMessage(player, toSendAfter);
	}
}
