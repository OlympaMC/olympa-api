package fr.olympa.api.command.essentials.tp;

import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalNotification;

import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.spigot.SpigotUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class TpaHandler implements Listener {
	
	private static final int TELEPORTATION_SECONDS = 3;
	private static final int TELEPORTATION_TICKS = TELEPORTATION_SECONDS * 20;
	
	private Cache<Player, Request> requests = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES).removalListener(this::invalidate).build();
	
	Plugin plugin;
	OlympaPermission permission;

	public TpaHandler(Plugin plugin, OlympaPermission permission) {
		this.plugin = plugin;
		this.permission = permission;
		
		new TpaCommand(this).register();
		new TpaHereCommand(this).register();
		new TpnoCommand(this).register();
		new TpyesCommand(this).register();
	}
	
	private void invalidate(RemovalNotification<Player, Request> notif) {
		if (notif.getValue().task != null) {
			notif.getValue().task.cancel();
			if (notif.getValue().from.isOnline()) Prefix.BAD.sendMessage(notif.getValue().from, "La téléportation a été annulée.");
		}
	}

	public void addRequest(Player player, Request request) {
		requests.put(player, request);
	}

	public void removeAllRequests(Player player) {
		requests.invalidate(player);
		Set<Entry<Player, Request>> toRemoved = requests.asMap().entrySet().stream().filter(entry -> entry.getKey().equals(player)).collect(Collectors.toSet());
		if (!toRemoved.isEmpty())
			requests.invalidateAll(toRemoved);
	}
	
	private boolean testRequest(Player player, Player target) {
		Request request = requests.getIfPresent(target);
		if (request != null && request.task != null) {
			Prefix.DEFAULT_BAD.sendMessage(player, "Ce joueur est déjà en train de se téléporter.");
			return false;
		}
		return true;
	}

	public void sendRequestTo(Player player, Player target) {
		if (!testRequest(player, target)) return;
		requests.invalidate(player); 
		addRequest(target, new Request(player, target));
		TextComponent base = new TextComponent(TextComponent.fromLegacyText("§m§l----------- TPA -----------"));
		base.addExtra("\n\n");
		base.addExtra(new TextComponent(TextComponent.fromLegacyText("§2" + player.getName() + "§7 veut se téléporter à toi.")));
		base.addExtra("\n");
		TextComponent tp = new TextComponent(TextComponent.fromLegacyText("§2[§aOUI§2]"));
		tp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, TextComponent.fromLegacyText("§2Accepte la téléportation §lVERS§2 toi.")));
		tp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpayes " + player.getName()));
		base.addExtra(tp);
		base.addExtra(" ");
		tp = new TextComponent(TextComponent.fromLegacyText("§4[§cNon§4]"));
		tp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, TextComponent.fromLegacyText("§4Refuse la téléportation §lVERS§c toi.")));
		tp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpano " + player.getName()));
		base.addExtra(tp);
		base.addExtra("\n\n");
		target.spigot().sendMessage(base);
		Prefix.DEFAULT_GOOD.sendMessage(player, "Tu as envoyé une requête à §2%s§a.", target.getName());
	}

	public void sendRequestHere(Player player, Player target) {
		if (!testRequest(player, target)) return;
		requests.invalidate(player);
		addRequest(target, new Request(target, player));
		TextComponent base = new TextComponent(TextComponent.fromLegacyText("§m§l----------- TPA -----------"));
		base.addExtra("\n\n");
		base.addExtra(new TextComponent(TextComponent.fromLegacyText("§4" + player.getName() + "§7 veut que §lTU§7 te téléporte à §lLUI§7.")));
		base.addExtra("\n");
		TextComponent tp = new TextComponent(TextComponent.fromLegacyText("§2[§aOUI§2]"));
		tp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, TextComponent.fromLegacyText("§2Accepte de te téléporter à " + player.getName() + ".")));
		tp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpahereyes " + player.getName()));
		base.addExtra(tp);
		base.addExtra(" ");
		tp = new TextComponent(TextComponent.fromLegacyText("§4[§cNon§4]"));
		tp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, TextComponent.fromLegacyText("§4Refuse de te téléporter à " + player.getName() + ".")));
		tp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpahereno " + player.getName()));
		base.addExtra(tp);
		base.addExtra("\n\n");
		target.spigot().sendMessage(base);
		Prefix.DEFAULT_GOOD.sendMessage(player, "Tu as envoyé une requête à §2%s§a.", target.getName());
	}
	
	public void acceptRequest(Player player) {
		Request request = requests.getIfPresent(player.getUniqueId());
		if (request == null) {
			Prefix.DEFAULT_BAD.sendMessage(player, "Tu n'as pas de demande de téléportation en attente...");
			return;
		}
		
		if (!request.from.isOnline() && !request.to.isOnline()) {
			Prefix.DEFAULT_BAD.sendMessage(player, "Le joueur qui t'as envoyé une demande de téléportation est maintenant hors-ligne.");
			return;
		}
		
		if (request.task != null) {
			Prefix.DEFAULT_BAD.sendMessage(player, "Tu es déjà en train de te faire téléporter !");
			return;
		}
		
		request.task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
			if (request.from.isOnline() && request.to.isOnline()) {
				Prefix.DEFAULT_GOOD.sendMessage(player, "Tu as été téléporté à §e%s§a.", request.to.getName());
				Prefix.DEFAULT_GOOD.sendMessage(player, "§e%s §as'est téléporté à toi.", request.from.getName());
				request.from.teleport(request.to.getLocation());
			}else Prefix.DEFAULT_BAD.sendMessage(player, "Le joueur qui t'as envoyé une demande de téléportation est maintenant hors-ligne.");
		}, TELEPORTATION_TICKS);
		Prefix.INFO.sendMessage(request.from, "Téléportation à %s dans " + TELEPORTATION_SECONDS + " secondes...", request.to.getName());
		Prefix.INFO.sendMessage(request.to, "%s va se téléporter à toi.", request.from.getName());
	}
	
	public void refuseRequest(Player player) {
		requests.invalidate(player);
		Prefix.DEFAULT_GOOD.sendMessage(player, "Tu as refusé la dernière demande de téléportation.");
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		removeAllRequests(player);
	}
	
	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		if (!SpigotUtils.isSameLocation(e.getFrom(), e.getTo())) {
			Request request = requests.getIfPresent(e.getPlayer());
			if (request != null && request.task != null) requests.invalidate(e.getPlayer());
		}
	}
	
	class Request {
		private Player from;
		private Player to;
		private BukkitTask task;
		
		public Request(Player from, Player to) {
			this.from = from;
			this.to = to;
		}
	}
}
