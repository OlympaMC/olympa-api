package fr.olympa.api.spigot.region.tracking.flags;

import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class DamageFlag extends Flag {

	private boolean onlyPlayers;
	private List<DamageCause> disabledCauses;

	/**
	 * Créé un flag empêchant les dommages.
	 * @param onlyPlayers si activé, seuls les dégâts sur les joueurs seront annulés.
	 * @param disabledCauses dégâts écoutés. Si vide, tous les dégâts seront annulés.
	 */
	public DamageFlag(boolean onlyPlayers, DamageCause... disabledCauses) {
		this.onlyPlayers = onlyPlayers;
		if (disabledCauses.length != 0) this.disabledCauses = Arrays.asList(disabledCauses);
	}

	public void damageEvent(EntityDamageEvent event) {
		if (onlyPlayers && !(event.getEntity() instanceof Player)) return;
		event.setCancelled(disabledCauses == null || disabledCauses.contains(event.getCause()));
	}
	
	@Override
	public void appendDescription(StringJoiner joiner) {
		super.appendDescription(joiner);
		joiner.add("Only players: §a" + onlyPlayers);
		joiner.add("Causes: §a" + (disabledCauses == null ? "all" : disabledCauses));
	}

}
