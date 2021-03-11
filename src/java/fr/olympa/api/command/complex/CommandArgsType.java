package fr.olympa.api.command.complex;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

import fr.olympa.api.match.MatcherPattern;
import fr.olympa.api.utils.Utils;

public enum CommandArgsType {

	PARSER,
	PARSER_UNLIMETED,
	INFORMATIVE(s -> MatcherPattern.of(" ").contains(s, 2)),
	STRING_UPPER(s -> Utils.isAllUpperCase(s)),
	STRING(s -> Utils.isAllLowerCase(s));

	Function<String, Boolean> detectType;

	public void setDetectType(Function<String, Boolean> detectType) {
		this.detectType = detectType;
	}

	/**
	 * @param detectType
	 */
	CommandArgsType(Function<String, Boolean> detectType) {
		this.detectType = detectType;
	}

	CommandArgsType() {}

	public static CommandArgsType extractType(String s) {
		CommandArgsType type;
		boolean isUnlimited = s.endsWith("...");
		type = Arrays.stream(CommandArgsType.values()).filter(at -> at.detectType != null && at.detectType.apply(isUnlimited ? s.substring(0, s.length() - 3) : s)).findFirst().orElse(null);
		if (type == PARSER && isUnlimited)
			type = PARSER_UNLIMETED;
		return type;
	}

	public static IArgument extractEntry(String s, Map<String, ? extends Parser<?>> parsers) {
		IArgument object;
		boolean isUnlimited = s.endsWith("...");
		object = parsers.get(isUnlimited ? s.substring(0, s.length() - 3) : s);
		if (object != null)
			if (isUnlimited)
				object.setType(PARSER_UNLIMETED);
			else
				object.setType(PARSER);
		else {
			CommandArgsType type = Arrays.stream(CommandArgsType.values()).filter(at -> at.detectType != null && at.detectType.apply(s)).findFirst().orElse(null);
			if (type == null)
				return null;
			object = new ParserString(s, type);
		}
		return object;
	}
}
