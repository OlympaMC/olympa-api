package fr.olympa.api.common.bash;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.function.Consumer;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.spigot.OlympaCore;
import net.md_5.bungee.api.ChatColor;

public class OlympaRuntime {

	// TODO replace .replace at ligne 60 by BashColor
	public enum BashColor {

		WHITE("[0m", ChatColor.WHITE),
		BOLD("[1m", ChatColor.BOLD),
		UNKNOWN("", null),

		;

		String bashColorCode;
		ChatColor minecraftColor;

		/**
		 * @param bashColorCode
		 * @param minecraftColor
		 */
		BashColor(String bashColorCode, ChatColor minecraftColor) {
			this.bashColorCode = bashColorCode;
			this.minecraftColor = minecraftColor;
		}
	}

	public static Thread action(String action, String serverName) {
		return action(action, serverName, null);
	}

	public static Thread action(String action, String serverName, Consumer<String> function) {
		return action("mc " + action + " " + serverName, function);
	}

	public static Thread action(String command) {
		return action(command, (Consumer<String>) null);
	}

	public static Thread action(String command, Consumer<String> functionForAllLines) {
		return new Thread(() -> {
			//			String out;
			try {
				//				StringBuilder sb = new StringBuilder();
				LinkSpigotBungee.Provider.link.sendMessage("&5EXEC COMMAND BASH < " + command);
				Process p = Runtime.getRuntime().exec(command);
				BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line;
				while ((line = br.readLine()) != null) {
					if (line.isEmpty())
						continue;
					line = line
							.replace("[0m", "§f").replace("[1m", "§l").replace("[4m", "§n").replace("[32m", "§3").replace("[36m", "§b").replace("[49m", "§f").replace("[30m", "§0").replace("[31m", "§4")
							.replace("[33m", "§6").replace("[34m", "§1").replace("[35m", "§5").replace("[37m", "§7").replace("[90m", "§8").replace("[91m", "§c")
							.replace("[92m", "§a").replace("[91m", "§e").replace("[91m", "§9").replace("[91m", "§d").replace("[91m", "§b").replace("[97m", "§f").replace("[1;32m", "§6")
							.replace("[1;33m", "§7").replace("[1;33", "§5").replace("[1;34m", "").replace("[1m", "").replace("[m", "").replace("0;", "").replaceAll("\\[\\d*(;\\d*)?m?", "").replace("", "");
					if (LinkSpigotBungee.Provider.link != null && line.replaceAll("§.", "").startsWith(" " + LinkSpigotBungee.Provider.link.getServerName() + " s'est arrêté")) {
						Runtime.getRuntime().addShutdownHook(action("sh start.sh"));
						if (LinkSpigotBungee.Provider.link.isSpigot())
							OlympaCore.getInstance().getServer().shutdown();
						else
							OlympaBungee.getInstance().getProxy().stop("restart in comming");
					}
					LinkSpigotBungee.Provider.link.sendMessage("&dREAD BASH > " + line);
					//					sb.append(line);
					if (functionForAllLines != null) // [1m
						functionForAllLines.accept(line);
				}
				//				out = sb.toString();
				br.close();
				p.waitFor();
			} catch (Exception e) {
				//				out = "&4ERROR&c " + e.getMessage();
				e.printStackTrace();
			}
		}, "Start command " + command);
	}
}
