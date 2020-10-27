package fr.olympa.api.holograms;

import java.util.Arrays;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.command.complex.ComplexCommand;
import fr.olympa.api.holograms.Hologram.HologramLine;
import fr.olympa.api.lines.AbstractLine;
import fr.olympa.api.lines.CyclingLine;
import fr.olympa.api.lines.FixedLine;
import fr.olympa.api.permission.OlympaAPIPermissions;
import fr.olympa.api.utils.ColorUtils;
import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.core.spigot.OlympaCore;

public class HologramsCommand extends ComplexCommand {

	private HologramsManager holograms;

	public HologramsCommand(HologramsManager holograms) {
		super(OlympaCore.getInstance(), "holograms", "Permet de gérer les hologrammes", OlympaAPIPermissions.COMMAND_HOLOGRAMS_MANAGE, "holo");
		this.holograms = holograms;
		super.addArgumentParser("PERSHOLOGRAM", (x) -> holograms.holograms.values().stream().map(hologram -> Integer.toString(hologram.getID())).collect(Collectors.toList()), (arg) -> {
			try {
				Hologram hologram = holograms.getHologram(Integer.parseInt(arg));
				if (hologram != null && hologram.isPersistent()) return hologram;
			}catch (NumberFormatException ex) {}
			return null;
		}, x -> String.format("L'hologramme avec l'ID %s n'existe pas.", x));
	}

	@Cmd (player = true, min = 1, syntax = "<lignes séparées par des |>")
	public void create(CommandContext cmd) {
		String[] linesStrings = cmd.getFrom(0).split("\\|");
		int id = holograms.createHologram(player.getLocation().add(0, 1, 0), true, Arrays.stream(linesStrings).map(this::createLine).toArray(AbstractLine[]::new)).getID();
		sendSuccess("Hologramme %d créé.", id);
	}

	@Cmd
	public void list(CommandContext cmd) {
		StringJoiner joiner = new StringJoiner("\n", "Liste des hologrammes persistants sur ce serveur :\n", "");
		holograms.holograms.values().stream().filter(Hologram::isPersistent).forEach(holo -> {
			joiner.add("§b§l" + holo.getID() + "§3 - §b" + holo.toString() + "§3 - §b" + SpigotUtils.convertLocationToString(holo.getBottom()));
		});
		sendInfo(joiner.toString());
	}

	@Cmd (player = true, min = 1, args = "PERSHOLOGRAM", syntax = "<id de l'hologramme>")
	public void remove(CommandContext cmd) {
		Hologram hologram = cmd.getArgument(0);
		if (holograms.deleteHologram(hologram)) {
			sendSuccess("L'hologramme %d vient d'être supprimé.", hologram.getID());
		}else {
			sendError("L'hologramme %d n'a pas pu être supprimé.", hologram.getID());
		}
	}

	@Cmd (player = true, min = 1, args = "PERSHOLOGRAM", syntax = "<id de l'hologramme>")
	public void move(CommandContext cmd) {
		Hologram hologram = cmd.getArgument(0);
		hologram.move(player.getLocation().add(0, 1, 0));
		sendSuccess("L'hologramme a été déplacé à votre position.");
	}

	@Cmd (player = true, min = 1, args = "PERSHOLOGRAM", syntax = "<id de l'hologramme>")
	public void teleport(CommandContext cmd) {
		Hologram hologram = cmd.getArgument(0);
		player.teleport(hologram.getBottom());
		sendSuccess("Vous vous êtes téléporté à l'hologramme.");
	}

	@Cmd (player = true, min = 1, args = "PERSHOLOGRAM", syntax = "<id de l'hologramme> <ligne>")
	public void addLine(CommandContext cmd) {
		Hologram hologram = cmd.getArgument(0);
		hologram.addLine(cmd.getArgumentsLength() == 1 ? FixedLine.EMPTY_LINE : createLine(cmd.getFrom(1)));
		sendSuccess("La ligne a bien été ajoutée à l'hologramme.");
	}

	@Cmd (player = true, min = 2, args = { "PERSHOLOGRAM", "INTEGER" }, syntax = "<id de l'hologramme> <id de la ligne> <ligne>")
	public void editLine(CommandContext cmd) {
		Hologram hologram = cmd.getArgument(0);
		hologram.setLine(cmd.getArgumentsLength() == 2 ? FixedLine.EMPTY_LINE : createLine(cmd.getFrom(2)), cmd.getArgument(1));
		sendSuccess("La ligne a bien été modifiée.");
	}
	
	@Cmd (player = true, min = 2, args = { "PERSHOLOGRAM", "INTEGER" }, syntax = "<id de l'hologramme> <id de la ligne> <ligne>")
	public void insertLine(CommandContext cmd) {
		Hologram hologram = cmd.getArgument(0);
		hologram.insertLine(cmd.getArgumentsLength() == 2 ? FixedLine.EMPTY_LINE : createLine(cmd.getFrom(2)), cmd.getArgument(1));
		sendSuccess("La ligne a bien été insérée.");
	}
	
	@Cmd (player = true, min = 2, args = { "PERSHOLOGRAM", "INTEGER" }, syntax = "<id de l'hologramme> <id de la ligne>")
	public void removeLine(CommandContext cmd) {
		Hologram hologram = cmd.getArgument(0);
		try {
			hologram.removeLine(cmd.<Integer>getArgument(1));
			sendSuccess("La ligne a bien été supprimée de l'hologramme.");
		}catch (IndexOutOfBoundsException ex) {
			sendError("Il n'y a pas de ligne avec cet ID.");
		}
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
		if (string.equalsIgnoreCase("animation")) {
			return CyclingLine.olympaAnimation();
		}else return new FixedLine<>(ColorUtils.color(string));
	}

}
