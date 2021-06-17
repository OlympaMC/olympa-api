package fr.olympa.api.spigot.economy.fluctuating;

import java.util.stream.Collectors;

import fr.olympa.api.common.command.complex.ArgumentParser;
import fr.olympa.api.common.command.complex.Cmd;
import fr.olympa.api.common.command.complex.CommandContext;
import fr.olympa.api.common.permission.OlympaSpigotPermission;
import fr.olympa.api.spigot.command.ComplexCommand;

public class EconomiesCommand extends ComplexCommand {
	
	private FluctuatingEconomiesManager manager;
	
	public EconomiesCommand(FluctuatingEconomiesManager manager, OlympaSpigotPermission permission) {
		super(manager.getPlugin(), "economies", "Gère les économies.", permission);
		this.manager = manager;
		
		super.addArgumentParser("ECONOMY", new ArgumentParser<>((sender, arg) -> manager.getEconomies().values().stream().map(FluctuatingEconomy::getId).collect(Collectors.toList()), arg -> manager.getEconomies().get(arg), x -> "Cette économie n'existe pas."));
	}
	
	@Cmd (min = 1, args = "ECONOMY")
	public void reset(CommandContext cmd) {
		FluctuatingEconomy eco = cmd.getArgument(0);
		eco.setValue(eco.getBase());
		sendSuccess("L'économie %s a été reset à sa valeur de %f.", eco.getId(), eco.getBase());
	}
	
}
