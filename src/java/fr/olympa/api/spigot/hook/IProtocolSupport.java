package fr.olympa.api.spigot.hook;

import org.bukkit.entity.Player;

import fr.olympa.api.spigot.utils.ProtocolAPI;

public interface IProtocolSupport {

	void disable1_6();

	void disable1_7();

	void disable1_8();

	String getBigVersion(String version);

	String[] getRangeVersionArray();

	default String getRangeVersion() {
		String[] array = getRangeVersionArray();
		return array[0] + " à " + array[1];
	}

	String getVersionUnSupportedInRange();

	String getVersionSupported();

	ProtocolAPI getPlayerVersion(Player p);

}