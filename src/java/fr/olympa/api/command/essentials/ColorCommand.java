package fr.olympa.api.command.essentials;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.chat.ColorUtils;
import fr.olympa.api.chat.TableGenerator;
import fr.olympa.api.chat.TableGenerator.Alignment;
import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.command.complex.ComplexCommand;
import fr.olympa.api.utils.Prefix;
import net.md_5.bungee.api.ChatColor;

public class ColorCommand extends ComplexCommand {

	public ColorCommand(Plugin plugin) {
		super(plugin, "color", "Permet de voir toutes les couleurs possibles.", null);
	}

	@Override
	public boolean noArguments(CommandSender sender) {
		TableGenerator tg = new TableGenerator(Alignment.LEFT, Alignment.LEFT);
		tg.addRow("&f §fBlanc§r", "&0 §0Noir§r");
		tg.addRow("&c §cRouge§r", "&4 §4Rouge foncé§r");
		tg.addRow("&9 §9Bleu§r", "&1 §1Bleu foncé§r");
		tg.addRow("&b §bCyan§r", "&3 §3Cyan foncé§r");
		tg.addRow("&a §aVert§r", "&2 §2Vert foncé§r");
		tg.addRow("&d §dViolet§r", "&5 §5Violet foncé§r");
		tg.addRow("&7 §7Gris§r", "&8 §8Gris foncé§r");
		tg.addRow("&e §eJaune§r", "&6 §6Or§r");
		tg.addRow("&k §kMagic§r", "&l  §lGras§r");
		tg.addRow("&m §mBarré§r", "&n §nSouligné§r");
		tg.addRow("&o §oItalique§r", "&r §rReset§r");
		sender.sendMessage(tg.toString(sender));
		return true;
	}

	static int picaSize(String s) {
		// the following characters are sorted by width in Arial font
		String lookup = " .:,;'^`!|jl/\\i-()JfIt[]?{}sr*a\"ce_gFzLxkP+0123456789<=>~qvy$SbduEphonTBCXY#VRKZN%GUAHD@OQ&wmMW";
		int result = 0;
		for (int i = 0; i < s.length(); ++i) {
			int c = lookup.indexOf(s.charAt(i));
			result += (c < 0 ? 60 : c) * 7 + 200;
		}
		return result;
	}

	@Cmd(min = 1, args = "HEX_COLOR", syntax = "<#hexCode>", otherArg = true)
	public void hex(CommandContext cmd) {
		String arg = cmd.getArgument(0);
		ChatColor color = ChatColor.of(arg);
		sendHoverAndSuggest(Prefix.NONE, color + "[" + color.getName() + "] Lorem ipsum dolor sit amet.", color + "Clique pour récupérer le code Hexa de " + color.toString().replace("§", "&"), "/color " + color.getName());
	}

	@Cmd()
	public void random(CommandContext cmd) {
		for (int i = 0; i < 10; i++) {
			ChatColor color = ColorUtils.randomColor();
			sendHoverAndSuggest(Prefix.NONE, color + "[" + color.getName() + "] Lorem ipsum dolor sit amet.", color + "Clique pour récupérer le code Hexa", "/color " + color.getName());
		}
	}

	@Cmd(min = 1)
	public void test(CommandContext cmd) {
		player.sendMessage(translateHexColorCodes(cmd.getFrom(0)));
	}

	public String translateHexColorCodes(String message) {
		final Pattern hexPattern = Pattern.compile("&#([A-Fa-f0-9]{6})");
		Matcher matcher = hexPattern.matcher(message);
		StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);
		while (matcher.find()) {
			String group = matcher.group(1);
			matcher.appendReplacement(buffer, ChatColor.COLOR_CHAR + "x"
					+ ChatColor.COLOR_CHAR + group.charAt(0) + ChatColor.COLOR_CHAR + group.charAt(1)
					+ ChatColor.COLOR_CHAR + group.charAt(2) + ChatColor.COLOR_CHAR + group.charAt(3)
					+ ChatColor.COLOR_CHAR + group.charAt(4) + ChatColor.COLOR_CHAR + group.charAt(5));
		}
		return matcher.appendTail(buffer).toString();
	}
}
