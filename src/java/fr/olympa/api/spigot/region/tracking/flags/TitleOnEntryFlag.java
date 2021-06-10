package fr.olympa.api.spigot.region.tracking.flags;

import org.bukkit.entity.Player;

import fr.olympa.api.spigot.region.tracking.ActionResult;
import fr.olympa.api.spigot.region.tracking.RegionManager;
import fr.olympa.api.spigot.region.tracking.RegionEvent.EntryEvent;
import fr.olympa.api.spigot.region.tracking.RegionEvent.ExitEvent;

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
	public ActionResult enters(EntryEvent event) {
		send(event.getPlayer());
		return super.enters(event);
	}
	
	@Override
	public ActionResult leaves(ExitEvent event) {
		event.getRegionsTo().stream().sorted(RegionManager.REGION_COMPARATOR).map(x -> x.getFlag(TitleOnEntryFlag.class)).filter(x -> x != null).reduce((x, y) -> y).ifPresent(x -> x.send(event.getPlayer()));
		return super.leaves(event);
	}
	
}
