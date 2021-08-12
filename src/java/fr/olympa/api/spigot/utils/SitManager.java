package fr.olympa.api.spigot.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.block.data.type.Stairs.Shape;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.spigotmc.event.entity.EntityDismountEvent;

import fr.olympa.api.utils.Prefix;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class SitManager implements Listener {
	
	private Map<Location, Sitting> sits = new HashMap<>();
	private Plugin plugin;
	
	public SitManager(Plugin plugin) {
		this.plugin = plugin;
	}
	
	public boolean canSit(Player p) {
		return p.getGameMode() != GameMode.CREATIVE && p.getGameMode() == GameMode.SPECTATOR;
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		Player p = e.getPlayer();
		
		if (p.isInsideVehicle()) return;
		if (p.isSneaking()) return;
		if (e.getClickedBlock().getType().name().endsWith("_STAIRS")) {
			if (!e.getClickedBlock().equals(p.getTargetBlockExact(3))) return;
			if (canSit(p)) {
				p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§cVous ne pouvez pas vous asseoir maintenant."));
				return;
			}
			if (sits.containsKey(e.getClickedBlock().getLocation())) {
				Prefix.BAD.sendMessage(p, "Cette chaise est déjà occupée.");
			}else {
				Stairs stairs = (Stairs) e.getClickedBlock().getBlockData();
				if (stairs.getShape() != Shape.STRAIGHT || stairs.getHalf() == Half.TOP) return;
				if (e.getClickedBlock().getRelative(BlockFace.UP).getType() != Material.AIR) return;
				BlockFace facing = stairs.getFacing();
				double xMod = facing.getModX() * -0.12;
				double zMod = facing.getModZ() * -0.12;
				Location location = e.getClickedBlock().getLocation().add(0.5 + xMod, 0.32, 0.5 + zMod);
				location.setYaw(facing == BlockFace.SOUTH ? 180f : (facing.getModX() * 90f));
				ArmorStand stand = p.getWorld().spawn(location, ArmorStand.class, x -> {
					x.setPersistent(false);
					x.setInvisible(true);
					x.setMarker(true);
					x.setSmall(true);
					x.setGravity(false);
				});
				Location previous = p.getLocation();
				stand.addPassenger(p);
				sits.put(e.getClickedBlock().getLocation(), new Sitting(p, previous));
				Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§aTu es maintenant assis !")), 3);
			}
		}
	}
	
	@EventHandler
	public void onLeaveVehicle(EntityDismountEvent e) {
		for (Iterator<Sitting> iterator = sits.values().iterator(); iterator.hasNext();) {
			Sitting sitting = iterator.next();
			if (sitting.player().equals(e.getEntity())) {
				if (!e.getEntity().isDead()) {
					Bukkit.getScheduler().runTask(plugin, () -> {
						sitting.teleportBack();
						e.getDismounted().remove();
					});
				}
				iterator.remove();
				break;
			}
		}
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		for (Iterator<Sitting> iterator = sits.values().iterator(); iterator.hasNext();) {
			Sitting sitting = iterator.next();
			if (sitting.player().equals(e.getPlayer())) {
				sitting.teleportBack();
				iterator.remove();
				break;
			}
		}
	}
	
}

record Sitting(Player player, Location previous) {
	
	public void teleportBack() {
		player.teleport(previous.setDirection(player.getLocation().getDirection()));
	}
	
}
