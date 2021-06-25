package fr.olympa.api.bungee.utils;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import fr.olympa.api.bungee.player.DataHandler;
import fr.olympa.api.common.chat.ColorUtils;
import fr.olympa.api.common.permission.OlympaPermission;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.common.player.OlympaPlayerInformations;
import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.core.bungee.OlympaBungee;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;

public class BungeeUtils {

	private static Boolean isWaterfall;

	public static List<String> color(List<String> l) {
		return l.stream().map(s -> ColorUtils.color(s)).collect(Collectors.toList());
	}

	public static boolean isWaterfall() {
		if (isWaterfall == null)
			try {
				isWaterfall = Class.forName("io.github.waterfallmc.waterfall.event.ProxyDefineCommandsEvent") != null;
			} catch (ClassNotFoundException e) {
				isWaterfall = false;
			}
		return isWaterfall;
	}

	@Deprecated(forRemoval = true)
	public static BaseComponent[] format(String format, Object... args) {
		return TextComponent.fromLegacyText(ColorUtils.format(format, args));
	}

	@SuppressWarnings("deprecation")
	public static String getIP(Connection connection) {
		return connection.getAddress().getAddress().getHostAddress();
	}

	public static String connectScreen(String s, Object... args) {
		Configuration config = OlympaBungee.getInstance().getConfig();
		return ColorUtils.color(config.getString("default.connectscreenprefix") + String.format(s, args) + config.getString("default.connectscreensuffix")) + "§r";
	}

	public static TextComponent connectScreenComponent(String s, Object... args) {
		Configuration config = OlympaBungee.getInstance().getConfig();
		return stringToTextConponent(config.getString("default.connectscreenprefix") + String.format(s, args) + config.getString("default.connectscreensuffix") + "§r");
	}

	public static TextComponent stringToTextConponent(String s) {
		return new TextComponent(TextComponent.fromLegacyText(ColorUtils.color(s)));
	}

	public static String getName(UUID playerUniqueId) {
		ProxiedPlayer player = ProxyServer.getInstance().getPlayer(playerUniqueId);
		if (player != null)
			return player.getName();
		try {
			OlympaPlayer olympaPlayer = new AccountProviderAPI(playerUniqueId).get();
			if (olympaPlayer != null)
				return olympaPlayer.getName();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "";
	}

	public static String getName(long playerId) {
		OlympaPlayerInformations olympaPlayer = AccountProviderAPI.getter().getPlayerInformations(playerId);
		if (olympaPlayer != null)
			return olympaPlayer.getName();
		return "";
	}

	public static Set<ProxiedPlayer> getPlayers() {
		return OlympaBungee.getInstance().getProxy().getPlayers().stream().filter(p -> !DataHandler.isUnlogged(p)).collect(Collectors.toSet());
	}

	public static void getPlayers(OlympaPermission permission, Consumer<? super Set<ProxiedPlayer>> success, Consumer<? super Set<ProxiedPlayer>> noPerm) {
		Set<ProxiedPlayer> playersWithNoPerm = new HashSet<>();
		Set<ProxiedPlayer> playersWithPerm = new HashSet<>();
		OlympaBungee.getInstance().getProxy().getPlayers().stream().forEach(p -> {
			OlympaPlayer op;
			try {
				op = new AccountProviderAPI(p.getUniqueId()).get();
			} catch (SQLException e) {
				op = null;
				e.printStackTrace();
			}
			if (op != null && !DataHandler.isUnlogged(p) && permission.hasPermission(op))
				playersWithPerm.add(p);
			else
				playersWithNoPerm.add(p);
		});
		if (!playersWithPerm.isEmpty() && success != null)
			success.accept(playersWithPerm);
		if (!playersWithNoPerm.isEmpty() && noPerm != null)
			noPerm.accept(playersWithNoPerm);
	}

	public static void changeSlots(int slots) throws ReflectiveOperationException {
		ProxyServer proxy = ProxyServer.getInstance();
		Class<?> configClass = proxy.getConfig().getClass();
		if (!configClass.getSuperclass().equals(Object.class))
			configClass = configClass.getSuperclass();
		Field playerLimitField = configClass.getDeclaredField("playerLimit");
		playerLimitField.setAccessible(true);
		playerLimitField.set(proxy.getConfig(), slots);
	}
}
