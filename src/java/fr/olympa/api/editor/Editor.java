package fr.olympa.api.editor;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.olympa.api.gui.Inventories;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.spigot.OlympaCore;

public abstract class Editor implements Listener {

	private static Map<Player, Editor> players = new HashMap<>();

	private static void enter(Player p, Editor editor) {
		editor.begin();
		Bukkit.getPluginManager().registerEvents(editor, OlympaCore.getInstance());

		players.put(p, editor);
	}

	public static <T extends Editor> T enterOrLeave(Player p, T editor) {
		if (editor == null) {
			return null;
		}
		Editor edit = players.get(p);
		if (edit == null) {
			enter(p, editor);
		} else {
			Prefix.BAD.sendMessage(p, "Vous êtes déjà dans un éditeur.");
		}
		return editor;
	}

	public static boolean hasEditor(Player player) {
		return players.containsKey(player);
	}

	public static void leave(Player player) {
		if (!hasEditor(player)) {
			return;
		}
		Editor editor = players.remove(player);
		HandlerList.unregisterAll(editor);
		editor.end();
	}

	public static void leaveAll() {
		for (Player p : players.keySet()) {
			leave(p);
		}
		players.clear();
	}

	protected final Player p;

	protected Editor(Player p) {
		this.p = p;
	}

	protected void begin() {
		Inventories.closeWithoutExit(this.p);
	}

	private final void callChat(String rawText) {
		rawText = ChatColor.stripColor(rawText.trim().replaceAll("\\uFEFF", "")); // remove blank characters, remove space at the beginning, remove colors on the string
		if (!this.chat(ChatColor.translateAlternateColorCodes('&', rawText))) {
			Prefix.DEFAULT.sendMessage(this.p, "Vous êtes dans le mode éditeur.");
		}
	}

	/**
	 * Happens when the player in the editor type somthing in the chat
	 * @param message Message typed
	 * @return false if the plugin needs to send an help message to the player
	 */
	public boolean chat(String message) {
		return false;
	}

	public void end() {
	}

	public <T extends Editor> T enterOrLeave() {
		return (T) enterOrLeave(p, this);
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onChat(AsyncPlayerChatEvent e) {
		if (e.getPlayer() != this.p) {
			return;
		}
		e.setCancelled(true);
		if (e.isAsynchronous()) {
			Bukkit.getScheduler().runTask(OlympaCore.getInstance(), () -> callChat(e.getMessage()));
		} else {
			this.callChat(e.getMessage());
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		if (e.getPlayer() == this.p) {
			leave(this.p);
		}
	}
}
