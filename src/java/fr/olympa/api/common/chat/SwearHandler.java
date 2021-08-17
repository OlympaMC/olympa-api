package fr.olympa.api.common.chat;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.spigot.OlympaCore;

public class SwearHandler {

	static {
		if (LinkSpigotBungee.getInstance().isSpigot())
			OlympaCore.getInstance().getConfig().addTask("SwearHandler", customConfig -> {
				new SwearHandler(customConfig.getStringList("chat.insult"));
			});
		else
			OlympaBungee.getInstance().getDefaultConfig().addTask("SwearHandler", customConfig -> {
				new SwearHandler(customConfig.getConfig().getStringList("chat.insult"));
			});
	}
	private static Map<String, Pattern> regexSwear;

	public SwearHandler(List<String> swears) {
		regexSwear = new HashMap<>();
		for (String swear : swears) {
			StringBuilder sb = new StringBuilder();
			String start = new String();
			String end = new String();
			if (swear.startsWith("|")) {
				start = "\\b";
				swear = swear.substring(1);
			}
			if (swear.endsWith("|")) {
				end = "\\b";
				swear = swear.substring(0, swear.length() - 1);
			}
			for (char s : swear.toCharArray()) {
				String out;
				switch (s) {
				case 'o':
					out = "(0|au|eau|" + s + ")";
					break;
				case 'f':
					out = "(ph|" + s + ")";
					break;
				case 'k':
				case 'q':
					out = "(qu|q|k)";
					break;
				default:
					out = String.valueOf(s);
					break;
				}
				sb.append(out + "+[^a-zA-Z]*");
			}
			regexSwear.put(swear, Pattern.compile("(?iu)" + start + "(" + sb.toString() + end + ")"));
		}
	}

	public Collection<Pattern> getRegexSwear() {
		return regexSwear.values();
	}

	public Map<String, Pattern> getMap() {
		return regexSwear;
	}

	public String testAndReplace(String msg, String prefix, String suffix) {
		Multimap<String, String> test = test(msg);
		if (test.isEmpty())
			return null;
		return replace(msg, test, prefix, suffix);
	}

	public Multimap<String, String> test(String msg) {
		Multimap<String, String> match = ArrayListMultimap.create();
		for (Entry<String, Pattern> entry : regexSwear.entrySet()) {
			String word = entry.getKey();
			Pattern pattern = entry.getValue();
			Matcher matcher = pattern.matcher(Utils.removeAccents(msg));
			while (matcher.find())
				match.put(word, matcher.group());
		}
		return match;
	}

	public String replace(String messageRaw, Multimap<String, String> test, String prefix, String suffix) {
		for (Entry<String, String> entry : test.entries())
			messageRaw = messageRaw.replace(entry.getValue(), prefix + entry.getValue() + suffix);
		return messageRaw;
	}
}
