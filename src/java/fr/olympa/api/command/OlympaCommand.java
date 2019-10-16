package fr.olympa.api.command;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.SpigotUtils;
import net.md_5.bungee.api.chat.BaseComponent;

public abstract class OlympaCommand implements CommandExecutor, TabExecutor {

	protected static CommandMap cmap;

	protected Plugin plugin;

	protected List<String> alias;
	protected String command;
	protected String description;
	protected OlympaPermission permission;

	public Player player;

	protected CommandSender sender;

	protected String usageString;
	protected Integer minArg = 0;
	protected boolean allowConsole = true;

	public boolean isAsynchronous = false;

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

	public OlympaPlayer getOlympaPlayer() {
		return AccountProvider.get(this.player.getUniqueId());
	}

	public Player getPlayer() {
		return this.player;
	}

	public boolean hasPermission() {
		if (this.player == null) {
			return true;
		}

		if (this.getOlympaPlayer() == null) {
			return false;
		}
		return this.permission.hasPermission(this.getOlympaPlayer());
	}

	@Override
	public abstract boolean onCommand(CommandSender sender, Command cmd, String label, String[] args);

	@Override
	public abstract List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args);

	public void register() {
		ReflectCommand reflectCommand = new ReflectCommand(this.command);
		if (this.alias != null) {
			reflectCommand.setAliases(this.alias);
		}
		if (this.description != null) {
			reflectCommand.setDescription(this.description);
		}
		reflectCommand.setExecutor(this);
		// TODO test null instanceof ""
		this.getCommandMap().register("", reflectCommand);
	}

	public void sendDoNotHavePermission() {
		this.sendErreur("Vous n'avez pas la permission &l(◑_◑)");
	}

	public void sendErreur(String message) {
		this.sendMessage(Prefix.DEFAULT_BAD, message);
	}

	public void sendImpossibleWithConsole() {
		this.sendErreur("Impossible avec la console.");
	}

	public void sendImpossibleWithOlympaPlayer() {
		this.sendErreur("Une erreur est survenu avec vos donnés.");
	}

	public void sendMessage(BaseComponent... text) {
		this.player.spigot().sendMessage(text);
	}

	public void sendMessage(CommandSender sender, Prefix prefix, String text) {
		this.sendMessage(sender, prefix + SpigotUtils.color(text));
	}

	public void sendMessage(CommandSender sender, String text) {
		sender.sendMessage(SpigotUtils.color(text));
	}

	public void sendMessage(Prefix prefix, String text) {
		this.sendMessage(this.sender, prefix, text);
	}

	public void sendMessage(String text) {
		this.sendMessage(this.sender, SpigotUtils.color(text));
	}

	public void sendMessageToAll(Prefix prefix, String text) {
		this.sendMessageToAll(prefix + text);
	}

	public void sendMessageToAll(String text) {
		Bukkit.getOnlinePlayers().forEach(player -> this.sendMessage(player, text));
		this.sendMessage(Bukkit.getConsoleSender(), text);
	}

	public void sendUnknownPlayer(String name) {
		this.sendErreur("Le joueur &4%player&c est introuvable.".replaceFirst("%player", name));
		// TODO check historique player
	}

	public void sendUsage(String label) {
		this.sendMessage(Prefix.USAGE, "/" + label + " " + this.usageString);
	}

	public void setAllowConsole(boolean allowConsole) {
		this.allowConsole = allowConsole;
	}

	/**
	 * Don't foget to set {@link OlympaCommand#usageString}
	 */
	public void setMinArg(Integer minArg) {
		this.minArg = minArg;
	}

	/**
	 * Format: <%obligatory%|%obligatory%> [%optional%]
	 * Other like 'Usage : /command' is autofill.
	 */
	public void setUsageString(String usageString) {
		this.usageString = usageString;
	}
}
