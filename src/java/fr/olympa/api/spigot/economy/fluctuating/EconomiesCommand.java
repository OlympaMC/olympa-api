package fr.olympa.api.spigot.economy.fluctuating;

import java.text.DecimalFormat;
import java.util.List;
import java.util.stream.Collectors;

import fr.olympa.api.common.command.Paginator;
import fr.olympa.api.common.command.complex.ArgumentParser;
import fr.olympa.api.common.command.complex.Cmd;
import fr.olympa.api.common.command.complex.CommandContext;
import fr.olympa.api.common.permission.OlympaSpigotPermission;
import fr.olympa.api.spigot.command.ComplexCommand;
import fr.olympa.api.utils.Utils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class EconomiesCommand extends ComplexCommand {
	
	private FluctuatingEconomiesManager manager;
	private DecimalFormat format = new DecimalFormat("0.###");
	
	private Paginator<FluctuatingEconomy> paginator = new Paginator<FluctuatingEconomy>(10, "Économies") {
		
		@Override
		protected List<FluctuatingEconomy> getObjects() {
			return manager.getEconomies();
		}
		
		@Override
		protected BaseComponent getObjectDescription(FluctuatingEconomy eco) {
			TextComponent compo = new TextComponent(TextComponent.fromLegacyText(
					"§8➤ §7" + eco.getId() +
					"§8, base : §7" + format.format(eco.getBase()) +
					"§8, min : §7" + format.format(eco.getMin()) +
					"§8, §lvaleur : §7§l" + format.format(eco.getValue()) +
					"§8, prochaine montée : §7" + (eco.getNextUpdate() == 0 ? "aucune" : Utils.durationToString(format, eco.getNextUpdate() - System.currentTimeMillis()))));
			compo.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new Text(eco.getClass().getName())));
			return compo;
		}
		
		@Override
		protected String getCommand(int page) {
			return "economies list " + page;
		}
		
	};
	
	public EconomiesCommand(FluctuatingEconomiesManager manager, OlympaSpigotPermission permission) {
		super(manager.getPlugin(), "economies", "Gère les économies.", permission);
		this.manager = manager;
		
		super.addArgumentParser("ECONOMY", new ArgumentParser<>((sender, arg) -> manager.getEconomies().stream().map(FluctuatingEconomy::getId).collect(Collectors.toList()), manager::getEconomy, x -> "Cette économie n'existe pas."));
	}
	
	@Cmd (args = "INTEGER", syntax = "[page]")
	public void list(CommandContext cmd) {
		sendComponents(paginator.getPage(cmd.getArgument(0, 1)));
	}
	
	@Cmd (args = { "ECONOMY", "DOUBLE" }, syntax = "<id> <valeur>")
	public void set(CommandContext cmd) {
		FluctuatingEconomy eco = cmd.getArgument(0);
		double value = cmd.getArgument(1);
		eco.setValue(value);
		sendSuccess("L'économie %s a été modifiée à une valeur de %s.", eco.getId(), format.format(value));
	}
	
	@Cmd (min = 1, args = "ECONOMY", syntax = "<id>")
	public void reset(CommandContext cmd) {
		FluctuatingEconomy eco = cmd.getArgument(0);
		eco.setValue(eco.getBase());
		sendSuccess("L'économie %s a été reset à sa valeur de %s.", eco.getId(), format.format(eco.getBase()));
	}
	
	@Cmd (min = 1, args = "ECONOMY", syntax = "<id>")
	public void get(CommandContext cmd) {
		FluctuatingEconomy eco = cmd.getArgument(0);
		sendSuccess("L'économie §l%s §a(%s) a une valeur de §l%s§a.", eco.getId(), eco.getClass().getName(), format.format(eco.getValue()));
	}
	
}
