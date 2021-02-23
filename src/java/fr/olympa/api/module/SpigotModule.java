package fr.olympa.api.module;

import java.util.function.Function;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.module.OlympaModule.ModuleApi;
import fr.olympa.api.plugin.OlympaAPIPlugin;
import fr.olympa.api.plugin.OlympaPluginInterface;
import fr.olympa.api.utils.CreateInstance;

public class SpigotModule<T extends ModuleApi<P>, L extends Listener, P extends OlympaAPIPlugin, C extends OlympaCommand> extends OlympaModule<T, L, P, C> {

	public SpigotModule(P plugin, String name, Function<P, T> functionInitialize) {
		super(plugin, name, functionInitialize);
	}

	@Override
	public void registerListeners() {
		try {
			Plugin plugin = getPlugin();
			if (getEventsToRegister() == null)
				return;
			if (!plugin.isEnabled())
				throw new IllegalPluginAccessException("Unable to register Listener while plugin is disable.");
			for (Class<? extends L> event : getEventsToRegister())
				if (getApi().getClass().isAssignableFrom(event)) {
					plugin.getServer().getPluginManager().registerEvents((Listener) getApi(), plugin);
					if (OlympaModule.DEBUG || isDebugEnabled())
						((OlympaPluginInterface) plugin).sendMessage("&eModule &6%s&e : listener &6%s&e register et liée à l'Api.", getName(), event.getSimpleName());
				} else {
					L listener = new CreateInstance<L>().of(event);
					getEventsRegistered().add(listener);
					plugin.getServer().getPluginManager().registerEvents(listener, plugin);
					if (OlympaModule.DEBUG || isDebugEnabled())
						((OlympaPluginInterface) plugin).sendMessage("&eModule &6%s&e : listener &6%s&e register.", getName(), event.getSimpleName());
				}
		} catch (Exception e) {
			this.sendErrorModule(e, "register Listener");
		}
	}

	@Override
	public void unregisterListeners() {
		try {
			Plugin plugin = getPlugin();
			if (getEventsToRegister() == null || !plugin.isEnabled())
				return;
			for (Listener listener : getEventsRegistered())
				HandlerList.unregisterAll(listener);
			getEventsRegistered().clear();
		} catch (Exception e) {
			this.sendErrorModule(e, "unregister Listener");
		}
	}
}