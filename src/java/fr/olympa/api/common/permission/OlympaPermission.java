package fr.olympa.api.common.permission;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.common.groups.OlympaGroup;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.api.common.server.ServerFrameworkType;
import net.md_5.bungee.api.chat.BaseComponent;

public abstract class OlympaPermission implements IOlympaPermission {
	
	public static final Map<String, OlympaPermission> permissions = new HashMap<>();

	public static void registerPermissions(Class<?> clazz) {
		int i = 0;
		for (Field f : clazz.getDeclaredFields())
			try {
				if (OlympaPermission.class.isAssignableFrom(f.getType()) && Modifier.isStatic(f.getModifiers())) {
					OlympaPermission permission = (OlympaPermission) f.get(null);
					permission.setName(f.getName());
					permissions.put(f.getName(), permission);
					i++;
				}
			} catch (Exception e) {
				LinkSpigotBungee.getInstance().sendMessage("&cError when registering permission &4%s&c. %s", f.getName(), e.getMessage());
				e.printStackTrace();
			}
		try {
			LinkSpigotBungee.getInstance().sendMessage("Registered %d permissions from %s", i, clazz.getName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		//		} catch (ReflectiveOperationException ex) {
		//			LinkSpigotBungee.getInstance().sendMessage("Error when registering permissions from class " + clazz.getName());
		//			ex.printStackTrace();
		//		}
	}

	String permissionIfBukkit = null;
	@Nullable
	OlympaGroup minGroup = null;
	@Nullable
	OlympaGroup[] allowedGroups = null;
	boolean disabled = false;
	UUID[] allowedBypass = null;
	boolean lockPermission = false;
	private String name;

	public OlympaPermission() {}

	public OlympaPermission(OlympaGroup minGroup) {
		this.minGroup = minGroup;
	}

	public OlympaPermission(OlympaGroup minGroup, boolean lockPermission) {
		this.minGroup = minGroup;
		this.lockPermission = lockPermission;
	}

	public OlympaPermission(OlympaGroup... allowedGroups) {
		this.allowedGroups = allowedGroups;
	}

	public OlympaPermission(boolean lockPermission, OlympaGroup... allowedGroups) {
		this.lockPermission = lockPermission;
		this.allowedGroups = allowedGroups;
	}

	public OlympaPermission(OlympaGroup minGroup, OlympaGroup[] allowedGroups) {
		this.minGroup = minGroup;
		this.allowedGroups = allowedGroups;
	}

	public OlympaPermission(OlympaGroup minGroup, OlympaGroup[] allowedGroups, boolean lockPermission) {
		this.minGroup = minGroup;
		this.allowedGroups = allowedGroups;
		this.lockPermission = lockPermission;
	}

	@Override
	public String getName() {
		return name;
	}

	private void setName(String name) {
		this.name = name;
	}

	public abstract ServerFrameworkType getServerType();

	public OlympaGroup getMinGroup() {
		return minGroup;
	}

	public UUID[] getAllowedBypass() {
		return allowedBypass;
	}

	public boolean isInAllowedBypass(UUID uuid) {
		return allowedBypass != null && Arrays.stream(allowedBypass).anyMatch(u -> u.equals(uuid));
	}

	public OlympaGroup[] getAllowedGroups() {
		return allowedGroups;
	}

	public OlympaGroup[] clearAllowedGroups() {
		return allowedGroups = null;
	}

	public OlympaGroup[] getAllGroupsAllowed() {
		List<OlympaGroup> allowGroupsList = new ArrayList<>();
		if (minGroup != null)
			allowGroupsList.addAll(Arrays.stream(OlympaGroup.values()).filter(g -> g.getPower() >= minGroup.getPower()).collect(Collectors.toList()));
		if (allowedGroups != null)
			allowGroupsList.addAll(Arrays.asList(allowedGroups));
		return allowGroupsList.stream().sorted((o1, o2) -> o1.getPower() - o2.getPower()).toArray(OlympaGroup[]::new);
	}

	public boolean allowGroup(OlympaGroup group) {
		if (lockPermission)
			return false;
		List<OlympaGroup> allowGroupsList = new ArrayList<>();
		if (allowedGroups != null)
			allowGroupsList.addAll(Arrays.asList(allowedGroups));
		allowGroupsList.add(group);
		allowedGroups = allowGroupsList.stream().toArray(OlympaGroup[]::new);
		return true;
	}

	public boolean disallowGroup(OlympaGroup group) {
		if (lockPermission)
			return false;
		List<OlympaGroup> allowGroupsList = new ArrayList<>();
		if (allowedGroups != null)
			allowGroupsList.addAll(Arrays.asList(allowedGroups));
		boolean b = allowGroupsList.remove(group);
		allowedGroups = allowGroupsList.stream().toArray(OlympaGroup[]::new);
		return b;
	}

	public OlympaPermission lockPermission() {
		lockPermission = true;
		return this;
	}

	public void setMinGroup(OlympaGroup group) {
		if (lockPermission)
			return;
		minGroup = group;
	}

	@Override
	public boolean hasPermission(UUID uniqueId) {
		return this.hasPermission(AccountProviderAPI.getter().<OlympaPlayer>get(uniqueId));
	}

	@Override
	public boolean hasPermission(OlympaPlayer olympaPlayer) {
		return olympaPlayer != null && (this.hasPermission(olympaPlayer.getGroups()) || allowedBypass != null && Arrays.stream(allowedBypass).anyMatch(ab -> ab.equals(olympaPlayer.getUniqueId()))
				|| olympaPlayer.hasCustomPermission(name, LinkSpigotBungee.getInstance().getOlympaServer()));
	}

	@Override
	public boolean hasPermission(Map<OlympaGroup, Long> groups) {
		return groups.entrySet().stream().anyMatch(entry -> this.hasPermission(entry.getKey()));
	}

	@Override
	public boolean hasPermission(OlympaGroup group) {
		return (!disabled || group.isHighStaff())
				&& (minGroup != null && group.getPower() >= minGroup.getPower() || allowedGroups != null && Arrays.stream(allowedGroups).anyMatch(ga -> ga.getPower() == group.getPower()));

	}

	public boolean isLocked() {
		return lockPermission;
	}

	public void disable() {
		disabled = true;
	}

	public void enable() {
		disabled = false;
	}

	public abstract void sendMessage(BaseComponent... baseComponents);

	public abstract void sendMessage(String message, Object... args);
}
