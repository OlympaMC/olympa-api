package fr.olympa.core.spigot.datamanagement.listeners;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachment;

import fr.olympa.api.customevents.OlympaPlayerLoadEvent;
import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.provider.OlympaPlayerInformationsObject;
import fr.olympa.api.utils.ColorUtils;
import fr.olympa.core.spigot.OlympaCore;

public class DataManagmentListener implements Listener {

	{
		Bukkit.getOnlinePlayers().forEach(player -> init(new AccountProvider(player.getUniqueId()).createOlympaPlayer(player.getName(), player.getAddress().getAddress().getHostAddress())));
	}

	private void init(OlympaPlayer olympaPlayer) {
		olympaPlayer.setGroup(OlympaGroup.DEV);
		AccountProvider.cache.put(olympaPlayer.getUniqueId(), olympaPlayer);
		olympaPlayer.setId(AccountProvider.cache.size());
		AccountProvider.cachedInformations.put(olympaPlayer.getId(), new OlympaPlayerInformationsObject(olympaPlayer.getId(), olympaPlayer.getName(), olympaPlayer.getUniqueId()));
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerLogin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		OlympaPlayer olympaPlayer = AccountProvider.get(player.getUniqueId());

		if (olympaPlayer == null) {
			player.kickPlayer("§cUne erreur de données est survenu, merci de réessayer.");
			event.setJoinMessage(null);
			return;
		}

		event.setJoinMessage(ColorUtils.color("&7[&a+&7] %prefix%name".replaceAll("%group", olympaPlayer.getGroupName()).replaceAll("%prefix", olympaPlayer.getGroupPrefix()).replaceAll("%name", player.getDisplayName())));
		
		PermissionAttachment attachment = player.addAttachment(OlympaCore.getInstance());
		olympaPlayer.getGroups().keySet().forEach(group -> group.runtimePermissions.forEach((perm, value) -> attachment.setPermission(perm, value)));
		player.recalculatePermissions();
		
		OlympaPlayerLoadEvent loginevent = new OlympaPlayerLoadEvent(player, olympaPlayer, false);
		Bukkit.getPluginManager().callEvent(loginevent);
	}

	@EventHandler
	public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
		UUID uuid = event.getUniqueId();
		init(new AccountProvider(uuid).createOlympaPlayer(event.getName(), event.getAddress().getHostAddress()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerQuitHighest(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		AccountProvider.cache.remove(player.getUniqueId());
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerQuitLow(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		OlympaPlayer olympaPlayer = AccountProvider.get(player.getUniqueId());
		if (olympaPlayer != null) {
			event.setQuitMessage(ColorUtils.color("&7[&c-&7] %prefix%name".replaceAll("%group", olympaPlayer.getGroupName()).replaceAll("%prefix", olympaPlayer.getGroupPrefix()).replaceAll("%name", player.getDisplayName())));
		} else {
			event.setQuitMessage(null);
		}
	}
}
