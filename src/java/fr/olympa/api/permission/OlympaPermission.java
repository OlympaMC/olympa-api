package fr.olympa.api.permission;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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
import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.core.spigot.OlympaCore;
import net.md_5.bungee.api.chat.BaseComponent;

public class OlympaPermission {

	public static final Map<String, OlympaPermission> permissions = new HashMap<>();

	public static void registerPermissions(Class<?> clazz) {
		try {
			int initialSize = permissions.size();
			for (Field f : clazz.getDeclaredFields()) {
				if (f.getType() == OlympaPermission.class && Modifier.isStatic(f.getModifiers())) {
					permissions.put(f.getName(), (OlympaPermission) f.get(null));
				}
			}
			OlympaCore.getInstance().sendMessage("Registered " + (permissions.size() - initialSize) + " permissions from " + clazz.getName());
		} catch (ReflectiveOperationException ex) {
			OlympaCore.getInstance().sendMessage("Error when registering permissions from class " + clazz.getName());
			ex.printStackTrace();
		}
	}

	OlympaGroup min_group = null;
	OlympaGroup[] groups_allow = null;

	public OlympaPermission(OlympaGroup min_group) {
		this.min_group = min_group;
	}

	public OlympaPermission(OlympaGroup... groups_allowGroup) {
		groups_allow = groups_allowGroup;
	}

	public OlympaPermission(OlympaGroup[] groups_allowGroup, OlympaGroup min_group) {
		this.min_group = min_group;
		groups_allow = groups_allowGroup;
	}

	public OlympaGroup getGroup() {
		return min_group;
	}

	public void getPlayers(Consumer<? super Set<Player>> success) {
		Collection<? extends Player> players = Bukkit.getOnlinePlayers();
		Set<Player> playerswithPerm = players.stream().filter(player -> this.hasPermission(AccountProvider.<OlympaPlayer>get(player.getUniqueId()))).collect(Collectors.toSet());
		if (!playerswithPerm.isEmpty()) {
			success.accept(playerswithPerm);
		}
	}

	public void getPlayers(Consumer<? super Set<Player>> success, Consumer<? super Set<Player>> noPerm) {
		Collection<? extends Player> players = Bukkit.getOnlinePlayers();
		Set<Player> playersWithNoPerm = new HashSet<>();
		Set<Player> playersWithPerm = new HashSet<>();
		players.stream().forEach(player -> {
			if (this.hasPermission(AccountProvider.<OlympaPlayer>get(player.getUniqueId()))) {
				playersWithPerm.add(player);
			} else {
				playersWithNoPerm.add(player);
			}
		});
		if (!playersWithPerm.isEmpty() && success != null) {
			success.accept(playersWithPerm);
		}
		if (!playersWithNoPerm.isEmpty() && noPerm != null) {
			noPerm.accept(playersWithPerm);
		}
	}

	public boolean hasPermission(CommandSender sender) {
		if (sender instanceof Player) {
			return this.hasPermission(((Player) sender).getUniqueId());
		}
		return true;
	}

	public boolean hasPermission(OlympaGroup group) {
		return min_group != null && group.getPower() >= min_group.getPower()
				|| groups_allow != null && Arrays.stream(groups_allow).anyMatch(group_allow -> group_allow.getPower() == group.getPower());

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

	public void sendMessage(BaseComponent baseComponent) {
		this.getPlayers(players -> players.forEach(player -> player.spigot().sendMessage(baseComponent)), null);
	}

	public void sendMessage(String message) {
		this.getPlayers(players -> players.forEach(player -> player.sendMessage(message)), null);
	}

}
