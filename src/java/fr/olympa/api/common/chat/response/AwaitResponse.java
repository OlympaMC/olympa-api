package fr.olympa.api.common.chat.response;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import fr.olympa.api.common.chat.TxtComponentBuilder;
import fr.olympa.api.common.chat.sender.ISender;
import net.md_5.bungee.api.chat.TextComponent;

public class AwaitResponse<T> {

	static final String PREFIX = "$";
	private static final String SEPARATOR = "\n";
	private static final String EXIT_COMMAND = PREFIX + "leave";

	static final Map<ISender, AwaitResponse<?>> map = new HashMap<>();

	ISender sender;
	Function<String, T> function;
	Consumer<T> consumer;

	String prefix;
	String suffix;
	String title;
	String description;

	List<ClickChoice> choices;

	public AwaitResponse(ISender sender, String prefix, String suffix, String title, String description, List<ClickChoice> choices) {
		this.sender = sender;
		this.prefix = prefix;
		this.suffix = suffix;
		this.title = title;
		this.description = description;
		this.choices = choices;
	}

	public AwaitResponse(ISender sender, String prefix, String suffix, String title, String description, ClickChoice... choices) {
		this.sender = sender;
		this.prefix = prefix;
		this.suffix = suffix;
		this.title = title;
		this.description = description;
		if (choices != null && choices.length != 0)
			choices(choices);
	}

	public AwaitResponse<T> choices(ClickChoice... choices) {
		this.choices = Arrays.stream(choices).collect(Collectors.toList());
		return this;
	}

	public AwaitResponse<T> function(Function<String, T> function) {
		this.function = function;
		return this;
	}

	public AwaitResponse<T> consumer(Consumer<T> consumer) {
		this.consumer = consumer;
		return this;
	}

	public T get(String message) {
		if (message.startsWith(PREFIX))
			message = message.substring(PREFIX.length());
		return function.apply(message);
	}

	public Boolean applyIfExist(String message) {
		if (EXIT_COMMAND.equalsIgnoreCase(message))
			// EXIT
			return null;
		T t = get(message);
		if (t != null)
			if (consumer != null) {
				consumer.accept(t);
				return true;
			} else
				throw new NullPointerException("consumer is null");
		return false;
	}

	public TextComponent build() {
		if (suffix != null) {
			if (prefix != null) {
				prefix = prefix + SEPARATOR;
				suffix = SEPARATOR + suffix;
			}
		} else if (prefix != null)
			prefix = prefix + " ";
		TxtComponentBuilder builder = new TxtComponentBuilder(prefix + title);
		if (description != null)
			builder.extra(SEPARATOR + description);
		if (choices != null)
			choices.forEach(c -> builder.extra(c.build()));
		if (suffix != null)
			builder.extra(suffix);
		return builder.build();
	}
}
