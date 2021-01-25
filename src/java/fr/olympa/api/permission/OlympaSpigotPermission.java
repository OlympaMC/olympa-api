package fr.olympa.api.permission;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.olympa.api.chat.ColorUtils;
import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.server.ServerType;
import net.md_5.bungee.api.chat.BaseComponent;

public class OlympaSpigotPermission extends OlympaPermission {
	
	public OlympaSpigotPermission(boolean lockPermission, OlympaGroup... allowedGroups) {
		super(lockPermission, allowedGroups);
	}
	
	public OlympaSpigotPermission(OlympaGroup minGroup, boolean lockPermission) {
		super(minGroup, lockPermission);
	}
	
	public OlympaSpigotPermission(OlympaGroup minGroup, OlympaGroup[] allowedGroups, boolean lockPermission) {
		super(minGroup, allowedGroups, lockPermission);
	}
	
	public OlympaSpigotPermission(OlympaGroup minGroup, OlympaGroup[] allowedGroups) {
		super(minGroup, allowedGroups);
	}

	public OlympaSpigotPermission(OlympaGroup... allowedGroups) {
		super(allowedGroups);
	}
	
	public OlympaSpigotPermission(OlympaGroup minGroup) {
		super(minGroup);
	}
	
	@Override
	public ServerType getServerType() {
		return ServerType.SPIGOT;
	}
	
	@Override
	public OlympaSpigotPermission lockPermission() {
		return (OlympaSpigotPermission) super.lockPermission();
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
	
	public boolean hasSenderPermission(CommandSender sender) {
		if (sender instanceof Player)
			return this.hasPermission(((Player) sender).getUniqueId());
		return true;
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
	
	@Override
	public void sendMessage(BaseComponent... baseComponents) {
		this.getPlayers(players -> players.forEach(player -> player.spigot().sendMessage(baseComponents)), null);
	}
	
	@Override
	public void sendMessage(String message, Object... args) {
		this.getPlayers(players -> players.forEach(player -> player.sendMessage(ColorUtils.color(String.format(message, args)))), null);
	}
	
}
