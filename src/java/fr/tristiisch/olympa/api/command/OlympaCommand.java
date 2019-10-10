package fr.tristiisch.olympa.api.command;

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

import fr.tristiisch.olympa.api.objects.OlympaPlayer;
import fr.tristiisch.olympa.api.permission.OlympaPermission;
import fr.tristiisch.olympa.api.provider.AccountProvider;
import fr.tristiisch.olympa.api.utils.Prefix;
import fr.tristiisch.olympa.api.utils.SpigotUtils;
import net.md_5.bungee.api.chat.BaseComponent;

public abstract class OlympaCommand implements CommandExecutor, TabExecutor {

	protected static CommandMap cmap;

	protected final Plugin plugin;

	protected List<String> alias;
	protected final String command;
	protected String description;
	protected OlympaPermission permission;

	public Player player;

	protected CommandSender sender;

	protected String usageString;
	protected Integer minArg = 0;
	protected boolean allowConsole = true;

	public boolean isAsynchronous = false;

	public OlympaCommand(final Plugin plugin, final String command, final OlympaPermission permission, final String... alias) {
		this.plugin = plugin;
		this.command = command;
		this.permission = permission;
		this.alias = Arrays.asList(alias);
	}

	public OlympaCommand(final Plugin plugin, final String command, final String... alias) {
		this.plugin = plugin;
		this.command = command;
		this.alias = Arrays.asList(alias);
	}

	public OlympaCommand(final Plugin plugin, final String command, final String description, final OlympaPermission permission, final String... alias) {
		this.plugin = plugin;
		this.permission = permission;
		this.command = command;
		this.description = description;
		this.alias = Arrays.asList(alias);
	}

	public void broadcast(final String message) {
		Bukkit.broadcastMessage(message);
	}

	public String buildText(final int min, final String[] args) {
		return String.join(" ", Arrays.copyOfRange(args, min, args.length));
	}

	final CommandMap getCommandMap() {
		if (cmap == null) {
			try {
				final Field f = Bukkit.getServer().getClass().getDeclaredField("commandMap");
				f.setAccessible(true);
				cmap = (CommandMap) f.get(Bukkit.getServer());
				f.setAccessible(false);
			} catch (final Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return cmap;
	}

	public OlympaPlayer getOlympaPlayer() {
		return AccountProvider.get(this.player);
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
	public abstract boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args);

	@Override
	public abstract List<String> onTabComplete(final CommandSender sender, final Command cmd, final String label, final String[] args);

	public void register() {
		final ReflectCommand reflectCommand = new ReflectCommand(this.command);
		if (this.alias != null) {
			reflectCommand.setAliases(this.alias);
		}
		if (this.description != null) {
			reflectCommand.setDescription(this.description);
		}
		reflectCommand.setExecutor(this);
		this.getCommandMap().register("", reflectCommand);
	}

	public void sendDoNotHavePermission() {
		this.sendErreur("Vous n'avez pas la permission &l(◑_◑)");
	}

	public void sendErreur(final String message) {
		this.sendMessage(Prefix.DEFAULT_BAD, message);
	}

	public void sendImpossibleWithConsole() {
		this.sendErreur("Impossible avec la console.");
	}

	public void sendImpossibleWithOlympaPlayer() {
		this.sendErreur("Une erreur est survenu avec vos donnés.");
	}

	public void sendMessage(final BaseComponent... text) {
		this.player.spigot().sendMessage(text);
	}

	public void sendMessage(final CommandSender sender, final Prefix prefix, final String text) {
		this.sendMessage(sender, prefix + SpigotUtils.color(text));
	}

	public void sendMessage(final CommandSender sender, final String text) {
		sender.sendMessage(SpigotUtils.color(text));
	}

	public void sendMessage(final Prefix prefix, final String text) {
		this.sendMessage(this.sender, prefix, text);
	}

	public void sendMessage(final String text) {
		this.sendMessage(this.sender, SpigotUtils.color(text));
	}

	public void sendMessageToAll(final Prefix prefix, final String text) {
		this.sendMessageToAll(prefix + text);
	}

	public void sendMessageToAll(final String text) {
		Bukkit.broadcastMessage(text);
		this.sendMessage(Bukkit.getConsoleSender(), text);
	}

	public void sendUnknownPlayer(final String name) {
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
