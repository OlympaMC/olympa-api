package fr.olympa.api.clans;

import fr.olympa.api.economy.MoneyPlayerInterface;

public interface ClanPlayerInterface<T extends Clan<T, D>, D extends ClanPlayerData<T, D>> extends MoneyPlayerInterface {

	T getClan();

	void setClan(T clan);

}
