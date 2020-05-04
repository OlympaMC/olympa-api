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

import fr.olympa.api.customevents.OlympaPlayerLoadEvent;
import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.ColorUtils;

public class SmallDataManagmentListener implements Listener {

	{
		Bukkit.getOnlinePlayers().forEach(player -> init(new AccountProvider(player.getUniqueId()).createOlympaPlayer(player.getName(), player.getAddress().getAddress().getHostAddress())));
	}

	private void init(OlympaPlayer olympaPlayer) {
		olympaPlayer.setGroup(OlympaGroup.DEV);
		AccountProvider.cache.put(olympaPlayer.getUniqueId(), olympaPlayer);
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
