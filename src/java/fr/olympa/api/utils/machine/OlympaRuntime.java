package fr.olympa.api.utils.machine;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.function.Consumer;

import fr.olympa.api.LinkSpigotBungee;
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
					line = line.replace("", "")
							.replace("[0m", "§f").replace("[1m", "§l").replace("[4m", "§n").replace("[32m", "§3").replace("[36m", "§b").replace("[49m", "§f").replace("[30m", "§0").replace("[31m", "§4")
							.replace("[32m", "§2").replace("[33m", "§6").replace("[34m", "§1").replace("[35m", "§5").replace("[36m", "§3").replace("[37m", "§7").replace("[90m", "§8").replace("[91m", "§c")
							.replace("[92m", "§a").replace("[91m", "§e").replace("[91m", "§9").replace("[91m", "§d").replace("[91m", "§b").replace("[97m", "§f").replace("[1;32m", "§6")
							.replace("[1;33m", "§7").replace("[1;33", "§5").replace("[1;34m", "").replace("[36m", "").replace("[m", "").replace("0;", "");
					if (LinkSpigotBungee.Provider.link != null && line.startsWith(" " + LinkSpigotBungee.Provider.link.getServerName() + " s'est arrêté")) {
						Runtime.getRuntime().addShutdownHook(action("sh start.sh"));
						OlympaCore.getInstance().getServer().shutdown();
					}
					LinkSpigotBungee.Provider.link.sendMessage("&dREAD BASH > " + line);
					//					sb.append(line);
					if (functionForAllLines != null)
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
