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
	protected List<String> aliases;
	protected LinkedHashMap<Boolean, List<CommandArgument>> args = new LinkedHashMap<>();
	protected String command;
	protected String description;
	protected OlympaPermission permission;
	protected Player player;
	protected CommandSender sender;
	protected String usageString;
	protected Integer minArg = 0;
	protected boolean allowConsole = true;
	protected boolean isAsynchronous = false;

	public OlympaCommand(Plugin plugin, String command, OlympaPermission permission, String... aliases) {
		this.plugin = plugin;
		this.command = command;
		this.permission = permission;
		this.aliases = Arrays.asList(aliases);
	}

	public OlympaCommand(Plugin plugin, String command, String... aliases) {
		this.plugin = plugin;
		this.command = command;
		this.aliases = Arrays.asList(aliases);
	}

	public OlympaCommand(Plugin plugin, String command, String description, OlympaPermission permission, String... aliases) {
		this.plugin = plugin;
		this.permission = permission;
		this.command = command;
		this.description = description;
		this.aliases = Arrays.asList(aliases);
	}

	public void addCommandArguments(boolean isMandatory, List<CommandArgument> ca) {
		args.put(isMandatory, ca);
	}
	
	@Deprecated
	public void addArgs(boolean isMandatory, List<String> arg) {
		args.put(isMandatory, arg.stream().map(a -> new CommandArgument(a)).collect(Collectors.toList()));
	}
	
	public void addArgs(boolean isMandatory, String... args) {
		addCommandArguments(isMandatory, Arrays.stream(args).map(a -> new CommandArgument(a)).collect(Collectors.toList()));
	}

	public String buildText(int min, String[] args) {
		return String.join(" ", Arrays.copyOfRange(args, min, args.length));
	}

	CommandMap getCommandMap() {
		if (cmap == null)
			try {
				Field f = Bukkit.getServer().getClass().getDeclaredField("commandMap");
				f.setAccessible(true);
				cmap = (CommandMap) f.get(Bukkit.getServer());
				f.setAccessible(false);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
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
		if (perm == null || player == null)
			return true;
		OlympaPlayer olympaPlayer = this.getOlympaPlayer();
		if (olympaPlayer == null)
			return false;
		return perm.hasPermission(olympaPlayer);
	}

	public boolean hasPermission(String permName) {
		if (permName == null || permName.isEmpty())
			return true;
		OlympaPermission perm = OlympaPermission.permissions.get(permName);
		if (perm == null)
			return false;
		return this.hasPermission(perm);
	}

	public abstract boolean onCommand(CommandSender sender, Command cmd, String label, String[] args);

	public abstract List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args);

	private void build() {
		usageString = args.entrySet().stream().map(entry -> {
			boolean isMandatory = entry.getKey();
			List<CommandArgument> ca = entry.getValue();
			return (isMandatory ? "<" : "[") + ca.stream().map(c -> c.getArgName()).collect(Collectors.joining("|")) + (isMandatory ? ">" : "]");
		}).collect(Collectors.joining(" "));
		minArg = (int) args.entrySet().stream().count();
	}
	
	public void register() {
		build();
		ReflectCommand reflectCommand = new ReflectCommand(command);
		if (aliases != null)
			reflectCommand.setAliases(aliases);
		if (description != null)
			reflectCommand.setDescription(description);
		reflectCommand.setExecutor(this);
		getCommandMap().register("Olympa", reflectCommand);
	}

	public void registerPreProcess() {
		build();
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
		for (CommandSender sender : senders)
			sender.sendMessage(text);
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
