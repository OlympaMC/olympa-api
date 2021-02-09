package fr.olympa.api.module;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import fr.olympa.api.module.OlympaModule.ModuleApi;
import fr.olympa.api.plugin.OlympaPluginInterface;

public class OlympaModule<T extends ModuleApi<P>, L, P, C> {

	public static final boolean DEBUG = false;

	public interface ModuleApi<P> {
		boolean disable(P plugin);

		boolean enable(P plugin);

		boolean isEnabled();
	}

	String name;
	T api;
	P plugin;
	List<Class<? extends L>> eventsToRegister;
	List<Class<? extends C>> commandToRegister;
	Function<P, T> functionInitialize;
	BiConsumer<P, T> functionSetToPlugin;

	/**
	 * @param plugin
	 * @param name
	 * @param api
	 * @param functionInitialize
	 * @param functionEnable
	 * @param functionDisable
	 * @param functionCheckEnabled
	 * @param eventToRegister
	 */
	public OlympaModule(P plugin, String name, Function<P, T> functionInitialize, BiConsumer<P, T> functionSetToPlugin, List<Class<? extends L>> eventsToRegister, List<Class<? extends C>> commandToRegister) {
		this.plugin = plugin;
		this.name = name;
		this.functionInitialize = functionInitialize;
		this.functionSetToPlugin = functionSetToPlugin;
		this.eventsToRegister = eventsToRegister;
		this.commandToRegister = commandToRegister;
	}

	public String getName() {
		return name;
	}

	public String isEnabledString() {
		return isEnabled() ? "&2Enabled" : "&4Disabled";
	}

	public boolean isEnabled() {
		return api.isEnabled();
	}
	//	public boolean isEnabled() {
	//		try {
	//			Boolean isEnable = checkIfEnabled();
	//			if (isEnable != enable)
	//				enable = isEnable;
	//			return enable;
	//		} catch (Exception e) {
	//			sendErrorModule(e, "isEnable");
	//			return false;
	//		}
	//	}

	public List<Class<? extends L>> getEventToRegister() {
		return eventsToRegister;
	}

	public T reInitialize() {
		disable();
		return initialize();
	}

	public T initialize() {
		if (api != null)
			return api;
		try {
			api = functionInitialize.apply(plugin);
			if (DEBUG)
				((OlympaPluginInterface) plugin).sendMessage("&eModule &6%s&e initialisé.", name);
			return api;
		} catch (Exception e) {
			sendErrorModule(e, "initialize");
			return null;
		}
	}

	public boolean enable() {
		if (isEnabled())
			return false;
		try {
			api.enable(plugin);
			((OlympaPluginInterface) plugin).sendMessage("&aModule &2%s&a activé.", name);
			return true;
		} catch (Exception e) {
			sendErrorModule(e, "enable");
			return false;
		}
	}

	public boolean setToPlugin() {
		if (!isEnabled() || functionSetToPlugin == null)
			return false;
		try {
			functionSetToPlugin.accept(plugin, api);
			if (DEBUG)
				((OlympaPluginInterface) plugin).sendMessage("&aModule &2%s&a associé au plugin %s.", name, plugin.getClass().getSimpleName());
			return true;
		} catch (Exception e) {
			sendErrorModule(e, "setToPlugin");
			return false;
		}
	}

	public boolean disable() {
		if (!isEnabled())
			return false;
		try {
			api.disable(plugin);
			((OlympaPluginInterface) plugin).sendMessage("&cModule &4" + name + "&c désactivé.");
			return true;
		} catch (Exception e) {
			sendErrorModule(e, "disable");
			return false;
		}
	}

	public void sendErrorModule(Exception e, String whileDoing) {
		sendErrorModule(this, e, whileDoing);
	}

	public T getApi() {
		return api;
	}

	public P getPlugin() {
		return plugin;
	}

	public static void sendErrorModule(OlympaModule<?, ?, ?, ?> module, Exception e, String whileDoing) {
		new Exception(String.format("§cError in Module§4 %s §cwhile do§4 %s§c.", module.getName(), whileDoing)).initCause(e).printStackTrace();
	}

}
