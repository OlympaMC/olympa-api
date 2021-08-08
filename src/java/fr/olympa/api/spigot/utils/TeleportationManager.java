package fr.olympa.api.spigot.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import fr.olympa.api.common.permission.OlympaSpigotPermission;
import fr.olympa.api.utils.Prefix;

public class TeleportationManager implements Listener {
	
	public static final int TELEPORTATION_SECONDS = 3;
	public static final int TELEPORTATION_TICKS = TELEPORTATION_SECONDS * 20;
	
	private Map<Player, Teleportation> teleportations = new HashMap<>();
	
	private final Plugin plugin;
	private final OlympaSpigotPermission bypassPermission;
	
	public TeleportationManager(Plugin plugin, OlympaSpigotPermission bypassPermission) {
		this.plugin = plugin;
		this.bypassPermission = bypassPermission;
	}
	
	public boolean teleport(Player p, Location to, String message) {
		return teleport(p, to, message, null);
	}
	
	public boolean teleport(Player p, Location to, String message, Runnable run) {
		return teleport(p, () -> to, message, run, null, null);
	}
	
	public boolean teleport(Player p, Player to, String message, Runnable run, BooleanSupplier check, Runnable cancel) {
		return teleport(p, to::getLocation, message, run, check, null);
	}
	
	public boolean teleport(Player p, Supplier<Location> to, String message, Runnable run, BooleanSupplier check, Runnable cancel) {
		if (!canTeleport(p)) {
			Prefix.BAD.sendMessage(p, "La téléportation ne peut être effectuée.");
			return false;
		}
		
		Runnable teleport = () -> {
			teleportations.remove(p);
			if (check != null && !check.getAsBoolean()) return;
			if (!p.isOnline()) return;
			p.teleport(to.get());
			if (message != null) p.sendMessage(message);
			if (run != null) run.run();
		};
		
		if (canBypass(p)) {
			Bukkit.getScheduler().runTask(plugin, teleport);
			return true;
		}
		
		Teleportation removed = teleportations.remove(p);
		if (removed != null) {
			removed.cancel();
			Prefix.INFO.sendMessage(p, "La téléportation précédente a été annulée.");
		}
		teleportations.put(p, new Teleportation(cancel, Bukkit.getScheduler().runTaskLater(plugin, teleport, TELEPORTATION_TICKS)));
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
			Teleportation task = teleportations.remove(e.getPlayer());
			if (task != null) {
				task.cancel();
				Prefix.BAD.sendMessage(e.getPlayer(), "La téléportation a été annulée.");
				if (task.cancel != null) task.cancel.run();
			}
		}
	}
	
	record Teleportation(Runnable cancel, BukkitTask task) {}
	
}
