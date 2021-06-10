package fr.olympa.api.common.chat.sender;

import fr.olympa.api.common.chat.TxtComponentBuilder;
import fr.olympa.api.common.player.OlympaPlayer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.Connection;

public class BungeeSender extends ISender {

	CommandSender sender;

	public BungeeSender(CommandSender sender, OlympaPlayer olympaPlayer) {
		super(sender.getName(), !(sender instanceof Connection), olympaPlayer);
		this.sender = sender;
	}

	@Override
	public void sendMessage(String msg) {
		sender.sendMessage(TextComponent.fromLegacyText(msg));
	}

	@Override
	public void sendMessage(TextComponent msg) {
		sender.sendMessage(msg);
	}

	@Override
	public void sendMessage(TxtComponentBuilder msgBuilder) {
		sender.sendMessage(msgBuilder.build());
	}

}
