package fr.olympa.api.command.essentials.tp;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.match.MatcherPattern;
import fr.olympa.api.match.RegexMatcher;
import fr.olympa.api.permission.OlympaAPIPermissions;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;

public class TpCommand extends OlympaCommand {

	public TpCommand(Plugin plugin) {
		super(plugin, "teleport", "Permet de se téléporter à un joueur ou une position.", OlympaAPIPermissions.TP_COMMAND, "tp");
		addArgs(false, "JOUEUR", "RELATIVE", "NUMBER");
		addArgs(false, "JOUEUR", "RELATIVE", "NUMBER");
		addArgs(false, "RELATIVE", "NUMBER");
		allowConsole = false;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		List<Entity> targets = new ArrayList<>();
		List<Entity> sources = new ArrayList<>();

		// tp @p|player
		if (args.length == 1) {
			sources.add(player);
			targets = getEntities(args, 0);
			if (targets == null)
				return false;
			// tp @p|player @p|player
		} else if (args.length == 2) {
			int i = 0;
			sources = getEntities(args, i);
			if (sources == null)
				return false;
			targets = getEntities(args, ++i);
			if (targets == null)
				return false;
			// tp 10 100 10 180 0
		} else if (args.length >= 3) {
			int i = 0;
			Player source = player;
			Location location = getLocation(args, i);
			if (location == null)
				return false;
			player.teleport(location);
			String turne = AccountProvider.get(source.getUniqueId()).getGender().getTurne();
			DecimalFormat formatter = new DecimalFormat("0.#");
			Prefix.DEFAULT_GOOD.sendMessage(source, "&aTu as été téléporté%s en &2%s %s %s&a.", turne, formatter.format(location.getX()), formatter.format(location.getY()), formatter.format(location.getZ()));
			return true;
		} else {
			sendUsage(label);
			return false;
		}
		Entity target = targets.get(0);
		sources.forEach(s -> {
			s.teleport(target);
			if (s instanceof Player) {
				String turne = AccountProvider.get(s.getUniqueId()).getGender().getTurne();
				if (s != player)
					sendMessage(Prefix.DEFAULT_GOOD, "&2%s&a a été téléporté à &2%s&a.", turne, s.getName(), target.getName());
				Prefix.DEFAULT_GOOD.sendMessage(s, "&aTu as été téléporté%s à &2%s&a.", turne, target.getName());
			}
		});
		return true;
	}

	private List<Entity> getEntities(String[] args, int i) {
		List<Entity> entities = new ArrayList<>();
		if (args[i].startsWith("@")) {
			entities = Bukkit.selectEntities(sender, args[i]);
			if (entities.isEmpty()) {
				sendError("Le selecteur &4%s&c n'est pas valide.", args[i]);
				return null;
			}
		} else if (!RegexMatcher.DOUBLE.is(args[i])) {
			Player source = Bukkit.getPlayer(args[i]);
			if (source == null) {
				sendUnknownPlayer(args[i]);
				return null;
			}
			entities.add(source);
		} else {
			sendError("&4%s&c n'est ni un joueur ni un selecteur.", args[i]);
			return null;
		}
		return entities;
	}

	private Location getLocation(String[] args, int i) {
		Location location = player.getLocation();
		MatcherPattern d = RegexMatcher.DOUBLE;
		MatcherPattern f = RegexMatcher.FLOAT;
		MatcherPattern r = RegexMatcher.RELATIVE;
		if (d.is(args[i]))
			location.setX((Double) d.parse(args[i]));
		else if (r.is(args[i]))
			location.add((Double) d.extractAndParse(args[i]), 0d, 0d);
		else {
			sendError("La position x = &4%s&c n'est pas valide.", args[i]);
			return null;
		}
		if (d.is(args[++i]))
			location.setY((Double) d.parse(args[i]));
		else if (r.is(args[i]))
			location.add(0d, (Double) d.extractAndParse(args[i]), 0d);
		else {
			sendError("La position y = &4%s&c n'est pas valide.", args[i]);
			return null;
		}
		if (d.is(args[++i]))
			location.setZ((Double) d.parse(args[i]));
		else if (r.is(args[i]))
			location.add(0d, 0d, (Double) d.extractAndParse(args[i]));
		else {
			sendError("La position z = &4%s&c n'est pas valide.", args[i]);
			return null;
		}
		if (args.length >= ++i + 2) {
			if (d.is(args[i]))
				location.setYaw((Float) f.parse(args[i]));
			else if (r.is(args[i]))
				location.setYaw(location.getYaw() + (Float) f.parse(args[i]));
			else {
				sendError("La position yaw = &4%s&c n'est pas valide.", args[i]);
				return null;
			}
			if (d.is(args[++i]))
				location.setPitch((Float) f.parse(args[i]));
			else if (r.is(args[i]))
				location.setPitch(location.getPitch() + (Float) f.parse(args[i]));
			else {
				sendError("La position pitch = &4%s&c n'est pas valide.", args[i]);
				return null;
			}
		}
		return location;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
}
