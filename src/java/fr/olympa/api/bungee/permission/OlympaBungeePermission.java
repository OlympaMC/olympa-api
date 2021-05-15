package fr.olympa.api.bungee.permission;

import java.sql.SQLException;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import fr.olympa.api.chat.ColorUtils;
import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.server.ServerType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class OlympaBungeePermission extends OlympaPermission {

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
	public ServerType getServerType() {
		return ServerType.BUNGEE;
	}

	public void getPlayersBungee(Consumer<? super Set<ProxiedPlayer>> success) {
		Set<ProxiedPlayer> players = ProxyServer.getInstance().getPlayers().stream().filter(p -> {
			try {
				return this.hasPermission(new AccountProvider(p.getUniqueId()).get());
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
			return this.hasPermission(((ProxiedPlayer) sender).getUniqueId());
		return true;
	}
}
