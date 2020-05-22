package fr.olympa.api.region.tracking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.olympa.api.region.Region;

public class TrackedRegion {

	private final Region region;
	private final String id;
	private final List<Flag> flags = new ArrayList<>();

	public TrackedRegion(Region region, String id, Flag... flags) {
		this.region = region;
		this.id = id;
		this.getFlags().addAll(Arrays.asList(flags));
	}

	public Region getRegion() {
		return region;
	}

	public String getID() {
		return id;
	}

	public List<Flag> getFlags() {
		return flags;
	}

	public <T extends Flag> T getFlag(Class<T> clazz) {
		for (Flag flag : flags) {
			if (flag.getClass() == clazz) return (T) flag;
		}
		return null;
	}

}