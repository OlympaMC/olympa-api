package fr.olympa.core;

import fr.olympa.api.common.groups.OlympaGroup;
import fr.olympa.api.common.player.OlympaAccount;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.api.common.provider.OlympaPlayerInformationsObject;

public class FakeData {

	public static void init(OlympaPlayer olympaPlayer) {
		olympaPlayer.setGroup(OlympaGroup.DEV);
		OlympaAccount.getCache().put(olympaPlayer.getUniqueId(), olympaPlayer);
		olympaPlayer.setId(OlympaAccount.getCache().size());
		AccountProviderAPI.getter().getCachedInformations().put(olympaPlayer.getId(), new OlympaPlayerInformationsObject(olympaPlayer.getId(), olympaPlayer.getName(), olympaPlayer.getUniqueId()));
	}
}
