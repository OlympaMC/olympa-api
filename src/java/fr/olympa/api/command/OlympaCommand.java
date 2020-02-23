package fr.olympa.api.command;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.SpigotUtils;
import net.md_5.bungee.api.chat.BaseComponent;

public abstract class OlympaCommand {

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

	public void addArgs(boolean isMandatory, String... args) {
		this.args.put(isMandatory, Arrays.asList(args));
	}

	public void broadcast(String message) {
		Bukkit.broadcastMessage(message);
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
		return AccountProvider.get(this.player.getUniqueId());
	}

	public Player getPlayer() {
		return this.player;
	}

	public boolean hasPermission() {
		return this.hasPermission(this.permission);
	}

	public boolean hasPermission(OlympaPermission perm) {
		if (perm == null || this.player == null) {
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
		this.usageString = this.args.entrySet().stream().map(entry -> (entry.getKey() ? "<" : "[") + String.join("|", entry.getValue()) + (entry.getKey() ? ">" : "]")).collect(Collectors.joining(" "));
		this.minArg = (int) this.args.entrySet().stream().filter(entry -> entry.getKey()).count();
		ReflectCommand reflectCommand = new ReflectCommand(this.command);
		if (this.alias != null) {
			reflectCommand.setAliases(this.alias);
		}
		if (this.description != null) {
			reflectCommand.setDescription(this.description);
		}
		reflectCommand.setExecutor(this);
		this.getCommandMap().register(new String(), reflectCommand);
	}

	public void sendDoNotHavePermission() {
		this.sendError("Tu n'as pas la permission &l(◑_◑)");
	}

	public void sendError(String message) {
		this.sendMessage(Prefix.DEFAULT_BAD, message);
	}

	public void sendImpossibleWithConsole() {
		this.sendError("Impossible avec la console.");
	}

	public void sendImpossibleWithOlympaPlayer() {
		this.sendError("Une erreur est survenu avec tes donnés.");
	}

	public void sendIncorrectSyntax() {
		this.sendError("Syntaxe incorrecte.");
	}

	public void sendIncorrectSyntax(String correctSyntax) {
		this.sendError("Syntaxe attendue : &o" + correctSyntax);
	}

	public void sendMessage(BaseComponent... text) {
		this.player.spigot().sendMessage(text);
	}

	public void sendMessage(CommandSender sender, Prefix prefix, String text) {
		this.sendMessage(sender, prefix + text);
	}

	public void sendMessage(CommandSender sender, String text) {
		sender.sendMessage(SpigotUtils.color(text));
	}

	public void sendMessage(Prefix prefix, String text) {
		this.sendMessage(this.sender, prefix, text);
	}

	public void sendMessage(String text) {
		this.sendMessage(this.sender, text);
	}

	public void sendMessageToAll(Prefix prefix, String text) {
		this.sendMessageToAll(prefix + text);
	}

	public void sendMessageToAll(String text) {
		Bukkit.getOnlinePlayers().forEach(player -> this.sendMessage(player, text));
		this.sendMessage(Bukkit.getConsoleSender(), text);
	}

	public void sendSuccess(String message) {
		this.sendMessage(Prefix.DEFAULT_GOOD, message);
	}

	public void sendUnknownPlayer(String name) {
		this.sendError("Le joueur &4%player&c est introuvable.".replaceFirst("%player", name));
		// TODO check historique player
	}

	public void sendUsage(String label) {
		this.sendMessage(Prefix.USAGE, "/" + label + " " + this.usageString);
	}

	public void setAllowConsole(boolean allowConsole) {
		this.allowConsole = allowConsole;
	}
}
