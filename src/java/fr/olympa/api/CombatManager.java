package fr.olympa.api;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.olympa.api.plugin.OlympaAPIPlugin;
import fr.olympa.api.utils.Prefix;

public class CombatManager implements Listener {
	
	private Map<Player, CombatPlayer> inCombat = new HashMap<>();
	private final int combatTime, combatTimeMillis;
	
	public CombatManager(OlympaAPIPlugin plugin, int combatTime) {
		this.combatTime = combatTime;
		this.combatTimeMillis = combatTime * 1000;
		plugin.sendMessage("Lancement du système de combat...");
		
		plugin.getTask().scheduleSyncRepeatingTask(() -> {
			for (Iterator<Entry<Player, CombatPlayer>> iterator = inCombat.entrySet().iterator(); iterator.hasNext();) {
				Entry<Player, CombatPlayer> entry = iterator.next();
				long elapsed = System.currentTimeMillis() - entry.getValue().lastDamage;
				if (elapsed > combatTimeMillis) {
					Prefix.DEFAULT_GOOD.sendMessage(entry.getKey(), "Le mode combat est terminé. Tu peux maintenant te déconnecter sans risque.");
					iterator.remove();
				}
			}
		}, 1, 1, TimeUnit.SECONDS);
	}
	
	public CombatPlayer getOrSetCombat(Player p) {
		CombatPlayer combatPlayer = inCombat.get(p);
		if (combatPlayer == null) {
			combatPlayer = new CombatPlayer();
			inCombat.put(p, combatPlayer);
			Prefix.DEFAULT_BAD.sendMessage(p, "Tu entres en combat ! Ne tente pas de te déconnecter avant %d secondes !", combatTime);
		}
		combatPlayer.lastDamage = System.currentTimeMillis();
		return combatPlayer;
	}
	
	@EventHandler
	public void onDamage(EntityDamageByEntityEvent e) {
		if (e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
			Player damaged = (Player) e.getEntity();
			Player damager = (Player) e.getDamager();
			getOrSetCombat(damaged).damager = damager;
			getOrSetCombat(damager);
		}
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		CombatPlayer combatPlayer = inCombat.get(e.getPlayer());
		if (combatPlayer != null) {
			Prefix.DEFAULT_BAD.sendMessage(e.getPlayer(), "Tu t'es déconnecté en combat... Ton stuff est tombé par terre.");
			e.getPlayer().damage(1000000, combatPlayer.damager);
		}
	}
	
	class CombatPlayer {
		private long lastDamage;
		private Player damager;
	}
	
}
