package fr.olympa.api.utils;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerLocalData {

	public static void delete(Player player) {
		String worldName = Bukkit.getWorlds().get(0).getName();
		String playerUuid = player.getUniqueId().toString();
		String path = Bukkit.getServer().getWorldContainer().getPath() + "/" + worldName;
		new File(path + "/" + worldName + "/playerdata/" + playerUuid + ".dat").delete();
		new File(path + "/" + worldName + "/advancements/" + playerUuid + ".json").delete();
		File stats = new File(path + "/" + worldName + "/stats/" + playerUuid + ".json");
		if (stats.exists()) {
			stats.delete();
		}
	}
}
