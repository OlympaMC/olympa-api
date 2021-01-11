package fr.olympa.api.command.essentials;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.permission.OlympaPermission;

public class BackCommand extends OlympaCommand implements Listener {

	public BackCommand(Plugin plugin, OlympaPermission permission) {
		super(plugin, "back", "Renvoie l'utilisateur au lieu de sa dernière mort.", permission);
		setAllowConsole(false);
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		List<MetadataValue> metadata = getPlayer().getMetadata("lastDeath");
		if (metadata.isEmpty()) {
			sendError("Tu n'es pas mort dernièrement.");
		}else {
			teleport(getPlayer(), (Location) metadata.get(0).value());
		}
		return false;
	}
	
	protected void teleport(Player p, Location location) {
		p.teleport(location);
		sendSuccess("Tu as été téléporté sur le lieu de ta mort.");
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		e.getEntity().setMetadata("lastDeath", new FixedMetadataValue(plugin, e.getEntity().getLocation()));
	}

}
