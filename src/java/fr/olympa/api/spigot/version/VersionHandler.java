package fr.olympa.api.spigot.version;

import java.util.List;
import java.util.Map.Entry;

import fr.olympa.api.spigot.utils.ProtocolAPI;

public interface VersionHandler<T> {

	default boolean isPlayerVersionUnder(T player, ProtocolAPI version) {
		int protocolPlayer = getPlayerProtocol(player);
		return version.getProtocolNumber() > protocolPlayer;
	}

	default boolean isPlayerVersionSameOrUpper(T player, ProtocolAPI version) {
		int protocolPlayer = getPlayerProtocol(player);
		return version.getProtocolNumber() <= protocolPlayer;
	}

	default String getVersion(T player) {
		return ProtocolAPI.getName(getPlayerProtocol(player));
	}

	default String getVersionsDisabled() {
		return ProtocolAPI.getRange(getProtocolsDisabled());
	}

	//	default List<ProtocolAPI> getVersionsSupportedDisable() {
	//		List<ProtocolAPI> versions = new ArrayList<>();
	//		getProtocolsDisabled().forEach(protocolNb -> versions.addAll(ProtocolAPI.getAll(protocolNb)));
	//		return versions.stream().sorted(new Sorting<>(ProtocolAPI::ordinal)).collect(Collectors.toList());
	//	}
	//
	//	default List<Integer> getProtocolsEnabled() {
	//		return getProtocolSupported().stream().map(ProtocolAPI::getProtocolNumber).collect(Collectors.toList());
	//	}

	default Entry<String, String> getRangeVersionArray() {
		return ProtocolAPI.getRangeEntry(getProtocolsSupported());
	}

	int getPlayerProtocol(T player);

	List<ProtocolAPI> getProtocolsSupported();

	List<ProtocolAPI> getProtocolsDisabled();

	boolean disable(ProtocolAPI[] versions);

	boolean disable(ProtocolAPI versions);

	boolean disableAllUnderI(ProtocolAPI version);

	boolean disableAllUpperI(ProtocolAPI version);

	default String getVersions() {
		return ProtocolAPI.getRange(getProtocolsSupported());
	}

}