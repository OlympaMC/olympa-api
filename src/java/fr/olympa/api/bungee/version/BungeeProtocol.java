package fr.olympa.api.bungee.version;

import java.util.ArrayList;
import java.util.List;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.bungee.plugin.OlympaBungeeCore;
import fr.olympa.api.common.annotation.SpigotOrBungee;
import fr.olympa.api.common.annotation.SpigotOrBungee.AllowedFramework;
import fr.olympa.api.spigot.utils.ProtocolAPI;
import fr.olympa.api.spigot.version.VersionHandler;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.ProtocolConstants;

@SpigotOrBungee(allow = AllowedFramework.BUNGEE)
public class BungeeProtocol implements VersionHandler<ProxiedPlayer> {

	public BungeeProtocol(OlympaBungeeCore olympaBungeeCore) {
		olympaBungeeCore.setProtocols(getProtocolsSupported());
	}

	@Override
	public int getPlayerProtocol(ProxiedPlayer player) {
		return player.getPendingConnection().getVersion();
	}

	/**
	 * Return all versions names natively supported by Bungeecord.
	 * It dosen't contains other versions supported with ProtocolSupportBungee or ViaVersionBungee
	 * @throw IllegalAccessError when spigot instance call this method
	 */
	public List<String> getBungeeVersionsNames() {
		if (LinkSpigotBungee.Provider.link.isSpigot())
			throw new IllegalAccessError("Can't get BungeeVersion on Spigot instance.");
		return ProtocolConstants.SUPPORTED_VERSIONS;
	}

	/**
	 * Return all versions id natively supported by Bungeecord.
	 * It dosen't contains other versions supported with ProtocolSupportBungee or ViaVersionBungee
	 * @throw IllegalAccessError when spigot instance call this method
	 */
	public List<Integer> getBungeeVersionId() {
		if (LinkSpigotBungee.Provider.link.isSpigot())
			throw new IllegalAccessError("Can't get BungeeVersionId on Spigot instance.");
		return ProtocolConstants.SUPPORTED_VERSION_IDS;
	}

	/**
	 * Return all versions id natively supported by Bungeecord.
	 * It dosen't contains other versions supported with ProtocolSupportBungee or ViaVersionBungee
	 * @throw IllegalAccessError when spigot instance call this method
	 */
	@Override
	public List<ProtocolAPI> getProtocolsSupported() {
		List<ProtocolAPI> versions = new ArrayList<>();
		getBungeeVersionId().forEach(protocolNb -> versions.addAll(ProtocolAPI.getAll(protocolNb)));
		return versions;
	}

	@Override
	public List<ProtocolAPI> getProtocolsDisabled() {
		return new ArrayList<>();
	}

	@Override
	public boolean disable(ProtocolAPI[] versions) {
		return false;
	}

	@Override
	public boolean disable(ProtocolAPI versions) {
		return false;
	}

	@Override
	public boolean disableAllUnderI(ProtocolAPI version) {
		return false;
	}

	@Override
	public boolean disableAllUpperI(ProtocolAPI version) {
		return false;
	}

}
