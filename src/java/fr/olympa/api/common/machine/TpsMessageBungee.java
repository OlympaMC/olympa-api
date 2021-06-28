package fr.olympa.api.common.machine;

import fr.olympa.api.common.chat.TxtComponentBuilder;
import fr.olympa.api.common.player.OlympaPlayer;

public class TpsMessageBungee extends TpsMessage {

	public TpsMessageBungee(OlympaPlayer olympaPlayer) {
		super(olympaPlayer);
	}

	@Override
	public TxtComponentBuilder getInfoMessage() {
		//		if (main.isSpigot())
		//			throw new UnsupportedOperationException("Unable to get Bungee Info on not Bungee Environment");
		return super.getInfoMessage();
	}
}
