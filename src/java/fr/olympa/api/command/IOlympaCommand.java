package fr.olympa.api.command;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import fr.olympa.api.chat.TxtComponentBuilder;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.spigot.OlympaCore;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Content;
import net.md_5.bungee.api.chat.hover.content.Text;

public interface IOlympaCommand {

	default void sendDoNotHavePermission() {
		sendError("Tu n'as pas la permission &l(◑_◑)");
	}

	default void sendImpossibleWithConsole() {
		sendError("Impossible avec la console.");
	}

	default void sendImpossibleWithOlympaPlayer() {
		sendError("Une erreur est survenue avec tes données.");
	}

	default void sendIncorrectSyntax() {
		sendError("Syntaxe incorrecte.");
	}

	default void sendIncorrectSyntax(String correctSyntax) {
		sendError("Syntaxe attendue : &o" + correctSyntax);
	}

	default void sendUnknownPlayer(String name) {
		sendError("Le joueur &4%s&c est introuvable.", name);
	}

	default void sendUnknownPlayer(String name, CharSequence... potentialsNames) {
		sendError("Le joueur &4%s&c est introuvable%s.", name, potentialsNames.length == 0 ? "" : " essaye plutôt avec §4" + String.join("§c, §4", potentialsNames));
	}

	default void sendUnknownPlayer(String name, Collection<? extends CharSequence> potentialsNames) {
		sendError("Le joueur &4%s&c est introuvable%s.", name, potentialsNames.isEmpty() ? "" : " essaye plutôt avec §4" + String.join("§c, §4", potentialsNames));
	}

	void sendMessage(Prefix prefix, String message, Object... args);

	default void sendError() {
		sendError("Une erreur est survenue ¯\\_(ツ)_/¯");
	}

	default void sendError(Throwable throwable) {
		Throwable cause;
		if (throwable.getCause() != null)
			cause = throwable.getCause();
		else
			cause = throwable;
		sendError("Une erreur est survenue ¯\\_(ツ)_/¯ %s", cause.getMessage());
	}

	default void sendHoverAndCopy(Prefix prefix, String message, String hoverText, String copyToClipboard) {
		sendJSON(prefix, message, ClickEvent.Action.COPY_TO_CLIPBOARD, copyToClipboard, HoverEvent.Action.SHOW_TEXT, new Text(TextComponent.fromLegacyText(hoverText.replace("&", "§"))));
	}

	default void sendHoverAndURL(Prefix prefix, String message, String hoverText, String urlToOpen) {
		sendJSON(prefix, message, ClickEvent.Action.OPEN_URL, urlToOpen, HoverEvent.Action.SHOW_TEXT, new Text(TextComponent.fromLegacyText(hoverText.replace("&", "§"))));
	}

	default void sendHoverAndCommand(Prefix prefix, String message, String hoverText, String command) {
		sendJSON(prefix, message, ClickEvent.Action.RUN_COMMAND, checkCommand(command), HoverEvent.Action.SHOW_TEXT, new Text(TextComponent.fromLegacyText(hoverText.replace("&", "§"))));
	}

	default void sendHoverAndSuggest(Prefix prefix, String message, String hoverText, String command) {
		sendJSON(prefix, message, ClickEvent.Action.SUGGEST_COMMAND, checkCommand(command), HoverEvent.Action.SHOW_TEXT, new Text(TextComponent.fromLegacyText(hoverText.replace("&", "§"))));
	}

	default void sendJSON(Prefix prefix, String message, ClickEvent.Action clickAction, String clickActionValue, HoverEvent.Action hoverAction, Content... contents) {
		sendComponents(prefix, TxtComponentBuilder.of(prefix, message, clickAction, clickActionValue, hoverAction, contents));
	}

	void sendComponents(BaseComponent... components);

	default void sendComponents(Prefix prefix, BaseComponent... components) {
		TextComponent text = new TextComponent(TextComponent.fromLegacyText(prefix.toString()));
		text.addExtra(new TextComponent(components));
		sendComponents(text);
	}

	int broadcast(Prefix prefix, String text, Object... args);

	int broadcastToAll(Prefix prefix, String text, Object... args);

	default void sendInfo(String message, Object... args) {
		sendMessage(Prefix.INFO, message, args);
	}

	default void sendSuccess(String message, Object... args) {
		sendMessage(Prefix.DEFAULT_GOOD, message, args);
	}

	default void sendError(String message, Object... args) {
		sendMessage(Prefix.DEFAULT_BAD, message, args);
	}

	default void sendDefault(String message, Object... args) {
		sendMessage(Prefix.DEFAULT, message, args);
	}

	void sendUsage(String label);

	OlympaPermission getOlympaPermission();

	default boolean hasPermission(OlympaPermission perm, OlympaPlayer player) {
		if (perm == null)
			return true;
		if (player == null)
			return false;
		return perm.hasPermission(player);
	}

	default boolean hasPermission(OlympaPermission perm) {
		return hasPermission(perm, getOlympaPlayer());
	}

	default boolean hasPermission(OlympaPlayer player) {
		return hasPermission(getOlympaPermission(), player);
	}

	default boolean hasPermission() {
		return hasPermission(getOlympaPermission(), getOlympaPlayer());
	}

	default String buildText(int min, String[] args) {
		return String.join(" ", Arrays.copyOfRange(args, min, args.length));
	}

	Object getPlayer();

	Object getSender();

	boolean isConsole();

	<T extends OlympaPlayer> T getOlympaPlayer();

	void setAllowConsole(boolean allowConsole);

	boolean isConsoleAllowed();

	default boolean hasPermission(String permName) {
		if (permName == null || permName.isEmpty())
			return true;
		OlympaPermission perm = OlympaPermission.permissions.get(permName);
		if (perm == null)
			return false;
		return this.hasPermission(perm, getOlympaPlayer());
	}

	void addCommandArguments(boolean isMandatory, List<CommandArgument> ca);

	default void addCommandArguments(boolean isMandatory, CommandArgument... cas) {
		addCommandArguments(isMandatory, Arrays.asList(cas));
	}

	default void addArgs(boolean isMandatory, List<String> arg) {
		addCommandArguments(isMandatory, arg.stream().map(CommandArgument::new).collect(Collectors.toList()));
	}

	default void addArgs(boolean isMandatory, String... args) {
		addCommandArguments(isMandatory, Arrays.stream(args).map(CommandArgument::new).collect(Collectors.toList()));
	}

	private String checkCommand(String command) {
		if (command.charAt(0) != '/') {
			OlympaCore.getInstance().sendMessage("&cBAD USAGE OF JSON ClickEvent, you fogot the '/'. I'll add it this time, but correct it pls.");
			return "/" + command;
		}
		return command;
	}

	void unregister();

	IOlympaCommand register();

	IOlympaCommand registerPreProcess();
}
