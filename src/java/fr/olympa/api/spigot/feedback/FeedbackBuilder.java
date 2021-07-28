package fr.olympa.api.spigot.feedback;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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
		entry.position = player.getLocation();
		entry.worldName = entry.position.getWorld().getName();
		entry.position.setWorld(null);
		
		iterator = steps.listIterator(0);
		step = iterator.next();
		step.begin.accept(this);
	}
	
	public boolean next(String arg) {
		step.complete.accept(this, arg);
		iterator.next().begin.accept(this);
		if (iterator.hasNext()) return true;
		finish();
		return false;
	}
	
	public void previous() {
		if (iterator.hasPrevious()) iterator.previous().begin.accept(this);
	}
	
	private void beginType() {
		player.openBook(typeBook);
	}
	
	private void setType(String arg) {
		try {
			entry.type = FeedbackType.valueOf(arg);
		}catch (IllegalArgumentException ex) {
			Prefix.ERROR.sendMessage(player, "Type de feedback invalide: %s", arg);
			if (entry.type == null) entry.type = FeedbackType.BUG;
		}
	}
	
	private void beginServer() {
		player.openBook(serverBook);
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
	
	private void beginDescription() {
		Prefix.DEFAULT_GOOD.sendMessage(player, entry.type.getDescriptionMessage());
		new TextEditor<String>(player, this::next, this::previous, false).enterOrLeave();
	}
	
	private void setDescription(String arg) {
		entry.description = arg;
	}
	
	private void finish() {
		entry.date = System.currentTimeMillis();
		entry.owner = AccountProviderAPI.getter().getPlayerInformations(player.getUniqueId());
		entry.serverInfo = new ServerInfoAdvancedSpigot(OlympaCore.getInstance(), false);
	}
	
	static class Step {
		Consumer<FeedbackBuilder> begin;
		BiConsumer<FeedbackBuilder, String> complete;
		
		public Step(Consumer<FeedbackBuilder> begin, BiConsumer<FeedbackBuilder, String> complete) {
			this.begin = begin;
			this.complete = complete;
		}
	}
	
}
