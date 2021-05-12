package fr.olympa.api.region.tracking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.event.EventPriority;

import fr.olympa.api.region.Region;
import fr.olympa.api.region.tracking.flags.Flag;
import fr.olympa.core.spigot.OlympaCore;

public class TrackedRegion {

	private Region region;
	private final String id;
	private final EventPriority priority;
	private final List<Flag> flags = new ArrayList<>();

	TrackedRegion(Region region, String id, EventPriority priority, Flag... flags) {
		this.region = region;
		this.id = id;
		this.priority = priority;
		this.flags.addAll(Arrays.asList(flags));
	}

	public Region getRegion() {
		return region;
	}
	
	public void updateRegion(Region region) {
		OlympaCore.getInstance().getRegionManager().updateRegion(this, this.region, region);
		this.region = region;
	}

	public String getID() {
		return id;
	}

	public EventPriority getPriority() {
		return priority;
	}

	public List<Flag> getFlags() {
		return flags;
	}

	public <T extends Flag> T getFlag(Class<T> clazz) {
		for (Flag flag : flags) {
			if (clazz.isAssignableFrom(flag.getClass())) return (T) flag;
		}
		return null;
	}

	public void registerFlags(Flag... flag) {
		flags.addAll(Arrays.asList(flag));
	}

	public void unregister() {
		OlympaCore.getInstance().getRegionManager().unregisterRegion(id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj instanceof TrackedRegion) {
			return ((TrackedRegion) obj).id.equals(this.id);
		}
		return false;
	}

}