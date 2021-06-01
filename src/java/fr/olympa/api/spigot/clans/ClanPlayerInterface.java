package fr.olympa.api.spigot.clans;

import fr.olympa.api.spigot.economy.MoneyPlayerInterface;

public interface ClanPlayerInterface<T extends Clan<T, D>, D extends ClanPlayerData<T, D>> extends MoneyPlayerInterface {

	T getClan();

	void setClan(T clan);

}
