package fr.olympa.api.spigot.utils;

import java.util.HashMap;
import java.util.Map;

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
	
	private Map<Player, BukkitTask> teleportations = new HashMap<>();
	
	private final Plugin plugin;
	private final OlympaSpigotPermission bypassPermission;
	
	public TeleportationManager(Plugin plugin, OlympaSpigotPermission bypassPermission) {
		this.plugin = plugin;
		this.bypassPermission = bypassPermission;
	}
	
	public void teleport(Player p, Location to, String message) {
		teleport(p, to, message, null);
	}
	
	public void teleport(Player p, Location to, String message, Runnable run) {
		if (!canTeleport(p, to)) {
			Prefix.BAD.sendMessage(p, "La téléportation ne peut être effectuée.");
			return;
		}
		
		Runnable teleport = () -> {
			teleportations.remove(p);
			if (!p.isOnline()) return;
			p.teleport(to);
			p.sendMessage(message);
			if (run != null) run.run();
		};
		
		if (canBypass(p, to)) {
			teleport.run();
			return;
		}
		
		BukkitTask removed = teleportations.remove(p);
		if (removed != null) {
			removed.cancel();
			Prefix.INFO.sendMessage(p, "La téléportation précédente a été annulée.");
		}
		teleportations.put(p, Bukkit.getScheduler().runTaskLater(plugin, teleport, TELEPORTATION_TICKS));
		Prefix.INFO.sendMessage(p, "Téléportation dans " + TELEPORTATION_SECONDS + " secondes...");
	}
	
	public boolean canTeleport(Player p, Location to) {
		return true;
	}
	
	public boolean canBypass(Player p, Location to) {
		return bypassPermission.hasPermission(p.getUniqueId());
	}
	
	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		if (!SpigotUtils.isSameLocation(e.getFrom(), e.getTo())) {
			BukkitTask task = teleportations.remove(e.getPlayer());
			if (task != null) {
				task.cancel();
				Prefix.BAD.sendMessage(e.getPlayer(), "La téléportation a été annulée.");
			}
		}
	}
	
}
