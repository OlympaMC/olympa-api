package fr.olympa.api.common.machine;

import fr.olympa.api.common.chat.TxtComponentBuilder;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.spigot.utils.ProtocolAPI;

public class TpsMessageBungee extends TpsMessage {

	public TpsMessageBungee(OlympaPlayer olympaPlayer) {
		super(olympaPlayer);
	}

	@Override
	public TxtComponentBuilder getInfoMessage() {
		if (main.isSpigot())
			throw new UnsupportedOperationException("Unable to get Bungee Info on not Bungee Environment");
		TxtComponentBuilder textBuilder = super.getInfoMessage();
		textBuilder.extra(new TxtComponentBuilder("\n&3Versions autorisées: &b%s&3 ", ProtocolAPI.getRange(ProtocolAPI.getBungeeVersions())));
		return textBuilder;
	}
}
