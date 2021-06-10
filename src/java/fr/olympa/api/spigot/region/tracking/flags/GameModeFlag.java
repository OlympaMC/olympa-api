package fr.olympa.api.spigot.region.tracking.flags;

import org.bukkit.GameMode;

import fr.olympa.api.spigot.region.tracking.ActionResult;
import fr.olympa.api.spigot.region.tracking.RegionManager;
import fr.olympa.api.spigot.region.tracking.RegionEvent.EntryEvent;
import fr.olympa.api.spigot.region.tracking.RegionEvent.ExitEvent;

public class GameModeFlag extends Flag {

	private GameMode mode;

	public GameModeFlag(GameMode mode) {
		this.mode = mode;
	}

	public GameMode getMode() {
		return mode;
	}

	@Override
	public ActionResult enters(EntryEvent event) {
		event.getPlayer().setGameMode(getMode());
		return super.enters(event);
	}

	@Override
	public ActionResult leaves(ExitEvent event) {
		event.getRegionsTo().stream().sorted(RegionManager.REGION_COMPARATOR).map(x -> x.getFlag(GameModeFlag.class)).filter(x -> x != null).reduce((x, y) -> y).ifPresent(x -> event.getPlayer().setGameMode(x.getMode()));
		return super.leaves(event);
	}

}
