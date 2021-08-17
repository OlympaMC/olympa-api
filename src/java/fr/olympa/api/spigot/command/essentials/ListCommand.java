package fr.olympa.api.spigot.command.essentials;

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

import fr.olympa.api.common.groups.OlympaGroup;
import fr.olympa.api.common.permission.OlympaSpigotPermission;
import fr.olympa.api.common.permission.list.OlympaAPIPermissionsSpigot;
import fr.olympa.api.common.player.Gender;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.api.spigot.afk.AfkHandler;
import fr.olympa.api.spigot.command.OlympaCommand;
import fr.olympa.api.spigot.vanish.IVanishApi;
import fr.olympa.core.spigot.OlympaCore;

public class ListCommand extends OlympaCommand {

	public ListCommand(Plugin plugin) {
		super(plugin, "list", "Affiche une liste des joueurs connectés.", (OlympaSpigotPermission) null);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Collection<? extends Player> players = Bukkit.getOnlinePlayers();
		boolean canSeeVanish = hasPermission(OlympaAPIPermissionsSpigot.VANISH_SEE);
		Map<OlympaGroup, List<Player>> groups = new EnumMap<>(OlympaGroup.class);
		OlympaCore core = OlympaCore.getInstance();
		AfkHandler afk = core.getAfkHandler();
		IVanishApi vanish = core.getVanishApi();
		int onlineCount = 0;
		for (Player p : players) {
			if (!canSeeVanish && vanish.isVanished(p))
				continue;
			OlympaPlayer player = AccountProviderAPI.getter().get(p.getUniqueId());
			List<Player> groupPlayers = groups.computeIfAbsent(player.getGroup(), group -> new ArrayList<>());
			groupPlayers.add(p);
			onlineCount++;
		}
		sendSuccess("Il y a §2%s§a joueur%s en ligne:", onlineCount, onlineCount == 1 ? "" : "s");
		groups.entrySet().stream().sorted((o1, o2) -> Integer.compare(o1.getKey().ordinal(), o2.getKey().ordinal())).forEach(entry -> {
			sender.sendMessage("§7➤ " + entry.getKey().getColor() + entry.getKey().getName(Gender.UNSPECIFIED) + "§8 (" + entry.getValue().size() + "): §7"
					+ entry.getValue().stream().map(x -> x.getName() + (afk != null && afk.isAfk(x) ? "§8 (AFK)§7" : "") + (canSeeVanish && vanish != null && vanish.isVanished(x) ? "§8 (VANISH)§7" : "")).collect(Collectors.joining(", ")));
		});
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

}
