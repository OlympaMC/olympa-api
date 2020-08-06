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
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.core.spigot.OlympaCore;
import net.md_5.bungee.api.chat.BaseComponent;

public class OlympaPermission {

	public static final Map<String, OlympaPermission> permissions = new HashMap<>();

	public static void registerPermissions(Class<?> clazz) {
		try {
			int initialSize = permissions.size();
			for (Field f : clazz.getDeclaredFields())
				if (f.getType() == OlympaPermission.class && Modifier.isStatic(f.getModifiers()))
					permissions.put(f.getName(), (OlympaPermission) f.get(null));
			OlympaCore.getInstance().sendMessage("Registered " + (permissions.size() - initialSize) + " permissions from " + clazz.getName());
		} catch (ReflectiveOperationException ex) {
			OlympaCore.getInstance().sendMessage("Error when registering permissions from class " + clazz.getName());
			ex.printStackTrace();
		}
	}

	OlympaGroup minGroup = null;
	OlympaGroup[] allowedGroups = null;

	public OlympaPermission(OlympaGroup minGroup) {
		this.minGroup = minGroup;
	}

	public OlympaPermission(OlympaGroup... allowedGroups) {
		this.allowedGroups = allowedGroups;
	}

	public OlympaPermission(OlympaGroup minGroup, OlympaGroup[] allowedGroups) {
		this.minGroup = minGroup;
		this.allowedGroups = allowedGroups;
	}

	public OlympaGroup getGroup() {
		return minGroup;
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

	public boolean hasPermission(OlympaGroup group) {
		return minGroup != null && group.getPower() >= minGroup.getPower()
				|| allowedGroups != null && Arrays.stream(allowedGroups).anyMatch(group_allow -> group_allow.getPower() == group.getPower());

	}

	// TODO set to protected
	public void addAllowGroup(OlympaGroup group) {
		List<OlympaGroup> allowGroupsList = new ArrayList<>(Arrays.asList(allowedGroups));
		allowGroupsList.add(group);
		allowedGroups = allowGroupsList.stream().toArray(OlympaGroup[]::new);
	}

	public boolean hasPermission(OlympaPlayer olympaPlayer) {
		return olympaPlayer != null && this.hasPermission(olympaPlayer.getGroups());
	}

	public boolean hasPermission(TreeMap<OlympaGroup, Long> groups) {
		return groups.entrySet().stream().anyMatch(entry -> this.hasPermission(entry.getKey()));
	}

	public boolean hasPermission(UUID uniqueId) {
		return this.hasPermission(AccountProvider.<OlympaPlayer>get(uniqueId));
	}

	public boolean hasSenderPermission(CommandSender sender) {
		if (sender instanceof Player)
			return this.hasPermission(((Player) sender).getUniqueId());
		return true;
	}

	public void sendMessage(BaseComponent baseComponent) {
		this.getPlayers(players -> players.forEach(player -> player.spigot().sendMessage(baseComponent)), null);
	}

	public void sendMessage(String message) {
		this.getPlayers(players -> players.forEach(player -> player.sendMessage(message)), null);
	}

}
