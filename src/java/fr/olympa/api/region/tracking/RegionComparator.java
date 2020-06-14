package fr.olympa.api.region.tracking;

import java.util.Comparator;

public class RegionComparator implements Comparator<TrackedRegion> {

	public static final RegionComparator COMPARATOR = new RegionComparator();

	private RegionComparator() {}

	@Override
	public int compare(TrackedRegion o1, TrackedRegion o2) {
		return Integer.compare(o1.getPriority().getSlot(), o2.getPriority().getSlot());
	}

}
