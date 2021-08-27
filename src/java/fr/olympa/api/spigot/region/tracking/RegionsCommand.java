package fr.olympa.api.spigot.region.tracking;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Shulker;
import org.bukkit.plugin.Plugin;

import com.google.common.collect.Sets;

import fr.olympa.api.common.chat.TxtComponentBuilder;
import fr.olympa.api.common.command.complex.Cmd;
import fr.olympa.api.common.command.complex.CommandContext;
import fr.olympa.api.common.permission.list.OlympaAPIPermissionsSpigot;
import fr.olympa.api.spigot.command.ComplexCommand;
import fr.olympa.api.spigot.region.Region;
import fr.olympa.api.spigot.region.tracking.flags.Flag;
import fr.olympa.api.spigot.utils.SpigotUtils;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.spigot.OlympaCore;

public class RegionsCommand extends ComplexCommand {
	
	public RegionsCommand(Plugin plugin) {
		super(plugin, "regions", "Permet de gérer les régions.", OlympaAPIPermissionsSpigot.COMMAND_REGIONS_MANAGE);
		
		super.addArgumentParser("REGION",
				(sender, arg) -> new ArrayList<>(OlympaCore.getInstance().getRegionManager().getTrackedRegions().keySet()),
				OlympaCore.getInstance().getRegionManager().getTrackedRegions()::get,
				x -> "Cette région n'existe pas !");
	}
	
	@Cmd (player = true)
	public void regions(CommandContext cmd) {
		RegionManager regionManager = OlympaCore.getInstance().getRegionManager();
		Collection<TrackedRegion> trackedRegions = regionManager.getTrackedRegions().values();
		
		sendInfo("Régions trackées : %d", trackedRegions.size());
		sendInfo("Total de points : %d", trackedRegions.stream().mapToInt(x -> x.getRegion().getLocations().size()).sum());
		
		Set<TrackedRegion> playerRegions = regionManager.getCachedPlayerRegions(getPlayer());
		if (playerRegions == null)
			playerRegions = Collections.emptySet();
		sendInfo("Vous êtes actuellement dans les régions : §l%s", playerRegions.stream().map(x -> x.getID()).collect(Collectors.joining(", ", "[", "]")));
		
		Set<TrackedRegion> applicable = trackedRegions.stream().filter(x -> x.getRegion().isIn(getPlayer())).collect(Collectors.toSet());
		sendInfo("Différences entre les régions en cache et les régions calculées : §l%s", Sets.symmetricDifference(playerRegions, applicable).stream().map(x -> x.getID()).collect(Collectors.joining(", ", "[", "]")));
	}
	
	@Cmd (min = 4, args = { "WORLD", "INTEGER", "INTEGER", "INTEGER" })
	public void testRegion(CommandContext cmd) {
		World world = cmd.getArgument(0);
		int x = cmd.getArgument(1);
		int y = cmd.getArgument(2);
		int z = cmd.getArgument(3);
		for (TrackedRegion trackedRegion : OlympaCore.getInstance().getRegionManager().getTrackedRegions().values()) if (trackedRegion.getRegion().isIn(world, x, y, z))
			sendInfo("Is in " + trackedRegion.getID());
	}
	
	@Cmd (min = 1, args = "REGION", syntax = "<region id>")
	public void displayRegion(CommandContext cmd) {
		TrackedRegion region = cmd.getArgument(0);
		for (Location location : region.getRegion().getLocations()) {
			Shulker shulker = location.getWorld().spawn(location, Shulker.class);
			shulker.setPersistent(false);
			shulker.setAI(false);
			shulker.setGravity(false);
			shulker.setInvulnerable(true);
			shulker.setSilent(true);
			shulker.setGlowing(true);
		}
		sendSuccess("La région %s a été affichée.", region.getID());
	}
	
	@Cmd (min = 1, args = { "REGION", "BOOLEAN" }, syntax = "<region id> [show points: true]")
	public void regionInfo(CommandContext cmd) {
		TrackedRegion trackedRegion = cmd.getArgument(0);
		Region region = trackedRegion.getRegion();
		sendSuccess("Région §e%s §a(%s)", trackedRegion.getID(), region.getWorld().getName());
		sendSuccess("Type: §e%s", region.getClass().getSimpleName());
		sendSuccess("Min: §e%s §a| Max: §e%s", SpigotUtils.convertLocationToHumanString(region.getMin()), SpigotUtils.convertLocationToHumanString(region.getMax()));
		if (cmd.getArgument(1, Boolean.FALSE)) {
			sendSuccess("%d points:", region.getLocations().size());
			for (Location location : region.getLocations()) sendMessage(Prefix.SYMBOLE, SpigotUtils.convertLocationToHumanString(location));
		}else {
			sendSuccess("%d points §7(ajouter §otrue§7 pour les voir)", region.getLocations().size());
		}
		sendSuccess("Priorité: §e%s", trackedRegion.getPriority().name());
		sendSuccess("%d flag(s):", trackedRegion.getFlags().size(), trackedRegion.getFlags().stream().map(flag -> flag.getType()).collect(Collectors.joining(", ", "[", "]")));
		TxtComponentBuilder builder = new TxtComponentBuilder().extraSpliter("\n");
		for (Flag flag : trackedRegion.getFlags()) {
			TxtComponentBuilder flagCompo = new TxtComponentBuilder("§7- §e" + flag.getType());
			StringJoiner description = new StringJoiner("\n§7", "§7", "");
			flag.appendDescription(description);
			flagCompo.onHoverText(description.toString());
			builder.extra(flagCompo);
		}
		sendComponents(builder.build());
	}
	

}
