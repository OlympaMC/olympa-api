package fr.olympa.api.command.essentials;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.permission.OlympaSpigotPermission;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.api.utils.spigot.SpigotUtils;

public class KitCommand<T extends OlympaPlayer> extends OlympaCommand {
	
	private static final NumberFormat numberFormat = new DecimalFormat("00");
	private final Supplier<Stream<IKit<T>>> kitsStreamSupplier;

	public KitCommand(Plugin plugin, Supplier<Stream<IKit<T>>> kitsStreamSupplier) {
		super(plugin, "kit", "Permet d'obtenir un kit.", (OlympaSpigotPermission) null, "kits");
		super.minArg = 1;
		super.allowConsole = false;
		this.kitsStreamSupplier = kitsStreamSupplier;
	}
	
	public KitCommand(Plugin plugin, IKit<T>... kits) {
		this(plugin, () -> Arrays.stream(kits));
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Optional<IKit<T>> okit = kitsStreamSupplier.get().filter(kit -> kit.getId().equalsIgnoreCase(args[0])).findFirst();
		if (okit.isEmpty()) {
			sendError("Le kit %s n'existe pas !", args[0]);
		}else {
			IKit<T> kit = okit.get();
			T olympaPlayer = getOlympaPlayer();
			if (kit.canTake(olympaPlayer)) {
				long timeToWait = (kit.getLastTake(olympaPlayer) + kit.getTimeBetween()) - System.currentTimeMillis();
				if (timeToWait > 0) {
					sendError("Tu dois encore attendre %s avant de pouvoir reprendre le kit %s !", Utils.durationToString(numberFormat, timeToWait), kit.getId());
				}else {
					kit.give(olympaPlayer, getPlayer());
					kit.setLastTake(olympaPlayer, System.currentTimeMillis());
				}
			}else {
				sendError("Tu n'as pas la permission de prendre le kit %s...", kit.getId());
			}
		}
		return false;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return kitsStreamSupplier.get().map(IKit::getId).collect(Collectors.toList());
	}
	
	public interface IKit<T extends OlympaPlayer> {
		
		String getId();
		
		boolean canTake(OlympaPlayer player);
		
		long getTimeBetween();
		
		long getLastTake(T player);
		
		void setLastTake(T player, long time);
		
		void give(T olympaPlayer, Player p);
		
	}
	
	public static class SimpleKit<T extends OlympaPlayer> implements IKit<T> {
		private final String id;
		private final OlympaPermission permission;
		private final long timeBetween;
		private final Function<T, Long> getLastTake;
		private final BiConsumer<T, Long> setLastTask;
		private final BiFunction<T, Player, ItemStack[]> items;
		
		public SimpleKit(String id, OlympaPermission permission, long timeBetween, Function<T, Long> getLastTake, BiConsumer<T, Long> setLastTask, BiFunction<T, Player, ItemStack[]> items) {
			this.id = id;
			this.permission = permission;
			this.timeBetween = timeBetween;
			this.getLastTake = getLastTake;
			this.setLastTask = setLastTask;
			this.items = items;
		}
		
		@Override
		public String getId() {
			return id;
		}
		
		@Override
		public boolean canTake(OlympaPlayer player) {
			return permission == null || permission.hasPermission(player);
		}
		
		@Override
		public long getTimeBetween() {
			return timeBetween;
		}
		
		@Override
		public long getLastTake(T player) {
			return getLastTake.apply(player);
		}
		
		@Override
		public void setLastTake(T player, long time) {
			setLastTask.accept(player, time);
		}

		@Override
		public void give(T olympaPlayer, Player p) {
			SpigotUtils.giveItems(p, items.apply(olympaPlayer, p));
			Prefix.DEFAULT_GOOD.sendMessage(p, "Tu as reçu le kit %s !", id);
		}
		
	}
	
}
