package fr.olympa.api.afk;

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

import fr.olympa.api.customevents.AsyncOlympaPlayerChangeGroupEvent;
import fr.olympa.api.customevents.AsyncOlympaPlayerChangeGroupEvent.ChangeType;
import fr.olympa.api.permission.OlympaAPIPermissions;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.scoreboard.tab.INametagApi;
import fr.olympa.core.spigot.OlympaCore;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;

public class AfkHandler implements Listener {

	private final Map<UUID, AfkPlayer> afkPlayers = new HashMap<>();

	public AfkHandler() {
		OlympaCore.getInstance().getNameTagApi().addNametagHandler(EventPriority.HIGH, (nametag, player, to) -> {
			if (isAfk(player.getPlayer()))
				nametag.appendSuffix(AfkPlayer.AFK_SUFFIX);
		});
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
		unhandlePlayerPacket(e.getPlayer());

		afkPlayers.remove(e.getPlayer().getUniqueId()).cancelTask();
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerJoin(PlayerJoinEvent e) {
		afkPlayers.put(e.getPlayer().getUniqueId(), new AfkPlayer(e.getPlayer()));

		handlePlayerPackets(e.getPlayer());
	}

	private void handlePlayerPackets(Player p) {

		ChannelDuplexHandler channelDuplexHandler = new ChannelDuplexHandler() {

			@Override
			public void channelRead(ChannelHandlerContext channelHandlerContext, Object handledPacket) throws Exception {
				super.channelRead(channelHandlerContext, handledPacket);

				if (afkPlayers.containsKey(p.getUniqueId()))
					afkPlayers.get(p.getUniqueId()).addToLog(handledPacket);
			}

			@Override
			public void write(ChannelHandlerContext channelHandlerContext, Object packet, ChannelPromise channelPromise) throws Exception {
				super.write(channelHandlerContext, packet, channelPromise);
			}
		};

		ChannelPipeline pipeline = ((CraftPlayer) p).getHandle().playerConnection.networkManager.channel.pipeline();
		pipeline.addBefore("packet_handler", p.getName() + "_AfkHandler", channelDuplexHandler);
	}

	private void unhandlePlayerPacket(Player p) {
		Channel channel = ((CraftPlayer) p).getHandle().playerConnection.networkManager.channel;
		channel.eventLoop().submit(() -> {
			channel.pipeline().remove(p.getName());
			return null;
		});
	}

	@EventHandler
	public void onOlympaPlayerChangeGroup(AsyncOlympaPlayerChangeGroupEvent event) {
		ChangeType changeType = event.getChangeType();
		OlympaPlayer olympaPlayer = event.getOlympaPlayer();
		INametagApi nameTagApi = OlympaCore.getInstance().getNameTagApi();
		if (Arrays.stream(event.getGroupsChanges()).noneMatch(OlympaAPIPermissions.AFK_SEE_IN_TAB::hasPermission))
			return;
		if ((ChangeType.SET.equals(changeType) || ChangeType.ADD.equals(changeType)) && OlympaAPIPermissions.AFK_SEE_IN_TAB.hasPermission(olympaPlayer) || ChangeType.REMOVE.equals(changeType)) {
			List<OlympaPlayer> toPlayer = Arrays.asList(olympaPlayer);
			get().stream().forEach(entry -> {
				nameTagApi.callNametagUpdate(AccountProvider.get(entry.getKey()), toPlayer);
			});
		}
	}
}
