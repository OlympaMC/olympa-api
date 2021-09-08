package fr.olympa.api.spigot.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import fr.olympa.api.common.permission.OlympaSpigotPermission;
import fr.olympa.api.common.plugin.OlympaAPIPlugin;
import fr.olympa.api.common.task.OlympaTask;
import fr.olympa.api.spigot.command.OlympaCommand;
import fr.olympa.api.spigot.command.essentials.tp.TpaHandler;
import fr.olympa.api.utils.Prefix;

public class TeleportationManager implements Listener {

	public static final int TELEPORTATION_SECONDS = 3;
	public static final int TELEPORTATION_TICKS = TELEPORTATION_SECONDS * 20;

	private Set<OlympaCommand> commands = new HashSet<>();
	private Map<Player, Teleportation> teleportations = new HashMap<>();


	private final OlympaAPIPlugin plugin;
	@Nullable
	private final OlympaSpigotPermission bypassPermission;

	public TeleportationManager(OlympaAPIPlugin plugin, OlympaSpigotPermission bypassPermission) {
		this.plugin = plugin;
		this.bypassPermission = bypassPermission;
	}

	public Set<OlympaCommand> getCommands() {
		return commands;
	}

	public void addCommand(OlympaCommand command) {
		commands.add(command);
	}

	//	@Nullable
	//	public Teleportation remove(Player p) {
	//		//		OlympaTask taskManager = plugin.getTask();
	//		Teleportation teleportation = teleportations.remove(p);
	//		if (teleportation != null && teleportation.request != null)
	//			teleportation.request.invalidate();
	//		return teleportation;
	//	}

	public boolean teleport(Player p, Location to, String message) {
		return teleport(p, to, message, null);
	}

	public boolean teleport(Player p, Location to, String message, Runnable run) {
		return teleport(null, p, () -> to, message, run, null, null);
	}

	public boolean teleport(TpaHandler.Request request, String message, Runnable run, BooleanSupplier check, Runnable cancel) {
		return teleport(request, request.from, () -> request.to.getLocation(), message, run, check, cancel);
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

		Teleportation teleportationFromBefore = teleportations.remove(p);
		if (teleportationFromBefore != null) {
			if (teleportationFromBefore.taskId != 0)
				taskManager.cancelTaskById(teleportationFromBefore.taskId);
			if (teleportationFromBefore.request != null) {
				Prefix.DEFAULT_BAD.sendMessage(teleportationFromBefore.request.from, "La téléportation vers &4%s&c a été annulée.", request.to.getName());
				Prefix.DEFAULT_BAD.sendMessage(teleportationFromBefore.request.to, "Téléportation de &4%s&c &lVERS&c toi annulée.", request.from);
				teleportationFromBefore.request.invalidate();
			} else
				Prefix.INFO.sendMessage(p, "La téléportation précédente a été annulée.");
		}
		teleportations.put(p, new Teleportation(cancel, taskManager.runTaskLater(teleport, TELEPORTATION_TICKS), request));
		Prefix.INFO.sendMessage(p, "Téléportation dans " + TELEPORTATION_SECONDS + " secondes...");

		return true;
	}

	public boolean canTeleport(Player p) {
		return true;
	}

	public boolean canBypass(Player p) {
		return bypassPermission != null && bypassPermission.hasPermission(p.getUniqueId());
	}

	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		if (!SpigotUtils.isSameLocationXZ(e.getFrom(), e.getTo())) {
			Teleportation teleportation = teleportations.remove(e.getPlayer());
			if (teleportation != null) {
				if (teleportation.taskId != 0) {
					OlympaTask taskManager = plugin.getTask();
					taskManager.cancelTaskById(teleportation.taskId);
				}
				if (teleportation.cancel != null)
					teleportation.cancel.run();
				else
					Prefix.BAD.sendMessage(e.getPlayer(), "La téléportation a été annulée.");
			}
		}
	}

	record Teleportation(Runnable cancel, int taskId, TpaHandler.Request request) {}

}
