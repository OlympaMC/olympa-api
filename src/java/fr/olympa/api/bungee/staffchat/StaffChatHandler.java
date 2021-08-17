package fr.olympa.api.bungee.staffchat;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import fr.olympa.api.bungee.customevent.StaffChatEvent;
import fr.olympa.api.bungee.player.DataHandler;
import fr.olympa.api.common.permission.list.OlympaAPIPermissionsBungee;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class StaffChatHandler {

	private static final Set<UUID> staffChat = new HashSet<>();

	public static int sendMessage(OlympaPlayer olympaPlayer, CommandSender sender, String msg) {
		String message = msg.replaceAll("( )\\1+", " ");
		String senderName;
		ProxiedPlayer player = null;
		boolean isDiscord = sender == null;
		if (olympaPlayer != null) {
			player = ProxyServer.getInstance().getPlayer(olympaPlayer.getName());
			if (player != null)
				sender = player;
		}
		if (sender == null)
			senderName = "Discord " + olympaPlayer.getGroupNameColored() + " " + olympaPlayer.getName();
		else if (sender instanceof ProxiedPlayer)
			senderName = Utils.capitalize(((ProxiedPlayer) sender).getServer().getInfo().getName()) + " " + olympaPlayer.getGroupNameColored() + " " + sender.getName();
		else
			senderName = "§e" + sender.getName();

		BaseComponent[] messageComponent = TextComponent.fromLegacyText(Prefix.STAFFCHAT + senderName + " §7: " + message);
		List<ProxiedPlayer> staff = ProxyServer.getInstance().getPlayers().stream().filter(p -> {
			try {
				return !DataHandler.isUnlogged(p) && OlympaAPIPermissionsBungee.STAFF_CHAT.hasPermission(new AccountProviderAPI(p.getUniqueId()).get());
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		})
				.collect(Collectors.toList());
		staff.forEach(p -> p.sendMessage(messageComponent));
		ProxyServer.getInstance().getConsole().sendMessage(messageComponent);
		if (!isDiscord)
			ProxyServer.getInstance().getPluginManager().callEvent(new StaffChatEvent(sender, olympaPlayer, msg));
		else
			return staff.size();
		return -1;
	}

	public static Set<UUID> getStaffchat() {
		return staffChat;
	}
}
