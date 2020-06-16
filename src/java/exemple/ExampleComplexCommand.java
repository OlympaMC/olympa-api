package exemple;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.command.complex.ComplexCommand;

public class ExampleComplexCommand extends ComplexCommand {

	public ExampleComplexCommand(Plugin plugin) {
		super(plugin, "complex", "Exemple de commande complexe", ExemplePermissions.EXEMPLE_COMMAND);
	}

	@Override
	public boolean noArguments(CommandSender sender) {
		sendInfo("Vous n'avez donné aucun argument");
		return true;
	}

	@Cmd(player = true)
	public void gui(CommandContext cmd) {
		new ExempleGUI().create(getPlayer());
	}

	@Cmd (args = { "PLAYERS", "" }, min = 1, permissionName = "EXEMPLE_LOL")
	public void lol(CommandContext cmd) {
		Player p = cmd.getArgument(0);
		p.sendMessage(cmd.getArgument(1, "valeurDef"));
	}

	@Cmd(player = true, permissionName = "EXEMPLE_NYAN")
	public void nyan(CommandContext cmd) {
		getPlayer().setVelocity(getPlayer().getLocation().getDirection().multiply(3));
	}

}
