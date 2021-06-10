package fr.olympa.api.bungee.permission;

import java.sql.SQLException;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import fr.olympa.api.common.chat.ColorUtils;
import fr.olympa.api.common.groups.OlympaGroup;
import fr.olympa.api.common.permission.OlympaPermission;
import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.api.common.server.ServerFrameworkType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class OlympaBungeePermission extends OlympaPermission {

	public OlympaBungeePermission() {}

	public OlympaBungeePermission(OlympaGroup minGroup) {
		super(minGroup);
	}

	public OlympaBungeePermission(OlympaGroup... allowedGroups) {
		super(allowedGroups);
	}

	public OlympaBungeePermission(OlympaGroup minGroup, boolean lockPermission) {
		super(minGroup, lockPermission);
	}

	public OlympaBungeePermission(boolean lockPermission, OlympaGroup... allowedGroups) {
		super(lockPermission, allowedGroups);
	}

	public OlympaBungeePermission(OlympaGroup minGroup, OlympaGroup[] allowedGroups) {
		super(minGroup, allowedGroups);
	}

	public OlympaBungeePermission(OlympaGroup minGroup, OlympaGroup[] allowedGroups, boolean lockPermission) {
		super(minGroup, allowedGroups, lockPermission);
	}

	@Override
	public ServerFrameworkType getServerType() {
		return ServerFrameworkType.BUNGEE;
	}

	public void getPlayersBungee(Consumer<? super Set<ProxiedPlayer>> success) {
		Set<ProxiedPlayer> players = ProxyServer.getInstance().getPlayers().stream().filter(p -> {
			try {
				return this.hasPermission(new AccountProviderAPI(p.getUniqueId()).get());
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		}).collect(Collectors.toSet());
		if (!players.isEmpty())
			success.accept(players);
		else
			success.accept(null);
	}

	@Override
	public OlympaBungeePermission lockPermission() {
		return (OlympaBungeePermission) super.lockPermission();
	}

	@Override
	public void sendMessage(BaseComponent... baseComponents) {
		getPlayersBungee(players -> {
			if (players != null)
				players.forEach(player -> player.sendMessage(baseComponents));
		});
	}

	@Override
	public void sendMessage(String message, Object... args) {
		getPlayersBungee(players -> players.forEach(player -> player.sendMessage(TextComponent.fromLegacyText(ColorUtils.color(String.format(message, args))))));
	}

	public boolean hasSenderPermissionBungee(CommandSender sender) {
		if (sender instanceof ProxiedPlayer)
			try {
				return this.hasPermission(new AccountProviderAPI(((ProxiedPlayer) sender).getUniqueId()).get());
			} catch (SQLException e) {
				e.addSuppressed(new IllegalAccessError(String.format("Can't allow %s to use olympa permission %s cause of SQLException.", sender.getName(), getName())));
				e.printStackTrace();
				return false;
			}
		return true;
	}
}
