package fr.olympa.api.common.permission;

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

import fr.olympa.api.common.chat.ColorUtils;
import fr.olympa.api.common.groups.OlympaGroup;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.api.common.server.ServerFrameworkType;
import fr.olympa.api.utils.Prefix;
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
	public ServerFrameworkType getServerType() {
		return ServerFrameworkType.SPIGOT;
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
		Set<Player> playerswithPerm = players.stream().filter(player -> this.hasPermission(AccountProviderAPI.getter().<OlympaPlayer>get(player.getUniqueId()))).collect(Collectors.toSet());
		if (!playerswithPerm.isEmpty())
			success.accept(playerswithPerm);
	}

	public void getPlayers(Consumer<? super Set<Player>> success, Consumer<? super Set<Player>> noPerm) {
		Collection<? extends Player> players = Bukkit.getOnlinePlayers();
		Set<Player> playersWithNoPerm = new HashSet<>();
		Set<Player> playersWithPerm = new HashSet<>();
		players.forEach(player -> {
			if (this.hasPermission(AccountProviderAPI.getter().<OlympaPlayer>get(player.getUniqueId())))
				playersWithPerm.add(player);
			else
				playersWithNoPerm.add(player);
		});
		if (!playersWithPerm.isEmpty() && success != null)
			success.accept(playersWithPerm);
		if (!playersWithNoPerm.isEmpty() && noPerm != null)
			noPerm.accept(playersWithNoPerm);
	}

	public void getOlympaPlayers(Consumer<? super Set<OlympaPlayer>> success, Consumer<? super Set<OlympaPlayer>> noPerm) {
		Collection<? extends Player> players = Bukkit.getOnlinePlayers();
		Set<OlympaPlayer> playersWithNoPerm = new HashSet<>();
		Set<OlympaPlayer> playersWithPerm = new HashSet<>();
		players.forEach(player -> {
			OlympaPlayer op = AccountProviderAPI.getter().<OlympaPlayer>get(player.getUniqueId());
			if (this.hasPermission(op))
				playersWithPerm.add(op);
			else
				playersWithNoPerm.add(op);
		});
		if (!playersWithPerm.isEmpty() && success != null)
			success.accept(playersWithPerm);
		if (!playersWithNoPerm.isEmpty() && noPerm != null)
			noPerm.accept(playersWithNoPerm);
	}

	/**
	 * Check if the player has the permission, and sends an alert message if not
	 * @param olympaPlayer
	 * @return
	 */
	public boolean hasPermissionWithMsg(OlympaPlayer olympaPlayer) {
		boolean b = hasPermission(olympaPlayer);
		Player player = (Player) olympaPlayer.getPlayer();
		if (!b)
			if (getMinGroup() != null)
				Prefix.DEFAULT_BAD.sendMessage(player, "Le grade %s est requis pour exécuter cette action.", getMinGroup().getName(olympaPlayer.getGender()));
			else if (getAllowedGroups() != null && getAllowedGroups().length != 0)
				Prefix.DEFAULT_BAD.sendMessage(player, "Pour exécuter cette action, tu dois avoir l'un des groupes suivants : %s.", Arrays.stream(getAllowedGroups()).map(g -> g.getName(olympaPlayer.getGender())));
			else
				Prefix.DEFAULT_BAD.sendMessage(player, "Tu n'a pas la permission.");

		return b;
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
