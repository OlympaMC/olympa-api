package fr.olympa.api.spigot.chat;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.google.gson.Gson;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.common.machine.TpsMessage;
import fr.olympa.api.common.module.OlympaModule.ModuleApi;
import fr.olympa.api.common.permission.list.OlympaAPIPermissionsSpigot;
import fr.olympa.api.spigot.command.OlympaCommand;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.spigot.OlympaCore;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_16_R3.IChatBaseComponent;
import net.minecraft.server.v1_16_R3.PacketPlayOutChat;

public class ChatCatcher extends OlympaCommand implements Listener, ModuleApi<OlympaCore> {

	private final Map<Player, List<TextComponent>> chatPlayer = new HashMap<>();

	private boolean isEnabled = false;
	private boolean isDebugEnable = true;
	
	private Field packetComponent;
	private Field packetUUID;

	public ChatCatcher(OlympaCore plugin) {
		super(plugin, "chatcatcher", "Affiche les messages reçus par les joueurs.", OlympaAPIPermissionsSpigot.CHATCATCHER_COMMAND);
		addArgs(false, "debug");
	}

	@Override
	public boolean isEnabled() {
		return isEnabled;
	}

	@Override
	public boolean enable(OlympaCore plugin) {
		isEnabled = true;
		plugin.getServer().getOnlinePlayers().forEach(p -> addPlayer(p));
		return isEnabled;
	}

	@Override
	public boolean disable(OlympaCore plugin) {
		isEnabled = false;
		for (Entry<Player, List<TextComponent>> entry : chatPlayer.entrySet())
			removePlayer(entry.getKey());
		chatPlayer.clear();
		return !isEnabled;
	}

	public boolean toggle(OlympaCore plugin) {
		boolean lastStatus = isEnabled;
		if (isEnabled)
			disable(plugin);
		else
			enable(plugin);
		return !lastStatus == isEnabled;
	}

	public boolean toggleDebug() {
		return isEnabled = !isEnabled;
	}

	@Override
	public boolean setToPlugin(OlympaCore plugin) {
		//		plugin.setAfkApi(this);
		return true;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length > 0 && args[0].equalsIgnoreCase("debug")) {
			if (toggleDebug())
				sendMessage(Prefix.DEFAULT_GOOD, "Le DEBUG du chat catcher a été activée.");
			else
				sendMessage(Prefix.DEFAULT_BAD, "Le DEBUG du chat catcher a été désactivée.");
			return false;
		}
		if (toggle(OlympaCore.getInstance())) {
			if (isEnabled)
				sendMessage(Prefix.DEFAULT_GOOD, "Le chat catcher a été activée.");
			else
				sendMessage(Prefix.DEFAULT_BAD, "Le chat catcher a été désactivée.");
		} else
			sendComponents(new TpsMessage(player == null).getInfoMessage().build());
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		//unhandlePlayerPacket(e.getPlayer()); pas besoin, à la déconnexion le channel se vide tout seul

		//		chatPlayer.remove(e.getPlayer())
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerJoin(PlayerJoinEvent e) {
		if (!isEnabled)
			return;
		if (!e.getPlayer().isOnline())
			return;
		addPlayer(e.getPlayer());
	}

	public boolean addPlayer(Player p) {
		//		if (!chatPlayer.containsKey(p) && handlePlayerPackets(p)) {
		//			chatPlayer.put(p, new ArrayList<>());
		//			return true;
		//		}
		List<TextComponent> list = new ArrayList<>();
		chatPlayer.put(p, list);
		injectPlayer(p, list);
		return false;
	}

	public boolean removePlayer(Player p) {
		unhandlePlayerPacket(p);
		return chatPlayer.remove(p) != null;
	}

	private void interceptPacket(Player p, ChannelHandlerContext ctx, PacketPlayOutChat packet, boolean isWrite) {
		TextComponent sendToConsole = new TextComponent(TextComponent.fromLegacyText("§7Text envoyé à §e" + p.getName() + "§7 " + packet.d() + " " + (isWrite ? "write" : "read") + "> &r"));
		if (packet.components != null) {
			TextComponent message = new TextComponent(packet.components);
			if (isDebugEnable)
				sendToConsole.addExtra(message);
			if (chatPlayer.containsKey(p))
				chatPlayer.get(p).add(message);
		} else
			sendToConsole.addExtra(new Gson().toJson(packet));
		OlympaCore.getInstance().getServer().getConsoleSender().sendMessage(sendToConsole);
	}

