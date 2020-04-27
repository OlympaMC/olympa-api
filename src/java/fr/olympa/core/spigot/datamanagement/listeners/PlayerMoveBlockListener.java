package fr.olympa.core.spigot.datamanagement.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import fr.olympa.api.customevents.PlayerMoveBlockEvent;
import fr.olympa.api.customevents.PlayerMoveBlockXZEvent;
import fr.olympa.api.customevents.PlayerMoveBlockYEvent;

public class PlayerMoveBlockListener implements Listener {

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Location loc1 = event.getFrom();
		Location loc2 = event.getTo();

		if (loc1.getBlockX() != loc2.getBlockX() || loc1.getBlockZ() != loc2.getBlockZ()) {
			Bukkit.getPluginManager().callEvent(new PlayerMoveBlockXZEvent(event));

			if (loc1.getBlockY() != loc2.getBlockY()) {
				Bukkit.getPluginManager().callEvent(new PlayerMoveBlockYEvent(event));
				Bukkit.getPluginManager().callEvent(new PlayerMoveBlockEvent(event));
			}

		} else if (loc1.getBlockY() != loc2.getBlockY()) {
			Bukkit.getPluginManager().callEvent(new PlayerMoveBlockYEvent(event));
		}
	}
}
