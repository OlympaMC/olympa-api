package fr.olympa.api.spigot.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitTask;

import fr.olympa.api.common.permission.OlympaSpigotPermission;
import fr.olympa.api.common.plugin.OlympaAPIPlugin;
import fr.olympa.api.common.task.OlympaTask;
import fr.olympa.api.spigot.command.essentials.tp.TpaHandler;
import fr.olympa.api.utils.Prefix;

public class TeleportationManager implements Listener {

	public static final int TELEPORTATION_SECONDS = 3;
	public static final int TELEPORTATION_TICKS = TELEPORTATION_SECONDS * 20;

	private Map<Player, Teleportation> teleportations = new HashMap<>();

	private final OlympaAPIPlugin plugin;
	private final OlympaSpigotPermission bypassPermission;

	public TeleportationManager(OlympaAPIPlugin plugin, OlympaSpigotPermission bypassPermission) {
		this.plugin = plugin;
		this.bypassPermission = bypassPermission;
	}

	@Nullable
	public Teleportation remove(Player p) {
		OlympaTask taskManager = plugin.getTask();
		Teleportation removed = teleportations.remove(p);
		if (removed != null && removed.request != null)
			removed.request.invalidate();
		//			taskManager.runTask(removed.deleteRequest);
		return removed;
	}

	public boolean teleport(Player p, Location to, String message) {
		return teleport(p, to, message, null);
	}

	public boolean teleport(Player p, Location to, String message, Runnable run) {
		return teleport(null, p, () -> to, message, run, null, null);
	}

	public boolean teleport(TpaHandler.Request request, String message, Runnable run, BooleanSupplier check, Runnable cancel) {
		return teleport(request, request.from, () -> request.to.getLocation(), message, run, check, null);
	}

	public boolean teleport(Player p, Player to, String message, Runnable run, BooleanSupplier check, Runnable cancel) {
		return teleport(null, p, to::getLocation, message, run, check, null);
	}

	public boolean teleport(TpaHandler.Request request, Player p, Supplier<Location> to, String message, Runnable run, BooleanSupplier check, Runnable cancel) {
		if (!canTeleport(p)) {
			Prefix.BAD.sendMessage(p, "La téléportation ne peut être effectuée.");
			return false;
		}
		OlympaTask taskManager = plugin.getTask();

		Runnable teleport = () -> {
			teleportations.remove(p);
			if (check != null && !check.getAsBoolean()) return;
			if (!p.isOnline()) return;
			p.teleport(to.get());
			if (message != null) p.sendMessage(message);
			if (run != null) run.run();
		};

		if (canBypass(p)) {
			taskManager.runTask(teleport);
			return true;
		}

		Teleportation removed = teleportations.remove(p);
		if (removed != null) {
			removed.task.cancel();
			if (removed.request != null) {
				Prefix.DEFAULT_BAD.sendMessage(request.from, "La téléportation précédente vers &4%s&c a été annulée.", request.to.getName());
				Prefix.DEFAULT_BAD.sendMessage(request.to, "Téléportation de &4%s&c &lVERS&c toi annulée.", request.from);
				removed.request.invalidate();
			} else
				Prefix.INFO.sendMessage(p, "La téléportation précédente a été annulée.");
		}
		BukkitTask task = (BukkitTask) taskManager.getTask(taskManager.runTaskLater(teleport, TELEPORTATION_TICKS));
		teleportations.put(p, new Teleportation(cancel, task, request));
		Prefix.INFO.sendMessage(p, "Téléportation dans " + TELEPORTATION_SECONDS + " secondes...");

		return true;
	}

	public boolean canTeleport(Player p) {
		return true;
	}

	public boolean canBypass(Player p) {
		return bypassPermission.hasPermission(p.getUniqueId());
	}

	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		if (!SpigotUtils.isSameLocationXZ(e.getFrom(), e.getTo())) {
			Teleportation task = remove(e.getPlayer());
			if (task != null) {
				task.task.cancel();
				if (task.cancel != null)
					task.cancel.run();
				else
					Prefix.BAD.sendMessage(e.getPlayer(), "La téléportation a été annulée.");
			}
		}
	}

	record Teleportation(Runnable cancel, BukkitTask task, TpaHandler.Request request) {}

}
