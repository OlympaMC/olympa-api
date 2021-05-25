package fr.olympa.core;

import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.provider.OlympaPlayerInformationsObject;

public class FakeData {

	public static void init(OlympaPlayer olympaPlayer) {
		olympaPlayer.setGroup(OlympaGroup.DEV);
		AccountProvider.cache.put(olympaPlayer.getUniqueId(), olympaPlayer);
		olympaPlayer.setId(AccountProvider.cache.size());
		AccountProvider.cachedInformations.put(olympaPlayer.getId(), new OlympaPlayerInformationsObject(olympaPlayer.getId(), olympaPlayer.getName(), olympaPlayer.getUniqueId()));
	}
}
