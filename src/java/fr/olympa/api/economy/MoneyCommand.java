package fr.olympa.api.economy;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.command.complex.ComplexCommand;
import fr.olympa.api.permission.OlympaSpigotPermission;
import fr.olympa.api.provider.AccountProvider;

public class MoneyCommand<T extends MoneyPlayerInterface> extends ComplexCommand {

	private OlympaSpigotPermission getOtherMoneyPermission;

	public MoneyCommand(Plugin plugin, String cmd, String description, OlympaSpigotPermission globalPermission, OlympaSpigotPermission getOtherMoneyPermission, OlympaSpigotPermission manageMoneyPermission, String... alias) {
		super(plugin, cmd, description, globalPermission, alias);
		this.getOtherMoneyPermission = getOtherMoneyPermission;
		for (String internal : new String[] {"set", "give", "withdraw"}) {
			super.getCommand(internal).perm = manageMoneyPermission;
		}
	}

	@Override
	public boolean noArguments(CommandSender sender) {
		if (sender instanceof Player) {
			get(new CommandContext(this, new String[0], "money"));
			return true;
		}else return false;
	}

	@Cmd (args = "PLAYERS", syntax = "[joueur]", description = "Affiche la monnaie qu'a le joueur spécifié")
	public void get(CommandContext cmd) {
		if (cmd.getArgumentsLength() == 0) {
			if (player != null) {
				sendSuccess("Vous disposez de " + getGameMoney(getPlayer()).getFormatted());
			}else sendImpossibleWithConsole();
		}else if (getOtherMoneyPermission.hasSenderPermission(getSender())) {
			sendSuccess("Le joueur dispose de " + getGameMoney(cmd.getArgument(0)).getFormatted());
		}else sendDoNotHavePermission();
	}

	@Cmd (min = 2, args = { "PLAYERS", "DOUBLE" }, syntax = "<joueur> <quantité>", description = "Modifie la monnaie d'un joueur")
	public void set(CommandContext cmd) {
		OlympaMoney money = getGameMoney(cmd.getArgument(0));
		money.set(cmd.getArgument(1));
		sendSuccess("Le joueur dispose maintenant de " + money.getFormatted());
	}

	@Cmd (min = 2, args = { "PLAYERS", "DOUBLE" }, syntax = "<joueur> <quantité>", description = "Donne une quantité d'argent au joueur")
	public void give(CommandContext cmd) {
		OlympaMoney money = getGameMoney(cmd.getArgument(0));
		money.give(cmd.getArgument(1));
		sendSuccess("Le joueur dispose maintenant de " + money.getFormatted());
	}

	@Cmd (min = 2, args = { "PLAYERS", "DOUBLE" }, syntax = "<joueur> <quantité>", description = "Retire une quantité d'argent au joueur")
	public void withdraw(CommandContext cmd) {
		OlympaMoney money = getGameMoney(cmd.getArgument(0));
		money.withdraw(cmd.getArgument(1));
		sendSuccess("Le joueur dispose maintenant de " + money.getFormatted());
	}

	private OlympaMoney getGameMoney(Player p) {
		return AccountProvider.<T>get(p.getUniqueId()).getGameMoney();
	}

}
