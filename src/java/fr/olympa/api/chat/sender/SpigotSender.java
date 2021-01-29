package fr.olympa.api.chat.sender;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import fr.olympa.api.chat.TxtComponentBuilder;
import fr.olympa.api.player.OlympaPlayer;
import net.md_5.bungee.api.chat.TextComponent;

public class SpigotSender extends ISender {

	CommandSender sender;

	public SpigotSender(CommandSender sender, OlympaPlayer olympaPlayer) {
		super(sender.getName(), sender instanceof ConsoleCommandSender, olympaPlayer);
		this.sender = sender;
	}

	@Override
	public void sendMessage(String msg) {
		sender.sendMessage(msg);
	}

	@Override
	public void sendMessage(TextComponent msg) {
		sender.spigot().sendMessage(msg);
	}

	@Override
	public void sendMessage(TxtComponentBuilder msgBuilder) {
		sender.spigot().sendMessage(msgBuilder.build());
	}

}
