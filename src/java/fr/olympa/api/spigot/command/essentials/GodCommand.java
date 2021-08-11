package fr.olympa.api.spigot.command.essentials;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.common.permission.list.OlympaAPIPermissionsSpigot;
import fr.olympa.api.spigot.command.OlympaCommand;
import fr.olympa.api.utils.Prefix;

public class GodCommand extends OlympaCommand implements Listener {

	public boolean isGodModEnable(Player p) {
		for (MetadataValue meta : p.getMetadata("god"))
			if (meta.asBoolean())
				return true;
		return false;
	}

	public boolean toggle(Player p) {
		if (isGodModEnable(p)) {
			disable(p);
			return false;
		} else {
			enable(p);
			return true;
		}
	}

	public void enable(Player p) {
		player.setMetadata("god", new FixedMetadataValue(plugin, true));
	}

	public void disable(Player p) {
		player.removeMetadata("god", plugin);
	}

	public GodCommand(Plugin plugin) {
		super(plugin, "god", "Rends invincible.", OlympaAPIPermissionsSpigot.GOD_COMMAND, "g");
		setAllowConsole(false);
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (toggle(player))
			sendMessage(Prefix.DEFAULT_GOOD, "Tu es désormais invincible.");
		else
			sendMessage(Prefix.DEFAULT_BAD, "Tu n'es plus invincible.");
		return false;
	}


	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (!(event.getEntity() instanceof Player))
			return;
		Player victim = (Player) event.getEntity();
		if (!isGodModEnable(victim))
			return;
		event.setDamage(0);
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityDamageByEntity(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player))
			return;
		Player victim = (Player) event.getEntity();
		if (!isGodModEnable(victim))
			return;
		event.setDamage(0);
		event.setCancelled(true);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

}
