package fr.olympa.api.plugin;

import fr.olympa.api.task.OlympaTask;
import fr.olympa.api.utils.Utils;

public interface OlympaPluginInterface {

	long upTime = Utils.getCurrentTimeInSeconds();

	String getPrefixConsole();

	OlympaTask getTask();

	default String getUptime() {
		return Utils.timestampToDuration(OlympaPluginInterface.upTime);
	}

	default long getUptimeLong() {
		return upTime;
	}

	void sendMessage(String message);

}