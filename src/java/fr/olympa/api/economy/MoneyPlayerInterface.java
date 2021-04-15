package fr.olympa.api.economy;

import fr.olympa.api.player.OlympaPlayer;

public interface MoneyPlayerInterface extends OlympaPlayer {

	@Override
	default void loaded() {
		
	}
	
	public OlympaMoney getGameMoney();

}
