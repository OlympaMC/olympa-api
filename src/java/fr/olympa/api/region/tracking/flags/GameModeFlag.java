package fr.olympa.api.region.tracking.flags;

import java.util.Set;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import fr.olympa.api.region.tracking.ActionResult;
import fr.olympa.api.region.tracking.RegionComparator;
import fr.olympa.api.region.tracking.TrackedRegion;

public class GameModeFlag extends Flag {

	private GameMode mode;

	public GameModeFlag(GameMode mode) {
		this.mode = mode;
	}

	public GameMode getMode() {
		return mode;
	}

	@Override
	public ActionResult enters(Player p, Set<TrackedRegion> to) {
		p.setGameMode(getMode());
		return super.enters(p, to);
	}

	@Override
	public ActionResult leaves(Player p, Set<TrackedRegion> to) {
		to.stream().sorted(RegionComparator.COMPARATOR).map(x -> x.getFlag(GameModeFlag.class)).filter(x -> x != null).reduce((x, y) -> y).ifPresent(x -> p.setGameMode(x.getMode()));
		return super.leaves(p, to);
	}

}
