package fr.olympa.api.spigot.feedback;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import org.bukkit.entity.Player;

import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.api.common.server.OlympaServer;
import fr.olympa.api.common.server.ServerInfoAdvancedSpigot;
import fr.olympa.api.spigot.editor.TextEditor;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.spigot.OlympaCore;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;

public class FeedbackBuilder {
	
	private static LinkedList<Step> steps = new LinkedList<>(Arrays.asList(
			new Step(FeedbackBuilder::beginType, FeedbackBuilder::setType),
			new Step(FeedbackBuilder::beginStatus, FeedbackBuilder::setStatus),
			new Step(FeedbackBuilder::beginServer, FeedbackBuilder::setServer),
			new Step(FeedbackBuilder::beginDescription, FeedbackBuilder::setDescription)
			));
	
	private static Book typeBook = Book.book(
			Component.text("Olympa Feedback"), 
			Component.text("Olympa"), 
			Component.text("\n\n\n   Que comptez-vous     faire remonter au          staff ?", NamedTextColor.DARK_GRAY)
			.append(Component.text("\n\n         [", NamedTextColor.RED)
					.append(Component.text("BUG", Style.style(TextDecoration.BOLD)).clickEvent(ClickEvent.runCommand("/feedback next BUG")))
					.append(Component.text("]")))
			.append(Component.text("\n     [", NamedTextColor.GOLD)
					.append(Component.text("SUGGESTION", Style.style(TextDecoration.BOLD)).clickEvent(ClickEvent.runCommand("/feedback next SUGGESTION")))
					.append(Component.text("]")))
			.append(Component.text("\n         [", NamedTextColor.DARK_GREEN)
					.append(Component.text("AVIS", Style.style(TextDecoration.BOLD)).clickEvent(ClickEvent.runCommand("/feedback next AVIS")))
					.append(Component.text("]")))
			.append(Component.text("\n\n\n\n\n ", NamedTextColor.DARK_RED)
					.append(Component.text("◀").clickEvent(ClickEvent.runCommand("/feedback prev")))
					.append(Component.text("        "))
					.append(Component.text("✖").clickEvent(ClickEvent.runCommand("/feedback exit")))));
	
	private static Book statusBook = Book.book(
			Component.text("Olympa Feedback"), 
			Component.text("Olympa"), 
			Component.text("\n\n\n  Quelle est la gravité          du bug ?", NamedTextColor.DARK_GRAY)
			.append(Component.text("\n\n       [", NamedTextColor.DARK_RED)
					.append(Component.text("CRITIQUE", Style.style(TextDecoration.BOLD)).clickEvent(ClickEvent.runCommand("/feedback next CRITICAL")))
					.append(Component.text("]")))
			.append(Component.text("\n      [", NamedTextColor.RED)
					.append(Component.text("IMPORTANT", Style.style(TextDecoration.BOLD)).clickEvent(ClickEvent.runCommand("/feedback next IMPORTANT")))
					.append(Component.text("]")))
			.append(Component.text("\n        [", NamedTextColor.GOLD)
					.append(Component.text("MOYEN", Style.style(TextDecoration.BOLD)).clickEvent(ClickEvent.runCommand("/feedback next MEDIUM")))
					.append(Component.text("]")))
			.append(Component.text("\n        [", NamedTextColor.DARK_GREEN)
					.append(Component.text("MINEUR", Style.style(TextDecoration.BOLD)).clickEvent(ClickEvent.runCommand("/feedback next MINOR")))
					.append(Component.text("]")))
			.append(Component.text("\n\n\n\n ", NamedTextColor.DARK_RED)
					.append(Component.text("◀").clickEvent(ClickEvent.runCommand("/feedback prev")))
					.append(Component.text("        "))
					.append(Component.text("✖").clickEvent(ClickEvent.runCommand("/feedback exit")))));
	
