package fr.olympa.api.spigot.version;

import java.util.List;
import java.util.Map.Entry;

import org.bukkit.entity.Player;

import fr.olympa.api.spigot.utils.ProtocolAPI;

public interface VersionHandler {

	boolean isPlayerVersionUnder(Player player, ProtocolAPI version);

	boolean isPlayerVersionSameOrUpper(Player player, ProtocolAPI version);

	String getVersion(Player player);

	int getPlayerProtocol(Player player);

	List<ProtocolAPI> getProtocolSupported();

	boolean disable(ProtocolAPI[] versions);

	boolean disable(ProtocolAPI versions);

	boolean disableAllUnderI(ProtocolAPI version);

	boolean disableAllUpperI(ProtocolAPI version);

	Entry<String, String> getRangeVersionArray();

	String getVersionsDisabled();

	String getVersions();

	List<ProtocolAPI> getVersionsSupportedDisable();

}