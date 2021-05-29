package fr.olympa.api.frame;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.CommandArgument;
import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.permission.list.OlympaAPIPermissionsSpigot;
import fr.olympa.core.spigot.OlympaCore;

public class ImageMapCommand extends OlympaCommand {
	public ImageMapCommand(Plugin plugin) {
		super(plugin, "imagemap", "Pour convertir une image en item frames.", OlympaAPIPermissionsSpigot.COMMAND_IMAGEMAP, "imap");
		minArg = 1;
		addCommandArguments(true, List.of(new CommandArgument("lienDl")));
		addCommandArguments(true, List.of(new CommandArgument("info"), new CommandArgument("download"), new CommandArgument("true"), new CommandArgument("false"), new CommandArgument("scale")));
	}

	/**
	 * Get all values of a String array which start with a given String
	 *
	 * @param value the given String
	 * @param list the array
	 * @return a List of all matches
	 */
	public static List<String> getMatches(String value, String[] list) {
		List<String> result = new LinkedList<>();

		for (String str : list)
			if (str.startsWith(value))
				result.add(str);

		return result;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		ImageFrameManager manager = ((OlympaCore) plugin).getImageFrameManager();

		if (args.length < 1)
			return false;

		String filename = args[0];
		for (int i = 0; i < filename.length(); i++)
			if (filename.charAt(i) == '/'
					|| filename.charAt(i) == '\\'
					|| filename.charAt(i) == ':') {
				sender.sendMessage("Sorry, this filename isn't allowed");
				return true;
			}

		if (args.length >= 2 && args[1].equalsIgnoreCase("reload")) {
			manager.reloadImage(args[0]);
			sender.sendMessage("Image " + args[0] + " reloaded!");
			return true;
		}

		if (args.length >= 2 && args[1].equals("info")) {
			BufferedImage image = manager.loadImage(args[0]);
			if (image == null) {
				sender.sendMessage("Error getting this image, please consult server logs");
				return true;
			}
			int tileWidth = (image.getWidth() + ImageFrameManager.MAP_WIDTH - 1) / ImageFrameManager.MAP_WIDTH;
			int tileHeight = (image.getHeight() + ImageFrameManager.MAP_HEIGHT - 1) / ImageFrameManager.MAP_HEIGHT;

			sender.sendMessage(String.format("This image is %d by %d tiles (%d by %d pixels).", tileWidth, tileHeight, image.getWidth(), image.getHeight()));
			return true;
		}

		if (args.length >= 2 && args[1].equals("download")) {
			if (sender.hasPermission("imagemaps.download"))
				manager.appendDownloadTask(new ImageDownloadTask(plugin, args[2], args[0], sender));
			else
				sender.sendMessage("You don't have download permission");
			return true;
		}

		if (!(sender instanceof Player)) {
			sender.sendMessage("You need to be a player to do that");
			return true;
		}

		BufferedImage image = manager.loadImage(args[0]);
		if (image == null) {
			sender.sendMessage("Error getting this image, please consult server logs");
			return true;
		}

		boolean fastsend = false;
		int tilesx = 0;
		int tilesy = 0;

		for (int i = 1; i < args.length; i++)
			if (args[i].equalsIgnoreCase("true"))
				fastsend = true;
			else if (args[i].equalsIgnoreCase("false"))
				fastsend = false;
			else if (args[i].equalsIgnoreCase("scale") && i + 2 < args.length) {
				try {
					tilesx = Integer.parseInt(args[i + 1]);
					tilesy = Integer.parseInt(args[i + 2]);
				} catch (NumberFormatException ex) {
					tilesx = tilesy = 0;
				}
				if (tilesx < 0 || tilesy < 0) {
					sender.sendMessage("Need to pass two integers to scale");
					return true;
				}
				i += 2;
			} else
				sender.sendMessage("ignoring unknown parameter " + args[i] + " (continuing)");

		double scalex = tilesx * 128.0 / image.getWidth();
		double scaley = tilesy * 128.0 / image.getHeight();
		double finalScale;

		if (scalex == 0 && scaley == 0)
			finalScale = 1.0;
		else if (scalex == 0)
			finalScale = scaley;
		else if (scaley == 0)
			finalScale = scalex;
		else
			finalScale = Math.min(scalex, scaley);

		manager.startPlacing((Player) sender, args[0], fastsend, finalScale);

		int width = (int) Math.ceil((double) image.getWidth() / (double) ImageFrameManager.MAP_WIDTH * finalScale - 0.0001);
		int height = (int) Math.ceil((double) image.getHeight() / (double) ImageFrameManager.MAP_WIDTH * finalScale - 0.0001);

		sender.sendMessage(String.format("Started placing of %s, which needs a %d by %d area.", args[0], width, height));
		sender.sendMessage("Rightclick on the block, that should be the upper left corner.");

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		ImageFrameManager manager = ((OlympaCore) plugin).getImageFrameManager();
		switch (args.length) {
		case 1:
			return getMatches(args[0], new File(manager.plugin.getDataFolder(), "images").list());
		case 2:
			return Arrays.asList("scale", "true", "false", "reload", "download", "info");
		case 3:
			if (args[2].equals("true") || args[2].equals("false"))
				return Arrays.asList("scale");
			break;
		case 5:
			if (args[2].equals("scale"))
				return Arrays.asList("true", "false");
			break;
		default:
			return Collections.emptyList();
		}

		return Collections.emptyList();
	}

}
