package fr.olympa.api.common.utils;

import java.util.function.Consumer;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import org.apache.commons.lang3.exception.ExceptionUtils;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.common.match.RegexMatcher;

public class ErrorLoggerHandler extends Handler {

	private Consumer<String> sendError;

	public ErrorLoggerHandler(Consumer<String> sendError) {
		this.sendError = sendError;
	}

	public String replaceInfo(String trace) {
		return trace.replaceAll("ask #?\\d+", "ask XXX").replaceAll(RegexMatcher.IP.getRegex(), "xxx.xxx.xxx.xxx");
	}

	@Override
	public void publish(LogRecord record) {
		if (record.getThrown() != null)
			try {
				String stackTrace = ExceptionUtils.getStackTrace(record.getThrown());
				sendError.accept(record.getLevel().getName() + " [" + record.getLoggerName() + "] " + replaceInfo(record.getMessage()) + "\n" + stackTrace); // remove "Task XXXX"
			} catch (Exception ex) {
				LinkSpigotBungee.getInstance().sendMessage("§cUne erreur est survenue durant le passage d'une erreur au bungee via redis: ", ExceptionUtils.getMessage(ex));
			}
	}

	@Override
	public void flush() {}

	@Override
	public void close() throws SecurityException {}

}