	private static Book serverBook = Book.book(
			Component.text("Olympa Feedback"), 
			Component.text("Olympa"), 
			Component.text("\n\n\n Ce bug concerne-t-il ", NamedTextColor.DARK_GRAY)
			.append(Component.text("tout le serveur", NamedTextColor.GOLD))
			.append(Component.text(" ou  seulement le serveur  "))
			.append(Component.text(OlympaCore.getInstance().getOlympaServer().getNameCaps(), NamedTextColor.RED))
			.append(Component.text("?"))
			.append(Component.text("\n\n  [", NamedTextColor.GOLD)
					.append(Component.text("TOUT", Style.style(TextDecoration.BOLD)).clickEvent(ClickEvent.runCommand("/feedback next TOUT")))
					.append(Component.text("]")))
			.append(Component.text("   [", NamedTextColor.RED)
					.append(Component.text("SERVEUR", Style.style(TextDecoration.BOLD)).clickEvent(ClickEvent.runCommand("/feedback next SERVEUR")))
					.append(Component.text("]")))
			.append(Component.text("\n\n\n\n\n ", NamedTextColor.DARK_RED)
					.append(Component.text("◀").clickEvent(ClickEvent.runCommand("/feedback prev")))
					.append(Component.text("        "))
					.append(Component.text("✖").clickEvent(ClickEvent.runCommand("/feedback exit")))));
	
	private final Player player;
	private final ListIterator<Step> iterator;
	
	private Step step;
	
	private FeedbackEntry entry;
	
	public FeedbackBuilder(Player player) {
		this.player = player;
		
		entry = new FeedbackEntry();
		entry.setPosition(player.getLocation());
		
		iterator = steps.listIterator(0);
		next(null);
	}
	
	public boolean next(String arg) {
		if (step != null) step.complete.accept(this, arg);
		step = iterator.next();
		if (!step.begin.test(this)) {
			step = null;
			if (!iterator.hasNext()) {
				finish();
				return false;
			}
			return next(null);
		}
		if (iterator.hasNext()) return true;
		finish();
		return false;
	}
	
	public void previous() {
		if (iterator.hasPrevious()) iterator.previous().begin.test(this);
	}
	
	private boolean beginType() {
		player.openBook(typeBook);
		return true;
	}
	
	private void setType(String arg) {
		try {
			entry.type = FeedbackType.valueOf(arg);
		}catch (IllegalArgumentException ex) {
			Prefix.ERROR.sendMessage(player, "Type de feedback invalide: %s", arg);
			if (entry.type == null) entry.type = FeedbackType.BUG;
		}
	}
	
	private boolean beginStatus() {
		if (entry.type != FeedbackType.BUG) return false;
		player.openBook(statusBook);
		return true;
	}
	
	private void setStatus(String arg) {
		try {
			entry.status = FeedbackStatus.valueOf(arg);
		}catch (IllegalArgumentException ex) {
			Prefix.ERROR.sendMessage(player, "Statut de feedback invalide: %s", arg);
			if (entry.status == null) entry.status = FeedbackStatus.MEDIUM;
		}
	}
	
	private boolean beginServer() {
		player.openBook(serverBook);
		return true;
	}
	
	private void setServer(String arg) {
		switch (arg) {
		case "TOUT":
			entry.server = OlympaServer.ALL;
			break;
		case "SERVEUR":
			entry.server = OlympaCore.getInstance().getOlympaServer();
			break;
		default:
			Prefix.ERROR.sendMessage(player, "Type de serveur invalide: %s", arg);
			if (entry.server == null) entry.server = OlympaServer.ALL;
			break;
		}
	}
	
	private boolean beginDescription() {
		Prefix.DEFAULT_GOOD.sendMessage(player, entry.type.getDescriptionMessage());
		new TextEditor<String>(player, this::next, this::previous, false).enterOrLeave();
		return true;
	}
	
	private void setDescription(String arg) {
		entry.description = arg;
	}
	
	private void finish() {
		entry.date = System.currentTimeMillis();
		entry.owner = AccountProviderAPI.getter().getPlayerInformations(player.getUniqueId());
		entry.serverInfo = new ServerInfoAdvancedSpigot(OlympaCore.getInstance(), false);
		
		OlympaCore.getInstance().getFeedbackManager().registerFeedback(entry, id -> {
			Prefix.DEFAULT_GOOD.sendMessage(player, "Ton retour a été fait, il porte le numéro %d ! Merci beaucoup de nous aider à améliorer Olympa ;-)", id);
		}, error -> {
			Prefix.ERROR.sendMessage(player, "Une erreur est survenue lors de l'envoi de ton retour : §4%s", error.getMessage());
		});
	}
	
	static class Step {
		Predicate<FeedbackBuilder> begin;
		BiConsumer<FeedbackBuilder, String> complete;
		
		public Step(Predicate<FeedbackBuilder> begin, BiConsumer<FeedbackBuilder, String> complete) {
			this.begin = begin;
			this.complete = complete;
		}
	}
	
}
