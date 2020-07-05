package fr.olympa.api.command;

import java.util.Arrays;
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
		// TODO check historique player
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

	void sendUsage(String label);

	void sendComponents(BaseComponent... components);

	default boolean hasPermission(OlympaPermission perm) {
		if (perm == null || isConsole())
			return true;
		OlympaPlayer olympaPlayer = getOlympaPlayer();
		if (olympaPlayer == null)
			return false;
		return perm.hasPermission(olympaPlayer);
	}

	default String buildText(int min, String[] args) {
		return String.join(" ", Arrays.copyOfRange(args, min, args.length));
	}

	Object getPlayer();

	Object getSender();

	boolean isConsole();

	<T extends OlympaPlayer> T getOlympaPlayer();

	boolean hasPermission();

	void setAllowConsole(boolean allowConsole);

	boolean isConsoleAllowed();

	default boolean hasPermission(String permName) {
		if (permName == null || permName.isEmpty())
			return true;
		OlympaPermission perm = OlympaPermission.permissions.get(permName);
		if (perm == null)
			return false;
		return this.hasPermission(perm);
	}

	void addCommandArguments(boolean isMandatory, List<CommandArgument> ca);

	default void addArgs(boolean isMandatory, List<String> arg) {
		addCommandArguments(isMandatory, arg.stream().map(CommandArgument::new).collect(Collectors.toList()));
	}

	default void addArgs(boolean isMandatory, String... args) {
		addCommandArguments(isMandatory, Arrays.stream(args).map(CommandArgument::new).collect(Collectors.toList()));
	}

	void register();

	void registerPreProcess();
}
