package fr.olympa.api.module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import fr.olympa.api.command.IOlympaCommand;
import fr.olympa.api.module.OlympaModule.ModuleApi;
import fr.olympa.api.plugin.OlympaPluginInterface;
import fr.olympa.api.utils.CreateInstance;

public class OlympaModule<T extends ModuleApi<P>, L, P extends OlympaPluginInterface, C extends IOlympaCommand> {

	public static boolean DEBUG = false;

	public interface ModuleApi<P> {
		boolean disable(P plugin);

		boolean enable(P plugin);

		/**
		 * @param plugin
		 *
		 * @return boolean == false ? error print in console : all is good
		 * 	srsly if you didn't return boolean == true, it will print error in console
		 */
		boolean setToPlugin(P plugin);

		boolean isEnabled();
	}

	String name;
	T api;
	P plugin;
	boolean debug = false;
	List<Class<? extends L>> eventsToRegister;
	List<L> eventsRegistered = new ArrayList<>();
	List<Class<? extends C>> commandsToRegister;
	List<C> commandsRegistered = new ArrayList<>();
	Function<P, T> functionInitialize;
	List<OlympaModule<? extends ModuleApi<?>, ?, ?, ?>> dependances;
	List<OlympaModule<? extends ModuleApi<?>, ?, ?, ?>> softDependances;

	/**
	 * @param plugin
	 * @param name
	 * @param api
	 * @param functionInitialize
	 * @param eventToRegister
	 */
	public OlympaModule(P plugin, String name, Function<P, T> functionInitialize, List<Class<? extends L>> eventsToRegister, List<Class<? extends C>> commandsToRegister) {
		this.plugin = plugin;
		this.name = name;
		this.functionInitialize = functionInitialize;
		this.eventsToRegister = eventsToRegister;
		this.commandsToRegister = commandsToRegister;
	}

	public OlympaModule(P plugin, String name, Function<P, T> functionInitialize, Class<? extends L> eventToRegister, Class<? extends C> commandToRegister) {
		this(plugin, name, functionInitialize, eventToRegister != null ? List.of(eventToRegister) : null, commandToRegister != null ? List.of(commandToRegister) : null);
	}

	public OlympaModule(P plugin, String name, Function<P, T> functionInitialize, Class<? extends L> eventToRegister, Class<? extends C> commandToRegister, OlympaModule<?, ?, ?, ?>... softDependances) {
		this(plugin, name, functionInitialize, eventToRegister != null ? List.of(eventToRegister) : null, commandToRegister != null ? List.of(commandToRegister) : null);
		this.softDependances = Arrays.stream(softDependances).collect(Collectors.toList());
	}

	public OlympaModule(P plugin, String name, Function<P, T> functionInitialize, Class<? extends L> eventToRegister, Class<? extends C> commandToRegister,
			List<OlympaModule<? extends ModuleApi<?>, ?, ?, ?>> softDependances, List<OlympaModule<? extends ModuleApi<?>, ?, ?, ?>> dependances) {
		this(plugin, name, functionInitialize, eventToRegister != null ? List.of(eventToRegister) : null, commandToRegister != null ? List.of(commandToRegister) : null);
		this.softDependances = softDependances;
		this.dependances = dependances;
	}

	public String getName() {
		return name;
	}

	public String isEnabledString() {
		return isEnabled() ? "&2Activé" : "&4Désactivé";
	}

	public String isDebugEnabledString() {
		return isDebugEnabled() ? "&2Activé" : "&4Désactivé";
	}

	public boolean isEnabled() {
		return api.isEnabled();
	}

	public boolean registerCommands() {
		if (commandsToRegister == null || commandsToRegister.isEmpty())
			return false;
		commandsToRegister.forEach(clazz -> {
			C olympaCommand;
			if (api.getClass().isAssignableFrom(clazz)) {
				((IOlympaCommand) api).register();
				if (DEBUG || debug)
					((OlympaPluginInterface) plugin).sendMessage("&eModule &6%s&e : command &6%s&e register et liée à l'Api.", name, clazz.getSimpleName());
			} else {
				olympaCommand = new CreateInstance<C>().of(clazz, plugin, api);
				if (olympaCommand == null) {
					plugin.sendMessage("&cModule &4%s&c : can't register &4%s&c command, constructor was not found.", name, clazz.getSimpleName());
					return;
				}
				olympaCommand.register();
				commandsRegistered.add(olympaCommand);
				if (DEBUG || debug)
					plugin.sendMessage("&eModule &6%s&e : command &6%s&e register.", name, clazz.getSimpleName());
			}
		});
		return true;
	}

	public boolean unregisterCommands() {
		if (commandsRegistered.isEmpty())
			return false;
		this.getCommandsRegistered().forEach(olympaCommand -> {
			olympaCommand.unregister();
		});
		return true;
	}

	//	public void registerListeners();
	//
	//	public void unregisterListeners();

	public List<Class<? extends L>> getEventsToRegister() {
		return eventsToRegister;
	}

	public List<Class<? extends C>> getCommandsToRegister() {
		return commandsToRegister;
	}

	public List<L> getEventsRegistered() {
		return eventsRegistered;
	}

	public List<C> getCommandsRegistered() {
		return commandsRegistered;
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
			if (DEBUG || debug)
				plugin.sendMessage("&eModule &6%s&e initialisé.", name);
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
			plugin.sendMessage("&aModule &2%s&a activé.", name);
			return true;
		} catch (Exception e) {
			sendErrorModule(e, "enable");
			return false;
		}
	}

	public boolean setToPlugin() {
		if (!isEnabled())
			return false;
		try {
			boolean succes = api.setToPlugin(plugin);
			if (!succes)
				plugin.sendMessage("&4Impossible&c d'associer un Module &4%s&c au plugin &4%s&c.", name, plugin.getClass().getSimpleName());
			else if (DEBUG || debug)
				plugin.sendMessage("&aModule &2%s&a associé au plugin &2%s&a avec &2succès&a.", name, plugin.getClass().getSimpleName());
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
			plugin.sendMessage("&cModule &4" + name + "&c désactivé.");
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

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public boolean isDebugEnabled() {
		return debug;
	}

	public static void sendErrorModule(OlympaModule<?, ?, ?, ?> module, Exception e, String whileDoing) {
		new Exception(String.format("§cError in Module§4 %s §cwhile do§4 %s§c.", module.getName(), whileDoing)).initCause(e).printStackTrace();
	}

}
