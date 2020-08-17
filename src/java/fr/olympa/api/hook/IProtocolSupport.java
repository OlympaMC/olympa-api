package fr.olympa.api.hook;

import org.bukkit.entity.Player;

import fr.olympa.api.utils.spigot.ProtocolAPI;

public interface IProtocolSupport {

	void disable1_6();

	void disable1_7();

	void disable1_8();

	String getBigVersion(String version);

	String getRangeVersion();

	String getVersionUnSupportedInRange();

	String getVersionSupported();
	
	ProtocolAPI getPlayerVersion(Player p);

}