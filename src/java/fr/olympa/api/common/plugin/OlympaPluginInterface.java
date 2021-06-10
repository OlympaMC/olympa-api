package fr.olympa.api.common.plugin;

import fr.olympa.api.common.task.OlympaTask;

public interface OlympaPluginInterface {

	String getPrefixConsole();

	OlympaTask getTask();

	void sendMessage(String message, Object... args);

}