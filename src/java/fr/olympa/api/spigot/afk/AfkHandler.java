package fr.olympa.api.spigot.afk;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.olympa.api.common.module.OlympaModule.ModuleApi;
import fr.olympa.api.common.permission.list.OlympaAPIPermissionsSpigot;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.api.spigot.customevents.AsyncOlympaPlayerChangeGroupEvent;
import fr.olympa.api.spigot.customevents.AsyncOlympaPlayerChangeGroupEvent.ChangeType;
import fr.olympa.api.spigot.scoreboard.tab.INametagApi;
import fr.olympa.api.spigot.scoreboard.tab.INametagApi.NametagHandler;
import fr.olympa.core.spigot.OlympaCore;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;

public class AfkHandler implements Listener, ModuleApi<OlympaCore> {

	private boolean isEnabled = false;
	private final Map<UUID, AfkPlayer> afkPlayers = new HashMap<>();
	private NametagHandler handler;

	@Override
	public boolean isEnabled() {
		return isEnabled;
	}

	@Override
	public boolean enable(OlympaCore plugin) {
		INametagApi nameTagApi = plugin.getNameTagApi();
		if (nameTagApi != null) {
			handler = (nametag, player, to) -> {
				if (isAfk((Player) player.getPlayer()) && (OlympaAPIPermissionsSpigot.AFK_SEE_IN_TAB.hasPermission(to) || player.getUniqueId().equals(to.getUniqueId())))
					nametag.appendSuffix(AfkPlayer.AFK_SUFFIX);
			};
			nameTagApi.addNametagHandler(EventPriority.HIGH, handler);
		} else
			plugin.sendMessage("&4AfkHandler &7> &cCan't add nameTagHandler because nameTagApi is disable.");
		isEnabled = true;
		return isEnabled;
	}

	@Override
	public boolean disable(OlympaCore plugin) {
		INametagApi nameTagApi = plugin.getNameTagApi();
		if (nameTagApi != null)
			nameTagApi.removeNametagHandler(handler);
		isEnabled = false;
		return !isEnabled;
	}

	@Override
	public boolean setToPlugin(OlympaCore plugin) {
		plugin.setAfkApi(this);
		return true;
	}

	public AfkPlayer get(Player player) {
		AfkPlayer afk = afkPlayers.get(player.getUniqueId());
		if (afk == null)
			afkPlayers.put(player.getUniqueId(), afk = new AfkPlayer(player));
		return afk;
	}

	public Set<Entry<UUID, AfkPlayer>> get() {
		return afkPlayers.entrySet();
	}

	public boolean isAfk(Player target) {
		AfkPlayer afkPlayer = afkPlayers.get(target.getUniqueId());
		return afkPlayer != null && afkPlayer.isAfk();
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		//unhandlePlayerPacket(e.getPlayer()); pas besoin, à la déconnexion le channel se vide tout seul

		AfkPlayer data = afkPlayers.remove(e.getPlayer().getUniqueId());
		if (data != null)
			data.cancelTask();
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerJoin(PlayerJoinEvent e) {
		if (!e.getPlayer().isOnline()) {
			OlympaCore.getInstance().sendMessage("§4%s §ca quitté avant de pouvoir setup son AFK handler.", e.getPlayer().getName());
			return;
		}
		if (handlePlayerPackets(e.getPlayer()))
			afkPlayers.put(e.getPlayer().getUniqueId(), new AfkPlayer(e.getPlayer()));
	}

	private boolean handlePlayerPackets(Player p) {
		ChannelDuplexHandler channelDuplexHandler = new ChannelDuplexHandler() {
			@Override
			public void channelRead(ChannelHandlerContext channelHandlerContext, Object handledPacket) throws Exception {
				super.channelRead(channelHandlerContext, handledPacket);
				if (afkPlayers.containsKey(p.getUniqueId()))
					afkPlayers.get(p.getUniqueId()).addToLog(handledPacket);
			}
		};

		ChannelPipeline pipeline = ((CraftPlayer) p).getHandle().playerConnection.networkManager.channel.pipeline();
		if (pipeline.get("packet_handler") == null) {
			OlympaCore.getInstance().sendMessage("§cImpossible de trouver le packet_handler pour le joueur §4%s§c. Sa détection d'AFK est désactivée.", p.getName());
			return false;
		}
		pipeline.addBefore("packet_handler", p.getName() + "_AfkHandler", channelDuplexHandler);
		return true;
	}

	@EventHandler
	public void onOlympaPlayerChangeGroup(AsyncOlympaPlayerChangeGroupEvent event) {
		ChangeType changeType = event.getChangeType();
		OlympaPlayer olympaPlayer = event.getOlympaPlayer();
		INametagApi nameTagApi = OlympaCore.getInstance().getNameTagApi();
		if (Arrays.stream(event.getGroupsChanges()).noneMatch(OlympaAPIPermissionsSpigot.AFK_SEE_IN_TAB::hasPermission))
			return;
		if ((ChangeType.SET.equals(changeType) || ChangeType.ADD.equals(changeType)) && OlympaAPIPermissionsSpigot.AFK_SEE_IN_TAB.hasPermission(olympaPlayer) || ChangeType.REMOVE.equals(changeType)) {
			List<OlympaPlayer> toPlayer = Arrays.asList(olympaPlayer);
			get().stream().forEach(entry -> {
				nameTagApi.callNametagUpdate(AccountProviderAPI.getter().get(entry.getKey()), toPlayer);
			});
		}
	}
}
