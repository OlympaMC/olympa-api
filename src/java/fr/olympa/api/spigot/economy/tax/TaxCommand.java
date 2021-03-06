package fr.olympa.api.spigot.economy.tax;

import java.sql.SQLException;

import org.bukkit.plugin.Plugin;

import fr.olympa.api.common.command.complex.Cmd;
import fr.olympa.api.common.command.complex.CommandContext;
import fr.olympa.api.common.permission.OlympaSpigotPermission;
import fr.olympa.api.spigot.command.ComplexCommand;

public class TaxCommand extends ComplexCommand {

	private TaxManager tax;

	public TaxCommand(Plugin plugin, OlympaSpigotPermission permission, TaxManager tax) {
		super(plugin, "tax", "Permet de gérer la taxe du serveur.", permission, "taxe");
		this.tax = tax;
	}

	@Cmd
	public void informations(CommandContext cmd) {
		sendInfo("Taxe actuelle : §6§l%s", tax.getTax());
		sendInfo("Monnaie taxée au total : §6%f", tax.getTotalTaxedMoney());
	}

	@Cmd (min = 1, args = "DOUBLE", syntax = "<taxe>")
	public void set(CommandContext cmd) {
		try {
			tax.setTax(cmd.getArgument(0), true);
			sendSuccess("La taxe est désormais à %s", tax.getTax());
		}catch (SQLException e) {
			e.printStackTrace();
			sendError("Une erreur est survenue lors de la mise à jour de la taxe.");
		}
	}

}
