package fr.olympa.api.module;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.IOlympaCommand;
import fr.olympa.api.plugin.OlympaPluginInterface;

public class PluginModule {

	protected static final List<OlympaModule<? extends Object, Listener, ? extends OlympaPluginInterface, ? extends IOlympaCommand>> modules = new ArrayList<>();

	public class OlympaClassLoader extends ClassLoader {

		public Class<?> load(Class<?> clazz) throws IOException {
			File file = new File(clazz.getProtectionDomain().getCodeSource().getLocation().getPath());
			InputStream is = OlympaClassLoader.class.getResourceAsStream(file.getAbsolutePath());
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int b = -1;
			while ((b = is.read()) > -1)
				baos.write(b);
			return super.defineClass(file.getName(), baos.toByteArray(), 0, baos.size());
		}
	}

	public static void unregisterListener(OlympaModule<? extends Object, Listener, ? extends Plugin, ? extends IOlympaCommand> module) {
		try {
			Plugin plugin = module.getPlugin();
			if (module.getEventToRegister() == null || !plugin.isEnabled())
				return;
			for (Class<? extends Listener> event : module.getEventToRegister()) {
				Constructor<?>[] constructors = event.getConstructors();
				if (constructors.length == 0)
					throw new ClassNotFoundException("Unable to get the first public constructor, can't create the class.");
				Listener cl = (Listener) constructors[0].newInstance();
				HandlerList.unregisterAll(cl);
			}
		} catch (Exception e) {
			module.sendErrorModule(e, "unregister Listener");
		}
	}

	public static void registerListener(OlympaModule<? extends Object, Listener, ? extends Plugin, ? extends IOlympaCommand> module) {
		try {
			Plugin plugin = module.getPlugin();
			if (module.getEventToRegister() == null)
				return;
			if (!plugin.isEnabled())
				throw new IllegalPluginAccessException("Unable to register Listener while plugin is disable.");
			for (Class<? extends Listener> event : module.getEventToRegister())
				if (module.getApi().getClass().isAssignableFrom(event))
					plugin.getServer().getPluginManager().registerEvents((Listener) module.getApi(), plugin);
				else {
					Constructor<?>[] constructors = event.getConstructors();
					if (constructors.length == 0)
						throw new ClassNotFoundException("Unable to get the first public constructor, can't create the class.");
					Listener cl = (Listener) constructors[0].newInstance();
					plugin.getServer().getPluginManager().registerEvents(cl, plugin);
				}
		} catch (Exception e) {
			module.sendErrorModule(e, "register Listener");
		}
	}

	public static void addModule(OlympaModule<? extends Object, Listener, ? extends OlympaPluginInterface, ? extends IOlympaCommand> module) {
		modules.add(module);
	}

	public static void removeModule(OlympaModule<? extends Object, Listener, ? extends OlympaPluginInterface, ? extends IOlympaCommand> module) {
		modules.remove(module);
	}

	public static void enableModule(OlympaModule<? extends Object, Listener, ? extends Plugin, ? extends IOlympaCommand> module) {
		module.initialize();
		module.enable();
		registerListener(module);
		module.setToPlugin();
	}

	public static void disableModule(OlympaModule<? extends Object, Listener, ? extends Plugin, ? extends IOlympaCommand> module) {
		unregisterListener(module);
		module.disable();
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