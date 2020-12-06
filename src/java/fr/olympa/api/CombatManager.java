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
	
	private final OlympaAPIPlugin plugin;
	private final int combatTime, combatTimeMillis;
	
	private final Map<Player, CombatPlayer> inCombat = new HashMap<>();
	
	public CombatManager(OlympaAPIPlugin plugin, int combatTime) {
		this.plugin = plugin;
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
			getOrSetCombat(damaged).damageEvent = e;
			getOrSetCombat(damager);
		}
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		CombatPlayer combatPlayer = inCombat.get(p);
		if (combatPlayer != null) {
			inCombat.remove(p);
			plugin.sendMessage("Le joueur §6%s §es'est déconnecté en combat.", p.getName());
			Prefix.DEFAULT_BAD.sendMessage(p, "Tu t'es déconnecté en combat... Ton stuff est tombé par terre."); // le message ne sera sûrement pas reçu (je pense ?)
			p.setLastDamageCause(combatPlayer.damageEvent);
			p.setHealth(0);
		}
	}
	
	class CombatPlayer {
		private long lastDamage;
		private EntityDamageByEntityEvent damageEvent;
	}
	
}
