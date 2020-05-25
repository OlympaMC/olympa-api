package fr.olympa.api.command;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;
import net.md_5.bungee.api.chat.BaseComponent;

public abstract class OlympaCommand {

	protected static Map<String, OlympaCommand> commandPreProcess = new HashMap<>();
	protected static CommandMap cmap;
	protected Plugin plugin;
	protected List<String> alias;
	protected LinkedHashMap<Boolean, List<String>> args = new LinkedHashMap<>();
	protected String command;
	protected String description;
	protected OlympaPermission permission;
	protected Player player;
	protected CommandSender sender;
	protected String usageString;
	protected Integer minArg = 0;
	protected boolean allowConsole = true;
	protected boolean isAsynchronous = false;

	public OlympaCommand(Plugin plugin, String command, OlympaPermission permission, String... alias) {
		this.plugin = plugin;
		this.command = command;
		this.permission = permission;
		this.alias = Arrays.asList(alias);
	}

	public OlympaCommand(Plugin plugin, String command, String... alias) {
		this.plugin = plugin;
		this.command = command;
		this.alias = Arrays.asList(alias);
	}

	public OlympaCommand(Plugin plugin, String command, String description, OlympaPermission permission, String... alias) {
		this.plugin = plugin;
		this.permission = permission;
		this.command = command;
		this.description = description;
		this.alias = Arrays.asList(alias);
	}

	public void addArgs(boolean isMandatory, List<String> args) {
		this.args.put(isMandatory, args);
	}

	public void addArgs(boolean isMandatory, String... args) {
		addArgs(isMandatory, Arrays.asList(args));
	}

	public String buildText(int min, String[] args) {
		return String.join(" ", Arrays.copyOfRange(args, min, args.length));
	}

	CommandMap getCommandMap() {
		if (cmap == null) {
			try {
				Field f = Bukkit.getServer().getClass().getDeclaredField("commandMap");
				f.setAccessible(true);
				cmap = (CommandMap) f.get(Bukkit.getServer());
				f.setAccessible(false);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return cmap;
	}

	public <T extends OlympaPlayer> T getOlympaPlayer() {
		return AccountProvider.get(player.getUniqueId());
	}

	public Player getPlayer() {
		return player;
	}

	public CommandSender getSender() {
		return sender;
	}

	public void setAllowConsole(boolean allowConsole) {
		this.allowConsole = allowConsole;
	}

	public boolean hasPermission() {
		return this.hasPermission(permission);
	}

	public boolean hasPermission(OlympaPermission perm) {
		if (perm == null || player == null) {
			return true;
		}
		OlympaPlayer olympaPlayer = this.getOlympaPlayer();
		if (olympaPlayer == null) {
			return false;
		}
		return perm.hasPermission(olympaPlayer);
	}

	public boolean hasPermission(String permName) {
		if (permName == null || permName.isEmpty()) {
			return true;
		}
		OlympaPermission perm = OlympaPermission.permissions.get(permName);
		if (perm == null) {
			return false;
		}
		return this.hasPermission(perm);
	}

	public abstract boolean onCommand(CommandSender sender, Command cmd, String label, String[] args);

	public abstract List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args);

	public void register() {
		usageString = args.entrySet().stream().map(entry -> (entry.getKey() ? "<" : "[") + String.join("|", entry.getValue()) + (entry.getKey() ? ">" : "]")).collect(Collectors.joining(" "));
		minArg = (int) args.entrySet().stream().filter(entry -> entry.getKey()).count();
		ReflectCommand reflectCommand = new ReflectCommand(command);
		if (alias != null) {
			reflectCommand.setAliases(alias);
		}
		if (description != null) {
			reflectCommand.setDescription(description);
		}
		reflectCommand.setExecutor(this);
		getCommandMap().register("Olympa", reflectCommand);
	}

	public void registerPreProcess() {
		usageString = args.entrySet().stream().map(entry -> (entry.getKey() ? "<" : "[") + String.join("|", entry.getValue()) + (entry.getKey() ? ">" : "]")).collect(Collectors.joining(" "));
		minArg = (int) args.entrySet().stream().filter(entry -> entry.getKey()).count();
		commandPreProcess.put(command, this);
	}

	public void sendDoNotHavePermission() {
		sendError("Tu n'as pas la permission &l(◑_◑)");
	}

	public void sendImpossibleWithConsole() {
		sendError("Impossible avec la console.");
	}

	public void sendImpossibleWithOlympaPlayer() {
		sendError("Une erreur est survenu avec tes données.");
	}

	public void sendIncorrectSyntax() {
		sendError("Syntaxe incorrecte.");
	}

	public void sendIncorrectSyntax(String correctSyntax) {
		sendError("Syntaxe attendue : &o" + correctSyntax);
	}

	public void sendUnknownPlayer(String name) {
		sendError("Le joueur &4%s&c est introuvable.", name);
		// TODO check historique player
	}

	public void sendMessage(Iterable<? extends CommandSender> senders, Prefix prefix, String text, Object... args) {
		text = prefix.formatMessage(text, args);
		for (CommandSender sender : senders) {
			sender.sendMessage(text);
		}
	}

	public void broadcast(Prefix prefix, String text, Object... args) {
		sendMessage(Bukkit.getOnlinePlayers(), prefix, text, args);
	}

	public void broadcastToAll(Prefix prefix, String text, Object... args) {
		broadcast(prefix, text, args);
		prefix.sendMessage(Bukkit.getConsoleSender(), text, args);
	}

	public void sendComponents(BaseComponent... components) {
		sender.spigot().sendMessage(components);
	}

	public void sendMessage(Prefix prefix, String message, Object... args) {
		prefix.sendMessage(sender, message, args);
	}

	public void sendInfo(String message, Object... args) {
		sendMessage(Prefix.INFO, message, args);
	}

	public void sendSuccess(String message, Object... args) {
		sendMessage(Prefix.DEFAULT_GOOD, message, args);
	}

	public void sendError(String message, Object... args) {
		sendMessage(Prefix.DEFAULT_BAD, message, args);
	}

	public void sendUsage(String label) {
		sendMessage(Prefix.USAGE, "/%s %s", label, usageString);
	}
}
