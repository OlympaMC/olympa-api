package fr.olympa.api.command.essentials.tp;

import java.sql.SQLException;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
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
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalNotification;

import fr.olympa.api.permission.OlympaSpigotPermission;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.CacheStats;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.spigot.SpigotUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class TpaHandler implements Listener {

	private static int TELEPORTATION_SECONDS;
	private static int TELEPORTATION_TICKS;// = TELEPORTATION_SECONDS * 20;

	private Cache<Request, Player> requests = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).removalListener(this::invalidate).build();

	Plugin plugin;
	OlympaSpigotPermission permission;

	public TpaHandler(Plugin plugin, OlympaSpigotPermission permission) {
		this(plugin, permission, 3);
	}

	public TpaHandler(Plugin plugin, OlympaSpigotPermission permission, int tpDelay) {
		this.plugin = plugin;
		this.permission = permission;

		TELEPORTATION_SECONDS = tpDelay;
		TELEPORTATION_TICKS = tpDelay * 20;
		
		new TpaCommand(this).register();
		new TpaHereCommand(this).register();
		//		new TpHereConfirmCommand(this).register();
		new TpConfirmCommand(this).register();
		CacheStats.addCache("TPA_REQUESTS", requests);
	}

	private void invalidate(RemovalNotification<Request, Player> notif) {
		Request request = notif.getKey();
		if (request.task != null)
			request.task.cancel();
		else if (notif.getCause() == RemovalCause.EXPIRED) {
			if (request.from.isOnline())
				Prefix.BAD.sendMessage(request.from, "La téléportation vers &4%s&c a expiré.", request.to.getName());
			if (request.to.isOnline())
				Prefix.BAD.sendMessage(request.to, "La téléportation de &4%s&c §lVERS§c toi a expiré.", request.from.getName());
		}

	}

	public void addRequest(Player player, Request request) {
		requests.put(request, player);
	}

	public Request getRequest(Player creator, Player target) {
		UUID creatorUUID = creator.getUniqueId();
		UUID targetUUID = target.getUniqueId();
		return requests.asMap().entrySet().stream()
				.filter(entry -> entry.getValue().getUniqueId().equals(creatorUUID) && (entry.getKey().to.getUniqueId().equals(targetUUID) || entry.getKey().from.getUniqueId().equals(target.getUniqueId())))
				.map(Entry::getKey).findFirst().orElse(null);
	}

	public Cache<Request, Player> getRequests() {
		return requests;
	}

	public List<Request> getRequestsByPlayerTeleported(Player target) {
		return requests.asMap().entrySet().stream().filter(entry -> entry.getKey().from.getUniqueId().equals(target.getUniqueId())).map(Entry::getKey).collect(Collectors.toList());
	}

	public Player getCreatorByTarget(Player target) {
		UUID targetUUID = target.getUniqueId();
		return requests.asMap().entrySet().stream().filter(entry -> !entry.getValue().getUniqueId().equals(targetUUID) &&
				(entry.getKey().from.getUniqueId().equals(targetUUID) || entry.getKey().to.getUniqueId().equals(targetUUID)))
				.map(Entry::getValue).findFirst().orElse(null);
	}

	public void removeAllRequests(Player player) {
		UUID playerUUID = player.getUniqueId();
		requests.asMap().entrySet().stream().filter(entry -> entry.getValue().getUniqueId().equals(playerUUID) || entry.getKey().from.getUniqueId().equals(playerUUID)
				|| entry.getKey().to.getUniqueId().equals(playerUUID)).map(Entry::getKey).forEach(r -> requests.invalidate(r));
	}

	private boolean testRequest(Player creator, Player target) {
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
		addRequest(creator, new Request(creator, target));
		target.spigot().sendMessage(getCompo(creator, "§2" + creator.getName() + "§e veut se téléporter à toi.", "§2Accepte la téléportation §lVERS§2 toi.", "§4Refuse la téléportation §lVERS§c toi."));
		Prefix.DEFAULT_GOOD.sendMessage(creator, "Tu as envoyé une requête à §2%s§a.", target.getName());
	}

	public void sendRequestHere(Player creator, Player target) {
		if (!testRequest(creator, target))
			return;
		addRequest(creator, new Request(target, creator));
		target.spigot()
				.sendMessage(getCompo(creator, "§4" + creator.getName() + "§e veut que §lTU§e te téléporte à §lLUI§e.", "§2Accepte de te téléporter à " + creator.getName() + ".", "§4Refuse de te téléporter à " + creator.getName() + "."));
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

		if (request.task != null) {
			Prefix.DEFAULT_BAD.sendMessage(target, "Tu es déjà en train de te faire téléporter !");
			return;
		}
		
		if (TELEPORTATION_SECONDS > 0)
			Prefix.INFO.sendMessage(request.from, "Téléportation vers %s dans " + TELEPORTATION_SECONDS + " secondes...", request.to.getName());
		
		Prefix.INFO.sendMessage(request.to, "%s va se téléporter à toi.", request.from.getName());
		request.task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
			if (request.from.isOnline() && request.to.isOnline()) {
				String tune = AccountProvider.get(request.from.getUniqueId()).getGender().getTurne();
				Prefix.DEFAULT_GOOD.sendMessage(request.from, "Tu as été téléporté%s à §e%s§a.", tune, request.to.getName());
				Prefix.DEFAULT_GOOD.sendMessage(request.to, "§e%s §as'est téléporté%s à toi.", request.from.getName(), tune);
				request.from.teleport(request.to.getLocation());
				requests.invalidate(request);
			} else if (!request.from.isOnline() && request.to.isOnline())
				try {
					Prefix.DEFAULT_BAD.sendMessage(request.to, "&4%s&c s'est déconnecté, %s ne va pas se téléporter.", request.from.getName(), new AccountProvider(request.from.getUniqueId()).get().getGender().getTurne());
				} catch (SQLException e) {
					e.printStackTrace();
				}
			else if (!request.to.isOnline() && request.from.isOnline())
				try {
					Prefix.DEFAULT_BAD.sendMessage(request.from, "&4%s&c s'est déconnecté, %s ne va pas se téléporter.", request.to.getName(), new AccountProvider(request.to.getUniqueId()).get().getGender().getTurne());
				} catch (SQLException e) {
					e.printStackTrace();
				}

		}, TELEPORTATION_TICKS);
	}

	public void refuseRequest(Player target, Player creator) {
		Request request = getRequest(creator, target);
		if (request == null) {
			Prefix.DEFAULT_BAD.sendMessage(target, "Tu n'as pas de demande de téléportation en attente...");
			return;
		}
		requests.invalidate(request);
		if (creator.isOnline())
			Prefix.DEFAULT_BAD.sendMessage(creator, "&4%s&c a refusé la demande de téléportation.", target.getName());
		Prefix.DEFAULT_GOOD.sendMessage(target, "Tu as refusé la demande de téléportation de &2%s&a.", creator.getName());
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		removeAllRequests(player);
	}

	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		if (SpigotUtils.isSameLocationXZ(e.getFrom(), e.getTo()))
			return;
		Player player = e.getPlayer();
		OlympaPlayer olympaPlayer = AccountProvider.get(player.getUniqueId());
		List<Request> requestsTarget = getRequestsByPlayerTeleported(player);
		requestsTarget.forEach(r -> {
			if (r != null && r.task != null) {
				requests.invalidate(r);
				Prefix.DEFAULT_BAD.sendMessage(player, "Téléportation annulée, bouges pas !.");
				Prefix.DEFAULT_BAD.sendMessage(r.to, "Téléportation de &4%s&c &lVERS&c toi a été annulée, %s a bougé pendant la tp.", player.getName(), olympaPlayer.getGender().getPronoun());
			}
		});

	}

	class Request {
		public Player from;
		public Player to;
		public BukkitTask task;

		public Request(Player from, Player to) {
			this.from = from;
			this.to = to;
		}
	}
}
