package fr.olympa.api.scoreboard.sign;

import java.util.Arrays;
import java.util.function.Function;

import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.utils.Utils;

public class DynamicLine<T extends OlympaPlayer> extends ScoreboardLine<T> {

	private Function<T, String> value;

	public DynamicLine(Function<T, String> value) {
		this(value, 0, 0);
	}

	public DynamicLine(Function<T, String> value, int refresh, int length) {
		super(refresh, length);
		this.value = value;
	}

	@Override
	public String getValue(T player) {
		int i = player.getGroup().getId();
		do {
			player.setGroup((OlympaGroup) Utils.getRandom(Arrays.asList(OlympaGroup.values())));
		} while (player.getGroup().getId() == i);
		return value.apply(player);
	}

}
