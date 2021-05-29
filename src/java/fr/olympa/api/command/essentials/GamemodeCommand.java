package fr.olympa.api.command.essentials;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.match.RegexMatcher;
import fr.olympa.api.permission.list.OlympaAPIPermissionsSpigot;
import fr.olympa.api.utils.Prefix;

public class GamemodeCommand extends OlympaCommand {

	public enum Gm {
		SURVIVAL("survie"),
		CREATIVE("creatif"),
		ADVENTURE("aventure"),
		SPECTATOR("spectateur");

		private String name;
		private GameMode gamemode;
		
		private Gm(String name) {
			this.name = name;
			this.gamemode = GameMode.valueOf(name());
		}

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

		public GameMode getGameMode() {
			return gamemode;
		}

		public static Gm getByStartWith(String startWith) {
			return Arrays.stream(Gm.values()).filter(gm -> gm.getName().startsWith(startWith)).findFirst().orElse(null);
		}

		public static Gm get(String nameOrId) {
			Gm gamemode;
			if (RegexMatcher.INT.is(nameOrId))
				gamemode = Arrays.stream(Gm.values()).filter(gm -> gm.ordinal() == Integer.parseInt(nameOrId)).findFirst().orElse(null);
			else
				gamemode = Arrays.stream(Gm.values()).filter(gm -> gm.isName(nameOrId)).findFirst().orElse(null);
			return gamemode;
		}

		public boolean isGameMode(GameMode gameMode) {
			return gameMode.name().equals(name());
		}

		@SuppressWarnings ("deprecation")
		public static Gm get(GameMode gameMode) {
			return Gm.values()[gameMode.getValue()];
		}
	}

	private BiFunction<CommandSender, Player, Boolean> canExecute = (sender, target) -> true;

	public GamemodeCommand(Plugin plugin) {
		super(plugin, "gm", "Change ton mode de jeu.", OlympaAPIPermissionsSpigot.GAMEMODE_COMMAND, "gms", "gma", "gmc", "gmsp");
		addArgs(false, "adventure", "creative", "survival", "spectator", "JOUEUR");
		addArgs(false, "JOUEUR");
		allowConsole = true;
	}
	
	public void setCanExecuteFunction(BiFunction<CommandSender, Player, Boolean> function) {
		canExecute = function;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player target = null;
		Gm gm = null;
		boolean shortCommand = !label.equals("gm") && label.startsWith("gm");
		if (shortCommand) {
			gm = Gm.getByStartWith(label.substring(2));
		}else {
			if (args.length == 0) {
				sendIncorrectSyntax();
			}else gm = Gm.get(args[0]);
		}
		
		if (args.length == 0) {
			if (gm == null) {
				sendIncorrectSyntax();
				return false;
			}
			if (player == null) {
				sendImpossibleWithConsole();
				return false;
			}
			target = player;
		}else if (args.length == 1) {
			if (shortCommand || gm == null) {
				target = Bukkit.getPlayer(args[0]);
				if (target == null) {
					sendUnknownPlayer(args[0]);
					return false;
				}
			}else {
				if (player == null) {
					sendImpossibleWithConsole();
					return false;
				}
				target = player;
			}
		}else {
			if (gm == null) {
				sendError("Gamemode %s inconnu.", args[0]);
				return false;
			}
			target = Bukkit.getPlayer(args[1]);
			if (target == null) {
				sendUnknownPlayer(args[1]);
				return false;
			}
		}
		
		if (!canExecute.apply(sender, target))
			return false;
		
		if (gm == Gm.CREATIVE && !hasPermission(OlympaAPIPermissionsSpigot.GAMEMODE_COMMAND_CREATIVE)) {
			sendDoNotHavePermission();
			return false;
		}
		
		if (gm == null) {
			sendMessage(Prefix.DEFAULT_GOOD, "&2%s&a est en gamemode &2%s&a.", target.getName(), Gm.get(target.getGameMode()).getName());
		}else {
			Gm oldMode = Gm.get(target.getGameMode());
			if (gm == oldMode) {
				if (target != player)
					sendError("&4%s&c est déjà en gamemode &4%s&c.", target.getName(), gm.getName());
				else
					sendError("&cTu es déjà en gamemode &4%s&c.", gm.getName());
			}else {
				target.setGameMode(gm.getGameMode());
				if (target != player)
					sendSuccess("&2%s&a est désormais en gamemode &2%s&a (avant &2%s&a).", target.getName(), gm.getName(), oldMode.getName());
				Prefix.DEFAULT_GOOD.sendMessage(target, "&aTu es désormais en gamemode &2%s&a.", gm.getName());
			}
		}
		return false;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

}
