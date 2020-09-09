package fr.olympa.api.report;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class ReportUtils {

	public static TextComponent getAlert(OlympaReport report, String authorName, String targetName, String targetServer, String authorServer) {
		TextComponent out = new TextComponent();
		TextComponent tc = new TextComponent("[REPORT] ");
		tc.setColor(ChatColor.DARK_PURPLE);
		out.addExtra(tc);
		tc = new TextComponent(targetName);
		if (targetServer == null) {
			tc.setColor(ChatColor.RED);
			tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Joueur déconnecté").color(ChatColor.RED).create()));
		} else if (targetServer.equals(authorServer)) {
			tc.setColor(ChatColor.AQUA);
			tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Joueur connecté ici").color(ChatColor.AQUA).create()));
		} else if (targetServer.equals(report.getServerName())) {
			tc.setColor(ChatColor.GREEN);
			tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Joueur toujours connecté sur le serveur  " + targetServer).color(ChatColor.GREEN).create()));
		} else {
			tc.setColor(ChatColor.LIGHT_PURPLE);
			tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Joueur connecté au serveur " + targetServer).color(ChatColor.LIGHT_PURPLE).create()));
		}
		out.addExtra(tc);
		tc = new TextComponent(" par ");
		tc.setColor(ChatColor.DARK_PURPLE);
		out.addExtra(tc);
		tc = new TextComponent(authorName);
		tc.setColor(ChatColor.LIGHT_PURPLE);
		out.addExtra(tc);
		tc = new TextComponent(" pour ");
		tc.setColor(ChatColor.DARK_PURPLE);
		out.addExtra(tc);
		tc = new TextComponent(report.getReason().getReason());
		tc.setColor(ChatColor.LIGHT_PURPLE);
		out.addExtra(tc);
		tc = new TextComponent(" sur ");
		tc.setColor(ChatColor.DARK_PURPLE);
		out.addExtra(tc);
		tc = new TextComponent(report.getServerName());
		tc.setColor(ChatColor.LIGHT_PURPLE);
		out.addExtra(tc);
		tc = new TextComponent(".");
		tc.setColor(ChatColor.DARK_PURPLE);
		out.addExtra(tc);
		out.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Clique pour avoir plus d'info").color(ChatColor.YELLOW).create()));
		out.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/report see " + report.getId()));
		return out;
	}
}
