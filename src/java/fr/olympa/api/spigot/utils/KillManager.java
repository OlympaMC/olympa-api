package fr.olympa.api.spigot.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import fr.olympa.api.common.chat.TxtComponentBuilder;
import fr.olympa.api.common.command.Paginator;
import fr.olympa.api.common.command.complex.ArgumentParser;
import fr.olympa.api.common.command.complex.Cmd;
import fr.olympa.api.common.command.complex.CommandContext;
import fr.olympa.api.common.module.OlympaModule.ModuleApi;
import fr.olympa.api.common.module.SpigotModule;
import fr.olympa.api.common.permission.list.OlympaAPIPermissionsSpigot;
import fr.olympa.api.common.plugin.OlympaAPIPlugin;
import fr.olympa.api.spigot.command.ComplexCommand;
import fr.olympa.api.spigot.command.OlympaCommand;
import fr.olympa.api.spigot.gui.OlympaGUI;
import fr.olympa.api.utils.Prefix;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class KillManager extends ComplexCommand implements ModuleApi<OlympaAPIPlugin>, Listener {
	
	private static DateFormat dateFormat = new SimpleDateFormat("HH:mm");
	
	private OlympaAPIPlugin plugin;
	
	private int lastID = 0;
	private Map<Integer, KillEntry> kills = new TreeMap<>(Collections.reverseOrder());
	private Cache<Integer, KillEntry> killsCache = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).removalListener(notif -> kills.remove(notif.getKey())).build();
	
	private Paginator<KillEntry> paginator;
	
	public KillManager(OlympaAPIPlugin plugin) throws Exception {
		super(plugin, "kills", "Affiche une liste des derniers kill", OlympaAPIPermissionsSpigot.KILL_SEE_COMMAND);
		SpigotModule<KillManager, Listener, OlympaAPIPlugin, OlympaCommand> module = new SpigotModule<>(plugin, "killmanager", x -> this);
		module.listener(getClass());
		module.cmd(getClass());
		module.enableModule();
		module.registerModule();
		
		paginator = new Paginator<KillEntry>(10, "Kills") {
			
			@Override
			protected List<KillEntry> getObjects() {
				return new ArrayList<>(kills.values());
			}
			
			@Override
			protected BaseComponent getObjectDescription(KillEntry object) {
				return TxtComponentBuilder.of(
						Prefix.NONE,
						object.getBasicInfos(),
						ClickEvent.Action.RUN_COMMAND,
						"/kills inventory " + object.id,
						HoverEvent.Action.SHOW_TEXT,
						new Text("§7Clique pour afficher les drops."));
			}
			
			@Override
			protected String getCommand(int page) {
				return "/kills list " + page;
			}
		};
		
		addArgumentParser("KILL", new ArgumentParser<>(
				(sender, arg) -> kills.keySet().stream().map(String::valueOf).toList(),
				arg -> kills.get(Integer.parseInt(arg)),
				x -> "Aucun kill n'est register avec cet ID."));
	}
	
	@Override
	public boolean disable(OlympaAPIPlugin plugin) {
		if (this.plugin != null) {
			this.plugin = null;
		}
		return true;
	}
	
	@Override
	public boolean enable(OlympaAPIPlugin plugin) {
		if (this.plugin == null) {
			this.plugin = plugin;
		}
		return true;
	}
	
	@Override
	public boolean setToPlugin(OlympaAPIPlugin plugin) {
		return true;
	}
	
	@Override
	public boolean isEnabled() {
		return plugin != null;
	}
	
	@Override
	public boolean noArguments(CommandSender sender) {
		sendComponents(paginator.getPage(1));
		return true;
	}
	
	@Cmd (args = "INTEGER")
	public void list(CommandContext cmd) {
		sendComponents(paginator.getPage(cmd.getArgument(0, 1)));
	}
	
	@Cmd (player = true, args = "KILL", min = 1)
	public void inventory(CommandContext cmd) {
		new KillDropsGUI(cmd.getArgument(0)).create(getPlayer());
	}
	
	@EventHandler (priority = EventPriority.MONITOR)
	public void onPlayerDeath(PlayerDeathEvent e) {
		if (e.isCancelled()) return;
		int id = lastID++;
		KillEntry entry = new KillEntry(id, e.getEntity().getLastDamageCause(), e.getEntity().getLocation(), Arrays.asList(e.getEntity().getInventory().getContents()), new Date());
		kills.put(id, entry);
		killsCache.put(id, entry);
	}
	
	record KillEntry(int id, EntityDamageEvent cause, Location location, List<ItemStack> inv, Date date) {
		
		public String getDead() {
			return cause.getEntity().getName();
		}
		
		public Entity getDamager() {
			return cause instanceof EntityDamageByEntityEvent event ? event.getDamager() : null;
		}
		
		public String getBasicInfos() {
			Entity damager = getDamager();
			return getDead()
					+ " par " + (damager == null ? "x" : damager.getName())
					+ " à " + dateFormat.format(date)
					+ " en " + SpigotUtils.convertLocationToHumanString(location);
		}
		
	}
	
	class KillDropsGUI extends OlympaGUI {
		
		public KillDropsGUI(KillEntry entry) {
			super("Drops de " + entry.getDead(), Math.max(1, (int) (Math.ceil(entry.inv.size() / 9D) * 9)));
			for (int i = 0; i < entry.inv.size(); i++) {
				inv.setItem(i, entry.inv.get(i));
			}
		}
		
	}
	
}
