package fr.olympa.api.region.tracking.flags;

import java.util.Set;

import org.bukkit.entity.Player;

import fr.olympa.api.region.tracking.ActionResult;
import fr.olympa.api.region.tracking.RegionManager;
import fr.olympa.api.region.tracking.TrackedRegion;

public class TitleOnEntryFlag extends Flag {
	
	private String title, subtitle;
	private int fadeIn, stay, fadeOut;
	
	public TitleOnEntryFlag(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
		this.title = title;
		this.subtitle = subtitle;
		this.fadeIn = fadeIn;
		this.stay = stay;
		this.fadeOut = fadeOut;
	}
	
	public void send(Player p) {
		if (title != null || subtitle != null) p.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
	}
	
	@Override
	public ActionResult enters(Player p, Set<TrackedRegion> to) {
		send(p);
		return super.enters(p, to);
	}
	
	@Override
	public ActionResult leaves(Player p, Set<TrackedRegion> to) {
		to.stream().sorted(RegionManager.REGION_COMPARATOR).map(x -> x.getFlag(TitleOnEntryFlag.class)).filter(x -> x != null).reduce((x, y) -> y).ifPresent(x -> x.send(p));
		return super.leaves(p, to);
	}
	
}
