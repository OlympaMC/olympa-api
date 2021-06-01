package fr.olympa.api.common.player;

import org.bukkit.entity.Player;

import fr.olympa.api.LinkSpigotBungee;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class PlayerUniversal<T> {

	T player;
	private static final boolean isSpigot = LinkSpigotBungee.Provider.link.isSpigot();

	public String getName() {
		if (isSpigot)
			return ((Player) player).getName();
		else
			return ((ProxiedPlayer) player).getName();
	}

	public String getAdress() {
		if (isSpigot)
			return ((Player) player).getAddress().getAddress().getHostAddress();
		else
			return ((ProxiedPlayer) player).getName();
	}
}
