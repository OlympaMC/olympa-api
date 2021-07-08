package fr.olympa.api.spigot.region.tracking;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.common.command.complex.Cmd;
import fr.olympa.api.common.command.complex.CommandContext;
import fr.olympa.api.common.permission.list.OlympaAPIPermissionsSpigot;
import fr.olympa.api.spigot.command.ComplexCommand;

public class BypassFluidsCommand extends ComplexCommand {
	
	private static Map<World, Long> bypassFluidUntil = new HashMap<>();
	
	public BypassFluidsCommand(Plugin plugin) {
		super(plugin, "bypassfluids", "Permet de désactiver l'anti-fluides temporairement.", OlympaAPIPermissionsSpigot.COMMAND_BYPASS_REGIONS);
		
		super.setAllowConsole(false);
	}
	
	@Cmd (args = { "WORLD", "INTEGER" })
	public void on(CommandContext cmd) {
		World world;
		long seconds;
		if (cmd.getArgumentsLength() == 0) {
			if (isConsole()) {
				sendIncorrectSyntax();
				return;
			}
			world = getPlayer().getWorld();
		}else world = cmd.getArgument(0);
		seconds = cmd.getArgument(1, 60);
		
		Long old = bypassFluidUntil.put(world, System.currentTimeMillis() + seconds * 1000);
		if (old == null)
			sendError("L'anti-fluides est désactivé pour %d secondes sur le monde %s.", seconds, world.getName());
		else
			sendSuccess("L'anti-fluides est re-désactivé pour %d secondes sur le monde %s.", seconds, world.getName());
	}
	
	@Cmd (args = "WORLD")
	public void off(CommandContext cmd) {
		World world;
		if (cmd.getArgumentsLength() == 0) {
			if (isConsole()) {
				sendIncorrectSyntax();
				return;
			}
			world = getPlayer().getWorld();
		}else world = cmd.getArgument(0);
		
		Long old = bypassFluidUntil.remove(world);
		if (old == null)
			sendError("Le monde %s n'avait pas l'anti-fluides de désactivé.", world.getName());
		else
			sendSuccess("L'anti-fluides est réactivé sur le monde %s.", world.getName());
	}
	
	public static boolean doBypassFluids(World world) {
		Long until = bypassFluidUntil.get(world);
		return until != null && System.currentTimeMillis() < until.longValue();
	}
	
}
