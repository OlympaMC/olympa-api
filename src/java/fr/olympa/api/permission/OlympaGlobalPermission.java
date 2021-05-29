package fr.olympa.api.permission;

import java.util.Map;
import java.util.UUID;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.bungee.permission.OlympaBungeePermission;
import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.server.ServerType;
import net.md_5.bungee.api.chat.BaseComponent;

public class OlympaGlobalPermission extends OlympaPermission {
	
	private OlympaPermission underlying;
	
	public OlympaGlobalPermission(OlympaGroup minGroup) {
		super(minGroup);
		if (LinkSpigotBungee.Provider.link.isSpigot()) {
			underlying = new OlympaSpigotPermission(minGroup);
		}else underlying = new OlympaBungeePermission(minGroup);
	}
	
	public OlympaGlobalPermission(OlympaGroup... allowedGroups) {
		super(allowedGroups);
		if (LinkSpigotBungee.Provider.link.isSpigot()) {
			underlying = new OlympaSpigotPermission(allowedGroups);
		}else underlying = new OlympaBungeePermission(allowedGroups);
	}
	
	public OlympaGlobalPermission(OlympaGroup minGroup, OlympaGroup[] allowedGroups) {
		super(minGroup, allowedGroups);
		if (LinkSpigotBungee.Provider.link.isSpigot()) {
			underlying = new OlympaSpigotPermission(minGroup, allowedGroups);
		}else underlying = new OlympaBungeePermission(minGroup, allowedGroups);
	}
	
	@Override
	public ServerType getServerType() {
		return ServerType.SPIGOT;
	}
	
	@Override
	public boolean allowGroup(OlympaGroup group) {
		return underlying.allowGroup(group);
	}
	
	@Override
	public boolean disallowGroup(OlympaGroup group) {
		return underlying.disallowGroup(group);
	}
	
	@Override
	public OlympaGroup[] clearAllowedGroups() {
		return underlying.clearAllowedGroups();
	}
	
	@Override
	public OlympaGroup[] getAllGroupsAllowed() {
		return underlying.getAllGroupsAllowed();
	}
	
	@Override
	public UUID[] getAllowedBypass() {
		return underlying.getAllowedBypass();
	}
	
	@Override
	public boolean isInAllowedBypass(UUID uuid) {
		return underlying.isInAllowedBypass(uuid);
	}
	
	@Override
	public boolean hasPermission(Map<OlympaGroup, Long> groups) {
		return underlying.hasPermission(groups);
	}
	
	@Override
	public boolean hasPermission(OlympaGroup group) {
		return underlying.hasPermission(group);
	}
	
	@Override
	public boolean hasPermission(OlympaPlayer olympaPlayer) {
		return underlying.hasPermission(olympaPlayer);
	}
	
	@Override
	public boolean hasPermission(UUID uniqueId) {
		return underlying.hasPermission(uniqueId);
	}
	
	@Override
	public OlympaGroup[] getAllowedGroups() {
		return underlying.getAllowedGroups();
	}
	
	@Override
	public void sendMessage(BaseComponent... baseComponents) {
		underlying.sendMessage(baseComponents);
	}
	
	@Override
	public void sendMessage(String message, Object... args) {
		underlying.sendMessage(message, args);
	}
	
}
