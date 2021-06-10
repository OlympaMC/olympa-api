package fr.olympa.api.common.logger;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import fr.olympa.api.LinkSpigotBungee;

public class LoggerUtils {

	private static List<Handler> registerHandlers = new ArrayList<>();

	public static void hook(Handler handler) {
		LogManager manager = LogManager.getLogManager();
		Enumeration<String> names = manager.getLoggerNames();
		int i = 1;
		while (names.hasMoreElements()) {
			String name = names.nextElement();
			@Nullable
			Logger logger = manager.getLogger(name);
			if (logger == null) {
				LinkSpigotBungee.Provider.link.sendMessage("&cUnable to hook into logger '&6%s&e', it is null", name);
				continue;
			}
			logger.addHandler(handler);
			i++;
		}
		LinkSpigotBungee.Provider.link.sendMessage("Hooked error stream handler into &6%s&e loggers!", i);
	}

	public static void unHookAll() {
		if (registerHandlers.isEmpty())
			return;
		LogManager manager = LogManager.getLogManager();
		Enumeration<String> names = manager.getLoggerNames();
		int i = 1;
		while (names.hasMoreElements()) {
			String name = names.nextElement();
			@Nullable
			Logger logger = manager.getLogger(name);
			if (logger == null) {
				LinkSpigotBungee.Provider.link.sendMessage("&cUnable to unhook into logger '&6%s&e', it is null", name);
				continue;
			}
			registerHandlers.forEach(handler -> logger.addHandler(handler));
			i++;
		}
		LinkSpigotBungee.Provider.link.sendMessage("Hooked error stream handler into &6%s&e loggers!", i);
	}
}
