package fr.olympa.api.spigot.command.essentials;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.common.permission.OlympaPermission;
import fr.olympa.api.common.permission.OlympaSpigotPermission;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.api.spigot.command.OlympaCommand;
import fr.olympa.api.spigot.utils.SpigotUtils;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;

public class KitCommand<T extends OlympaPlayer> extends OlympaCommand {

	private static final NumberFormat numberFormat = new DecimalFormat("00");
	private final List<IKit<T>> kits;

	public KitCommand(Plugin plugin, List<IKit<T>> kits) {
		super(plugin, "kit", "Permet d'obtenir un kit.", (OlympaSpigotPermission) null, "kits");
		super.allowConsole = false;
		this.kits = kits;
		super.usageString = "<nom du kit>";
		super.minArg = 0;
	}
	
	public KitCommand(Plugin plugin, IKit<T>... kits) {
		this(plugin, Arrays.asList(kits));
	}

	protected void noArgument() {
		sendUsage("kit");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		IKit<T> kit;
		if (args.length == 0) {
			if (kits.size() == 1) {
				kit = kits.get(0);
			}else {
				sendSuccess("§eKits disponibles: §a", kits.stream().map(IKit::getId).collect(Collectors.joining(", ")));
				return false;
			}
		}
		kit = kits.stream().filter(x -> x.getId().equalsIgnoreCase(args[0])).findAny().orElse(null);
		if (kit == null)
			sendError("Le kit %s n'existe pas !", args[0]);
		else {
			T olympaPlayer = getOlympaPlayer();
			if (kit.canTake(olympaPlayer)) {
				long timeToWait = kit.getLastTake(olympaPlayer) + kit.getTimeBetween() - System.currentTimeMillis();
				if (timeToWait > 0)
					sendError("Tu dois encore attendre %s avant de pouvoir reprendre le kit %s !", Utils.durationToString(numberFormat, timeToWait), kit.getId());
				else {
					kit.give(olympaPlayer, getPlayer());
					kit.setLastTake(olympaPlayer, System.currentTimeMillis());
				}
			} else
				kit.sendImpossibleToTake(olympaPlayer);
		}
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		T player = sender instanceof Player ? AccountProviderAPI.getter().get(((Player) sender).getUniqueId()) : null;
		return kits.stream().filter(kit -> player == null || kit.canTake(player)).map(IKit::getId).collect(Collectors.toList());
	}

	public interface IKit<T extends OlympaPlayer> {

		String getId();

		boolean canTake(T player);

		void sendImpossibleToTake(T player);

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
		public boolean canTake(T player) {
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
		public void sendImpossibleToTake(T player) {
			Prefix.DEFAULT_BAD.sendMessage((Player) player.getPlayer(), "Tu n'as pas la permission de prendre le kit %s...", id);
		}

		@Override
		public void give(T olympaPlayer, Player p) {
			SpigotUtils.giveItems(p, items.apply(olympaPlayer, p));
			Prefix.DEFAULT_GOOD.sendMessage(p, "Tu as reçu le kit %s !", id);
		}

	}

}
