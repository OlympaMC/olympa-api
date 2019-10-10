package fr.tristiisch.olympa.api.objects;

import org.bukkit.plugin.Plugin;

import fr.tristiisch.olympa.api.task.TaskManager;

public interface OlympaPlugin extends Plugin{
	
	public abstract TaskManager getTaskManager();
	
}
