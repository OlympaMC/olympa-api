package fr.olympa.api.afk;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.olympa.api.customevents.AsyncOlympaPlayerChangeGroupEvent;
import fr.olympa.api.customevents.AsyncOlympaPlayerChangeGroupEvent.ChangeType;
import fr.olympa.api.customevents.OlympaPlayerLoadEvent;
import fr.olympa.api.permission.OlympaAPIPermissions;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.scoreboard.tab.INametagApi;
import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.core.spigot.OlympaCore;

public class AfkHandler implements Listener {

	private final Map<UUID, AfkPlayer> lastActions = new HashMap<>();

	public AfkHandler() {
		OlympaCore.getInstance().getNameTagApi().addNametagHandler(EventPriority.HIGH, (nametag, player, to) -> {
			if (isAfk(player.getPlayer())) nametag.appendSuffix(AfkPlayer.AFK_SUFFIX);
		});
	}
	
	public void updateLastAction(Player player, boolean afk) {
		AfkPlayer afkPlayer = get(player);
		boolean oldStatue = afkPlayer.isAfk();
		if (afk == oldStatue) {
			if (!afk)
				afkPlayer.launchTask(player);
		} else if (afk)
			afkPlayer.setAfk(player);
		else
			afkPlayer.setNotAfk(player);
	}

	public AfkPlayer get(Player player) {
		AfkPlayer afk = lastActions.get(player.getUniqueId());
		if (afk == null) {
			afk = new AfkPlayer(player);
			lastActions.put(player.getUniqueId(), afk);
		}
		return afk;
	}

	public Set<Entry<UUID, AfkPlayer>> get() {
		return lastActions.entrySet();
	}

	public void removeLastAction(Player player) {
		AfkPlayer afkPlayer = lastActions.remove(player.getUniqueId());
		if (afkPlayer != null)
			afkPlayer.disableTask();
	}

	public boolean isAfk(Player target) {
		AfkPlayer afkPlayer = lastActions.get(target.getUniqueId());
		return afkPlayer != null && afkPlayer.isAfk();
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		removeLastAction(player);
	}
	
	@EventHandler (priority = EventPriority.HIGH)
	public void onPlayerJoin(OlympaPlayerLoadEvent event) {
		Player player = event.getPlayer();
		updateLastAction(player, false);
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		if (SpigotUtils.isSameLocationXZ(event.getFrom(), event.getTo()))
			return;
		updateLastAction(player, false);
	}
	
	@EventHandler
	public void onOlympaPlayerChangeGroup(AsyncOlympaPlayerChangeGroupEvent event) {
		ChangeType changeType = event.getChangeType();
		OlympaPlayer olympaPlayer = event.getOlympaPlayer();
		INametagApi nameTagApi = OlympaCore.getInstance().getNameTagApi();
		if (Arrays.stream(event.getGroupsChanges()).noneMatch(OlympaAPIPermissions.AFK_SEE_IN_TAB::hasPermission))
			return;
		if (((ChangeType.SET.equals(changeType) || ChangeType.ADD.equals(changeType)) && OlympaAPIPermissions.AFK_SEE_IN_TAB.hasPermission(olympaPlayer)) || ChangeType.REMOVE.equals(changeType)) {
			List<OlympaPlayer> toPlayer = Arrays.asList(olympaPlayer);
			get().stream().forEach(entry -> {
				nameTagApi.callNametagUpdate(AccountProvider.get(entry.getKey()), toPlayer);
			});
		}
	}
}
