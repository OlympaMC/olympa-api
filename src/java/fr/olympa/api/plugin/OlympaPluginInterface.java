package fr.olympa.api.plugin;

import fr.olympa.api.task.OlympaTask;

public interface OlympaPluginInterface {

	String getPrefixConsole();

	OlympaTask getTask();

	void sendMessage(String message, Object... args);

}