package fr.olympa.api.lines;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

public class FixedLine<T extends LinesHolder<T>> extends AbstractLine<T> implements ConfigurationSerializable {

	@SuppressWarnings ("rawtypes")
	public static final FixedLine EMPTY_LINE = new FixedLine<>("");

	private String value;

	public FixedLine(String value) {
		this.value = value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getValue(T holder) {
		return value;
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<>();
		map.put("value", value);
		return map;
	}

	public static FixedLine<?> deserialize(Map<String, Object> map) {
		return new FixedLine<>((String) map.get("value"));
	}

}
