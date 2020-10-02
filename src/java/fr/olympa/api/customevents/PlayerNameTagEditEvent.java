package fr.olympa.api.customevents;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.scoreboard.tab.Nametag;
import fr.olympa.core.spigot.OlympaCore;

public class PlayerNameTagEditEvent extends PlayerEvent implements Cancellable {

	public static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	private OlympaPlayer olympaPlayer;
	private Nametag nameTag;
	private Nametag previousNameTag;
	private boolean cancel;
	private boolean forceCreateTeam;
	private int sortPriority;
	private Set<Player> targets = new HashSet<>();

	public PlayerNameTagEditEvent(Player player, OlympaPlayer olympaPlayer, Nametag nameTag, Nametag previousNameTag) {
		super(player);
		this.olympaPlayer = olympaPlayer;
		sortPriority = olympaPlayer.getGroup().getIndex();
		if (previousNameTag == null)
			this.nameTag = new Nametag();
		else
			this.nameTag = nameTag;
		if (previousNameTag == null)
			this.previousNameTag = OlympaCore.getInstance().getNameTagApi().getNametag(player);
		else
			this.previousNameTag = previousNameTag;
		cancel = false;
		forceCreateTeam = false;
	}

	@Override
	public boolean isCancelled() {
		return cancel;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancel = cancel;
	}

	public OlympaPlayer getOlympaPlayer() {
		return olympaPlayer;
	}

	public Nametag getNameTag() {
		return nameTag;
	}

	public Nametag getPreviousNameTag() {
		return previousNameTag;
	}

	public boolean isForceCreateTeam() {
		return forceCreateTeam;
	}

	public void setForceCreateTeam(boolean forceCreateTeam) {
		this.forceCreateTeam = forceCreateTeam;
	}

	public int getSortPriority() {
		return sortPriority;
	}

	public Set<Player> getTargets() {
		return targets;
	}

	public void setTargets(Set<Player> targets) {
		this.targets = targets;
	}

	public void addTarget(Player target) {
		targets.add(target);
	}
}
