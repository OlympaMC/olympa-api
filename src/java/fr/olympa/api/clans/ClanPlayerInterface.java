package fr.olympa.api.clans;

import fr.olympa.api.objects.OlympaPlayer;

public interface ClanPlayerInterface<T extends Clan<T>> extends OlympaPlayer {

	T getClan();

	void setClan(T clan);

}
