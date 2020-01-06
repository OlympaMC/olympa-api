package fr.olympa.api.permission;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.olympa.OlympaCore;
import fr.olympa.api.objects.OlympaGroup;
import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import net.md_5.bungee.api.chat.BaseComponent;

public class OlympaPermission {

	public static final Map<String, OlympaPermission> permissions = new HashMap<>();

	public static synchronized void registerPermissions(Class<?> clazz) {
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

	OlympaGroup min_group;
	OlympaGroup[] groups_allow;

	public OlympaPermission(OlympaGroup min_group) {
		this.min_group = min_group;
	}

	public OlympaPermission(OlympaGroup... groups_allowGroup) {
		this.groups_allow = groups_allowGroup;
	}

	public OlympaGroup getGroup() {
		return this.min_group;
	}

	public void getPlayers(Consumer<? super Set<Player>> success) {
		Set<Player> players = Bukkit.getOnlinePlayers().stream().filter(player -> this.hasPermission(AccountProvider.get(player.getUniqueId()))).collect(Collectors.toSet());
		if (!players.isEmpty()) {
			success.accept(players);
		}
	}

	public boolean hasPermission(CommandSender sender) {
		if (sender instanceof Player) {
			return this.hasPermission((Player) sender);
		}
		return true;
	}

	public boolean hasPermission(OlympaGroup group) {
		if (this.min_group != null) {
			return group.getPower() >= this.min_group.getPower();
		} else {
			return Arrays.stream(this.groups_allow).anyMatch(group_allow -> group_allow.getPower() == group.getPower());
		}
	}

	public boolean hasPermission(OlympaPlayer olympaPlayer) {
		return olympaPlayer != null && this.hasPermission(olympaPlayer);
	}

	public boolean hasPermission(Player player) {
		return this.hasPermission(player.getUniqueId());
	}

	public boolean hasPermission(UUID uniqueId) {
		return this.hasPermission(AccountProvider.get(uniqueId));
	}

	public void sendMessage(BaseComponent baseComponent) {
		this.getPlayers(players -> players.forEach(player -> player.spigot().sendMessage(baseComponent)));
	}

	public void sendMessage(String message) {
		this.getPlayers(players -> players.forEach(player -> player.sendMessage(message)));
	}

}
