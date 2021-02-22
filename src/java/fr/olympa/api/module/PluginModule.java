package fr.olympa.api.module;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.IOlympaCommand;
import fr.olympa.api.plugin.OlympaPluginInterface;
import fr.olympa.api.utils.CreateInstance;

public class PluginModule {

	protected static final List<OlympaModule<? extends Object, Listener, ? extends OlympaPluginInterface, ? extends IOlympaCommand>> modules = new ArrayList<>();

	//	public class OlympaClassLoader extends ClassLoader {
	//		public Class<?> load(Class<?> clazz) throws IOException {
	//			File file = new File(clazz.getProtectionDomain().getCodeSource().getLocation().getPath());
	//			InputStream is = OlympaClassLoader.class.getResourceAsStream(file.getAbsolutePath());
	//			ByteArrayOutputStream baos = new ByteArrayOutputStream();
	//			int b = -1;
	//			while ((b = is.read()) > -1)
	//				baos.write(b);
	//			return super.defineClass(file.getName(), baos.toByteArray(), 0, baos.size());
	//		}
	//	}

	public static void registerListeners(OlympaModule<? extends Object, Listener, ? extends Plugin, ? extends IOlympaCommand> module) {
		try {
			Plugin plugin = module.getPlugin();
			if (module.getEventsToRegister() == null)
				return;
			if (!plugin.isEnabled())
				throw new IllegalPluginAccessException("Unable to register Listener while plugin is disable.");
			for (Class<? extends Listener> event : module.getEventsToRegister())
				if (module.getApi().getClass().isAssignableFrom(event)) {
					plugin.getServer().getPluginManager().registerEvents((Listener) module.getApi(), plugin);
					if (OlympaModule.DEBUG)
						((OlympaPluginInterface) plugin).sendMessage("&eModule &6%s&e : listener &6%s&e register et liée à l'Api.", module.getName(), event.getSimpleName());
				} else {
					Listener listener = new CreateInstance<Listener>().of(event);
					module.getEventsRegistered().add(listener);
					plugin.getServer().getPluginManager().registerEvents(listener, plugin);
					if (OlympaModule.DEBUG)
						((OlympaPluginInterface) plugin).sendMessage("&eModule &6%s&e : listener &6%s&e register.", module.getName(), event.getSimpleName());
				}
		} catch (Exception e) {
			module.sendErrorModule(e, "register Listener");
		}
	}

	public static void unregisterListeners(OlympaModule<? extends Object, Listener, ? extends Plugin, ? extends IOlympaCommand> module) {
		try {
			Plugin plugin = module.getPlugin();
			if (module.getEventsToRegister() == null || !plugin.isEnabled())
				return;
			for (Listener listener : module.getEventsRegistered())
				HandlerList.unregisterAll(listener);
			module.getEventsRegistered().clear();
		} catch (Exception e) {
			module.sendErrorModule(e, "unregister Listener");
		}
	}

	public static void addModule(OlympaModule<? extends Object, Listener, ? extends OlympaPluginInterface, ? extends IOlympaCommand> module) {
		modules.add(module);
	}

	public static void removeModule(OlympaModule<? extends Object, Listener, ? extends OlympaPluginInterface, ? extends IOlympaCommand> module) {
		modules.remove(module);
	}

	public static void enableModule(OlympaModule<? extends Object, Listener, ? extends Plugin, ? extends IOlympaCommand> module) {
		try {
			module.initialize();
			module.enable();
			registerListeners(module);
			module.setToPlugin();
			module.registerCommands();
		} catch (Exception e) {
			module.sendErrorModule(e, "enable Module");
		}
	}

	public static void disableModule(OlympaModule<? extends Object, Listener, ? extends Plugin, ? extends IOlympaCommand> module) {
		try {
			unregisterListeners(module);
			module.unregisterCommands();
			module.disable();
		} catch (Exception e) {
			module.sendErrorModule(e, "disable Module");
		}
	}

	public static List<String> getModulesNames() {
		return modules.stream().map(OlympaModule::getName).collect(Collectors.toList());
	}

	public static List<OlympaModule<? extends Object, Listener, ? extends OlympaPluginInterface, ? extends IOlympaCommand>> getModules() {
		return modules;
	}

	public static OlympaModule<? extends Object, Listener, ? extends OlympaPluginInterface, ? extends IOlympaCommand> getModule(String x) {
		return modules.stream().filter(m -> m.getName().equalsIgnoreCase(x)).findFirst().orElse(null);
	}

}