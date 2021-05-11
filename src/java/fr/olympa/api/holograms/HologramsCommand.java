package fr.olympa.api.holograms;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.olympa.api.chat.ColorUtils;
import fr.olympa.api.command.Paginator;
import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.command.complex.ComplexCommand;
import fr.olympa.api.holograms.Hologram.HologramLine;
import fr.olympa.api.holograms.HologramsManager.HoloActionType;
import fr.olympa.api.lines.AbstractLine;
import fr.olympa.api.lines.CyclingLine;
import fr.olympa.api.lines.FixedLine;
import fr.olympa.api.permission.OlympaAPIPermissions;
import fr.olympa.api.plugin.OlympaAPIPlugin;
import fr.olympa.api.utils.spigot.SpigotUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public class HologramsCommand extends ComplexCommand {
	
	private HologramsManager holograms;
	private Paginator<Hologram> paginator;

	public HologramsCommand(OlympaAPIPlugin plugin, HologramsManager hologramsManager) {
		super(plugin, "holograms", "Permet de gérer les hologrammes", OlympaAPIPermissions.COMMAND_HOLOGRAMS_MANAGE, "holo");
		
		this.holograms = hologramsManager;
		super.addArgumentParser("PERSHOLOGRAM", (sender, arg) -> hologramsManager.holograms.values().stream()
				.filter(holo -> holograms.hasAccessTo(sender, holo, HoloActionType.COMMAND))
				.map(hologram -> Integer.toString(hologram.getID())).collect(Collectors.toList()), (arg) -> {
			try {
				Hologram hologram = hologramsManager.getHologram(Integer.parseInt(arg));
				if (holograms.hasAccessTo(sender, hologram, HoloActionType.COMMAND))
					return hologram;
			} catch (NumberFormatException ex) {
			}
			return null;
		}, x -> String.format("L'hologramme avec l'ID %s n'existe pas.", x));

		paginator = new Paginator<>(5, "Liste des hologrammes") {

			@Override
			protected List<Hologram> getObjects() {
				return hologramsManager.holograms.values().stream().filter(holo -> holograms.hasAccessTo(sender, holo, HoloActionType.COMMAND)).collect(Collectors.toList());
			}

			@Override
			protected BaseComponent getObjectDescription(Hologram object) {
				return new TextComponent("§b§l" + object.getID() + "§3 - §b" + object.toString() + "§3 - §b" + SpigotUtils.convertLocationToHumanString(object.getBottom()));
			}

			@Override
			protected String getCommand(int page) {
				return "/holograms list " + page;
			}
		};
	}

	@Cmd(player = true, min = 1, syntax = "<lignes séparées par des |>")
	public void create(CommandContext cmd) {
		if (!holograms.hasAccessTo(getSender(), null, HoloActionType.CREATE_PREPROCESS))
			return;
		
		String[] linesStrings = cmd.getFrom(0).split("\\|");
		int id = holograms.createHologram(player.getLocation().add(0, 1, 0), holograms.hasTempHoloCreationMode(), true,
				holograms.hasTempHoloCreationMode(), Arrays.stream(linesStrings).map(this::createLine).toArray(AbstractLine[]::new)).getID();

		if (!holograms.hasAccessTo(getSender(), holograms.getHologram(id), HoloActionType.CREATED))
			return;
		
		sendSuccess("Hologramme %d créé.", id);
	}

	@Cmd(args = "INTEGER", syntax = "[page]", description = "Affiche la liste des hologrammes persistants")
	public void list(CommandContext cmd) {
		sendComponents(paginator.getPage(cmd.getArgument(0, 1)));
	}

	@Cmd(player = true, min = 1, args = "PERSHOLOGRAM", syntax = "<id de l'hologramme>")
	public void remove(CommandContext cmd) {
		Hologram hologram = cmd.getArgument(0);
		
		if (!holograms.hasAccessTo(getSender(), hologram, HoloActionType.REMOVE))
			return;
		
		if (holograms.deleteHologram(hologram))
			sendSuccess("L'hologramme %d vient d'être supprimé.", hologram.getID());
		else
			sendError("L'hologramme %d n'a pas pu être supprimé.", hologram.getID());
	}

	@Cmd(player = true, min = 1, args = "PERSHOLOGRAM", syntax = "<id de l'hologramme>")
	public void move(CommandContext cmd) {
		Hologram hologram = cmd.getArgument(0);
		
		if (!holograms.hasAccessTo(getSender(), hologram, HoloActionType.MOVE))
			return;
		
		hologram.move(player.getLocation().add(0, 1, 0));
		sendSuccess("L'hologramme a été déplacé à votre position.");
	}

	@Cmd(player = true, min = 1, args = "PERSHOLOGRAM", syntax = "<id de l'hologramme>")
	public void teleport(CommandContext cmd) {
		Hologram hologram = cmd.getArgument(0);

		if (!holograms.hasAccessTo(getSender(), hologram, HoloActionType.TELEPORT))
			return;
		
		player.teleport(hologram.getBottom());
		sendSuccess("Vous vous êtes téléporté à l'hologramme.");
	}

	@Cmd(player = true, min = 1, args = "PERSHOLOGRAM", syntax = "<id de l'hologramme> <ligne>")
	public void addLine(CommandContext cmd) {
		Hologram hologram = cmd.getArgument(0);

		if (!holograms.hasAccessTo(getSender(), hologram, HoloActionType.EDIT_ADDLINE))
			return;
		
		hologram.addLine(cmd.getArgumentsLength() == 1 ? FixedLine.EMPTY_LINE : createLine(cmd.getFrom(1)));
		sendSuccess("La ligne a bien été ajoutée à l'hologramme.");
	}

	@Cmd(player = true, min = 2, args = { "PERSHOLOGRAM", "INTEGER" }, syntax = "<id de l'hologramme> <id de la ligne> [ligne]")
	public void editLine(CommandContext cmd) {
		Hologram hologram = cmd.getArgument(0);

		if (!holograms.hasAccessTo(getSender(), hologram, HoloActionType.EDIT_OTHER))
			return;
		
		if ((int) cmd.getArgument(1) >= hologram.getLines().size())
			sendIncorrectSyntax("L'indice maximum de l'hologramme " + hologram.getID() + " est de " + (hologram.getLines().size() - 1) + ".");
		else {
			hologram.setLine(cmd.getArgumentsLength() == 2 ? FixedLine.EMPTY_LINE : createLine(cmd.getFrom(2)), cmd.getArgument(1));
			sendSuccess("La ligne a bien été modifiée.");	
		}
	}

	@Cmd(player = true, min = 2, args = { "PERSHOLOGRAM", "INTEGER" }, syntax = "<id de l'hologramme> <id de la ligne> [ligne]")
	public void insertLine(CommandContext cmd) {
		Hologram hologram = cmd.getArgument(0);

		if (!holograms.hasAccessTo(getSender(), hologram, HoloActionType.EDIT_ADDLINE))
			return;
		
		hologram.insertLine(cmd.getArgumentsLength() == 2 ? FixedLine.EMPTY_LINE : createLine(cmd.getFrom(2)), cmd.getArgument(1));
		sendSuccess("La ligne a bien été insérée.");
	}

	@Cmd(player = true, min = 2, args = { "PERSHOLOGRAM", "INTEGER" }, syntax = "<id de l'hologramme> <id de la ligne>")
	public void removeLine(CommandContext cmd) {
		Hologram hologram = cmd.getArgument(0);

		if (!holograms.hasAccessTo(getSender(), hologram, HoloActionType.EDIT_OTHER))
			return;
		
		try {
			hologram.removeLine(cmd.<Integer>getArgument(1));
			sendSuccess("La ligne a bien été supprimée de l'hologramme.");
		} catch (IndexOutOfBoundsException ex) {
			sendError("Il n'y a pas de ligne avec cet ID.");
		}
	}

	@Cmd(min = 2, args = { "PERSHOLOGRAM", "PLAYERS", "BOOLEAN" }, syntax = "<id de l'hologramme> <joueur> [visibilité]")
	public void visibility(CommandContext cmd) {
		Hologram hologram = cmd.getArgument(0);

		if (!holograms.hasAccessTo(getSender(), hologram, HoloActionType.VISIBILITY))
			return;
		
		Player p = cmd.getArgument(1);
		boolean visibility;
		if (cmd.getArgumentsLength() > 2)
			visibility = cmd.getArgument(2);
		else
			visibility = !hologram.isVisibleTo(p);
		hologram.setVisibility(p, visibility);
		sendSuccess("L'hologramme est désormais %s pour le joueur %s.", visibility ? "visible" : "invisible", p.getName());
	}

	/*@Cmd (min = 1, args = "WORLD", syntax = "<world>")
	public void clearUnattachedHolograms(CommandContext cmd) {
		World world = cmd.getArgument(0);
		int removed = 0, kept = 0;
		for (ArmorStand armorStand : world.getEntitiesByClass(ArmorStand.class)) {
			if (armorStand.getPersistentDataContainer().has(HologramsManager.HOLOGRAM, PersistentDataType.INTEGER)) {
				int id = armorStand.getPersistentDataContainer().get(HologramsManager.HOLOGRAM, PersistentDataType.INTEGER);
				Hologram hologram = holograms.getHologram(id);
				if (!hologram.containsArmorStand(armorStand)) {
					armorStand.remove();
					removed++;
				}else kept++;
			}
		}
		sendSuccess("%d hologrammes supprimés, %d hologrammes gardés.", removed, kept);
	}*/

	private AbstractLine<HologramLine> createLine(String string) {
		if (string.equalsIgnoreCase("animation"))
			return CyclingLine.olympaAnimation();
		else
			return new FixedLine<>(ColorUtils.color(string));
	}	
}






