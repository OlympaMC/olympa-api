package fr.olympa.api.spigot.command.essentials.tp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import fr.olympa.api.common.permission.OlympaSpigotPermission;
import fr.olympa.api.common.player.Gender;
import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.api.spigot.utils.TeleportationManager;
import fr.olympa.api.utils.Prefix;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class TpaHandler implements Listener {

	private Map<Request, Player> requestsMap = new HashMap<>();

	Plugin plugin;
	OlympaSpigotPermission permission;

	private TeleportationManager teleportationManager;

	public TpaHandler(Plugin plugin, OlympaSpigotPermission permission, TeleportationManager teleportationManager) {
		this.plugin = plugin;
		this.permission = permission;

		this.teleportationManager = teleportationManager;

		teleportationManager.addCommand(new TpaCommand(this).register());
		teleportationManager.addCommand(new TpaHereCommand(this).register());
		teleportationManager.addCommand(new TpConfirmCommand(this).register());
	}

	public void addRequest(Player player, Request request) {
		requestsMap.put(request, player);
		request.task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
			request.invalidate();
			if (request.from.isOnline())
				Prefix.BAD.sendMessage(request.from, "La téléportation vers &4%s&c a expiré.", request.to.getName());
			if (request.to.isOnline())
				Prefix.BAD.sendMessage(request.to, "La téléportation de &4%s&c §lvers toi§c a expiré.", request.from.getName());
		}, 60 * 20);
	}

	public Request getRequest(Player creator, Player target) {
		UUID creatorUUID = creator.getUniqueId();
		UUID targetUUID = target.getUniqueId();
		return requestsMap.entrySet().stream()
				.filter(entry -> entry.getValue().getUniqueId().equals(creatorUUID) && (entry.getKey().to.getUniqueId().equals(targetUUID) || entry.getKey().from.getUniqueId().equals(target.getUniqueId())))
				.map(Entry::getKey).findFirst().orElse(null);
	}

	public Map<Request, Player> getRequests() {
		return requestsMap;
	}

	public List<Request> getRequestsByPlayerTeleported(Player target) {
		return requestsMap.entrySet().stream().filter(entry -> entry.getKey().from.getUniqueId().equals(target.getUniqueId())).map(Entry::getKey).collect(Collectors.toList());
	}

	public Player getCreatorByTarget(Player target) {
		UUID targetUUID = target.getUniqueId();
		return requestsMap.entrySet().stream().filter(entry -> !entry.getValue().getUniqueId().equals(targetUUID) &&
				(entry.getKey().from.getUniqueId().equals(targetUUID) || entry.getKey().to.getUniqueId().equals(targetUUID)))
				.map(Entry::getValue).findFirst().orElse(null);
	}

	public void removeAllRequests(Player player) {
		UUID playerUUID = player.getUniqueId();
		requestsMap.entrySet().stream().filter(entry -> entry.getValue().getUniqueId().equals(playerUUID) || entry.getKey().from.getUniqueId().equals(playerUUID)
				|| entry.getKey().to.getUniqueId().equals(playerUUID)).map(Entry::getKey).toList().forEach(Request::invalidate);
	}

	private boolean testRequest(Player creator, Player target) {
		if (creator.getUniqueId().equals(target.getUniqueId())) {
			Prefix.DEFAULT_BAD.sendMessage(creator, "Impossible de te tp à toi même.");
			return false;
		}
		Request request = getRequest(creator, target);
		if (request != null) {
			Prefix.DEFAULT_BAD.sendMessage(creator, "Tu as déjà envoyé une demande à &4%s&c.", target.getName());
			return false;
		}
		return true;
	}

	public void sendRequestTo(Player creator, Player target) {
		if (!testRequest(creator, target))
			return;
		addRequest(creator, new Request(this, creator, target));
		target.spigot().sendMessage(getCompo(creator, "§2" + creator.getName() + "§e veut se téléporter à toi.", "§2Accepte la téléportation §lVERS§2 toi.", "§4Refuse la téléportation §lVERS§c toi."));
		Prefix.DEFAULT_GOOD.sendMessage(creator, "Tu as envoyé une requête à §2%s§a.", target.getName());
	}

	public void sendRequestHere(Player creator, Player target) {
		if (!testRequest(creator, target))
			return;
		addRequest(creator, new Request(this, target, creator));
		target.spigot()
		.sendMessage(getCompo(creator, "§4" + creator.getName() + "§e veut que §lTU§e te téléportes à §lLUI§e.", "§2Accepte de te téléporter à " + creator.getName() + ".", "§4Refuse de te téléporter à " + creator.getName() + "."));
		Prefix.DEFAULT_GOOD.sendMessage(creator, "Tu as envoyé une requête à §2%s§a.", target.getName());
	}

	private BaseComponent getCompo(Player creator, String message, String yesDesc, String noDesc) {
		TextComponent base = new TextComponent(TextComponent.fromLegacyText("§e§m§l--------------§6§l TPA §e§m§l--------------"));
		base.addExtra("\n\n");
		base.addExtra(new TextComponent(TextComponent.fromLegacyText(message)));
		base.addExtra("\n           ");
		TextComponent tp = new TextComponent(TextComponent.fromLegacyText("§2[§aOUI§2]"));
		tp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(yesDesc)));
		tp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpayes " + creator.getName()));
		base.addExtra(tp);
		base.addExtra("     ");
		tp = new TextComponent(TextComponent.fromLegacyText("§4[§cNON§4]"));
		tp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(noDesc)));
		tp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpano " + creator.getName()));
		base.addExtra(tp);
		base.addExtra("\n\n");
		base.addExtra(new TextComponent(TextComponent.fromLegacyText("§e§m§l--------------------------------")));
		return base;
	}

	public void acceptRequest(Player target, Player creator) {
		Request request = getRequest(creator, target);
		if (request == null) {
			Prefix.DEFAULT_BAD.sendMessage(target, "Tu n'as pas de demande de téléportation en attente...");
			return;
		}

		if (!creator.isOnline()) {
			Prefix.DEFAULT_BAD.sendMessage(target, "&4%s&c est maintenant hors-ligne.");
			return;
		}

		if (request.into) {
			Prefix.DEFAULT_BAD.sendMessage(target, "Tu es déjà en train de te faire téléporter !");
			return;
		}

		if (!teleportationManager.canTeleport(target)) {
			request.invalidate();
			return;
		}

		Gender fromGender = AccountProviderAPI.getter().get(request.from.getUniqueId()).getGender();
		boolean teleport = teleportationManager.teleport(request, null, () -> {
			String tune = fromGender.getTurne();
			Prefix.DEFAULT_GOOD.sendMessage(request.from, "Tu as été téléporté%s à §e%s§a.", tune, request.to.getName());
			Prefix.DEFAULT_GOOD.sendMessage(request.to, "§e%s §as'est téléporté%s à toi.", request.from.getName(), tune);
			request.invalidate();
		}, () -> {
			if (!request.from.isOnline())
				Prefix.DEFAULT_BAD.sendMessage(request.to, "&4%s&c s'est déconnecté, %s ne va pas se téléporter.", request.from.getName(), fromGender.getPronoun());
			else if (!request.to.isOnline())
				Prefix.DEFAULT_BAD.sendMessage(request.from, "&4%s&c s'est déconnecté, tu ne va pas te téléporter.", request.to.getName());
			else
				return true;
			request.invalidate();
			return false;
		}, () -> {
			request.into = false;
			Prefix.DEFAULT_BAD.sendMessage(request.from, "Téléportation annulée, tu ne dois pas te déplacer !");
			Prefix.DEFAULT_BAD.sendMessage(request.to, "Téléportation de &4%s&c &lVERS&c toi annulée, %s a bougé...", request.from.getName(), fromGender.getPronoun());
			request.invalidate();
		});

		if (teleport) {
			request.into = true;
			Prefix.INFO.sendMessage(request.from, "Téléportation vers %s.", request.to.getName());
			Prefix.INFO.sendMessage(request.to, "%s va se téléporter à toi.", request.from.getName());
		} else
			request.invalidate();
	}

	public void refuseRequest(Player target, Player creator) {
		Request request = getRequest(creator, target);
		if (request == null) {
			Prefix.DEFAULT_BAD.sendMessage(target, "Tu n'as pas de demande de téléportation en attente...");
			return;
		}
		request.invalidate();
		if (creator.isOnline())
			Prefix.DEFAULT_BAD.sendMessage(creator, "&4%s&c a refusé la demande de téléportation.", target.getName());
		Prefix.DEFAULT_GOOD.sendMessage(target, "Tu as refusé la demande de téléportation de &2%s&a.", creator.getName());
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		removeAllRequests(player);
	}

	public class Request {
		public TpaHandler tpaHandler;
		public Player from;
		public Player to;
		public BukkitTask task;
		public boolean into = false;

		public Request(TpaHandler tpaHandler, Player from, Player to) {
			this.tpaHandler = tpaHandler;
			this.from = from;
			this.to = to;
		}

		public TpaHandler getHandler() {
			return tpaHandler;
		}

		public void invalidate() {
			//			teleportationManager.remove(from); Not needed
			if (task != null && !task.isCancelled())
				task.cancel();
			requestsMap.remove(this);
		}
	}
}
