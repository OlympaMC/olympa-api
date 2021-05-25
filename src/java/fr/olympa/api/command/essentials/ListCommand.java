package fr.olympa.api.command.essentials;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.permission.OlympaSpigotPermission;
import fr.olympa.api.player.Gender;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;

public class ListCommand extends OlympaCommand {
	
	public ListCommand(Plugin plugin) {
		super(plugin, "list", "Affiche une liste des joueurs connectés.", (OlympaSpigotPermission) null);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Collection<? extends Player> players = Bukkit.getOnlinePlayers();
		sendSuccess("Il y a §2%s§a joueur%s en ligne:", players.size(), players.size() == 1 ? "" : "s");
		Map<OlympaGroup, List<Player>> groups = new EnumMap<>(OlympaGroup.class);
		for (Player p : players) {
			OlympaPlayer player = AccountProvider.get(p.getUniqueId());
			List<Player> groupPlayers = groups.computeIfAbsent(player.getGroup(), group -> new ArrayList<>());
			groupPlayers.add(p);
		}
		groups.entrySet().stream().sorted((o1, o2) -> Integer.compare(o1.getKey().ordinal(), o2.getKey().ordinal())).forEach(entry -> {
			sender.sendMessage("§7➤ " + entry.getKey().getColor() + entry.getKey().getName(Gender.UNSPECIFIED) + "§8 (" + entry.getValue().size() + "): §7" + entry.getValue().stream().map(Player::getName).collect(Collectors.joining(", ")));
		});
		return false;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
	
}
