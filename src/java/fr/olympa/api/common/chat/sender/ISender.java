package fr.olympa.api.common.chat.sender;

import javax.annotation.Nullable;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.common.chat.TxtComponentBuilder;
import fr.olympa.api.common.player.OlympaPlayer;
import net.md_5.bungee.api.chat.TextComponent;

public abstract class ISender {

	public static ISender of(Object commandSender, OlympaPlayer olympaPlayer) {
		if (LinkSpigotBungee.getInstance().isSpigot())
			return new SpigotSender((org.bukkit.command.CommandSender) commandSender, olympaPlayer);
		else
			return new BungeeSender((net.md_5.bungee.api.CommandSender) commandSender, olympaPlayer);
	}

	private String name;
	@Nullable
	private OlympaPlayer olympaPlayer;
	private boolean isConsole;

	/**
	 * @param iSender
	 * @param name
	 * @param olympaPlayer
	 */
	public ISender(String name, boolean isConsole, OlympaPlayer olympaPlayer) {
		this.name = name;
		this.olympaPlayer = olympaPlayer;
	}

	public String getName() {
		return name;
	}

	public boolean isConsole() {
		return isConsole;
	}

	public OlympaPlayer getOlympaPlayer() {
		return olympaPlayer;
	}

	public abstract void sendMessage(String msg);

	public abstract void sendMessage(TxtComponentBuilder msgBuilder);

	public abstract void sendMessage(TextComponent msg);
}
