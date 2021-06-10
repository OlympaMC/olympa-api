package fr.olympa.api.spigot.enderchest;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.olympa.api.common.permission.OlympaPermission;
import fr.olympa.api.common.permission.OlympaSpigotPermission;
import fr.olympa.api.common.plugin.OlympaAPIPlugin;
import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.api.spigot.command.OlympaCommand;

public class EnderChestCommand extends OlympaCommand {

	private OlympaPermission permissionOther;
	private Map<EnderChestPlayerInterface, EnderChestGUI> guis = new HashMap<>();
	
	public EnderChestCommand(OlympaAPIPlugin plugin, OlympaSpigotPermission permission, OlympaSpigotPermission permissionOther) {
		super(plugin, "enderchest", "Ouvre l'enderchest.", permission, "ec", "enderc", "echest");
		this.permissionOther = permissionOther;
		setAllowConsole(false);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		EnderChestPlayerInterface target;
		if (args.length == 1 && hasPermission(permissionOther)) {
			target = AccountProviderAPI.getter().get(Bukkit.getPlayer(args[0]).getUniqueId());
		}else target = getOlympaPlayer();
		getEnderChestGUI(target).create(getPlayer());
		return true;
	}

	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 1 && hasPermission(permissionOther)) {
			String arg = args[0].toLowerCase();
			return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(name -> name.toLowerCase().startsWith(arg)).collect(Collectors.toList());
		}
		return Collections.EMPTY_LIST;
	}

	public EnderChestGUI getEnderChestGUI(EnderChestPlayerInterface player) {
		EnderChestGUI gui = guis.get(player);
		if (gui != null) return gui;
		gui = new EnderChestGUI(player);
		guis.put(player, gui);
		return gui;
	}
	
}
