package fr.olympa.api.spigot.config;

import java.util.Arrays;
import java.util.List;

public class ConfigYmlType<T> {

	T value;
	T defaultValue;
	List<T> possibleValue;

	@SafeVarargs
	public ConfigYmlType(T value, T defaultValue, T... possibleValue) {
		this.value = value;
		this.defaultValue = defaultValue;
		this.possibleValue = Arrays.asList(possibleValue);
	}
}