	private boolean handlePlayerPackets(Player p) {
		ChannelDuplexHandler channelDuplexHandler = new ChannelDuplexHandler() {
			@Override
			public void channelRead(ChannelHandlerContext ctx, Object object) throws Exception {
				try {
					if (object instanceof PacketPlayOutChat)
						interceptPacket(p, ctx, (PacketPlayOutChat) object, false);
				} catch (Exception e) {
					disable(OlympaCore.getInstance());
					e.printStackTrace();
				}
				super.channelRead(ctx, object);
			}

			@Override
			public void write(ChannelHandlerContext ctx, Object object, ChannelPromise promise) throws Exception {

				try {
					if (object instanceof PacketPlayOutChat)
						interceptPacket(p, ctx, (PacketPlayOutChat) object, true);
				} catch (Exception e) {
					disable(OlympaCore.getInstance());
					e.printStackTrace();
				}
				super.write(ctx, object, promise);
			}
		};
		ChannelPipeline pipeline = ((CraftPlayer) p).getHandle().playerConnection.networkManager.channel.pipeline();
		if (pipeline.get("packet_handler") == null) {
			OlympaCore.getInstance().sendMessage("§cImpossible de trouver le packet_handler pour le joueur §4%s§c. Impossible de récupérer les messages qu'il reçoit.", p.getName());
			return false;
		}
		pipeline.addBefore("packet_handler", p.getName() + "_ChatReader", channelDuplexHandler);
		return true;
	}

	private static void unhandlePlayerPacket(Player p) {
		Channel channel = ((CraftPlayer) p).getHandle().playerConnection.networkManager.channel;
		channel.eventLoop().submit(() -> {
			channel.pipeline().remove(p.getName() + "_ChatReader");
			return null;
		});
	}

	private void injectPlayer(Player player, List<TextComponent> messages) {
		ChannelDuplexHandler channelDuplexHandler = new ChannelDuplexHandler() {

			@Override
			public void write(ChannelHandlerContext ctx, Object object, ChannelPromise promise) throws Exception {
				if (object instanceof PacketPlayOutChat) {
					PacketPlayOutChat packet = (PacketPlayOutChat) object;
					try {
						if (packetComponent == null) {
							packetComponent = packet.getClass().getDeclaredField("a");
							packetComponent.setAccessible(true);
						}
						if (packetUUID == null) {
							packetUUID = packet.getClass().getDeclaredField("c");
							packetUUID.setAccessible(true);
						}
						String msg = CraftChatMessage.fromComponent((IChatBaseComponent) packetComponent.get(packet));
						String typeString = packet.d().name();
						UUID uuid = (UUID) packetUUID.get(packet);
						if (packet.components != null) {
							BaseComponent[] baseComponentMsg = packet.components;
							msg = BaseComponent.toLegacyText(baseComponentMsg);
							LinkSpigotBungee.Provider.link.sendMessage("§7Text %s BaseComponent envoyé à §e%s %s&7>&r", typeString, player.getName(), uuid != null ? uuid.toString() : "");
							OlympaCore.getInstance().getServer().getConsoleSender().sendMessage(baseComponentMsg);
						} else
							LinkSpigotBungee.Provider.link.sendMessage("§7Text %s envoyé à §e%s %s&7 > &r%s", typeString, player.getName(), uuid != null ? uuid.toString() : "", msg);
					} catch (Exception e) {
						e.printStackTrace();
						disable(OlympaCore.getInstance());
					}
				}
				super.write(ctx, object, promise);
			}
		};
		((CraftPlayer) player).getHandle().playerConnection.networkManager.channel.pipeline().addBefore("packet_handler", player.getName() + "_ChatReader", channelDuplexHandler);
	}
}
