package exemple;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.command.complex.ComplexCommand;

public class ExampleComplexCommand extends ComplexCommand {

	public ExampleComplexCommand(Plugin plugin) {
		super(null, plugin, "complex", "Exemple de commande complexe", OlympaAPIPermission.EXEMPLE_COMMAND);
	}

	@Cmd(player = true)
	public void gui(CommandContext cmd) {
		new ExempleGUI().create(cmd.player);
	}

	@Cmd(args = { "PLAYERS", "" }, min = 2, permissionName = "EXEMPLE_LOL")
	public void lol(CommandContext cmd) {
		Player p = (Player) cmd.args[0];
		p.sendMessage((String) cmd.args[1]);
	}

	@Cmd(player = true, permissionName = "EXEMPLE_NYAN")
	public void nyan(CommandContext cmd) {
		cmd.player.setVelocity(cmd.player.getLocation().getDirection().multiply(3));
	}

}
