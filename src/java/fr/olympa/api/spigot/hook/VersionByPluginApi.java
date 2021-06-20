package fr.olympa.api.spigot.hook;

import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import fr.olympa.api.spigot.utils.ProtocolAPI;

public interface VersionByPluginApi {

	Entry<String, String> getRangeVersionArray();

	default String getRangeVersion() {
		Entry<String, String> entry = getRangeVersionArray();
		return entry.getKey() + " à " + entry.getValue();
	}

	default String getVersionsSupported() {
		List<String> proto = getAllVersionsSupported().stream().map(id -> ProtocolAPI.getName(id)).distinct().collect(Collectors.toList());
		String lastProtocol = proto.get(0);
		if (proto.size() == 1)
			return lastProtocol;
		String firstProtocol = proto.get(proto.size() - 1);
		return firstProtocol + " à " + lastProtocol;
	}

	default String getVersionsUnSupported() {
		return getAllVersionsUnSupported().stream().map(id -> ProtocolAPI.getName(id)).distinct().collect(Collectors.joining(", "));
	}

	ProtocolAPI getPlayerVersion(Player p);

	List<Integer> getAllVersionsSupported();

	List<Integer> getAllVersionsUnSupported();

	boolean disable(ProtocolAPI[] versions);

	boolean disable(ProtocolAPI versions);

	boolean disableAllUnderI(ProtocolAPI version);

	boolean disableAllUpperI(ProtocolAPI version);

}