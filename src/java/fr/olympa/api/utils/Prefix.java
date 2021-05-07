package fr.olympa.api.utils;

import java.util.Collection;

import org.bukkit.command.CommandSender;

import fr.olympa.api.chat.ColorUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public enum Prefix {

	DEFAULT("&6%serverName &7%symbole "),
	DEFAULT_BAD("&6%serverName &7%symbole &c", ChatColor.RED, ChatColor.DARK_RED),
	DEFAULT_GOOD("&6%serverName &7%symbole &a", ChatColor.GREEN, ChatColor.DARK_GREEN),
	BROADCAST("§d§k##§7 "),
	BROADCAST_SERVER("§d§k##§6 %serverName §7%symbole "),
	FACTION("&6Faction &7%symbole &a", ChatColor.YELLOW, ChatColor.GREEN),
	STAFFCHAT("&cStaffChat &4%symbole "),
	BAD("&c✕ ", ChatColor.RED, ChatColor.DARK_RED),
	ERROR("&c⚠ ", ChatColor.RED, ChatColor.DARK_RED),
	INFO("&6INFO &6%symbole &e", ChatColor.YELLOW, ChatColor.GOLD),
	USAGE("&6Usage &7%symbole &c", ChatColor.RED, ChatColor.DARK_RED),
	NONE("", null, null);

	String prefix;
	ChatColor color;
	ChatColor color2;

	private Prefix(String prefix) {
		this(prefix, null, null);
	}

	private Prefix(String prefix, ChatColor color) {
		this(prefix, color, null);
	}

	private Prefix(String prefix, ChatColor color, ChatColor color2) {
		this.prefix = ColorUtils.color(prefix.replace("%serverName", "Olympa").replace("%symbole", "➤"));
		this.color = color;
		this.color2 = color2;
	}

	public ChatColor getColor() {
		return color;
	}

	public ChatColor getColor2() {
		return color2;
	}

	public void sendMessage(CommandSender sender, String msg, Object... args) {
		sender.sendMessage(formatMessage(msg, args));
	}

	public void sendMessage(Collection<? extends CommandSender> sender, String msg, Object... args) {
		String formattedMessage = formatMessage(msg, args);
		sender.forEach(s -> s.sendMessage(formattedMessage));
	}

	public String formatMessage(String msg, Object... args) {
		return ColorUtils.color(String.format(prefix + msg, args));
	}

	public BaseComponent[] formatMessageB(String msg, Object... args) {
		return TextComponent.fromLegacyText(ColorUtils.color(String.format(prefix + msg, args)));
	}

	@Override
	public String toString() {
		return prefix;
	}
	
}
