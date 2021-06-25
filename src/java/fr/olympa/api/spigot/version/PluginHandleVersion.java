package fr.olympa.api.spigot.version;

import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import fr.olympa.api.spigot.utils.ProtocolAPI;

public interface PluginHandleVersion {

	Entry<String, String> getRangeVersionArray();

	default String getRangeVersion() {
		Entry<String, String> entry = getRangeVersionArray();
		return entry.getKey() + " à " + entry.getValue();
	}

	default String getVersionsSupported() {
		List<String> proto = getEnabledProtocols().stream().map(id -> ProtocolAPI.getName(id)).distinct().collect(Collectors.toList());
		String lastProtocol = proto.get(0);
		if (proto.size() == 1)
			return lastProtocol;
		String firstProtocol = proto.get(proto.size() - 1);
		return firstProtocol + " à " + lastProtocol;
	}

	default String getVersionsUnSupported() {
		return getDisabledProtocols().stream().map(id -> ProtocolAPI.getName(id)).distinct().collect(Collectors.joining(", "));
	}

	int getPlayerProtocol(Player player);

	List<Integer> getEnabledProtocols();

	List<Integer> getDisabledProtocols();

	boolean disable(ProtocolAPI[] versions);

	boolean disable(ProtocolAPI versions);

	boolean disableAllUnderI(ProtocolAPI version);

	boolean disableAllUpperI(ProtocolAPI version);

	default ProtocolAPI getHighestVersion() {
		List<Integer> versions = getEnabledProtocols();
		return ProtocolAPI.getHighestVersion(versions.indexOf(Collections.max(versions)));
	}

	default ProtocolAPI getLowerVersion() {
		List<Integer> versions = getEnabledProtocols();
		return ProtocolAPI.getLowerVersion(versions.indexOf(Collections.min(versions)));
	}

	default List<ProtocolAPI> getProtocols() {
		List<Integer> versions = getEnabledProtocols();
		return ProtocolAPI.getAll(versions);
	}

}