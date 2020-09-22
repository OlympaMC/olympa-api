package fr.olympa.api.utils.machine;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.function.Consumer;

import fr.olympa.api.utils.Prefix;

public class OlympaRuntime {

	public static Thread action(String action, String serverName) {
		return action(action, serverName, null);
	}

	public static Thread action(String action, String serverName, Consumer<String> function) {
		return action("mc " + action + " " + serverName, function);
	}

	public static Thread action(String command) {
		return action(command, (Consumer<String>) null);
	}

	public static Thread action(String command, Consumer<String> function) {
		return new Thread(() -> {
			try {
				String out = Prefix.DEFAULT.toString();
				Process p = Runtime.getRuntime().exec(command);
				BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line;
				while ((line = br.readLine()) != null) {
					if (line.isEmpty())
						continue;
					out += line.replace("0;", "").replace("", "")
							.replace("[0m", "§f")
							.replace("[1m", "§l")
							.replace("[4m", "§n")
							.replace("[32m", "§3")
							.replace("[36m", "§b")
							.replace("[49m", "§f")
							.replace("[30m", "§0")
							.replace("[31m", "§4")
							.replace("[32m", "§2")
							.replace("[33m", "§6")
							.replace("[34m", "§1")
							.replace("[35m", "§5")
							.replace("[36m", "§3")
							.replace("[37m", "§7")
							.replace("[90m", "§8")
							.replace("[91m", "§c")
							.replace("[92m", "§a")
							.replace("[91m", "§e")
							.replace("[91m", "§9")
							.replace("[91m", "§d")
							.replace("[91m", "§b")
							.replace("[97m", "§f");
				}
				if (function != null)
					function.accept(out);
				br.close();
				p.waitFor();
				System.out.println(out);
			} catch (Exception e) {
				String out = Prefix.DEFAULT + "&4ERROR&c " + e.getMessage();
				if (function != null)
					function.accept(out);
				System.out.println(out);
				e.printStackTrace();
			}
		}, "Start command " + command);
	}
}
