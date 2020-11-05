package fr.olympa.api.command.essentials;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.match.RegexMatcher;
import fr.olympa.api.permission.OlympaAPIPermissions;
import fr.olympa.api.utils.Prefix;

public class GamemodeCommand extends OlympaCommand {

	private enum Gm {
		SURVIVAL("survie"),
		CREATIVE("creatif"),
		ADVENTURE("aventure"),
		SPECTATOR("spectateur");

		private Gm(String name) {
			this.name = name;
		}

		String name;

		public String getName() {
			return name;
		}

		public String getRealName() {
			return toString().toLowerCase();
		}

		public boolean isName(String name) {
			name = name.toLowerCase();
			return this.name.equals(name) || getRealName().equals(name);
		}

		public int getId() {
			return ordinal();
		}

		@SuppressWarnings("deprecation")
		public GameMode getGameMode() {
			return GameMode.getByValue(getId());
		}

		public static Gm getByStartWith(String startWith) {
			return Arrays.stream(Gm.values()).filter(gm -> gm.getName().startsWith(startWith)).findFirst().orElse(null);
		}

		public static Gm get(String nameOrId) {
			Gm gamemode;
			if (RegexMatcher.INT.is(nameOrId))
				gamemode = Arrays.stream(Gm.values()).filter(gm -> gm.getId() == Integer.parseInt(nameOrId)).findFirst().orElse(null);
			else
				gamemode = Arrays.stream(Gm.values()).filter(gm -> gm.isName(nameOrId)).findFirst().orElse(null);
			return gamemode;
		}

		public boolean isGameMode(GameMode gameMode) {
			return gameMode.toString().equals(toString());
		}

		public static Gm get(GameMode gameMode) {
			return Arrays.stream(Gm.values()).filter(gm -> gm.isGameMode(gameMode)).findFirst().orElse(null);
		}
	}

	public GamemodeCommand(Plugin plugin) {
		super(plugin, "gamemode", "Change ton mode de jeux", OlympaAPIPermissions.GAMEMODE_COMMAND, "gm", "gms", "gma", "gmc", "gmsp");
		addArgs(false, "adventure", "creative", "survival", "spectator", "JOUEUR");
		addArgs(false, "JOUEUR");
		allowConsole = true;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player target = player;
		Gm gm = null;
		if (!label.equals("gm") && label.startsWith("gm"))
			gm = Gm.getByStartWith(label.substring(2));
		String targetName = null;
		if (gm == null) {

			if (args.length < 2 || (gm = Gm.get(args[0])) == null) {
				if (args.length >= 1) {
					target = Bukkit.getPlayer(args[0]);
					if (target == null) {
						sendUnknownPlayer(args[0]);
						return false;
					}
					sendMessage(Prefix.DEFAULT_GOOD, "&2%s&a est en gamemode &2%s&a.", target.getName(), Gm.get(target.getGameMode()).getName());
					return true;
				}
				return false;
			}
			targetName = args[1];
		} else if (args.length >= 1)
			targetName = args[0];

		if (targetName != null) {
			target = Bukkit.getPlayer(targetName);
			if (target == null) {
				sendUnknownPlayer(targetName);
				return false;
			}
		}
		if (gm == Gm.CREATIVE && !hasPermission(OlympaAPIPermissions.GAMEMODE_COMMAND_CREATIVE)) {
			sendDoNotHavePermission();
			return false;
		}
		Gm oldgm = Gm.get(target.getGameMode());
		if (oldgm == null) {
			sendError("Gamemode %s inconnu.", target.getGameMode().name().toLowerCase());
			return false;
		}
		String oldGamemode = oldgm.getName();
		if (gm.isGameMode(target.getGameMode())) {
			if (target != player)
				sendMessage(Prefix.DEFAULT_BAD, "&4%s&c est déjà en gamemode &4%s&c.", target.getName(), oldGamemode);
			else
				Prefix.DEFAULT_BAD.sendMessage(target, "&cTu es déjà en gamemode &4%s&c.", oldGamemode);
			return false;
		}
		target.setGameMode(gm.getGameMode());
		if (target != player)
			sendMessage(Prefix.DEFAULT_GOOD, "&2%s&a est désormais en gamemode &2%s&a (avant &2%s&a).", target.getName(), gm.getName(), oldGamemode);
		Prefix.DEFAULT_GOOD.sendMessage(target, "&aTu es désormais en gamemode &2%s&a.", gm.getName());
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

}
