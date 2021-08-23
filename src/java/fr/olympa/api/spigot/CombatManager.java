package fr.olympa.api.spigot;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import fr.olympa.api.common.plugin.OlympaAPIPlugin;
import fr.olympa.api.utils.Prefix;

public class CombatManager implements Listener {
	
	private final OlympaAPIPlugin plugin;
	private final int combatTime, combatTimeMillis;
	
	private final Map<Player, CombatPlayer> inCombat = new HashMap<>();
	private BukkitTask task;
	private boolean sendMessages = true;
	
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
					if (sendMessages) sendExitCombatMessage(entry.getKey());
					iterator.remove();
				}
			}
		}, 20, 20);
	}
	
	public void setSendMessages(boolean sendMessages) {
		this.sendMessages = sendMessages;
	}
	
	public boolean isInCombat(Player p) {
		return inCombat.containsKey(p);
	}
	
	public void unload() {
		if (task == null) return;
		HandlerList.unregisterAll(this);
		task.cancel();
		task = null;
		plugin.sendMessage("§cArrêt du mode combat (%d joueurs).", inCombat.size());
		if (sendMessages) inCombat.keySet().forEach(this::sendExitCombatMessage);
		inCombat.clear();
	}
	
	public CombatPlayer getOrSetCombat(Player p) {
		CombatPlayer combatPlayer = inCombat.get(p);
		if (combatPlayer == null) {
			combatPlayer = new CombatPlayer();
			inCombat.put(p, combatPlayer);
			if (sendMessages) Prefix.DEFAULT_BAD.sendMessage(p, "Tu entres en combat ! Ne tente pas de te déconnecter avant %d secondes !", combatTime);
		}
		combatPlayer.lastDamage = System.currentTimeMillis();
		return combatPlayer;
	}
	
	protected void sendExitCombatMessage(Player player) {
		Prefix.DEFAULT_GOOD.sendMessage(player, "Le mode combat est terminé. Tu peux maintenant te déconnecter sans risque.");
	}
	
	public boolean canEnterCombat(Player damager, Player damaged) {
		return true;
	}
	
	@EventHandler (priority = EventPriority.MONITOR)
	public void onDamage(EntityDamageByEntityEvent e) {
		Player damager = null;
		if (e.getDamager() instanceof Player) {
			damager = (Player) e.getDamager();
		}else if (e.getDamager() instanceof Projectile) {
			Projectile proj = (Projectile) e.getDamager();
			if (proj.getShooter() instanceof Player) damager = (Player) proj.getShooter();
		}
		if (!e.isCancelled() && e.getEntity() instanceof Player && damager != null) {
			Player damaged = (Player) e.getEntity();
			if (damaged == damager) return;
			if (canEnterCombat(damager, damaged)) {
				getOrSetCombat(damaged).damageEvent = e;
				getOrSetCombat(damager);
			}
		}
	}
	
	@EventHandler
	public void onDeath(PlayerDeathEvent e) {
		Player player = e.getEntity();
		CombatPlayer removed = inCombat.remove(player);
		if (removed != null && sendMessages) sendExitCombatMessage(player);
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
