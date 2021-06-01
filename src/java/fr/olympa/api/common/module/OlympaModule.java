package fr.olympa.api.common.module;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import fr.olympa.api.common.command.IOlympaCommand;
import fr.olympa.api.common.module.OlympaModule.ModuleApi;
import fr.olympa.api.common.plugin.OlympaPluginInterface;
import fr.olympa.api.utils.CreateInstance;

public abstract class OlympaModule<T extends ModuleApi<P>, L, P extends OlympaPluginInterface, C extends IOlympaCommand> {

	protected static final List<OlympaModule<? extends ModuleApi<?>, ?, ? extends OlympaPluginInterface, ? extends IOlympaCommand>> modules = new ArrayList<>();
	public static boolean DEBUG = false;

	public static List<String> getModulesNames() {
		return modules.stream().map(OlympaModule::getName).collect(Collectors.toList());
	}

	public static List<OlympaModule<? extends ModuleApi<?>, ?, ? extends OlympaPluginInterface, ? extends IOlympaCommand>> getModules() {
		return modules;
	}

	public static OlympaModule<? extends ModuleApi<?>, ?, ? extends OlympaPluginInterface, ? extends IOlympaCommand> getModule(String x) {
		return modules.stream().filter(m -> m.getName().equalsIgnoreCase(x)).findFirst().orElse(null);
	}

	public static void enableAll() {
		for (OlympaModule<? extends ModuleApi<?>, ?, ? extends OlympaPluginInterface, ? extends IOlympaCommand> module : modules)
			try {
				module.enableModule();
			} catch (Exception e) {} // Exception already printed
	}

	public static void disableAll() {
		for (OlympaModule<? extends ModuleApi<?>, ?, ? extends OlympaPluginInterface, ? extends IOlympaCommand> module : modules)
			try {
				module.disableModule();
			} catch (Exception e) {} // Exception already printed
	}

	public static void sendErrorModule(OlympaModule<?, ?, ?, ?> module, Exception e, String whileDoing) {
		new Exception(String.format("§cError in Module§4 %s §cwhile do§4 %s§c.", module.getName(), whileDoing)).initCause(e).printStackTrace();
	}

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
	Class<? extends L>[] eventsToRegister;
	List<L> eventsRegistered = new ArrayList<>();
	Class<? extends C>[] commandsToRegister;
	List<C> commandsRegistered = new ArrayList<>();
	boolean commandsPreProcessRegister;
	Function<P, T> functionInitialize;
	OlympaModule<? extends ModuleApi<?>, ?, ?, ?>[] dependances;
	OlympaModule<? extends ModuleApi<?>, ?, ?, ?>[] softDependances;

	/**
	 * @param plugin
	 * @param name
	 * @param api
	 * @param functionInitialize
	 * @param eventToRegister
	 */
	protected OlympaModule(P plugin, String name, Function<P, T> functionInitialize) {
		this.plugin = plugin;
		this.name = name;
		this.functionInitialize = functionInitialize;
		commandsPreProcessRegister = false;
	}

	@SafeVarargs
	public final OlympaModule<T, L, P, C> listener(Class<? extends L>... listeners) {
		this.eventsToRegister = listeners;
		return this;
	}

	@SafeVarargs
	public final OlympaModule<T, L, P, C> cmd(Class<? extends C>... commands) {
		this.commandsToRegister = commands;
		return this;
	}

	@SafeVarargs
	public final OlympaModule<T, L, P, C> softDepend(OlympaModule<? extends ModuleApi<?>, ?, ?, ?>... softDependModule) {
		this.softDependances = softDependModule;
		return this;
	}

	@SafeVarargs
	public final OlympaModule<T, L, P, C> depend(OlympaModule<? extends ModuleApi<?>, ?, ?, ?>... dependModule) {
		this.dependances = dependModule;
		return this;
	}

	public final OlympaModule<T, L, P, C> commandPreProcess() {
		this.commandsPreProcessRegister = true;
		return this;
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
		if (commandsToRegister == null || commandsToRegister.length == 0)
			return false;
		for (Class<? extends C> clazz : commandsToRegister) {
			C olympaCommand;
			if (api.getClass().isAssignableFrom(clazz)) {
				((IOlympaCommand) api).register();
				if (commandsPreProcessRegister)
					((IOlympaCommand) api).registerPreProcess();
				if (DEBUG || debug)
					plugin.sendMessage("&eModule &6%s&e : command &6%s&e register et liée à l'Api.", name, clazz.getSimpleName());
			} else {
				olympaCommand = new CreateInstance<C>().of(clazz, plugin, api);
				if (olympaCommand == null) {
					plugin.sendMessage("&cModule &4%s&c : can't register &4%s&c command, constructor was not found.", name, clazz.getSimpleName());
					return false;
				}
				if (commandsPreProcessRegister)
					olympaCommand.registerPreProcess();
				olympaCommand.register();
				commandsRegistered.add(olympaCommand);
				if (DEBUG || debug)
					plugin.sendMessage("&eModule &6%s&e : command &6%s&e register.", name, clazz.getSimpleName());
			}
		}
		return true;
	}

	public boolean unregisterCommands() {
		if (commandsRegistered.isEmpty())
			return false;
		commandsRegistered.forEach(olympaCommand -> {
			olympaCommand.unregister();
		});
		commandsRegistered.clear();
		return true;
	}

	//	public void registerListeners();
	//
	//	public void unregisterListeners();

	public Class<? extends L>[] getEventsToRegister() {
		return eventsToRegister;
	}

	public Class<? extends C>[] getCommandsToRegister() {
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

	private boolean enable() {
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

	private boolean disable() {
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

	public abstract void registerListeners();

	public abstract void unregisterListeners();

	public void enableModule() throws Exception {
		try {
			this.initialize();
			this.enable();
			this.registerListeners();
			this.setToPlugin();
			this.registerCommands();
		} catch (Exception e) {
			this.sendErrorModule(e, "enable Module");
			throw e;
		}
	}

	public void disableModule() throws Exception {
		try {
			this.unregisterListeners();
			this.unregisterCommands();
			this.disable();
		} catch (Exception e) {
			this.sendErrorModule(e, "disable Module");
			throw e;
		}
	}

	public OlympaModule<T, L, P, C> registerModule() {
		modules.add(this);
		return this;
	}

	public OlympaModule<T, L, P, C> removeFromRegisterList() {
		modules.remove(this);
		return this;
	}

	public void sendErrorModule(Exception e, String whileDoing) {
		sendErrorModule(this, e, whileDoing);
	}

}
