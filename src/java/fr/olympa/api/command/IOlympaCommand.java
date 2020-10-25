package fr.olympa.api.command;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.utils.Prefix;
import net.md_5.bungee.api.chat.BaseComponent;

public interface IOlympaCommand {

	default void sendDoNotHavePermission() {
		sendError("Tu n'as pas la permission &l(◑_◑)");
	}

	default void sendImpossibleWithConsole() {
		sendError("Impossible avec la console.");
	}

	default void sendImpossibleWithOlympaPlayer() {
		sendError("Une erreur est survenu avec tes données.");
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
		sendError("Le joueur &4%s&c est introuvable%s.", name, potentialsNames.length == 0 ? "" : "essaye plutôt avec §4" + String.join("§c, §4", potentialsNames));
	}

	default void sendUnknownPlayer(String name, Collection<? extends CharSequence> potentialsNames) {
		sendError("Le joueur &4%s&c est introuvable%s.", name, potentialsNames.isEmpty() ? "" : "essaye plutôt avec §4" + String.join("§c, §4", potentialsNames));
	}

	void sendMessage(Prefix prefix, String message, Object... args);

	default void sendError() {
		sendError("Une erreur est survenu ¯\\_(ツ)_/¯");
	}

	void broadcast(Prefix prefix, String text, Object... args);

	void broadcastToAll(Prefix prefix, String text, Object... args);

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

	void sendComponents(BaseComponent... components);

	OlympaPermission getOlympaPermission();
	
	default boolean hasPermission(OlympaPermission perm, OlympaPlayer player) {
		if (perm == null) return true;
		if (player == null) return false;
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

	IOlympaCommand register();

	IOlympaCommand registerPreProcess();
}
