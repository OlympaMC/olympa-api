package fr.olympa.api.spigot.chat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
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
import fr.olympa.api.spigot.utils.Reflection;
import fr.olympa.api.spigot.utils.Reflection.ClassEnum;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.spigot.OlympaCore;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_16_R3.PacketPlayOutChat;

public class ChatCatcher extends OlympaCommand implements Listener, ModuleApi<OlympaCore> {

	private final Map<Player, List<TextComponent>> chatPlayer = new HashMap<>();

	private boolean isEnabled = false;
	private boolean isDebugEnable = true;

	public ChatCatcher(OlympaCore plugin) {
		super(plugin, "chatcatcher", "Affiche des informations sur l'état du serveur.", OlympaAPIPermissionsSpigot.CHATCATCHER_COMMAND);
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
			private Field iChatBaseComponent;
			private Field components;
			private Field chatMessageType;
			private Field uuidField;

			public boolean needToBeFill() {
				return iChatBaseComponent == null || components == null || chatMessageType == null || uuidField == null;
			}

			@Override
			public void channelRead(ChannelHandlerContext channelHandlerContext, Object packet) throws Exception {
				String name = packet.getClass().getSimpleName();
				Class<?> type = packet.getClass();
				super.channelRead(channelHandlerContext, packet);
			}

			@Override
			public void write(ChannelHandlerContext ctx, Object packet, ChannelPromise promise) throws Exception {
				String name = packet.getClass().getSimpleName();
				Class<?> type = packet.getClass();
				if (name.contains("PacketPlayOutChat"))
					try {
						Class<?> icbc = Reflection.getClass(ClassEnum.NMS, "IChatBaseComponent");
						Class<?> cmt = Reflection.getClass(ClassEnum.NMS, "ChatMessageType");
						if (needToBeFill()) {
							List<Field> fields = new ArrayList<>();
							fields.addAll(Arrays.asList(type.getFields()));
							fields.addAll(Arrays.asList(type.getDeclaredFields()));
							fields = new ArrayList<>(new HashSet<>(fields));
							for (Field field : fields) {
								if (!field.isAccessible())
									field.setAccessible(true);
								if (iChatBaseComponent == null)
									if (field.getType().getName().equalsIgnoreCase(icbc.getName())) {
										iChatBaseComponent = field;
										if (!field.isAccessible())
											field.setAccessible(true);
									}
								if (components == null)
									if (field.getType().getSimpleName().equalsIgnoreCase("BaseComponent[]")) {
										components = field;
										if (!field.isAccessible())
											field.setAccessible(true);
									}
								if (chatMessageType == null)
									if (field.getType().getSimpleName().equalsIgnoreCase("ChatMessageType")) {
										chatMessageType = field;
										if (!field.isAccessible())
											field.setAccessible(true);
									}
								if (uuidField == null)
									if (field.getType().getSimpleName().equalsIgnoreCase("UUID")) {
										uuidField = field;
										if (!field.isAccessible())
											field.setAccessible(true);
									}
							}
						}
						if (iChatBaseComponent == null && components == null && chatMessageType == null && uuidField == null) {
							super.write(ctx, packet, promise);
							return;
						}
						boolean accessible = iChatBaseComponent != null ? iChatBaseComponent.isAccessible() : false;
						boolean accessible2 = components != null ? components.isAccessible() : false;
						boolean accessible3 = chatMessageType != null ? chatMessageType.isAccessible() : false;
						boolean accessible4 = uuidField != null ? uuidField.isAccessible() : false;
						if (iChatBaseComponent != null)
							iChatBaseComponent.setAccessible(true);
						if (components != null)
							components.setAccessible(true);
						if (chatMessageType != null)
							chatMessageType.setAccessible(true);
						if (uuidField != null)
							uuidField.setAccessible(true);
						String msg = iChatBaseComponent == null ? ""
								: "" + Reflection.getClass(ClassEnum.CB, "util.CraftChatMessage")
										.getMethod("fromComponent", icbc).invoke(null, icbc.cast(iChatBaseComponent.get(packet)));
						String typeString = "";
						if (chatMessageType != null)
							typeString = chatMessageType.get(packet).toString();
						UUID uuid = null;
						if (uuidField != null) {
							Object obj = uuidField.get(packet);
							if (obj != null)
								uuid = (UUID) uuidField.get(packet);
						}
						if (components != null && components.get(packet) != null) {
							BaseComponent[] baseComponentMsg = (BaseComponent[]) components.get(packet);
							msg = BaseComponent.toLegacyText(baseComponentMsg);
							LinkSpigotBungee.Provider.link.sendMessage("§7Text %s BaseComponent envoyé à §e%s %s&7>&r", typeString, player.getName(), uuid != null ? uuid.toString() : "");
							OlympaCore.getInstance().getServer().getConsoleSender().sendMessage(baseComponentMsg);
						} else
							LinkSpigotBungee.Provider.link.sendMessage("§7Text %s envoyé à §e%s %s&7 > &r%s", typeString, player.getName(), uuid != null ? uuid.toString() : "", msg);
						if (iChatBaseComponent != null)
							iChatBaseComponent.setAccessible(accessible);
						if (components != null)
							components.setAccessible(accessible2);
						if (chatMessageType != null)
							chatMessageType.setAccessible(accessible3);
						if (uuidField != null)
							uuidField.setAccessible(accessible4);
					} catch (Exception e) {
						e.printStackTrace();
						disable(OlympaCore.getInstance());
					}
				super.write(ctx, packet, promise);
			}
		};
		getChannel(player).pipeline().addBefore("packet_handler", player.getName() + "_ChatReader", channelDuplexHandler);
	}

	Channel getChannel(Player player) {
		Channel channel = null;
		try {
			Class<?> cp = player.getClass();
			Method handle = cp.getMethod("getHandle");
			Object ep = handle.invoke(cp.cast(player));

			Field f = ep.getClass().getField("playerConnection");
			Field n = f.get(ep).getClass().getField("networkManager");
			Object x = null;
			x = n.get(f.get(ep));
			Field c = x.getClass().getField("channel");
			x = c.get(n.get(f.get(ep)));
			channel = (Channel) x;
		} catch (Exception e) {
			e.printStackTrace();
			disable(OlympaCore.getInstance());
			return null;
		}
		return channel;
	}
}
