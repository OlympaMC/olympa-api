package fr.olympa.api.command.essentials;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.api.utils.spigot.SpigotUtils;

public class KitCommand<T extends OlympaPlayer> extends OlympaCommand {
	
	private static final NumberFormat numberFormat = new DecimalFormat("00");
	
	private List<Kit<T>> kits;

	public KitCommand(Plugin plugin, Kit<T>... kitsArray) {
		super(plugin, "kit", "Permet d'obtenir un kit.", (OlympaPermission) null, "kits");
		super.minArg = 1;
		super.allowConsole = false;
		kits = new ArrayList<>(Arrays.asList(kitsArray));
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Optional<Kit<T>> okit = kits.stream().filter(kit -> kit.name.equalsIgnoreCase(args[0])).findFirst();
		if (okit.isEmpty()) {
			sendError("Le kit %s n'existe pas !", args[0]);
		}else {
			Kit<T> kit = okit.get();
			if (kit.permission == null || kit.permission.hasPermission(super.<OlympaPlayer>getOlympaPlayer())) {
				long timeToWait = (kit.getLastTake.apply(getOlympaPlayer()) + kit.timeBetween) - System.currentTimeMillis();
				if (timeToWait > 0) {
					sendError("Tu dois encore attendre %s avant de pouvoir reprendre le kit %s !", Utils.durationToString(numberFormat, timeToWait), kit.name);
				}else {
					kit.give(getOlympaPlayer(), getPlayer());
				}
			}else {
				sendError("Tu n'as pas la permission de prendre le kit %s...", kit.name);
			}
		}
		return false;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return kits.stream().map(Kit::getName).collect(Collectors.toList());
	}
	
	public static class Kit<T extends OlympaPlayer> {
		private final String name;
		private final OlympaPermission permission;
		private final long timeBetween;
		private final Function<T, Long> getLastTake;
		private final BiConsumer<T, Long> setLastTask;
		private final BiFunction<T, Player, ItemStack[]> items;
		
		public Kit(String name, OlympaPermission permission, long timeBetween, Function<T, Long> getLastTake, BiConsumer<T, Long> setLastTask, BiFunction<T, Player, ItemStack[]> items) {
			this.name = name;
			this.permission = permission;
			this.timeBetween = timeBetween;
			this.getLastTake = getLastTake;
			this.setLastTask = setLastTask;
			this.items = items;
		}
		
		public String getName() {
			return name;
		}
		
		public void give(T olympaPlayer, Player p) {
			SpigotUtils.giveItems(p, items.apply(olympaPlayer, p));
			setLastTask.accept(olympaPlayer, System.currentTimeMillis());
			Prefix.DEFAULT_GOOD.sendMessage(p, "Tu as reçu le kit %s !", name);
		}
		
	}
	
}
