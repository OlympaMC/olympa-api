package fr.olympa.api.permission;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.chat.ColorUtils;
import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.server.ServerType;
import fr.olympa.api.utils.Prefix;
import net.md_5.bungee.api.chat.BaseComponent;

public class OlympaPermission {

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
				LinkSpigotBungee.Provider.link.sendMessage("&cError when registering permission &4%s&c. %s", f.getName(), e.getMessage());
				e.printStackTrace();
			}
		try {
			LinkSpigotBungee.Provider.link.sendMessage("Registered %d permissions from %s", i, clazz.getName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		//		} catch (ReflectiveOperationException ex) {
		//			LinkSpigotBungee.Provider.link.sendMessage("Error when registering permissions from class " + clazz.getName());
		//			ex.printStackTrace();
		//		}
	}

	OlympaGroup minGroup = null;
	OlympaGroup[] allowedGroups = null;
	boolean disabled = false;
	UUID[] allowedBypass = null;
	boolean lockPermission = false;
	protected ServerType serverType = ServerType.SPIGOT;
	private String name;

	public OlympaPermission(OlympaGroup minGroup) {
		this.minGroup = minGroup;
	}

	public OlympaPermission(OlympaGroup minGroup, ServerType serverType) {
		this.minGroup = minGroup;
		this.serverType = serverType;
	}

	public OlympaPermission(OlympaGroup minGroup, boolean lockPermission) {
		this.minGroup = minGroup;
		this.lockPermission = lockPermission;
	}

	public OlympaPermission(OlympaGroup minGroup, boolean lockPermission, ServerType serverType) {
		this.minGroup = minGroup;
		this.lockPermission = lockPermission;
		this.serverType = serverType;
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

	public OlympaPermission(OlympaGroup minGroup, OlympaGroup[] allowedGroups, ServerType serverType) {
		this.minGroup = minGroup;
		this.allowedGroups = allowedGroups;
		this.serverType = serverType;
	}

	public OlympaPermission(OlympaGroup minGroup, OlympaGroup[] allowedGroups, boolean lockPermission) {
		this.minGroup = minGroup;
		this.allowedGroups = allowedGroups;
		this.lockPermission = lockPermission;
	}

	public OlympaPermission(OlympaGroup minGroup, OlympaGroup[] allowedGroups, boolean lockPermission, ServerType serverType) {
		this.minGroup = minGroup;
		this.allowedGroups = allowedGroups;
		this.lockPermission = lockPermission;
		this.serverType = serverType;
	}

	public String getName() {
		return name;
	}

	private void setName(String name) {
		this.name = name;
	}

	public ServerType getServerType() {
		return serverType;
	}

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

	public void getPlayers(Consumer<? super Set<Player>> success) {
		Collection<? extends Player> players = Bukkit.getOnlinePlayers();
		Set<Player> playerswithPerm = players.stream().filter(player -> this.hasPermission(AccountProvider.<OlympaPlayer>get(player.getUniqueId()))).collect(Collectors.toSet());
		if (!playerswithPerm.isEmpty())
			success.accept(playerswithPerm);
	}

	public void getPlayers(Consumer<? super Set<Player>> success, Consumer<? super Set<Player>> noPerm) {
		Collection<? extends Player> players = Bukkit.getOnlinePlayers();
		Set<Player> playersWithNoPerm = new HashSet<>();
		Set<Player> playersWithPerm = new HashSet<>();
		players.stream().forEach(player -> {
			if (this.hasPermission(AccountProvider.<OlympaPlayer>get(player.getUniqueId())))
				playersWithPerm.add(player);
			else
				playersWithNoPerm.add(player);
		});
		if (!playersWithPerm.isEmpty() && success != null)
			success.accept(playersWithPerm);
		if (!playersWithNoPerm.isEmpty() && noPerm != null)
			noPerm.accept(playersWithPerm);
	}

	public boolean allowPlayer(Player player) {
		if (lockPermission)
			return false;
		List<UUID> allowBypassList = new ArrayList<>();
		if (allowedBypass != null)
			allowBypassList.addAll(Arrays.asList(allowedBypass));
		allowBypassList.add(player.getUniqueId());
		allowedBypass = allowBypassList.stream().toArray(UUID[]::new);
		return true;
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

	public boolean disallowPlayer(Player player) {
		if (lockPermission)
			return false;
		List<UUID> allowBypassList = new ArrayList<>();
		if (allowedBypass != null)
			allowBypassList.addAll(Arrays.asList(allowedBypass));
		boolean b = allowBypassList.remove(player.getUniqueId());
		allowedGroups = allowBypassList.stream().toArray(OlympaGroup[]::new);
		return b;
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

	public boolean hasPermission(UUID uniqueId) {
		return this.hasPermission(AccountProvider.<OlympaPlayer>get(uniqueId));
	}

	public boolean hasPermission(OlympaPlayer olympaPlayer) {
		return olympaPlayer != null && this.hasPermission(olympaPlayer.getGroups()) || allowedBypass != null && Arrays.stream(allowedBypass).anyMatch(ab -> ab.equals(olympaPlayer.getUniqueId()));
	}

	/**
	 * Check if the player has the permission, and sends an alert message if not
	 * @param olympaPlayer
	 * @return
	 */
	public boolean hasPermissionWithMsg(OlympaPlayer olympaPlayer) {
		boolean b = hasPermission(olympaPlayer);
		if (!b)
			if (getMinGroup() != null)
				Prefix.DEFAULT_BAD.sendMessage(olympaPlayer.getPlayer(), "Le grade %s est requis pour exécuter cette action.", getMinGroup().getName(olympaPlayer.getGender()));
			else if (getAllowedGroups() != null && getAllowedGroups().length != 0)
				Prefix.DEFAULT_BAD.sendMessage(olympaPlayer.getPlayer(), "Pour exécuter cette action, tu dois avoir l'un des groupes suivants : %s.",
						Arrays.stream(getAllowedGroups()).map(g -> g.getName(olympaPlayer.getGender())).collect(Collectors.joining(", ")));
			else
				Prefix.DEFAULT_BAD.sendMessage(olympaPlayer.getPlayer(), "Tu n'a pas la permission.");

		return b;
	}

	public boolean hasPermission(TreeMap<OlympaGroup, Long> groups) {
		return groups.entrySet().stream().anyMatch(entry -> this.hasPermission(entry.getKey()));
	}

	public boolean hasPermission(OlympaGroup group) {
		return (!disabled || group.isHighStaff())
				&& (minGroup != null && group.getPower() >= minGroup.getPower() || allowedGroups != null && Arrays.stream(allowedGroups).anyMatch(ga -> ga.getPower() == group.getPower()));

	}

	public boolean hasSenderPermission(Object sender) {
		if (sender instanceof Player)
			return this.hasPermission(((Player) sender).getUniqueId());
		else if (sender instanceof ConsoleCommandSender)
			return true;
		return false;
	}

	public void sendMessage(BaseComponent baseComponent) {
		this.getPlayers(players -> players.forEach(player -> player.spigot().sendMessage(baseComponent)), null);
	}

	public void sendMessage(String message, Object... args) {
		this.getPlayers(players -> players.forEach(player -> player.sendMessage(ColorUtils.color(String.format(message, args)))), null);
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
}
