package fr.olympa.api.editor.parsers;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import fr.olympa.api.utils.Prefix;

public class EnumParser<T extends Enum<T>> implements TextParser<T> {

	private Map<String, T> names;
	private String namesString;

	public EnumParser(Class<T> enumClass) {
		try {
			T[] values = (T[]) enumClass.getDeclaredMethod("values").invoke(null);
			names = new HashMap<>(values.length + 1, 1);
			for (T value : values) {
				names.put(proceed(value.name()), value);
			}
			namesString = String.join(", ", names.keySet());
		}catch (ReflectiveOperationException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public T parse(Player p, String msg) {
		T obj = names.get(proceed(msg));
		if (obj == null) Prefix.DEFAULT_BAD.sendMessage(p, "L'élément spécifié est introuvable parmi " + namesString + ".");
		return obj;
	}

	public String getNames() {
		return namesString;
	}

	private String proceed(String key) {
		return Normalizer.normalize(key.toLowerCase().replaceAll(" |_", ""), Form.NFD);
	}

}
