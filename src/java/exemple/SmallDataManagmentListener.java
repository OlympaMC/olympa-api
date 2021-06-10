package exemple;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.olympa.api.common.chat.ColorUtils;
import fr.olympa.api.common.groups.OlympaGroup;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.api.spigot.customevents.OlympaPlayerLoadEvent;

public class SmallDataManagmentListener implements Listener {

	{
		Bukkit.getOnlinePlayers().forEach(player -> init(new AccountProviderAPI(player.getUniqueId()).createOlympaPlayer(player.getName(), player.getAddress().getAddress().getHostAddress())));
	}

	private void init(OlympaPlayer olympaPlayer) {
		olympaPlayer.setGroup(OlympaGroup.DEV);
		AccountProviderAPI.cache.put(olympaPlayer.getUniqueId(), olympaPlayer);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerLogin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		OlympaPlayer olympaPlayer = AccountProviderAPI.getter().get(player.getUniqueId());

		if (olympaPlayer == null) {
			player.kickPlayer("§cUne erreur de données est survenu, merci de réessayer.");
			event.setJoinMessage(null);
			return;
		}

		event.setJoinMessage(ColorUtils.color("&7[&a+&7] %prefix%name".replaceAll("%group", olympaPlayer.getGroupName()).replaceAll("%prefix", olympaPlayer.getGroupPrefix()).replaceAll("%name", player.getDisplayName())));
		OlympaPlayerLoadEvent loginevent = new OlympaPlayerLoadEvent(player, olympaPlayer, false);
		Bukkit.getPluginManager().callEvent(loginevent);
	}

	@EventHandler
	public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
		UUID uuid = event.getUniqueId();
		init(new AccountProviderAPI(uuid).createOlympaPlayer(event.getName(), event.getAddress().getHostAddress()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerQuitHighest(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		AccountProviderAPI.cache.remove(player.getUniqueId());
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerQuitLow(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		OlympaPlayer olympaPlayer = AccountProviderAPI.getter().get(player.getUniqueId());
		if (olympaPlayer != null) {
			event.setQuitMessage(ColorUtils.color("&7[&c-&7] %prefix%name".replaceAll("%group", olympaPlayer.getGroupName()).replaceAll("%prefix", olympaPlayer.getGroupPrefix()).replaceAll("%name", player.getDisplayName())));
		} else {
			event.setQuitMessage(null);
		}
	}
}
