package fr.olympa.api.common.report;

import fr.olympa.api.common.chat.TxtComponentBuilder;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
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
			tc.setHoverEvent(TxtComponentBuilder.hoverEvent(HoverEvent.Action.SHOW_TEXT, "&cJoueur déconnecté"));
		} else if (targetServer.equals(authorServer)) {
			tc.setColor(ChatColor.AQUA);
			tc.setHoverEvent(TxtComponentBuilder.hoverEvent(HoverEvent.Action.SHOW_TEXT, "&bJoueur connecté ici"));
		} else if (targetServer.equals(report.getServerName())) {
			tc.setColor(ChatColor.GREEN);
			tc.setHoverEvent(TxtComponentBuilder.hoverEvent(HoverEvent.Action.SHOW_TEXT, "&aJoueur toujours connecté sur le serveur %s", targetServer));
		} else {
			tc.setColor(ChatColor.LIGHT_PURPLE);
			tc.setHoverEvent(TxtComponentBuilder.hoverEvent(HoverEvent.Action.SHOW_TEXT, "&dJoueur connecté au serveur %s", targetServer));
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
		tc = new TextComponent(report.getReasonName());
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
		out.setHoverEvent(TxtComponentBuilder.hoverEvent(HoverEvent.Action.SHOW_TEXT, "&eClique pour avoir plus d'info"));
		out.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/report seeId " + report.getId()));
		return out;
	}
}
