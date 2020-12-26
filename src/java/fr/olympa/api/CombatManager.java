package fr.olympa.api;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import fr.olympa.api.plugin.OlympaAPIPlugin;
import fr.olympa.api.utils.Prefix;

public class CombatManager implements Listener {
	
	private final OlympaAPIPlugin plugin;
	private final int combatTime, combatTimeMillis;
	
	private final Map<Player, CombatPlayer> inCombat = new HashMap<>();
	private final BukkitTask task;
	
	public CombatManager(OlympaAPIPlugin plugin, int combatTime) {
		this.plugin = plugin;
		this.combatTime = combatTime;
		this.combatTimeMillis = combatTime * 1000;
		plugin.sendMessage("Lancement du système de combat...");
		
		task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
			for (Iterator<Entry<Player, CombatPlayer>> iterator = inCombat.entrySet().iterator(); iterator.hasNext();) {
				Entry<Player, CombatPlayer> entry = iterator.next();
				long elapsed = System.currentTimeMillis() - entry.getValue().lastDamage;
				if (elapsed > combatTimeMillis) {
					Prefix.DEFAULT_GOOD.sendMessage(entry.getKey(), "Le mode combat est terminé. Tu peux maintenant te déconnecter sans risque.");
					iterator.remove();
				}
			}
		}, 20, 20);
	}
	
	public void unload() {
		HandlerList.unregisterAll(this);
		task.cancel();
		plugin.sendMessage("§cArrêt du mode combat (%d joueurs).", inCombat.size());
		for (Player player : inCombat.keySet()) {
			Prefix.DEFAULT_GOOD.sendMessage(player, "Le mode combat est terminé. Tu peux maintenant te déconnecter sans risque.");
		}
		inCombat.clear();
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
	
	public boolean canEnterCombat(Player damager, Player damaged) {
		return true;
	}
	
	@EventHandler (priority = EventPriority.MONITOR)
	public void onDamage(EntityDamageByEntityEvent e) {
		if (!e.isCancelled() && e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
			Player damaged = (Player) e.getEntity();
			Player damager = (Player) e.getDamager();
			if (canEnterCombat(damager, damaged)) {
				getOrSetCombat(damaged).damageEvent = e;
				getOrSetCombat(damager);
			}
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
			p.spigot().respawn();
		}
	}
	
	class CombatPlayer {
		private long lastDamage;
		private EntityDamageByEntityEvent damageEvent;
	}
	
}
